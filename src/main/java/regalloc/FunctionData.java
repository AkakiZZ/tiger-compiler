package regalloc;

import ir_instructions.IRInstruction;

import java.util.List;

public class FunctionData {
    private final List<String> localVariables;
    private final List<String> parameters;

    private final List<IRInstruction> IRInstructions;

    public FunctionData(List<String> localVariables, List<String> parameters, List<IRInstruction> IRInstructions) {
        this.localVariables = localVariables;
        this.parameters = parameters;
        this.IRInstructions = IRInstructions;
    }

    public List<String> getLocalVariables() {
        return localVariables;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public List<IRInstruction> getInstructions() {
        return IRInstructions;
    }
}
