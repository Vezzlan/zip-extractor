package com.zip.api;

import com.zip.model.User;
import com.zip.services.zip.ZipGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RequestMapping("/api")
@RestController
public class ZipController {

    private final ZipGenerator zipGenerator;

    public ZipController(ZipGenerator zipGenerator) {
        this.zipGenerator = zipGenerator;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/stream", produces = { "application/octet-stream"})
    public ResponseEntity<StreamingResponseBody> getNewZip() {
        final var user = new User("id", "fileId", "videoLab", "simple hello world");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s.zip".formatted(user.name()))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(outputZip -> zipGenerator.generate(outputZip, user));
    }

}
