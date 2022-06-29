package util;

import ir.IRInstruction;
import ir.InstructionType;

import java.util.*;
import java.util.stream.Collectors;

public class Liveness {
    private final String functionName;
    private final Map<Integer, List<Integer>> cfg;
    private final TreeMap<Integer, Set<String>> outs;
    private final TreeMap<Integer, Set<String>> ins;
    private final List<IRInstruction> instructions;
    private final Set<String> arrayNames;

    public Liveness(String functionName, Map<Integer, List<Integer>> cfg, List<IRInstruction> instructions, Set<String> arrayNames) {
        this.functionName = functionName;
        this.cfg = cfg;
        this.instructions = instructions;
        this.arrayNames = arrayNames;
        outs = new TreeMap<>();
        ins = new TreeMap<>();
        analyse();

    }

    public List<List<Integer>> getVarLiveRanges(String var) {
        List<List<Integer>> result = new ArrayList<>();
        for (int lineNumber : outs.keySet()) {
            if ((outs.get(lineNumber).contains(var) ||
                    ins.get(lineNumber).contains(var)) &&
                    result.isEmpty()) {
                result.add(List.of(lineNumber, lineNumber));
            } else if ((outs.get(lineNumber).contains(var) ||
                    ins.get(lineNumber).contains(var)) &&
                    result.get(result.size() - 1).get(1) == lineNumber - 1) {
                int start = result.get(result.size() - 1).get(0);
                result.remove(result.size() - 1);
                result.add(List.of(start, lineNumber));
            } else if (outs.get(lineNumber).contains(var) ||
                    ins.get(lineNumber).contains(var)) {
                result.add(List.of(lineNumber, lineNumber));
            }
        }
        return result;
    }

    public TreeMap<Integer, Set<String>> getOuts() {
        return outs;
    }

    public TreeMap<Integer, Set<String>> getIns() {
        return ins;
    }

    public String getFunctionName() {
        return functionName;
    }

    private void analyse() {
        for (Integer i : cfg.keySet()) {
            ins.put(i, new HashSet<>());
            outs.put(i, new HashSet<>());
        }

        boolean hasChanges = true;
        while (hasChanges) {

            hasChanges = false;
            for (Integer instructionId : cfg.keySet()) {
                //in
                Set<String> def = getDefinedVars(instructions.get(instructionId));
                Set<String> use = getUsedVars(instructions.get(instructionId));
                int oldSize = ins.get(instructionId).size();
                HashSet<String> tmp = new HashSet<>(outs.get(instructionId));
                tmp.removeAll(def);
                tmp.addAll(use);
                int newSize = tmp.size();
                ins.put(instructionId, tmp);

                if (oldSize != newSize) hasChanges = true;

                //out
                HashSet<String> tmp1 = new HashSet<>(outs.get(instructionId));
                oldSize = outs.get(instructionId).size();
                for (Integer successorId : cfg.get(instructionId)) {
                    tmp1.addAll(ins.get(successorId));
                }
                newSize = tmp1.size();
                outs.put(instructionId, tmp1);
                if (oldSize != newSize) hasChanges = true;
            }
            //in[I] = (out[I] - def[I]) U use[I]
            //out[I] = U in [I'] for all successor I'
        }
    }

    private Set<String> getDefinedVars(IRInstruction instruction) {
        return instruction.getDefinedVariables()
                .stream()
                .filter(x -> !isArray(x))
                .collect(Collectors.toSet());
    }

    private Set<String> getUsedVars(IRInstruction instruction) {
        return instruction.getUsedVariables()
                .stream()
                .filter(x -> !isArray(x))
                .collect(Collectors.toSet());
    }

    private boolean isArray(String var) {
        return arrayNames.contains(var);
    }
}
