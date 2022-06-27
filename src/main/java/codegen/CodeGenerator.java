package codegen;

import ir_instructions.IRInstruction;
import ir_instructions.InstructionType;
import parser.FunctionParser;
import parser.Parser;
import regalloc.FunctionData;
import regalloc.MemoryTable;
import regalloc.allocator.Allocator;
import regalloc.factory.AllocatorFactory;

import java.util.*;

public class CodeGenerator {
    private final static List<String> mustSaveIntegerRegisters = List.of("$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7", "$ra");
    private final static List<String> tempIntegerRegisters = List.of("$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7");
    private final static List<String> argumentIntegerRegisters = List.of("$a0", "$a1", "$a2", "$a3");

    private final static List<String> mustSaveFloatRegisters = List.of("$s20", "$s22", "$s24", "$s26", "$s28", "$s30", "$s32");
    private final static List<String> tempFloatRegisters = List.of("$f4", "$f6", "$f8", "$f10", "$f14", "$f16");
    private final static List<String> argumentFloatRegisters = List.of("$f12", "$f14");

    // int registers
    private final static String LOAD_CONSTANT_IN_REGISTER_TEMPLATE = "\tli %s, %s";
    private final static String LOAD_VARIABLE_IN_REGISTER_TEMPLATE = "\tlw %s, %s(%s)";
    private final static String STORE_VARIABLE_FROM_REGISTER_TEMPLATE = "\tsw %s, %s(%s)";
    private final static String BRANCH_AND_ARITHMETIC_INSTRUCTION_TEMPLATE = "\t%s %s, %s, %s";
    private final static String STATIC_VARIABLE_TEMPLATE = "\t%s: .space %d";
    private final static String INCREASE_STACK_POINTER_TEMPLATE = "\taddi $sp, $sp, %d";
    private final static String DECREASE_STACK_POINTER_TEMPLATE = "\taddi $sp, $sp, -%d";
    private final static String GOTO_TEMPLATE = "\tj %s";
    private final static String JAL_TEMPLATE = "\tjal %s";

    private final static String REGISTER_TO_REGISTER_MOVE_TEMPLATE = "\tmove %s, %s";

    // float register
    private final static String LOAD_CONSTANT_IN_REGISTER_TEMPLATE_FLOAT = "\tli.s %s, %s";
    private final static String LOAD_VARIABLE_IN_REGISTER_TEMPLATE_FLOAT = "\tl.s %s, %s(%s)";
    private final static String STORE_VARIABLE_FROM_REGISTER_TEMPLATE_FLOAT = "\ts.s %s, %s(%s)";
    private final static String REGISTER_TO_REGISTER_MOVE_TEMPLATE_FLOAT = "\tmov.s %s, %s";
    private final static String MOVE_INT_REGISTER_TO_FLOAT_REGISTER = "\tmtc1 %s, %s";
    private final static String CONVERT_INT_TO_FLOAT = "\tcvt.s.w %s, %s";
    private static final String FLOAT_BRANCH_INSTRUCTION = "\t%s, %s, %s";
    private final static String BRANCH_TRUE_FLOAT = "\tbc1t %s";
    private final static String BRANCH_FALSE_FLOAT = "\tbc1f %s";


    private final static String ZERO_REGISTER = "$zero";
    private final static String SP_REGISTER = "$sp";
    private final static String INTEGER_RETURN_REGISTER = "$v0";
    private final static String FLOAT_RETURN_REGISTER = "$f0";

    private final static Map<String, String> IrOpsToMipsForIntegers = new HashMap<>() {{
        put("add", "add");
        put("sub", "sub");
        put("mult", "mul");
        put("div", "div");
        put("or", "or");
        put("and", "and");
    }};

    private final static Map<String, String> IrOpsToMipsForFloats = new HashMap<>() {{
        put("add", "add.s");
        put("sub", "sub.s");
        put("mult", "mul.s");
        put("div", "div.s");
    }};

    private final static Map<String, String> IrBranchesToMipsForIntegers = new HashMap<>() {{
        put("breq", "beq");
        put("brneq", "bne");
        put("brlt", "blt");
        put("brgt", "bgt");
        put("brleq", "ble");
        put("brgeq", "bge");
    }};

