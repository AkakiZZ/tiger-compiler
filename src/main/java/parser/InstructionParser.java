package parser;

import ir_instructions.IRInstruction;
import ir_instructions.InstructionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InstructionParser {

    public IRInstruction parse(String line) {
        List<String> tokens = tokenizeInstruction(line);
        String operation = tokens.get(0);
        tokens.remove(0);
        if (isLabel(line)) return new IRInstruction(operation, tokens, InstructionType.LABEL);
        if (isAssign(line)) return new IRInstruction(operation, tokens, InstructionType.ASSIGN);
        if (isCallOp(line)) return new IRInstruction(operation, tokens, InstructionType.CALL);
        if (isCallrOp(line)) return new IRInstruction(operation, tokens, InstructionType.CALLR);
        if (isReturn(line)) return new IRInstruction(operation, tokens, InstructionType.RETURN);
        if (isArithmeticOp(line)) return new IRInstruction(operation, tokens, InstructionType.ARITHMETIC);
        if (isBranchOp(line)) return new IRInstruction(operation, tokens, InstructionType.BRANCH);
        if (isGoto(line)) return new IRInstruction(operation, tokens, InstructionType.GOTO);
        if (isArrayStore(line)) return new IRInstruction(operation, tokens, InstructionType.ARRAY_STORE);
        if (isArrayLoad(line)) return new IRInstruction(operation, tokens, InstructionType.ARRAY_LOAD);
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
}
