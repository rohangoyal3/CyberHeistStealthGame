import java.io.*;
import java.util.*;

public class UserAuth {
    private static final String FILE_NAME = "users.csv";

    // Register a new user
    public static boolean registerUser(String username, String password) {
        if (doesUserExist(username)) return false;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(username + "," + password);
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error writing to user file: " + e.getMessage());
            return false;
        }
    }

    // Check if user exists
    public static boolean doesUserExist(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] creds = line.split(",");
                if (creds.length >= 1 && creds[0].equals(username)) return true;
            }
        } catch (IOException e) {
            System.out.println("Error reading user file: " + e.getMessage());
        }
        return false;
    }

    // Validate login
    public static boolean validateLogin(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] creds = line.split(",");
                if (creds.length >= 2 && creds[0].equals(username) && creds[1].equals(password)) return true;
            }
        } catch (IOException e) {
            System.out.println("Error reading user file: " + e.getMessage());
        }
        return false;
    }
}
