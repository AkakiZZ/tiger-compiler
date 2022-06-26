package parser;

import ir_instructions.IRInstruction;
import regalloc.FunctionData;

import java.util.ArrayList;
import java.util.List;

public class FunctionParser {
    private final List<String> lines;
    private final InstructionParser instructionParser;

    public FunctionParser(List<String> lines) {
        this.lines = lines;
        instructionParser = new InstructionParser();
    }

    public FunctionData getFunctionData() {
        List<String> parameters = new ArrayList<>();
        List<String> localVariables = new ArrayList<>();
        List<IRInstruction> IRInstructions = new ArrayList<>();
        String parameterLine = lines.get(1);
        String localLine = lines.get(2);

        parameterLine = parameterLine.substring(parameterLine.indexOf('(') + 1, parameterLine.indexOf(')'));
        localLine = localLine.substring(localLine.indexOf(':') + 1);

        String[] params = parameterLine.split(",");
        String[] locals = localLine.split(",");

        for (String param : params) {
            if (!param.isEmpty())
                parameters.add(param.trim().replace("int ", ""));
        }

        for (String local : locals) {
            if (!local.isEmpty())
                localVariables.add(local.trim());
        }

        for (int i = 4; i < lines.size(); i++) {
            IRInstructions.add(instructionParser.parse(lines.get(i)));
        }

        return new FunctionData(localVariables, parameters, IRInstructions);
    }

}
