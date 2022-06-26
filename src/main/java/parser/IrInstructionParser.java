package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IrInstructionParser {
    public static boolean isBranchOp(String instruction) {
        instruction = instruction.trim();
        return instruction.contains("breq") || instruction.contains("brneq")
                || instruction.contains("brlt") || instruction.contains("brgt")
                || instruction.contains("brleq") || instruction.contains("brgeq");
    }

    public static boolean isLabel(String instruction) {
        return instruction.contains(":");
    }

    public static boolean isGoto(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("goto");
    }

    public static boolean isAssign(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("assign");
    }

    public static boolean isArithmeticOp(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("add") || instruction.startsWith("sub") || instruction.startsWith("mult")
                || instruction.contains("div") || instruction.contains("or") || instruction.contains("and");
    }

    public static boolean isCallOp(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("call") && !instruction.startsWith("callr");
    }

    public static boolean isCallrOp(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("callr");
    }

    public static boolean isArrayStore(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("array_store");
    }

    public static boolean isArrayLoad(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("array_load");
    }

    public static boolean isReturn(String instruction) {
        instruction = instruction.trim();
        return instruction.startsWith("return");
    }

    public static List<String> tokenizeInstruction (String instruction) {
        instruction = instruction.trim();
        return Arrays.stream(instruction.split(", "))
                .map(x -> x.replace(",", "").trim()).filter(x -> !x.equals("")).collect(Collectors.toList());
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
