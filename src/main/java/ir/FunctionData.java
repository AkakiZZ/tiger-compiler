package ir;

import java.util.List;
import java.util.Map;

public class FunctionData {
    private final String name;
    private final List<String> localIntVariables;
    private final List<String> localFloatVariables;
    private final List<String> parameters;
    private final Map<String, Integer> arrays;
    private final List<Boolean> isParameterFlotList;

    private final List<IRInstruction> IRInstructions;

    public FunctionData(String name, List<String> localIntVariables, List<String> localFloatVariables, List<String> parameters, Map<String, Integer> arrays, List<Boolean> isParameterFlotList, List<IRInstruction> IRInstructions) {
        this.name = name;
        this.localIntVariables = localIntVariables;
        this.localFloatVariables = localFloatVariables;
        this.parameters = parameters;
        this.arrays = arrays;
        this.isParameterFlotList = isParameterFlotList;
        this.IRInstructions = IRInstructions;
    }

    public List<String> getLocalIntVariables() {
        return localIntVariables;
    }

    public List<String> getIntParameters() {
        return parameters;
    }

    public List<String> getLocalFloatVariables() {
        return localFloatVariables;
    }

    public List<Boolean> getFloatParameters() {
        return isParameterFlotList;
    }

    public List<IRInstruction> getInstructions() {
        return IRInstructions;
    }

    public Map<String, Integer> getArrays() {
        return arrays;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "FunctionData{" +
                "localIntVariables=" + localIntVariables +
                ", localFloatVariables=" + localFloatVariables +
                ", intParameters=" + parameters +
                ", floatParameters=" + isParameterFlotList +
                '}';
    }
}
