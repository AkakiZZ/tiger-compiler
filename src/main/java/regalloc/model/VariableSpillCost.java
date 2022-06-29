package regalloc.model;

public class VariableSpillCost implements Comparable<VariableSpillCost>{
    private final String varName;
    private final int spillCost;

    public VariableSpillCost(String varName, int spillCost) {
        this.varName = varName;
        this.spillCost = spillCost;
    }

    public String getVarName() {
        return varName;
    }

    public int getSpillCost() {
        return spillCost;
    }

    @Override
    public int compareTo(VariableSpillCost o) {
        return this.getSpillCost() - o.getSpillCost();
    }
}