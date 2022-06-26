package regalloc.factory;

import regalloc.allocator.Allocator;
import regalloc.FunctionData;
import regalloc.allocator.NaiveAllocator;

import java.util.Map;
import java.util.Set;

public class NaiveAllocatorFactory implements AllocatorFactory {
    @Override
    public Allocator create(FunctionData functionData, Map<String, Integer> staticArrays, Set<String> staticVariables) {
        return new NaiveAllocator(functionData, staticArrays, staticVariables);
    }
}
