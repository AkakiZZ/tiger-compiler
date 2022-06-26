package regalloc.allocator;

import regalloc.MemoryTable;

public interface Allocator {
    MemoryTable allocate();
}
