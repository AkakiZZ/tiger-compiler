package parser;

import ir.IRInstruction;
import ir.InstructionType;

import java.util.*;
import java.util.stream.Collectors;

public class InstructionParser {

    public IRInstruction parse(String line) {
        List<String> tokens = tokenizeInstruction(line);
        String operation = tokens.get(0);
        tokens.remove(0);
        List<String> arguments = new ArrayList<>(tokens);
        Set<String> definedVariables = new HashSet<>();
        Set<String> usedVariables = new HashSet<>();
        if (isLabel(line)) {
            return new IRInstruction(line, operation, arguments, InstructionType.LABEL, definedVariables, usedVariables);
        }
        if (isAssign(line)) {
            if (tokens.size() == 2) {
                definedVariables.add(tokens.get(0));
                if (isVar(tokens.get(1)))
                    usedVariables.add(tokens.get(1));
            }
            return new IRInstruction(line, operation, arguments, InstructionType.ASSIGN, definedVariables, usedVariables);
        }
        if (isCallOp(line)) {
            tokens.remove(0);
            usedVariables.addAll(tokens);
            return new IRInstruction(line, operation, arguments, InstructionType.CALL, definedVariables, usedVariables);
        }
        if (isCallrOp(line)) {
            definedVariables.add(tokens.get(0));
            tokens.remove(0);
            tokens.remove(0);
            usedVariables.addAll(tokens.stream().filter(this::isVar).toList());
            return new IRInstruction(line, operation, arguments, InstructionType.CALLR, definedVariables, usedVariables);
        }
        if (isReturn(line)) {
            usedVariables.addAll(tokens.stream().filter(this::isVar).toList());
            return new IRInstruction(line, operation, arguments, InstructionType.RETURN, definedVariables, usedVariables);
        }
        if (isArithmeticOp(line)) {
            if (isVar(tokens.get(0)))
                usedVariables.add(tokens.get(0));
            if (isVar(tokens.get(1)))
                usedVariables.add(tokens.get(1));
            definedVariables.add(tokens.get(2));
            return new IRInstruction(line, operation, arguments, InstructionType.ARITHMETIC, definedVariables, usedVariables);
        }
        if (isBranchOp(line)) {
            if (isVar(tokens.get(0)))
                usedVariables.add(tokens.get(0));
            if (isVar(tokens.get(1)))
                usedVariables.add(tokens.get(1));
            return new IRInstruction(line, operation, arguments, InstructionType.BRANCH, definedVariables, usedVariables);
        }
        if (isGoto(line)) {
            return new IRInstruction(line, operation, arguments, InstructionType.GOTO, definedVariables, usedVariables);
        }
        if (isArrayStore(line)) {
            if (isVar(tokens.get(2)))
                usedVariables.add(tokens.get(2));
            if (isVar(tokens.get(1)))
                usedVariables.add(tokens.get(1));
            return new IRInstruction(line, operation, arguments, InstructionType.ARRAY_STORE, definedVariables, usedVariables);
        }
        if (isArrayLoad(line)) {
            definedVariables.add(tokens.get(0));
            if (isVar(tokens.get(2)))
                usedVariables.add(tokens.get(2));
            return new IRInstruction(line, operation, arguments, InstructionType.ARRAY_LOAD, definedVariables, usedVariables);
        }
        return null;
    }

    public static boolean isBranchOp(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("breq") || instruction.startsWith("brneq")
                || instruction.startsWith("brlt") || instruction.startsWith("brgt")
                || instruction.startsWith("brleq") || instruction.startsWith("brgeq");
    }

    private boolean isLabel(String instruction) {
        return instruction.contains(":");
    }

    private boolean isGoto(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("goto");
    }

    private boolean isAssign(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("assign");
    }

    private boolean isArithmeticOp(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("add") || instruction.startsWith("sub") || instruction.startsWith("mult")
                || instruction.startsWith("div") || instruction.startsWith("or") || instruction.startsWith("and");
    }

    private boolean isCallOp(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("call") && !instruction.startsWith("callr");
    }

    private boolean isCallrOp(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("callr");
    }

    private boolean isArrayStore(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("array_store");
    }

    private boolean isArrayLoad(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("array_load");
    }

    private boolean isReturn(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("return");
    }

    private List<String> tokenizeInstruction (String instruction) {
        instruction = instruction.trim();
        return Arrays.stream(instruction.split(", "))
                .map(x -> x.replace(",", "").trim()).filter(x -> !x.equals("")).collect(Collectors.toList());
    }

    private boolean isVar(String var) {
        return !Character.isDigit(var.charAt(0));
    }
}
