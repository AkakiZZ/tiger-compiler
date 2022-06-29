package parser;

import ir.IRInstruction;
import ir.FunctionData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, Integer> arrays = new HashMap<>();
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
                String var = param.trim().replace("int ", "");
                //parameters.add(param.trim().replace("int ", ""));
                isParameterFlotList.add(false);
                if (isVariableArray(var)) {
                    arrays.put(getArrayName(var), getArraySize(var));
                    parameters.add(getArrayName(var));
                } else {
                    parameters.add(var);
                }
            }
            if (param.contains("float")) {
                String var = param.trim().replace("float ", "");
                //parameters.add(param.trim().replace("float ", ""));
                isParameterFlotList.add(false);
                if (isVariableArray(var)) {
                    arrays.put(getArrayName(var), getArraySize(var));
                    parameters.add(getArrayName(var));
                } else {
                    parameters.add(var);
                }
            }
        }

        addLocalVariables(localIntVariables, arrays, intLocals);

        addLocalVariables(localFloatVariables, arrays, floatLocals);

        for (int i = 4; i < lines.size(); i++) {
            IRInstructions.add(instructionParser.parse(lines.get(i)));
        }

        return new FunctionData(lines.get(1).trim(), localIntVariables, localFloatVariables, parameters, arrays, isParameterFlotList, IRInstructions);
    }

    private void addLocalVariables(List<String> variables, Map<String, Integer> arrays, String[] locals) {
        for (String local : locals) {
            if (!local.isEmpty()) {
                String var = local.trim();
                if (isVariableArray(var)) {
                    arrays.put(getArrayName(var), getArraySize(var));
                    variables.add(getArrayName(var));
                } else {
                    variables.add(var);
                }
            }
        }
    }

    private boolean isVariableArray(String variableName) {
        return variableName.contains("[");
    }
    private int getArraySize(String variableName) {
        return Integer.parseInt(variableName.substring(variableName.indexOf('[') + 1, variableName.indexOf(']')));
    }

    private String getArrayName(String variableName) {
        if (!isVariableArray(variableName))
            return variableName;
        return variableName.substring(0, variableName.indexOf('['));
    }
}