    private final static Map<String, String> IrBranchesToMipsForFloats = new HashMap<>() {{
        put("breq", "c.eq.s");
        put("brlt", "c.le.s");
        put("brleq", "c.lt.s");
        // not supported
        put("brneq", "c.eq.s");
        put("brgt", "c.lt.s");
        put("brgeq", "c.le.s");
    }};

    private final static Set<String> NotSupportedIrBranchesForFloats = new HashSet<>(List.of("brneq", "brgt", "brgeq"));

    private final static Map<String, String> libFunctionLabels = new HashMap<>() {{
        put("printi", "_lprinti");
        put("printf", "_lprintf");
        put("not", "_lnot");
        put("exit", "_lexit");
    }};

    private Set<String> staticVars;
    private Map<String, Integer> staticArrays;
    private Set<String> floats;
    private final Parser parser;
    private final AllocatorFactory allocatorFactory;

    public CodeGenerator(Parser parser, AllocatorFactory allocatorFactory) {
        this.parser = parser;
        this.allocatorFactory = allocatorFactory;
    }

    public List<String> generateMips() {
        List<String> instructions = generateInitSegment();
        while (true) {
            List<String> functionLines = parser.getNextFunction();
            if (functionLines == null) break;
            FunctionParser functionParser = new FunctionParser(functionLines);
            FunctionData data = functionParser.getFunctionData();
            Allocator allocator = allocatorFactory.create(data, staticArrays, staticVars, floats);
            MemoryTable memoryTable = allocator.allocate();
            instructions.addAll(generateFunction(memoryTable, data.getInstructions()));
        }
        instructions.addAll(generateStandardFunctions());
        return instructions;
    }

    private List<String> generateInitSegment() {
        // TODO float support
        List<String> instructions = new ArrayList<>();
        instructions.add(".data");
        staticVars = new HashSet<>(parser.getStaticVariables());
        staticArrays = parser.getStaticArrays();
        floats = parser.getFloatVariables();
        for (String var : staticVars) {
            instructions.add(generateStaticVariableInstruction(var, 4));
        }
        for (String arr : staticArrays.keySet()) {
            int arraySize = staticArrays.get(arr);
            staticVars.add(arr);
            instructions.add(generateStaticVariableInstruction(arr, arraySize * 4));
        }
        instructions.add(".text");
        instructions.add("");
        return instructions;
    }

    private List<String> generateFunction(MemoryTable memoryTable, List<IRInstruction> irInstructions) {
        List<String> instructions = new ArrayList<>();
        // write name label
        instructions.add(irInstructions.get(0).getOperation());
        irInstructions.remove(0);
        //decrement $sp
        instructions.add(generateDecreaseStackPointer(memoryTable.getFrameSize()));

        //save registers
        for (String registerName : mustSaveIntegerRegisters) {
            instructions.add(generateStoreVariableFromRegisterInstruction(registerName,
                    String.valueOf(memoryTable.getSavedRegisterOffset(registerName)),
                    SP_REGISTER, false));
        }

        for (IRInstruction irInstruction : irInstructions) {
            if (irInstruction.getInstructionType() == InstructionType.LABEL) instructions.add(irInstruction.getOperation());
            if (irInstruction.getInstructionType() == InstructionType.ASSIGN) instructions.addAll(generateAssign(irInstruction, memoryTable));
            if (irInstruction.getInstructionType() == InstructionType.CALL) instructions.addAll(generateCall(irInstruction, memoryTable));
            if (irInstruction.getInstructionType() == InstructionType.CALLR) instructions.addAll(generateCallR(irInstruction, memoryTable));
            if (irInstruction.getInstructionType() == InstructionType.RETURN) instructions.addAll(generateReturn(irInstruction, memoryTable));
            if (irInstruction.getInstructionType() == InstructionType.ARITHMETIC) instructions.addAll(generateArithmetic(irInstruction, memoryTable));
            if (irInstruction.getInstructionType() == InstructionType.BRANCH) instructions.addAll(generateBranch(irInstruction, memoryTable));
            if (irInstruction.getInstructionType() == InstructionType.GOTO) instructions.addAll(generateGoto(irInstruction));
            if (irInstruction.getInstructionType() == InstructionType.ARRAY_STORE) instructions.addAll(generateArrayStore(irInstruction, memoryTable));
            if (irInstruction.getInstructionType() == InstructionType.ARRAY_LOAD) instructions.addAll(generateArrayLoad(irInstruction, memoryTable));
        }

        return instructions;
    }

