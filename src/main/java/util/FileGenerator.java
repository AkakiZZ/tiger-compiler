package util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FileGenerator {
    public static void generateMipsFile(String path, List<String> instructions) throws IOException {
        FileWriter myWriter = new FileWriter(path);
        for (String line : instructions) {
            myWriter.write(line);
            myWriter.write('\n');
        }
        myWriter.close();
    }

    public static void generateCfgFile(String path, List<FunctionControlFlow> cfgs) throws IOException {
        FileWriter myWriter = new FileWriter(path);
        myWriter.write("digraph d {\n");
        int counter = 0;
        for (FunctionControlFlow cfg : cfgs) {
            HashSet<BasicBlock> visited = new HashSet<>();
            Map<BasicBlock, Set<BasicBlock>> graph = cfg.getBasicBlockGraph();
            HashSet<String> edges = new HashSet<>();
            Queue<BasicBlock> queue = new LinkedList<>();
            myWriter.write(String.format("subgraph cluster_%s {\n", counter));
            myWriter.write(String.format("label = \"%s\";", cfg.getName()));
            queue.add(cfg.getStartingBlock());
            while (!queue.isEmpty()) {
                BasicBlock currBlock = queue.poll();
                visited.add(currBlock);
                String node = String.format("%s [label=\"%s\" shape=box]\n", IdGenerator.idFor(currBlock), currBlock);
                myWriter.write(node);
                for (BasicBlock neighbour : graph.get(currBlock)) {
                    String edge = String.format("%d -> %d\n", IdGenerator.idFor(currBlock), IdGenerator.idFor(neighbour));
                    if (!edges.contains(edge)) {
                        myWriter.write(edge);
                        edges.add(edge);
                    }
                    if (!visited.contains(neighbour)) {
                        queue.add(neighbour);
                    }
                }
            }
            myWriter.write("};\n");

            counter++;
        }
        myWriter.write("}");
        myWriter.close();
    }

    public static void generateLivenessFile(String path, List<Liveness> livenessObjects) throws IOException {
        FileWriter myWriter = new FileWriter(path);
        for (Liveness liveness : livenessObjects) {
            myWriter.write(String.format("Liveness Analysis for function < %s >:\n{\n", liveness.getFunctionName()));
            for (int i : liveness.getOuts().keySet()) {
                myWriter.write(String.format("\tins  of %d: %s\n", i + 1, liveness.getIns().get(i).toString()));
                myWriter.write(String.format("\touts of %d: %s\n", i + 1, liveness.getOuts().get(i).toString()));
            }
            myWriter.write("}\n\n");
        }
        myWriter.close();
    }
}
