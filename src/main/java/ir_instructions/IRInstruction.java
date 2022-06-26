package ir_instructions;

import java.util.List;

public class IRInstruction {
    private final String operationName;
    private final List<String> argumentNames;

    private final InstructionType instructionType;

    public IRInstruction(String operationName, List<String> argumentNames, InstructionType instructionType) {
        this.operationName = operationName;
        this.argumentNames = argumentNames;
        this.instructionType = instructionType;
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

    @Override
    public String toString() {
        return "Instruction{" +
                "operationName='" + operationName + '\'' +
                ", argumentNames=" + argumentNames +
                ", instructionType=" + instructionType +
                '}';
    }
}
