package regalloc;

import ir_instructions.IRInstruction;

import java.util.List;

public class FunctionData {
    private final List<String> localIntVariables;
    private final List<String> localFloatVariables;
    private final List<String> parameters;
    private final List<Boolean> isParameterFlotList;

    private final List<IRInstruction> IRInstructions;

    public FunctionData(List<String> localIntVariables, List<String> localFloatVariables, List<String> parameters, List<Boolean> isParameterFlotList, List<IRInstruction> IRInstructions) {
        this.localIntVariables = localIntVariables;
        this.localFloatVariables = localFloatVariables;
        this.parameters = parameters;
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
