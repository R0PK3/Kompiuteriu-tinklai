import exceptions.CredentialsException;
import exceptions.ErrResponseException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class POP3Client {

    private static Socket socket;

    private static BufferedReader reader;

    private static BufferedWriter writer;

    public static void connect(String server, Integer port) throws IOException, ErrResponseException {

        SocketFactory socketFactory = SSLSocketFactory.getDefault();
        socket = socketFactory.createSocket(server, port);

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        readResponseLine();
    }

    private static String readResponseLine() throws IOException, ErrResponseException {
        String response = reader.readLine();
        if (response.startsWith("-ERR")) {
            throw new ErrResponseException("Error in server response." + response.replace("-ERR", ""));
        }
        return response;
    }

    public void authenticate(String username, String password) throws IOException, ErrResponseException {
        sendCommand("USER " + username);
        sendCommand("PASS " + password);
    }

    public  void closeConnection() throws IOException, ErrResponseException {
        sendCommand("QUIT");
        if(socket != null) {
            socket.close();
        }
        reader.close();
        writer.close();
    }

    private String sendCommand(String command) throws IOException, ErrResponseException {
        writer.write(command + '\n');
        writer.flush();
        return readResponseLine();
    }
    public void validateCredentials(String email, String password) throws CredentialsException {
        String emailRegex = "[-0-9a-zA-Z.+_]+@[-0-9a-zA-Z.+_]+\\.[a-zA-Z]{2,4}";
        if (email == null || password == null || email.equals("") || password.equals("") || email.equals(" ")) {
            throw new CredentialsException("Input can not be empty.");
        }
        if (!email.matches(emailRegex)) {
            throw new CredentialsException("Credentials are not valid.");
        }
    }
    public void listCommand() throws IOException, ErrResponseException {
        sendCommand("LIST");
        String response;
        while (!(response = readResponseLine()).equals(".")) {
            System.out.println(response + " size in bytes.");
        }
    }

    public void deleteMessage(int messageNumber) throws ErrResponseException, IOException {
        try {
            sendCommand("DELE " + messageNumber);
            System.out.println("Message " + messageNumber + " deleted.");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public int getNumberOfMessages() throws IOException, ErrResponseException {
        String response = sendCommand("STAT");
        String[] values = response.split(" ");
        return Integer.parseInt(values[1]);
    }

    public void getRawMessage(int i) throws IOException, ErrResponseException {
        try {
            String response;
            sendCommand("RETR " + i);
            StringBuilder rawMessageBuilder = new StringBuilder();
            while (!(response = readResponseLine()).equals(".")) {
                rawMessageBuilder.append(response).append("\n");
            }
            System.out.println(rawMessageBuilder.toString());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}

