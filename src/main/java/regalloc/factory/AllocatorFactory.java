package regalloc.factory;

import regalloc.allocator.Allocator;
import regalloc.FunctionData;

import java.util.Map;
import java.util.Set;

public interface AllocatorFactory {
    Allocator create(FunctionData functionData, Map<String, Integer> staticArrays, Set<String> staticVariables, Set<String> floatVariables);
}
