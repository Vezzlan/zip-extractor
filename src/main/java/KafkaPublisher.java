import model.User;

public class KafkaPublisher {

    public static void sendCommand(User sendCommand) {
        System.out.println("kafka: " + sendCommand);
    }
}