    private List<String> loadVariableInRegister(String variableToLoad, String register, MemoryTable memoryTable) {
        List<String> instructions = new ArrayList<>();

        if (isFloatConstant(variableToLoad)) {
            instructions.add(generateLoadConstantInRegisterInstruction(register, variableToLoad, true));
            return instructions;
        }

        if (isIntegerConstant(variableToLoad)) {
            instructions.add(generateLoadConstantInRegisterInstruction(register, variableToLoad, false));
            return instructions;
        }

        boolean isFloat = memoryTable.isVariableFloat(variableToLoad);

        if (memoryTable.isVariableInRegister(variableToLoad)) {
            instructions.add(generateRegisterToRegisterMove(register, memoryTable.getVariableRegister(variableToLoad), isFloat));
        } else {
            if (memoryTable.isVariableStack(variableToLoad)) {
                int offset = memoryTable.getStackVariableOffset(variableToLoad);
                instructions.add(generateLoadVariableInRegisterInstruction(register, String.valueOf(offset), SP_REGISTER, isFloat));
            }
            if (memoryTable.isVariableStatic(variableToLoad)) {
                instructions.add(generateLoadVariableInRegisterInstruction(register, variableToLoad, ZERO_REGISTER, isFloat));
            }
        }

        return instructions;
    }

    private List<String> saveVariableFromRegister(String variableToSave, String register, MemoryTable memoryTable) {
        List<String> instructions = new ArrayList<>();

        if (isFloatConstant(variableToSave)) {
            instructions.add(generateLoadConstantInRegisterInstruction(register, variableToSave, true));
            return instructions;
        }

        if (isIntegerConstant(variableToSave)) {
            instructions.add(generateLoadConstantInRegisterInstruction(register, variableToSave, false));
            return instructions;
        }

        boolean isFloat = memoryTable.isVariableFloat(variableToSave);

        // if in register move not save
        if (memoryTable.isVariableInRegister(variableToSave)) {
            instructions.add(generateRegisterToRegisterMove(memoryTable.getVariableRegister(variableToSave), register, isFloat));
        } else {

            if (memoryTable.isVariableStack(variableToSave)) {
                int offset = memoryTable.getStackVariableOffset(variableToSave);
                instructions.add(generateStoreVariableFromRegisterInstruction(register, String.valueOf(offset), SP_REGISTER, isFloat));
            }
            if (memoryTable.isVariableStatic(variableToSave)) {
                instructions.add(generateStoreVariableFromRegisterInstruction(register, variableToSave, ZERO_REGISTER, isFloat));
            }
        }

        return instructions;
    }

