import exceptions.AirportInfoException;
import exceptions.Failure;
import exceptions.Success;
import exceptions.Try;

import java.util.List;

public class AirportInfo {

    public static void getNameOfAllAirports() {
        var iataCodes = List.of("AUS", "DFW", "HOU", "IHA", "SAT");

        getNameOfAllAirports(iataCodes).stream()
                .map(name -> switch (name) {
                    case Success(String result) -> result;
                    case Failure(Throwable throwable) -> "Error: " + throwable.getMessage();
                })
                .forEach(System.out::println);
    }

    public static List<Try<String>> getNameOfAllAirports(List<String> iataCodes) {
        return iataCodes.stream()
                .map(code -> Try.of(() -> AirportInfo.apiCall(code)))
                .map(name -> name.map(String::toUpperCase))
                .toList();
    }

    public static String apiCall(String iataCode) throws AirportInfoException {
        if (iataCode.equals("IHA")) {
            throw new AirportInfoException("Invalid airport code: " + iataCode);
        }
        return iataCode;
    }
}
