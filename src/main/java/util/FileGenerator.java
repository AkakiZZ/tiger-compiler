package util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileGenerator {
    public static void generateMipsFile(String path, List<String> instructions) throws IOException {
        FileWriter myWriter = new FileWriter(path);
        for (String line : instructions) {
            myWriter.write(line);
            myWriter.write('\n');
        }
        myWriter.close();
    }
}