    private List<String> generateAssign(IRInstruction irInstruction, MemoryTable memoryTable) {
        List<String> instructions = new ArrayList<>();
        List<String> arguments = irInstruction.getArguments();
        boolean isVarToSaveFloat = memoryTable.isVariableFloat(arguments.get(0));

        if (arguments.size() == 3) { //array initialization
            for (int i = 0; i < memoryTable.getArraySize(arguments.get(0)); i++) {
                boolean isVarToLoadFloat = memoryTable.isVariableFloat(arguments.get(2)) || isFloatConstant(arguments.get(2));
                String tmpRegister = isVarToLoadFloat? tempFloatRegisters.get(1) : tempIntegerRegisters.get(1);
                instructions.addAll(loadVariableInRegister(arguments.get(2), tmpRegister, memoryTable));
                if (isVarToSaveFloat && !isVarToLoadFloat) {
                    String sourceRegister = tmpRegister;
                    tmpRegister = tempFloatRegisters.get(0);
                    instructions.add(generateMoveIntRegisterToFloatRegister(tmpRegister, sourceRegister));
                    instructions.add(generateConvertFloatToInt(tmpRegister, tmpRegister));
                }
                instructions.addAll(generateArrayStoreHelper(arguments.get(0), tmpRegister, String.valueOf(i), memoryTable));
            }
            return instructions;
        }

        if (memoryTable.isVariableArray(arguments.get(0)) && memoryTable.isVariableArray(arguments.get(1))) {
            for (int i = 0; i < memoryTable.getArraySize(arguments.get(0)); i++) {
                String tmpRegister = isVarToSaveFloat? tempFloatRegisters.get(1) : tempIntegerRegisters.get(1);
                instructions.addAll(generateArrayLoadHelper(arguments.get(1), tmpRegister, String.valueOf(i), memoryTable));
                instructions.addAll(generateArrayStoreHelper(arguments.get(0), tmpRegister, String.valueOf(i), memoryTable));
            }
            return instructions;
        }

        String varToSave = arguments.get(0);
        String varToLoad = arguments.get(1);
        boolean isVarToLoadFloat = memoryTable.isVariableFloat(varToLoad) || isFloatConstant(varToLoad);
        String reg = isVarToLoadFloat? tempFloatRegisters.get(0) : tempIntegerRegisters.get(0);

        instructions.addAll(loadVariableInRegister(varToLoad, reg, memoryTable));

        if (isVarToSaveFloat && !isVarToLoadFloat) {
            String sourceRegister = reg;
            reg = tempFloatRegisters.get(0);
            instructions.add(generateMoveIntRegisterToFloatRegister(reg, sourceRegister));
            instructions.add(generateConvertFloatToInt(reg, reg));
        }

        instructions.addAll(saveVariableFromRegister(varToSave, reg, memoryTable));
        return instructions;
    }

    private List<String> generateArrayStore(IRInstruction irInstruction, MemoryTable memoryTable) {
        List<String> instructions = new ArrayList<>();
        List<String> arguments = irInstruction.getArguments();

        String arrayToSave = arguments.get(0);
        String varName = arguments.get(2);
        String index = arguments.get(1);

        boolean isArrayToSaveFloat = memoryTable.isVariableFloat(arrayToSave);
        boolean isVarToLoadFloat = memoryTable.isVariableFloat(varName) || isFloatConstant(varName);

        String toSaveRegister = isVarToLoadFloat? tempFloatRegisters.get(1) : tempIntegerRegisters.get(1);
        instructions.addAll(loadVariableInRegister(varName, toSaveRegister, memoryTable));

        if (isArrayToSaveFloat && !isVarToLoadFloat) {
            String sourceRegister = toSaveRegister;
            toSaveRegister = tempFloatRegisters.get(0);
            instructions.add(generateMoveIntRegisterToFloatRegister(toSaveRegister, sourceRegister));
            instructions.add(generateConvertFloatToInt(toSaveRegister, toSaveRegister));
        }

        instructions.addAll(generateArrayStoreHelper(arrayToSave, toSaveRegister, index, memoryTable));

        return instructions;
    }

    private List<String> generateArrayStoreHelper(String arrayToSave, String toSaveRegister, String index, MemoryTable memoryTable) {
        List<String> instructions = new ArrayList<>();

        String addressRegister = tempIntegerRegisters.get(0);
        String multiplierRegister = tempIntegerRegisters.get(2);

        boolean isFloat = memoryTable.isVariableFloat(arrayToSave);
        // static array sw $rs, label($rd)
        if (memoryTable.isVariableStatic(arrayToSave)) {
            generateArrayElementAddressOffset(memoryTable, instructions, addressRegister, multiplierRegister, index);
            instructions.add(generateStoreVariableFromRegisterInstruction(toSaveRegister, arrayToSave, addressRegister, isFloat));
        }

        // stack array sw $rs, 0($address)
        if (memoryTable.isVariableStack(arrayToSave)) {
            generateArrayElementAddressOffset(memoryTable, instructions, addressRegister, multiplierRegister, index);
            instructions.add(generateArithmeticInstruction("add", addressRegister, addressRegister, SP_REGISTER));
            instructions.add(generateStoreVariableFromRegisterInstruction(toSaveRegister, String.valueOf(memoryTable.getStackVariableOffset(arrayToSave)), addressRegister, isFloat));
        }

        return instructions;
    }

