import exceptions.CredentialsException;
import exceptions.ErrResponseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class POP3ClientCLI {
    static String server = "pop.fastmail.com";
    static int port = 995;
    static String email = "kt2@fastmail.com";
    static String password = "aymuyj2hxd7l6tbd";

    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Welcome to the POP3 Client CLI!");

            POP3Client pop3Client = new POP3Client();

            pop3Client.validateCredentials(email, password);

            pop3Client.connect(server, port);
            pop3Client.authenticate(email, password);

            boolean running = true;
            while (running) {
                System.out.println("\nOptions:");
                System.out.println("1. List messages");
                System.out.println("2. Retrieve a specific message");
                System.out.println("3. Delete a specific message");
                System.out.println("4. Quit");

                System.out.print("Enter your choice (1-5): ");
                int choice = Integer.parseInt(br.readLine().trim());

                switch (choice) {
                    case 1 ->
                            pop3Client.listCommand();
                    case 2 -> {
                        System.out.print("Enter the message number: ");
                        int msgNumber = Integer.parseInt(br.readLine().trim());
                        int numOfMsg = pop3Client.getNumberOfMessages();
                        if (msgNumber >= 1 && msgNumber <= numOfMsg) {
                            pop3Client.getRawMessage(msgNumber);
                        } else {
                            System.out.println("Invalid index. Please enter a number between 1 and " + numOfMsg);
                        }
                    }
                    case 3 -> {
                        System.out.print("Enter the message number to delete: ");
                        int delMsgNumber = Integer.parseInt(br.readLine().trim());
                        int numOfMsg = pop3Client.getNumberOfMessages();
                        if(delMsgNumber >= 1 && delMsgNumber <= numOfMsg) {
                            pop3Client.deleteMessage(delMsgNumber);
                        } else {
                            System.out.println("Invalid index. Please enter a number between 1 and " + numOfMsg);
                        }

                    }
                    case 4 -> {
                        running = false;
                        pop3Client.closeConnection();
                        System.out.println("Goodbye!");
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (IOException | ErrResponseException | CredentialsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
