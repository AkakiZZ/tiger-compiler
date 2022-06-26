package regalloc;


import java.util.Map;
import java.util.Set;

public class MemoryTable {
    private final Map<String, Integer> stackVariableOffsets;
    private final Map<String, Integer> savedRegisterOffsets;
    private final Map<String, String> variableRegisters;
    private final Map<String, Integer> arrays;

    private final Set<String> staticVariables;
    private final int frameSize;

    public MemoryTable(Map<String, Integer> stackVariableOffsets,
                       Map<String, Integer> savedRegisterOffsets,
                       Map<String, String> variableRegisters,
                       Map<String, Integer> arrays,
                       Set<String> staticVariables,
                       int frameSize) {
        this.stackVariableOffsets = stackVariableOffsets;
        this.savedRegisterOffsets = savedRegisterOffsets;
        this.variableRegisters = variableRegisters;
        this.staticVariables = staticVariables;
        this.arrays = arrays;
        this.frameSize = frameSize;
    }

    /**
     * returns the offset of the variable
     */
    public int getStackVariableOffset(String variableName) {
        return stackVariableOffsets.get(variableName);
    }

    /**
     * returns the register where the variable is located
     * should be called when the return value of isVariableInRegister(String variableName) is true
     */
    public String getVariableRegister(String variableName) {
        return variableRegisters.get(variableName);
    }

    /**
     * returns location of the saved registers ($s1 ... $s7 and $ra)
     */
    public int getSavedRegisterOffset(String registerName) {
        return savedRegisterOffsets.get(registerName);
    }

    /**
     * returns size of an array
     */
    public int getArraySize(String arrayName) {
        return arrays.get(arrayName);
    }

    /**
     * returns whole size of a frame
     */
    public int getFrameSize() {
        return this.frameSize;
    }

    /**
     * returns whether a variable is in a register or not
     */
    public boolean isVariableInRegister(String variableName) {
        return variableRegisters.containsKey(variableName);
    }

    /**
     * returns whether a variable is an array or not (stack or static)
     */
    public boolean isVariableArray(String variableName) {
        return arrays.containsKey(variableName);
    }

    /**
     * returns whether a variable is a stack variable or not
     */
    public boolean isVariableStack(String variableName) {
        return stackVariableOffsets.containsKey(variableName);
    }

    /**
     * returns whether a variable is a static variable or not
     */
    public boolean isVariableStatic(String variableName) {
        return staticVariables.contains(variableName);
    }

    @Override
    public String toString() {
        return "MemoryTable{" +
                "stackVariableOffsets=" + stackVariableOffsets +
                ", savedRegisterOffsets=" + savedRegisterOffsets +
                ", variableRegisters=" + variableRegisters +
                ", arrays=" + arrays +
                ", staticVariables=" + staticVariables +
                ", frameSize=" + frameSize +
                '}';
    }
}
