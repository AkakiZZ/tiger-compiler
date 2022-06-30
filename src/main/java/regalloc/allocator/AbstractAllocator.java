package regalloc.allocator;

import ir.FunctionData;
import regalloc.model.MemoryTable;

import java.util.*;

public abstract class AbstractAllocator implements Allocator {
    protected final FunctionData functionData;
    protected final Set<String> staticVariables;
    protected final Map<String, Integer> arrays;
    protected final Set<String> floatVariables;

    public AbstractAllocator(FunctionData functionData, Map<String, Integer> staticArrays, Set<String> staticVariables, Set<String> floatVariables) {
        this.functionData = functionData;
        this.staticVariables = new HashSet<>(staticVariables);
        this.arrays = new HashMap<>(staticArrays);
        this.floatVariables = new HashSet<>(floatVariables);
    }

    @Override
    public MemoryTable allocate() {
        List<String> parameterList = functionData.getIntParameters();
        List<Boolean> isParameterFloatList = functionData.getFloatParameters();
        List<String> localIntVariableList = new ArrayList<>(functionData.getLocalIntVariables());
        List<String> localFloatVariableList = new ArrayList<>(functionData.getLocalFloatVariables());
        floatVariables.addAll(localFloatVariableList);
        for (int i = 0; i < isParameterFloatList.size(); i++) {
            if (isParameterFloatList.get(i)) floatVariables.add(parameterList.get(i));
        }

        Map<String, Integer> localArrays = functionData.getArrays();
        for (String localArray : localArrays.keySet()) {
            arrays.put(localArray, localArrays.get(localArray));
        }

        Map<String, Integer> savedRegisterOffsets = new HashMap<>();
        Map<String, Integer> stackVariableOffsets = new HashMap<>();

        Collections.reverse(localIntVariableList);
        Collections.reverse(localFloatVariableList);

        int offset = 0;
        offset = allocateVars(localIntVariableList, stackVariableOffsets, offset);
        offset = allocateVars(localFloatVariableList, stackVariableOffsets, offset);

        savedRegisterOffsets.put("$ra", offset);
        offset += 4;

        offset = allocateVars(parameterList, stackVariableOffsets, offset);

        int frameSize = offset - parameterList.size() * 4;

        Map<String, String> variableRegisters = new HashMap<>();

        return new MemoryTable(stackVariableOffsets, savedRegisterOffsets, variableRegisters, arrays, staticVariables, floatVariables, frameSize);
    }

    protected boolean isVariableArray(String variableName) {
        return arrays.containsKey(variableName);
    }

    private int allocateVars(List<String> localVariableList, Map<String, Integer> stackVariableOffsets, int offset) {
        for (String currVariable : localVariableList) {
            if (isVariableArray(currVariable)) {
                int size = arrays.get(currVariable);
                stackVariableOffsets.put(currVariable, offset);
                offset += size * 4;
            } else {
                stackVariableOffsets.put(currVariable, offset);
                offset += 4;
            }
        }
        return offset;
    }
}
