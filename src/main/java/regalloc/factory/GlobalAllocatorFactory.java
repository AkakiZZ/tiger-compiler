package regalloc.factory;

import regalloc.FunctionData;
import regalloc.allocator.Allocator;
import regalloc.allocator.GlobalAllocator;

import java.util.Map;
import java.util.Set;

public class GlobalAllocatorFactory implements AllocatorFactory{
    @Override
    public Allocator create(FunctionData functionData, Map<String, Integer> staticArrays, Set<String> staticVariables) {
        return new GlobalAllocator(functionData, staticArrays, staticVariables);
    }
}
