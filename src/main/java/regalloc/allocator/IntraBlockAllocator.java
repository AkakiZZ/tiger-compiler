package regalloc.allocator;

import ir.FunctionData;
import ir.IRInstruction;
import regalloc.model.MemoryTable;
import regalloc.model.VariableSpillCost;
import util.BasicBlock;

import java.util.*;
import java.util.stream.Collectors;

public class IntraBlockAllocator extends AbstractAllocator {
    private static final int WRITE_COST = 2;
    private static final int READ_COST = 2;
    private static final int MINIMUM_COST_TO_MOVE_IN_REGISTER = WRITE_COST + READ_COST;

    private final static List<String> AVAILABLE_FLOAT_REGISTERS = List.of("$f20", "$f22", "$f24", "$f26", "$f28", "$f30");
    private final static List<String> AVAILABLE_INTEGER_REGISTERS = List.of("$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7");

    public IntraBlockAllocator(FunctionData functionData, Map<String, Integer> staticArrays, Set<String> staticVariables, Set<String> floatVariables) {
        super(functionData, staticArrays, staticVariables, floatVariables);
    }

    @Override
    public List<String> reallocate(BasicBlock currBasicBlock, MemoryTable memoryTable) {
        List<String> variablesToMove = new ArrayList<>();
        Map<String, Integer> variableUsages = new HashMap<>();
        Map<String, Integer> variableDefinitions = new HashMap<>();
        List<VariableSpillCost> spillCosts = getSpillCosts(currBasicBlock, variableUsages, variableDefinitions);
        Map<String, String> newVariableRegisters = new HashMap<>();

        int floatRegistersRemaining = AVAILABLE_FLOAT_REGISTERS.size();
        int integerRegistersRemaining = AVAILABLE_INTEGER_REGISTERS.size();

        Collections.sort(spillCosts);
        Collections.reverse(spillCosts);
        for (VariableSpillCost variableSpillCost : spillCosts) {
            String varName = variableSpillCost.getVarName();
            int cost = variableSpillCost.getSpillCost();
            if (cost >= MINIMUM_COST_TO_MOVE_IN_REGISTER) {
                if (floatVariables.contains(varName) && floatRegistersRemaining > 0) {
                    variablesToMove.add(varName);
                    newVariableRegisters.put(varName, AVAILABLE_FLOAT_REGISTERS.get(floatRegistersRemaining - 1));
                    floatRegistersRemaining--;
                } else if (!floatVariables.contains(varName) && integerRegistersRemaining > 0) {
                    variablesToMove.add(varName);
                    newVariableRegisters.put(varName, AVAILABLE_INTEGER_REGISTERS.get(integerRegistersRemaining - 1));
                    integerRegistersRemaining--;
                }
            }
        }
        memoryTable.setVariableRegisters(newVariableRegisters);

        return variablesToMove;
    }

    private List<VariableSpillCost> getSpillCosts(BasicBlock basicBlock, Map<String, Integer> variableUsages, Map<String, Integer> variableDefinitions) {
        List<IRInstruction> instructions = basicBlock.getInstructions();
        Set<String> allVariables = new HashSet<>();
        List<VariableSpillCost> result = new ArrayList<>();
        for (IRInstruction irInstruction : instructions) {
            Set<String> definedVars = irInstruction
                    .getDefinedVariables()
                    .stream()
                    .filter(x -> !super.isVariableArray(x) & isVar(x))
                    .collect(Collectors.toSet());
            Set<String> usedVars = irInstruction.getUsedVariables()
                    .stream()
                    .filter(x -> !super.isVariableArray(x) & isVar(x))
                    .collect(Collectors.toSet());
            for (String definedVar : definedVars) {
                if (!variableDefinitions.containsKey(definedVar)) {
                    variableDefinitions.put(definedVar, 0);
                }
                allVariables.add(definedVar);
                variableDefinitions.put(definedVar, variableDefinitions.get(definedVar) + 1);
            }
            for (String usedVar : usedVars) {
                if (!variableUsages.containsKey(usedVar)) {
                    variableUsages.put(usedVar, 0);
                }
                allVariables.add(usedVar);
                variableUsages.put(usedVar, variableUsages.get(usedVar) + 1);
            }
        }
        for (String variable : allVariables) {
            result.add(new VariableSpillCost(variable, getSpillCost(variable, variableUsages, variableDefinitions)));
        }
        return result;
    }
    private int getSpillCost(String variable, Map<String, Integer> variableUsages, Map<String, Integer> variableDefinitions) {
        int zeroCost = 0;
        return READ_COST * variableUsages.getOrDefault(variable, zeroCost) + WRITE_COST * variableDefinitions.getOrDefault(variable, zeroCost);
    }

    private boolean isVar(String var) {
        return !Character.isDigit(var.charAt(0));
    }
}
