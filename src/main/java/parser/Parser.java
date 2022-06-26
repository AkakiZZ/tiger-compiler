package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Parser {
    private final List<String> lines;
    private int currLine;

    public Parser(String filePath) throws FileNotFoundException {
        lines = new ArrayList<>();
        currLine = 0;
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        scanner.close();
    }

    public List<String> getStaticVars() {
        List<String> staticVariables = new ArrayList<>();
        String staticLine = lines.get(1);
        staticLine = staticLine.substring(staticLine.indexOf(':') + 1);
        String[] statics = staticLine.split(",");

        for (String staticVar : statics) {
            staticVar = staticVar.trim();
            if (!staticVar.isEmpty() && !staticVar.contains("["))
                staticVariables.add(staticVar.trim());
        }
        return staticVariables;
    }

    public HashMap<String, Integer> getStaticArrays() {
        HashMap<String, Integer> staticArrays = new HashMap<>();
        String staticLine = lines.get(1);
        staticLine = staticLine.substring(staticLine.indexOf(':') + 1);
        String[] statics = staticLine.split(",");

        for (String staticVar : statics) {
            staticVar = staticVar.trim();
            if (staticVar.contains("[")) {
                String staticArrName = staticVar.substring(0, staticVar.indexOf('['));
                int size = Integer.parseInt(staticVar.substring(staticVar.indexOf('[') + 1, staticVar.indexOf(']')));
                staticArrays.put(staticArrName.trim(), size);
            }
        }
        return staticArrays;
    }

    public List<String> getNextFunction() {
        List<String> functionLines = new ArrayList<>();
        while (currLine < lines.size()) {
            if (lines.get(currLine).startsWith("start_function")) {
                do {
                    functionLines.add(lines.get(currLine));
                    currLine++;
                } while (!lines.get(currLine).startsWith("end_function"));
                return functionLines;
            }
            currLine++;
        }
        return null;
    }


}
