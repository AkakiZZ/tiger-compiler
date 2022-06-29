package regalloc.allocator;

import ir.FunctionData;
import regalloc.model.MemoryTable;
import util.BasicBlock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GlobalAllocator implements Allocator {
    private final FunctionData functionData;
    private final Set<String> staticVariables;
    private final Map<String, Integer> arrays;

    public GlobalAllocator(FunctionData functionData, Map<String, Integer> staticArrays, Set<String> staticVariables) {
        this.functionData = functionData;
        this.staticVariables = staticVariables;
        this.arrays = new HashMap<>(staticArrays);
    }

    @Override
    public MemoryTable allocate() {
        return null;
    }

    @Override
    public List<String> reallocate(BasicBlock currBasicBlock, MemoryTable memoryTable) {
        return null;
    }
}
