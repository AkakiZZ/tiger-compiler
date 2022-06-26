package main;

import codegen.CodeGenerator;
import org.apache.commons.cli.*;
import parser.Parser;
import regalloc.factory.GlobalAllocatorFactory;
import regalloc.factory.IntraBlockAllocatorFactory;
import regalloc.factory.NaiveAllocatorFactory;
import util.FileGenerator;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.exit;

public class Main {
    static List<String> floatTestFiles = List.of("source/3/tiger/demo_jacobi.tiger",
            "source/3/tiger/demo_matrix.tiger",
            "source/3/tiger/demo_motor.tiger",
            "source/3/tiger/demo_square_root.tiger",
            "source/3/tiger/demo_slope.tiger",
            "source/3/tiger/float_assign.tiger",
            "source/3/tiger/float_math.tiger",
            "source/3/tiger/float_math_mixed.tiger",
            "source/3/tiger/float_math_combo.tiger",
            "source/3/tiger/float_math_power_op.tiger",
            "source/3/tiger/lib_call_printf.tiger");

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

        if (floatTestFiles.contains(tigerFilePath)) exit(-1);

        String extension = tigerFilePath.substring(tigerFilePath.lastIndexOf('.'));

        String irFilePath = tigerFilePath.replace(extension, ".ir").replace("tiger", "ir");

        String mipsFile = tigerFilePath.replace(extension, ".s");

        String cfgFile = tigerFilePath.replace(extension, ".cfg.gv");

        String livenessFile = tigerFilePath.replace(extension, ".liveness");

        String naiveFile = tigerFilePath.replace(extension, ".naive.s");

        String intraBlockFile = tigerFilePath.replace(extension, ".ib.s");

        String briggsFile = tigerFilePath.replace(extension, ".briggs.s");

        Parser parser = new Parser(irFilePath);


        // default
        CodeGenerator codeGenerator = new CodeGenerator(parser, new NaiveAllocatorFactory());

        // naive allocation
        if (cmd.hasOption("n")) {
            codeGenerator = new CodeGenerator(parser, new NaiveAllocatorFactory());
        }

        // intra block allocation
        if (cmd.hasOption("b")) {
            codeGenerator = new CodeGenerator(parser, new IntraBlockAllocatorFactory());
        }

        // global allocation
        if (cmd.hasOption("g")) {
            codeGenerator = new CodeGenerator(parser, new GlobalAllocatorFactory());
        }

        if (cmd.hasOption("mips")) {
            /*
            CodeGenerator naiveAllocationGenerator = new CodeGenerator(parser, new NaiveAllocatorFactory());
            CodeGenerator intraBlockAllocationGenerator = new CodeGenerator(parser, new IntraBlockAllocatorFactory());
            CodeGenerator globalAllocationGenerator = new CodeGenerator(parser, new GlobalAllocatorFactory());
            FileGenerator.generateMipsFile(naiveFile, naiveAllocationGenerator.generateMips());
            FileGenerator.generateMipsFile(intraBlockFile, intraBlockAllocationGenerator.generateMips());
            FileGenerator.generateMipsFile(briggsFile, globalAllocationGenerator.generateMips());
            */
        }

        if (cmd.hasOption("cfg")) {

        }

        if (cmd.hasOption("liveness")) {

        }

        List<String> instructions = codeGenerator.generateMips();
        FileGenerator.generateMipsFile(mipsFile, instructions);
    }
}