    private List<String> generateArrayLoad(IRInstruction irInstruction, MemoryTable memoryTable) {
        List<String> arguments = irInstruction.getArguments();

        String array = arguments.get(1);
        String variableName = arguments.get(0);
        String index = arguments.get(2);

        boolean isFloat = memoryTable.isVariableFloat(array);

        String toLoadRegister = isFloat? tempFloatRegisters.get(1) : tempIntegerRegisters.get(1);

        List<String> instructions = generateArrayLoadHelper(array, toLoadRegister, index, memoryTable);
        instructions.addAll(saveVariableFromRegister(variableName, toLoadRegister, memoryTable));
        return instructions;
    }

    private List<String> generateArrayLoadHelper(String array, String toLoadRegister, String index, MemoryTable memoryTable) {
        List<String> instructions = new ArrayList<>();

        String addressRegister = tempIntegerRegisters.get(0);
        String multiplierRegister = tempIntegerRegisters.get(2);

        boolean isFloat = memoryTable.isVariableFloat(array);

        // static array lw $rs, label($rd)
        //              sw $rs, offset($sp)
        if (memoryTable.isVariableStatic(array)) {
            generateArrayElementAddressOffset(memoryTable, instructions, addressRegister, multiplierRegister, index);
            instructions.add(generateLoadVariableInRegisterInstruction(toLoadRegister, array, addressRegister, isFloat));
        }

        // stack array sw $rs, 0($address)
        if (memoryTable.isVariableStack(array)) {
            generateArrayElementAddressOffset(memoryTable, instructions, addressRegister, multiplierRegister, index);
            instructions.add(generateArithmeticInstruction("add", addressRegister, addressRegister, SP_REGISTER));
            instructions.add(generateLoadVariableInRegisterInstruction(toLoadRegister, String.valueOf(memoryTable.getStackVariableOffset(array)), addressRegister, isFloat));
        }
        return instructions;
    }

    private void generateArrayElementAddressOffset(MemoryTable memoryTable, List<String> instructions, String addressRegister, String multiplierRegister, String index) {
        instructions.addAll(loadVariableInRegister(index, addressRegister, memoryTable));
        instructions.add(generateLoadConstantInRegisterInstruction(multiplierRegister, String.valueOf(4), false));
        instructions.add(generateArithmeticInstruction("mul", addressRegister, addressRegister, multiplierRegister));
    }

    private List<String> generateCall(IRInstruction irInstruction, MemoryTable memoryTable) {
        List<String> instructions;
        List<String> arguments = irInstruction.getArguments();

        String calleeName = arguments.get(0);
        if (libFunctionLabels.containsKey(calleeName)) {
            String arg = irInstruction.getArguments().get(1);
            boolean isFloatFunction = calleeName.equals("printf");
            boolean isArgFloat = memoryTable.isVariableFloat(arg) || isFloatConstant(arg);
            String reg = isArgFloat? argumentFloatRegisters.get(0) : argumentIntegerRegisters.get(0);
            instructions = new ArrayList<>(loadVariableInRegister(arg, reg, memoryTable));
            if (isFloatFunction && !isArgFloat) {
                String sourceRegister = reg;
                reg =  argumentFloatRegisters.get(0);
                instructions.add(generateMoveIntRegisterToFloatRegister(reg, sourceRegister));
                instructions.add(generateConvertFloatToInt(reg, reg));
            }
            instructions.add(generateJalInstruction(libFunctionLabels.get(calleeName)));

        } else {
            int numParameters = arguments.size() - 1;
            instructions = callHelper(arguments, memoryTable, numParameters);
            // increment $sp
            instructions.add(generateIncreaseStackPointer( numParameters * 4));
        }

        return instructions;

    }

