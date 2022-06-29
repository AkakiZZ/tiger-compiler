package main;

import codegen.CodeGenerator;
import ir.FunctionData;
import ir.ProgramData;
import org.apache.commons.cli.*;
import parser.IrProgramParser;
import regalloc.allocator.Allocator;
import regalloc.factory.GlobalAllocatorFactory;
import regalloc.factory.IntraBlockAllocatorFactory;
import regalloc.factory.NaiveAllocatorFactory;
import regalloc.model.MemoryTable;
import util.FileGenerator;
import util.FunctionControlFlow;
import util.Liveness;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.lang.System.exit;

public class Main {

    public static void main(String[] args) throws IOException {
        Options options = new Options();
        options.addOption("i", "i", true, "tiger-file");
        options.addOption("r", "r", true, "ir-file");
        options.addOption("n", false, "naive-allocator");
        options.addOption("b", false, "intra-block-allocation");
        options.addOption("g", false, "global-allocation");
        options.addOption("l", "liveness", false, "liveness");
        options.addOption("c", "cfg", false, "cfg");
        options.addOption("m", "mips", false, "mips-files");

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(Arrays.toString(args));
            exit(1);
        }

        String tigerFilePath = cmd.getOptionValue("i");

        String extension = tigerFilePath.substring(tigerFilePath.lastIndexOf('.'));

        String irFilePath = tigerFilePath.replace(extension, ".ir").replace("tiger", "ir");

        String mipsFile = tigerFilePath.replace(extension, ".s");

        String cfgFile = tigerFilePath.replace(extension, ".cfg.gv");

        String livenessFile = tigerFilePath.replace(extension, ".liveness");

        String naiveFile = tigerFilePath.replace(extension, ".naive.s");

        String intraBlockFile = tigerFilePath.replace(extension, ".ib.s");

        String briggsFile = tigerFilePath.replace(extension, ".briggs.s");

        IrProgramParser parser = new IrProgramParser(irFilePath);
        ProgramData programData = parser.getProgramData();

        List<FunctionControlFlow> functionControlFlowList = new ArrayList<>();
        List<Liveness> livenessObjects = new ArrayList<>();

        for (FunctionData data: programData.getFunctions()) {
            FunctionControlFlow functionControlFlow = FunctionControlFlow.generateFunctionControlFlow(data);
            functionControlFlowList.add(functionControlFlow);
        }

        functionControlFlowList = new ArrayList<>();
        for (FunctionData data: programData.getFunctions()) {
            FunctionControlFlow functionControlFlow = FunctionControlFlow.generateFunctionControlFlow(data);
            functionControlFlowList.add(functionControlFlow);
            HashSet<String> arrs = new HashSet<>(programData.getStaticArrays().keySet());
            arrs.addAll(data.getArrays().keySet());
            Liveness liveness = new Liveness(data.getName(),
                    functionControlFlow.getInstructionFlowGraph(),
                    data.getInstructions(),
                    arrs);
            livenessObjects.add(liveness);
        }

        // default
        CodeGenerator codeGenerator = new CodeGenerator(programData, functionControlFlowList, livenessObjects, new NaiveAllocatorFactory());

        // naive allocation
        if (cmd.hasOption("n")) {
            codeGenerator = new CodeGenerator(programData, functionControlFlowList, livenessObjects, new NaiveAllocatorFactory());
        }

        // intra block allocation
        if (cmd.hasOption("b")) {
            codeGenerator = new CodeGenerator(programData, functionControlFlowList, livenessObjects, new IntraBlockAllocatorFactory());
        }

        // global allocation
        if (cmd.hasOption("g")) {
            codeGenerator = new CodeGenerator(programData, functionControlFlowList, livenessObjects, new GlobalAllocatorFactory());
        }

        FileGenerator.generateMipsFile(mipsFile, codeGenerator.generateMips());

        if (cmd.hasOption("mips")) {
            CodeGenerator naiveAllocationGenerator = new CodeGenerator(programData, functionControlFlowList, livenessObjects, new NaiveAllocatorFactory());
            CodeGenerator intraBlockAllocationGenerator = new CodeGenerator(programData, functionControlFlowList, livenessObjects, new IntraBlockAllocatorFactory());
            CodeGenerator globalAllocationGenerator = new CodeGenerator(programData, functionControlFlowList, livenessObjects, new GlobalAllocatorFactory());
            FileGenerator.generateMipsFile(naiveFile, naiveAllocationGenerator.generateMips());
            FileGenerator.generateMipsFile(intraBlockFile, intraBlockAllocationGenerator.generateMips());
            FileGenerator.generateMipsFile(briggsFile, globalAllocationGenerator.generateMips());
        }

        if (cmd.hasOption("cfg")) {
            FileGenerator.generateCfgFile(cfgFile, functionControlFlowList);
        }


        if (cmd.hasOption("liveness")) {
            FileGenerator.generateLivenessFile(livenessFile, livenessObjects);
        }

    }
}
