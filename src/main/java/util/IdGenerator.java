package util;

import java.util.IdentityHashMap;
import java.util.Map;

public class IdGenerator {
    private static final Map<Object, Integer> registry = new IdentityHashMap<>();
    private static int currId = 0;

    public static long idFor(Object o) {
        Integer id = registry.get(o);
        if (id == null) {
            id = currId++;
            registry.put(o, id);
        }
        return id;
    }
}