    private List<String> generateCallR(IRInstruction irInstruction, MemoryTable memoryTable) {
        List<String> instructions;
        List<String> arguments = irInstruction.getArguments();
        String varName = irInstruction.getArguments().get(0);
        String calleeName = irInstruction.getArguments().get(1);
        if (libFunctionLabels.containsKey(calleeName)) {
            String arg = irInstruction.getArguments().get(2);
            instructions = new ArrayList<>(loadVariableInRegister(arg, argumentIntegerRegisters.get(0), memoryTable));
            instructions.add(generateJalInstruction(libFunctionLabels.get(calleeName)));
        } else {
            arguments.remove(0);
            int numParameters = arguments.size() - 1;
            instructions = callHelper(arguments, memoryTable, numParameters);

            // increment $sp
            instructions.add(generateIncreaseStackPointer(numParameters * 4));
        }
        boolean isRetVariableFloat = memoryTable.isVariableFloat(varName) || isFloatConstant(varName);
        if (isRetVariableFloat)
            instructions.addAll(saveVariableFromRegister(varName, FLOAT_RETURN_REGISTER, memoryTable));
        else
            instructions.addAll(saveVariableFromRegister(varName, INTEGER_RETURN_REGISTER, memoryTable));
        return instructions;
    }

    private List<String> callHelper(List<String> arguments, MemoryTable memoryTable, int numParameters) {
        List<String> instructions = new ArrayList<>();
        String calleeName = arguments.get(0);

        for (int i = 1; i < arguments.size(); i++) {
            String var = arguments.get(i);
            boolean isFloat = isFloatConstant(arguments.get(i)) || memoryTable.isVariableFloat(var);
            String reg = isFloat? tempFloatRegisters.get(0) : tempIntegerRegisters.get(0);
            instructions.addAll(loadVariableInRegister(var, reg, memoryTable));
            int offset = (i - 1) * 4;
            instructions.add(generateStoreVariableFromRegisterInstruction(reg, String.valueOf(offset - numParameters * 4), SP_REGISTER, isFloat));
        }

        // decrement $sp
        instructions.add(generateDecreaseStackPointer(numParameters * 4));

        // jump
        instructions.add(generateJalInstruction(calleeName));

        return instructions;
    }
    private List<String> generateReturn(IRInstruction irInstruction, MemoryTable memoryTable) {
        List<String> instructions = new ArrayList<>();
        List<String> arguments = irInstruction.getArguments();

        //load saved registers
        for (String registerName : mustSaveIntegerRegisters) {
            instructions.add(generateLoadVariableInRegisterInstruction(registerName,
                    String.valueOf(memoryTable.getSavedRegisterOffset(registerName)),
                    SP_REGISTER, false));
        }
        if (arguments.size() != 0) {
            boolean isRetVariableFloat = memoryTable.isVariableFloat(arguments.get(0)) || isFloatConstant(arguments.get(0));
            if (isRetVariableFloat)
                instructions.addAll(loadVariableInRegister(arguments.get(0), FLOAT_RETURN_REGISTER, memoryTable));
            else
                instructions.addAll(loadVariableInRegister(arguments.get(0), INTEGER_RETURN_REGISTER, memoryTable));
        }

        //increment $sp
        instructions.add(generateIncreaseStackPointer(memoryTable.getFrameSize()));

        instructions.add(generateJrInstruction());

        return instructions;
    }

    public List<String> generateArithmetic(IRInstruction irInstruction, MemoryTable memoryTable) {
        List<String> instructions = new ArrayList<>();
        List<String> arguments = irInstruction.getArguments();
        String varToSave = arguments.get(2);
        String firstVarToAdd = arguments.get(0);
        String secondVarToAdd = arguments.get(1);
        boolean isFirstVarFloat = memoryTable.isVariableFloat(firstVarToAdd) || isFloatConstant(firstVarToAdd);
        boolean isSecondVarFloat = memoryTable.isVariableFloat(secondVarToAdd) || isFloatConstant(secondVarToAdd);
        boolean isVarToSaveFloat = memoryTable.isVariableFloat(varToSave);
        String mipsOperation = isVarToSaveFloat? IrOpsToMipsForFloats.get(irInstruction.getOperation()) : IrOpsToMipsForIntegers.get(irInstruction.getOperation());

        String reg1 = isFirstVarFloat? tempFloatRegisters.get(0) : tempIntegerRegisters.get(0);
        String reg2 = isSecondVarFloat? tempFloatRegisters.get(1) : tempIntegerRegisters.get(1);
        String reg3 = isVarToSaveFloat? tempFloatRegisters.get(2) : tempIntegerRegisters.get(2);

        //first term
        instructions.addAll(loadVariableInRegister(firstVarToAdd, reg1, memoryTable));
        if (isVarToSaveFloat && !isFirstVarFloat) {
            String sourceRegister = reg1;
            reg1 = tempFloatRegisters.get(0);
            instructions.add(generateMoveIntRegisterToFloatRegister(reg1, sourceRegister));
            instructions.add(generateConvertFloatToInt(reg1, reg1));
        }

        //second term
        instructions.addAll(loadVariableInRegister(secondVarToAdd, reg2, memoryTable));
        if (isVarToSaveFloat && !isSecondVarFloat) {
            String sourceRegister = reg2;
            reg2 = tempFloatRegisters.get(1);
            instructions.add(generateMoveIntRegisterToFloatRegister(reg2, sourceRegister));
            instructions.add(generateConvertFloatToInt(reg2, reg2));
        }

        // operation
        instructions.add(generateArithmeticInstruction(mipsOperation, reg3, reg1, reg2));

        //save
        instructions.addAll(saveVariableFromRegister(varToSave, reg3, memoryTable));
        return instructions;
    }

