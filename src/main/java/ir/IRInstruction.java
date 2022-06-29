package ir;

import java.util.List;
import java.util.Set;

public class IRInstruction {
    private final String instruction;
    private final String operationName;
    private final List<String> argumentNames;
    private final InstructionType instructionType;
    private final Set<String> definedVariables;
    private final Set<String> usedVariables;

    public IRInstruction(String instruction, String operationName, List<String> argumentNames, InstructionType instructionType, Set<String> definedVariables, Set<String> usedVariables) {
        this.instruction = instruction;
        this.operationName = operationName;
        this.argumentNames = argumentNames;
        this.instructionType = instructionType;
        this.definedVariables = definedVariables;
        this.usedVariables = usedVariables;
    }

    public String getOperation() {
        return operationName;
    }

    public List<String> getArguments() {
        return argumentNames;
    }

    public InstructionType getInstructionType() {
        return instructionType;
    }

    public Set<String> getDefinedVariables() {
        return definedVariables;
    }

    public Set<String> getUsedVariables() {
        return usedVariables;
    }

    @Override
    public String toString() {
        return instruction;
    }
}
