package regalloc.allocator;

import regalloc.model.MemoryTable;
import util.BasicBlock;

import java.util.List;

public interface Allocator {
    MemoryTable allocate();
    List<String> reallocate(BasicBlock currBasicBlock, MemoryTable memoryTable);
}
