package regalloc.allocator;

import regalloc.FunctionData;
import regalloc.MemoryTable;
import regalloc.allocator.Allocator;

import java.util.*;

public class NaiveAllocator implements Allocator {
    private final FunctionData functionData;
    private final Set<String> staticVariables;
    private final Map<String, Integer> arrays;

    public NaiveAllocator(FunctionData functionData, Map<String, Integer> staticArrays, Set<String> staticVariables) {
        this.functionData = functionData;
        this.staticVariables = staticVariables;
        this.arrays = new HashMap<>(staticArrays);
    }

    @Override
    public MemoryTable allocate() {

        List<String> parameterList = functionData.getParameters();
        List<String> localVariableList = new ArrayList<>(functionData.getLocalVariables());

        Map<String, Integer> savedRegisterOffsets = new HashMap<>();
        Map<String, Integer> stackVariableOffsets = new HashMap<>();

        Collections.reverse(localVariableList);

        int offset = 0;
        offset = allocateVars(localVariableList, stackVariableOffsets, offset);

        for (String registerName : List.of("$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7")) {
            savedRegisterOffsets.put(registerName, offset);
            offset += 4;
        }

        savedRegisterOffsets.put("$ra", offset);
        offset += 4;

        offset = allocateVars(parameterList, stackVariableOffsets, offset);

        int frameSize = offset - parameterList.size() * 4;

        // naive allocation, so we have all variable in memory not in registers
        Map<String, String> variableRegisters = new HashMap<>();

        return new MemoryTable(stackVariableOffsets, savedRegisterOffsets, variableRegisters, arrays, staticVariables, frameSize);
    }

    private boolean isVariableArray(String variableName) {
        return variableName.contains("[");
    }

    private int getArraySize(String variableName) {
        return Integer.parseInt(variableName.substring(variableName.indexOf('[') + 1, variableName.indexOf(']')));
    }

    private String getArrayName(String variableName) {
        return variableName.substring(0, variableName.indexOf('['));
    }

    private int allocateVars(List<String> localVariableList, Map<String, Integer> stackVariableOffsets, int offset) {
        for (String currVariable : localVariableList) {
            if (isVariableArray(currVariable)) {
                int size = getArraySize(currVariable);
                stackVariableOffsets.put(getArrayName(currVariable), offset);
                arrays.put(getArrayName(currVariable), size);
                offset += size * 4;
            } else {
                stackVariableOffsets.put(currVariable, offset);
                offset += 4;
            }
        }
        return offset;
    }
}