    private List<String> generateBranch(IRInstruction irInstruction, MemoryTable memoryTable) {
        List<String> instructions = new ArrayList<>();
        String branchType = IrBranchesToMipsForIntegers.get(irInstruction.getOperation());
        List<String> arguments = irInstruction.getArguments();
        String label = arguments.get(2);
        String first = arguments.get(0);
        String second = arguments.get(1);

        boolean isFirstVarFloat = memoryTable.isVariableFloat(first) || isFloatConstant(first);
        boolean isSecondVarFloat = memoryTable.isVariableFloat(second) || isFloatConstant(second);

        String reg1 = isFirstVarFloat? tempFloatRegisters.get(0) : tempIntegerRegisters.get(0);
        String reg2 = isSecondVarFloat? tempFloatRegisters.get(1) : tempIntegerRegisters.get(1);

        if (isFirstVarFloat || isSecondVarFloat) {
            branchType = IrBranchesToMipsForFloats.get(irInstruction.getOperation());
            if (isSecondVarFloat && !isFirstVarFloat) {
                String sourceRegister = reg1;
                reg1 = tempFloatRegisters.get(0);
                instructions.add(generateMoveIntRegisterToFloatRegister(reg1, sourceRegister));
                instructions.add(generateConvertFloatToInt(reg1, reg1));
            }
            if (isFirstVarFloat && !isSecondVarFloat) {
                String sourceRegister = reg2;
                reg2 = tempFloatRegisters.get(1);
                instructions.add(generateMoveIntRegisterToFloatRegister(reg2, sourceRegister));
                instructions.add(generateConvertFloatToInt(reg2, reg2));
            }
        }

        instructions.addAll(loadVariableInRegister(first, reg1, memoryTable));

        instructions.addAll(loadVariableInRegister(second, reg2, memoryTable));

        if (isFirstVarFloat || isSecondVarFloat) {
            instructions.add(generateFloatBranchInstruction(branchType, reg1, reg2));
            if (NotSupportedIrBranchesForFloats.contains(irInstruction.getOperation())) {
                instructions.add(generateBranchFalse(label));
            } else {
                instructions.add(generateBranchTrue(label));
            }
        } else {
            instructions.add(generateIntegerBranchInstruction(branchType, reg1, reg2, label));
        }

        return instructions;
    }

    private List<String> generateGoto(IRInstruction irInstruction) {
        List<String> instructions = new ArrayList<>();
        instructions.add(generateJumpInstruction(irInstruction.getArguments().get(0)));
        return instructions;
    }


    public List<String> generateStandardFunctions() {
        return List.of("_lprinti:",
                "\tli $v0, 1",
                "\tsyscall",
                "\tli $a0, 10",
                "\tli $v0, 11",
                "\tsyscall",
                "\tjr $ra",
                "_lprintf:",
                "\tli $v0, 2",
                "\tsyscall",
                "\tli $a0, 10",
                "\tli $v0, 11",
                "\tsyscall",
                "\tjr $ra",
                "_lnot:",
                "\tbne $a0, $zero, __ret_zero_start",
                "\tli $v0, 1",
                "\tj __ret_zero_end",
                "__ret_zero_start:",
                "\tli $v0, 0",
                "__ret_zero_end:",
                "\tjr $ra",
                "_lexit:",
                "\tli $v0, 17",
                "\tsyscall",
                "\tjr $ra",
                "\n");
    }

