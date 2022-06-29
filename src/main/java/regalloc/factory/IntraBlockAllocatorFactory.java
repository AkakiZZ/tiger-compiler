package regalloc.factory;

import ir.FunctionData;
import regalloc.allocator.Allocator;
import regalloc.allocator.IntraBlockAllocator;

import java.util.Map;
import java.util.Set;

public class IntraBlockAllocatorFactory implements AllocatorFactory{
    @Override
    public Allocator create(FunctionData functionData, Map<String, Integer> staticArrays, Set<String> staticVariables, Set<String> floatVariables) {
        return new IntraBlockAllocator(functionData, staticArrays, staticVariables, floatVariables);
    }
}
