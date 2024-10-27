import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class CurrentSolution {

    private final ObjectMapper objectMapper;

    @Autowired
    public CurrentSolution(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<CreatedResourceIds> importFunction(File zip) {
        List<CreatedResourceIds> resourceIds = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(zip, ZipFile.OPEN_READ)) {
            Map<String, ZipEntry> jsonFiles = new HashMap<>();
            Map<String, ZipEntry> pyFiles = new HashMap<>();

            var entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry  = entries.nextElement();
                if (entry.getName().endsWith(".json") && !entry.getName().startsWith("__MACOSX")) {
                    jsonFiles.put(entry.getName().replace(".json", ""), entry);
                }

                if (entry.getName().endsWith(".py") && !entry.getName().startsWith("__MACOSX")) {
                    pyFiles.put(entry.getName().replace(".py", ""), entry);
                }
            }

            for (Map.Entry<String, ZipEntry> jsonFileEntry : jsonFiles.entrySet()) {
                String name = jsonFileEntry.getKey();

                if (pyFiles.containsKey(name)) { //Ingen else här, dvs inget händer om en fil inte finns?
                    try {
                        ZipEntry metaEntryJson = jsonFileEntry.getValue();
                        InputStream inputStream = zipFile.getInputStream(metaEntryJson);
                        String jsonStr = new String(inputStream.readAllBytes());
                        var metadata = parseJson(jsonStr);

                        if (metadata != null) {
                            var valueWithIdsExportData = new User(
                                    metadata.id(),
                                    UUID.randomUUID().toString(),
                                    "kalle",
                                    metadata.description()); //Copy with new values
                            ZipEntry pyEntry = pyFiles.get(name); //Används till klienten

                            //var response = fileClient.uploadFile(file.getInputStream(pyEntry), token);
                            //if (response.getStatusCOde().isError()) throw new Exception();

                            //mocka ett nytt id från reponse
                            var responseWithFileId = fileClientMock(pyEntry);
                            if (responseWithFileId != null) {
                                var userCommand = new User(
                                        "nytt id!",
                                        responseWithFileId,
                                        valueWithIdsExportData.name(),
                                        valueWithIdsExportData.description()
                                );


                                KafkaPublisher.sendCommand(userCommand);

                                resourceIds.add(new CreatedResourceIds(
                                        userCommand.id(),
                                        responseWithFileId)
                                );

                            } else {
                                System.out.println("Cannot import haha!");
                            }
                        } else {
                            System.out.println("Cannot parse metadata!");
                        }
                    } catch (IOException e) {
                        System.out.println("Herregud! Onödig try catch eller?");
                    }
                }
            }

        } catch (IOException e) {
            throw new IllegalArgumentException();
        }

        return resourceIds;
    }

    private User parseJson(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String fileClientMock(ZipEntry pyCode) {
        System.out.println("fileClientMock with pyCode : " + pyCode);
        //hantera null här!
        return "nytt filId!";
    }

}
