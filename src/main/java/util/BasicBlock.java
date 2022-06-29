package util;

import ir.IRInstruction;

import java.util.List;

public class BasicBlock {
    private final List<IRInstruction> instructions;
    private final int startIndex;
    private final int endIndex;

    public BasicBlock(List<IRInstruction> instruction, int startIndex, int endIndex) {
        this.instructions = instruction;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public List<IRInstruction> getInstructions() {
        return instructions;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    @Override
    public String toString() {
        StringBuilder code = new StringBuilder();
        for (IRInstruction irInstruction : instructions) {
            code.append(irInstruction);
            code.append("\n");
        }
        return code.toString();
    }
}
