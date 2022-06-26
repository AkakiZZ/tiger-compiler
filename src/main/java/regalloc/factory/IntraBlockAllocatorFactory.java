package regalloc.factory;

import regalloc.FunctionData;
import regalloc.allocator.Allocator;
import regalloc.allocator.IntraBlockAllocator;

import java.util.Map;
import java.util.Set;

public class IntraBlockAllocatorFactory implements AllocatorFactory{
    @Override
    public Allocator create(FunctionData functionData, Map<String, Integer> staticArrays, Set<String> staticVariables) {
        return new IntraBlockAllocator(functionData, staticArrays, staticVariables);
    }
}