    private String generateLoadConstantInRegisterInstruction(String register, String constant, boolean coprocessor) {
        return coprocessor? String.format(LOAD_CONSTANT_IN_REGISTER_TEMPLATE_FLOAT, register, constant)
                : String.format(LOAD_CONSTANT_IN_REGISTER_TEMPLATE, register, constant);
    }

    private String generateLoadVariableInRegisterInstruction(String registerToLoad, String variableOffset, String addressRegister, boolean coprocessor) {
        return coprocessor? String.format(LOAD_VARIABLE_IN_REGISTER_TEMPLATE_FLOAT, registerToLoad, variableOffset, addressRegister)
                : String.format(LOAD_VARIABLE_IN_REGISTER_TEMPLATE, registerToLoad, variableOffset, addressRegister);
    }

    private String generateStoreVariableFromRegisterInstruction(String registerFromStore, String variableOffset, String addressRegister, boolean coprocessor) {
        return coprocessor? String.format(STORE_VARIABLE_FROM_REGISTER_TEMPLATE_FLOAT, registerFromStore, variableOffset, addressRegister)
                : String.format(STORE_VARIABLE_FROM_REGISTER_TEMPLATE, registerFromStore, variableOffset, addressRegister);
    }

    private String generateRegisterToRegisterMove(String destRegister, String srcRegister, boolean coprocessor) {
        return coprocessor? String.format(REGISTER_TO_REGISTER_MOVE_TEMPLATE_FLOAT, destRegister, srcRegister)
                : String.format(REGISTER_TO_REGISTER_MOVE_TEMPLATE, destRegister, srcRegister);
    }

    private String generateMoveIntRegisterToFloatRegister(String destRegister, String srcRegister) {
        return String.format(MOVE_INT_REGISTER_TO_FLOAT_REGISTER, srcRegister, destRegister);
    }

    private String generateConvertFloatToInt(String destRegister, String sourceRegister) {
        return String.format(CONVERT_INT_TO_FLOAT, destRegister, sourceRegister);
    }

    private String generateFloatBranchInstruction(String branchType, String register1, String register2) {
        return String.format(FLOAT_BRANCH_INSTRUCTION, branchType, register1, register2);
    }

    private String generateIntegerBranchInstruction(String branchType, String register1, String register2, String label) {
        return String.format(BRANCH_AND_ARITHMETIC_INSTRUCTION_TEMPLATE, branchType, register1, register2, label);
    }

    private String generateArithmeticInstruction(String operation, String saveRegister, String register1, String register2) {
        return String.format(BRANCH_AND_ARITHMETIC_INSTRUCTION_TEMPLATE, operation, saveRegister, register1, register2);
    }

    private String generateStaticVariableInstruction(String variableName, int size) {
        return String.format(STATIC_VARIABLE_TEMPLATE, variableName, size);
    }

    private String generateBranchTrue(String labelName) {
        return String.format(BRANCH_TRUE_FLOAT, labelName);
    }

    private String generateBranchFalse(String labelName) {
        return String.format(BRANCH_FALSE_FLOAT, labelName);
    }

    private String generateJumpInstruction(String labelName) {
        return String.format(GOTO_TEMPLATE, labelName);
    }

    private String generateJalInstruction(String functionName) {
        return String.format(JAL_TEMPLATE, functionName);
    }

    private String generateJrInstruction() {
        return "\tjr $ra";
    }

    private String generateIncreaseStackPointer(int increaseBy) {
        return String.format(INCREASE_STACK_POINTER_TEMPLATE, increaseBy);
    }

    private String generateDecreaseStackPointer(int decreaseBy) {
        return String.format(DECREASE_STACK_POINTER_TEMPLATE, decreaseBy);
    }

    public static boolean isIntegerConstant(String variable) {
        if (variable == null) {
            return false;
        }
        try {
            Integer.parseInt(variable);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    public static boolean isFloatConstant(String variable) {
        if (variable == null) {
            return false;
        }
        if (isIntegerConstant(variable)) return false;
        try {
            Double.parseDouble(variable);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
