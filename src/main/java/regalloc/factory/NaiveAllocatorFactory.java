package regalloc.factory;

import regalloc.allocator.Allocator;
import ir.FunctionData;
import regalloc.allocator.NaiveAllocator;

import java.util.Map;
import java.util.Set;

public class NaiveAllocatorFactory implements AllocatorFactory {
    @Override
    public Allocator create(FunctionData functionData, Map<String, Integer> staticArrays, Set<String> staticVariables, Set<String> floatVariables) {
        return new NaiveAllocator(functionData, staticArrays, staticVariables, floatVariables);
    }
}
