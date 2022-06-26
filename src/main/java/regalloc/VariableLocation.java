package regalloc;

public class VariableLocation {
    private String location;
    private boolean isVariableInRegister;

    public VariableLocation(String location, boolean isVariableInRegister) {
        this.location = location;
        this.isVariableInRegister = isVariableInRegister;
    }

    public String getLocation() {
        return location;
    }

    public boolean isVariableInRegister() {
        return isVariableInRegister;
    }

    @Override
    public String toString() {
        return " " + location + (isVariableInRegister? " R" : "");
    }
}
