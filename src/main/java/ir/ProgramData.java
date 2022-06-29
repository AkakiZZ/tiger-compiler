package ir;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ProgramData {
    private final Set<String> staticVariables;
    private final HashMap<String, Integer> staticArrays;
    private final Set<String> floatVars;
    private final List<FunctionData> functions;

    public ProgramData(Set<String> staticVariables, HashMap<String, Integer> staticArrays, Set<String> floatVars, List<FunctionData> functions) {
        this.staticVariables = staticVariables;
        this.staticArrays = staticArrays;
        this.floatVars = floatVars;
        this.functions = functions;
    }

    public Set<String> getStaticVariables() {
        return staticVariables;
    }

    public HashMap<String, Integer> getStaticArrays() {
        return staticArrays;
    }

    public Set<String> getFloatVars() {
        return floatVars;
    }

    public List<FunctionData> getFunctions() {
        return functions;
    }
}
