package regalloc.allocator;

import ir.FunctionData;
import regalloc.model.MemoryTable;
import util.BasicBlock;

import java.util.*;

public class NaiveAllocator extends AbstractAllocator {
    public NaiveAllocator(FunctionData functionData, Map<String, Integer> staticArrays, Set<String> staticVariables, Set<String> floatVariables) {
        super(functionData, staticArrays, staticVariables, floatVariables);
    }

    @Override
    public List<String> reallocate(BasicBlock currBasicBlock, MemoryTable memoryTable) {
        return new ArrayList<>();
    }
}
