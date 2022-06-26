package regalloc.allocator;

import regalloc.FunctionData;
import regalloc.MemoryTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IntraBlockAllocator implements Allocator {
    private final FunctionData functionData;
    private final Set<String> staticVariables;
    private final Map<String, Integer> arrays;

    public IntraBlockAllocator(FunctionData functionData, Map<String, Integer> staticArrays, Set<String> staticVariables) {
        this.functionData = functionData;
        this.staticVariables = staticVariables;
        this.arrays = new HashMap<>(staticArrays);
    }

    @Override
    public MemoryTable allocate() {
        return null;
    }
}
