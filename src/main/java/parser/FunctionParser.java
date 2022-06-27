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
        List<Boolean> isParameterFlotList = new ArrayList<>();
        List<String> localIntVariables = new ArrayList<>();
        List<String> localFloatVariables = new ArrayList<>();
        List<IRInstruction> IRInstructions = new ArrayList<>();
        String parameterLine = lines.get(1);
        String localLineInts = lines.get(2);
        String localLineFloats = lines.get(3);

        parameterLine = parameterLine.substring(parameterLine.indexOf('(') + 1, parameterLine.indexOf(')'));
        localLineInts = localLineInts.substring(localLineInts.indexOf(':') + 1);
        localLineFloats = localLineFloats.substring(localLineFloats.indexOf(':') + 1);

        String[] params = parameterLine.split(",");
        String[] intLocals = localLineInts.split(",");
        String[] floatLocals = localLineFloats.split(",");

        for (String param : params) {
            if (param.contains("int")) {
                parameters.add(param.trim().replace("int ", ""));
                isParameterFlotList.add(false);
            }
            if (param.contains("float")) {
                parameters.add(param.trim().replace("float ", ""));
                isParameterFlotList.add(true);
            }
        }

        for (String local : intLocals) {
            if (!local.isEmpty())
                localIntVariables.add(local.trim());
        }

        for (String local : floatLocals) {
            if (!local.isEmpty())
                localFloatVariables.add(local.trim());
        }

        for (int i = 4; i < lines.size(); i++) {
            IRInstructions.add(instructionParser.parse(lines.get(i)));
        }

        return new FunctionData(localIntVariables, localFloatVariables, parameters, isParameterFlotList, IRInstructions);
    }


    private boolean isVariableArray(String variableName) {
        return variableName.contains("[");
    }

    private String getArrayName(String variableName) {
        return variableName.substring(0, variableName.indexOf('['));
    }

}
