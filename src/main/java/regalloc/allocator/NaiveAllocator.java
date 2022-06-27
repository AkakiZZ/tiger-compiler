package regalloc.allocator;

import regalloc.FunctionData;
import regalloc.MemoryTable;

import java.util.*;
import java.util.stream.Collectors;

public class NaiveAllocator implements Allocator {
    private final FunctionData functionData;
    private final Set<String> staticVariables;
    private final Map<String, Integer> arrays;
    private final Set<String> floatVariables;
    public NaiveAllocator(FunctionData functionData, Map<String, Integer> staticArrays, Set<String> staticVariables, Set<String> floatVariables) {
        this.functionData = functionData;
        this.staticVariables = staticVariables;
        this.arrays = new HashMap<>(staticArrays);
        this.floatVariables = floatVariables;
    }

    @Override
    public MemoryTable allocate() {

        List<String> parameterList = functionData.getIntParameters();
        List<Boolean> isParameterFlotList = functionData.getFloatParameters();
        List<String> localIntVariableList = new ArrayList<>(functionData.getLocalIntVariables());
        List<String> localFloatVariableList = new ArrayList<>(functionData.getLocalFloatVariables());

        Map<String, Integer> savedRegisterOffsets = new HashMap<>();
        Map<String, Integer> stackVariableOffsets = new HashMap<>();

        Collections.reverse(localIntVariableList);
        Collections.reverse(localFloatVariableList);

        int offset = 0;
        offset = allocateVars(localIntVariableList, stackVariableOffsets, offset);
        offset = allocateVars(localFloatVariableList, stackVariableOffsets, offset);

        for (String registerName : List.of("$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7")) {
            savedRegisterOffsets.put(registerName, offset);
            offset += 4;
        }

        savedRegisterOffsets.put("$ra", offset);
        offset += 4;

        offset = allocateVars(parameterList, stackVariableOffsets, offset);

        int frameSize = offset - parameterList.size() * 4;

        // naive allocation, so we have all variables in memory not in registers
        Map<String, String> variableRegisters = new HashMap<>();

        Set<String> floatVariables = new HashSet<>(this.floatVariables);
        floatVariables.addAll(localFloatVariableList.stream().map(this::getArrayName).toList());
        for (int i = 0; i < isParameterFlotList.size(); i++) {
            if (isParameterFlotList.get(i)) floatVariables.add(parameterList.get(i));
        }

        return new MemoryTable(stackVariableOffsets, savedRegisterOffsets, variableRegisters, arrays, staticVariables, floatVariables, frameSize);
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
