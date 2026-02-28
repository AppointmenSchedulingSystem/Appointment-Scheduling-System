package Fall2026;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

// this class will be used temporary to save data to file,
// but in the future it will be replaced by a database
public class CredentialStorage {
    public List<String> ReadFromFile(String FileName) {
        List<String> Lines = new ArrayList<>();
        File file = new File(FileName);
        if (!file.exists()) {
            System.out.println("File does not exist"+FileName);
            return Lines;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {  // Skip empty lines
                    Lines.add(line);
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());

        }
        return Lines;
    }

    public boolean WriteToFile(String FileName, List<String> lines) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FileName))){
            for (String line : lines) {
                writer.println(line);
            }
            return true;
        }
            catch (Exception e) {
                System.out.println("Error writing to file: " + e.getMessage());
                return false;
            }
    }

    // Append a single line to file
    public boolean appendToFile(String filename, String line) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
            writer.println(line);
            return true;
        } catch (IOException e) {
            System.err.println("Error appending to file: " + e.getMessage());
            return false;
        }
    }
    // Check if file exists
    public boolean fileExists(String filename) {
        return new File(filename).exists();
    }

}
