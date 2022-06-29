package parser;

import ir.FunctionData;
import ir.IRInstruction;
import ir.ProgramData;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class IrProgramParser {
    private final List<String> lines;
    private final Set<String> floatVars;
    private final Set<String> staticVariables;
    private final HashMap<String, Integer> staticArrays;

    private final ProgramData programData;
    private int currLine;

    public IrProgramParser(String filePath) throws FileNotFoundException {
        lines = new ArrayList<>();
        currLine = 0;
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        scanner.close();
        floatVars = new HashSet<>();
        staticArrays = new HashMap<>();
        staticVariables = new HashSet<>();
        generateStaticVars();
        this.programData = generateProgramData();
    }

    public void generateStaticVars() {
        String intStaticLine = lines.get(1);
        String floatStaticLine = lines.get(2);
        intStaticLine = intStaticLine.substring(intStaticLine.indexOf(':') + 1);
        String[] intStatics = intStaticLine.split(",");
        floatStaticLine = floatStaticLine.substring(floatStaticLine.indexOf(':') + 1);
        String[] floatStatics = floatStaticLine.split(",");

        for (String staticVar : intStatics) {
            staticVar = staticVar.trim();
            if (!staticVar.isEmpty()) {
                if (!staticVar.contains("[")) {
                    staticVariables.add(staticVar.trim());
                } else {
                    String staticArrName = staticVar.substring(0, staticVar.indexOf('['));
                    int size = Integer.parseInt(staticVar.substring(staticVar.indexOf('[') + 1, staticVar.indexOf(']')));
                    staticArrays.put(staticArrName.trim(), size);
                }
            }
        }

        for (String staticVar : floatStatics) {
            staticVar = staticVar.trim();
            if (!staticVar.isEmpty()) {
                if (!staticVar.contains("[")) {
                    staticVariables.add(staticVar.trim());
                    floatVars.add(staticVar);
                } else {
                    String staticArrName = staticVar.substring(0, staticVar.indexOf('['));
                    int size = Integer.parseInt(staticVar.substring(staticVar.indexOf('[') + 1, staticVar.indexOf(']')));
                    staticArrays.put(staticArrName.trim(), size);
                    floatVars.add(staticArrName.trim());
                }
            }
        }
    }

    private List<String> getNextFunction() {
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

    private ProgramData generateProgramData() {
        List<FunctionData> functions = new ArrayList<>();
        while (true) {
            List<String> irInstructions = getNextFunction();
            if (irInstructions == null) break;
            functions.add(new FunctionParser(irInstructions).getFunctionData());
        }
        return new ProgramData(staticVariables, staticArrays, floatVars, functions);
    }

    public ProgramData getProgramData() {
        return programData;
    }
}
