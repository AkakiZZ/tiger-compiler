package util;

import ir.IRInstruction;
import ir.InstructionType;
import ir.FunctionData;

import java.util.*;

public class FunctionControlFlow {
    private final String name;
    private final BasicBlock startingBlock;
    private final Map<BasicBlock, Set<BasicBlock>> basicBlockGraph;
    private final List<BasicBlock> basicBlocks;
    private final Map<Integer, List<Integer>> instructionFlowGraph;

    public FunctionControlFlow(String functionName,
                               BasicBlock startingBlock,
                               Map<BasicBlock, Set<BasicBlock>> graph,
                               List<BasicBlock> basicBlocks,
                               Map<Integer, List<Integer>> cfg) {
        this.name = functionName;
        this.startingBlock = startingBlock;
        this.basicBlockGraph = graph;
        this.basicBlocks = basicBlocks;
        this.instructionFlowGraph = cfg;
    }
    public String getName() {
        return name;
    }

    public BasicBlock getStartingBlock() {
        return startingBlock;
    }

    public Map<BasicBlock, Set<BasicBlock>> getBasicBlockGraph() {
        return basicBlockGraph;
    }

    public List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public Map<Integer, List<Integer>> getInstructionFlowGraph() {
        return instructionFlowGraph;
    }

    public static FunctionControlFlow generateFunctionControlFlow(FunctionData functionData) {
        List<IRInstruction> instructions = functionData.getInstructions();
        SortedSet<Integer> leaderIndexes = new TreeSet<>();
        Map<String, Integer> labelIndexes = new HashMap<>();
        Map<Integer, List<Integer>> graph = new HashMap<>();
        Map<BasicBlock, Set<BasicBlock>> blockGraph = new HashMap<>();

        for (int i = 0; i < instructions.size(); i++) {
            IRInstruction instruction = instructions.get(i);
            if (instruction.getInstructionType() == InstructionType.LABEL) {
                labelIndexes.put(instruction.getOperation().replace(":", ""), i);
            }
        }

        leaderIndexes.add(0);
        for (int i = 1; i < instructions.size(); i++) {
            IRInstruction instruction = instructions.get(i);
            IRInstruction prevInstruction = instructions.get(i - 1);
            if (instruction.getInstructionType() == InstructionType.BRANCH) {
                String label = instruction.getArguments().get(2);
                leaderIndexes.add(labelIndexes.get(label));
                if (!graph.containsKey(i)) {
                    graph.put(i, new ArrayList<>());
                }
                graph.get(i).add(labelIndexes.get(label));
                graph.get(i).add(i + 1);
            }
            if (instruction.getInstructionType() == InstructionType.GOTO) {
                String label = instruction.getArguments().get(0);
                leaderIndexes.add(labelIndexes.get(label));
                if (!graph.containsKey(i)) {
                    graph.put(i, new ArrayList<>());
                }
                graph.get(i).add(labelIndexes.get(label));
            }
            if (prevInstruction.getInstructionType() == InstructionType.BRANCH ||
                    prevInstruction.getInstructionType() == InstructionType.GOTO) {
                leaderIndexes.add(i);
            }
        }

        for (int i = 1; i < instructions.size(); i++) {
            if (leaderIndexes.contains(i) &&
                    instructions.get(i - 1).getInstructionType() == InstructionType.LABEL) {
                graph.put(i - 1, new ArrayList<>(List.of(i)));
            }
        }

        for (int i = 0; i < instructions.size(); i++) {
            if (!graph.containsKey(i)) {
                graph.put(i, List.of(i + 1));
            }
        }
        graph.put(instructions.size() - 1, new ArrayList<>());

        Map<Integer, BasicBlock> basicBlocks = new TreeMap<>();
        List<BasicBlock> blocks = new ArrayList<>();

        int prevId = 0;
        for (int instructionIndex : leaderIndexes) {
            List<IRInstruction> blockInstructions = new ArrayList<>();
            for (int startIndex = prevId; startIndex < instructionIndex; startIndex++) {
                blockInstructions.add(instructions.get(startIndex));
            }
            BasicBlock basicBlock = new BasicBlock(blockInstructions, prevId, instructionIndex - 1);
            if (!blockInstructions.isEmpty()) {
                blocks.add(basicBlock);
                basicBlocks.put(prevId, basicBlock);
            }
            blockGraph.put(basicBlock, new HashSet<>(Set.of()));
            prevId = instructionIndex;
        }
        List<IRInstruction> blockInstructions = new ArrayList<>();
        for (int startIndex = prevId; startIndex < instructions.size(); startIndex++) {
            blockInstructions.add(instructions.get(startIndex));
        }
        BasicBlock basicBlock = new BasicBlock(blockInstructions, prevId, instructions.size() - 1);
        basicBlocks.put(prevId, basicBlock);
        blocks.add(basicBlock);

        for (int instructionIndex : leaderIndexes) {
            BasicBlock basicBlock1 = basicBlocks.get(instructionIndex);
            List<Integer> neighbourIndexes = graph.get(basicBlock1.getEndIndex());
            blockGraph.put(basicBlock1, new HashSet<>());
            for (int neighbourIndex : neighbourIndexes) {
                BasicBlock neighbourBlock = basicBlocks.get(neighbourIndex);
                if (neighbourBlock == null) continue;
                blockGraph.get(basicBlock1).add(neighbourBlock);
            }
        }

        return new FunctionControlFlow(functionData.getName(), basicBlocks.get(0), blockGraph, blocks, graph);
    }
}
