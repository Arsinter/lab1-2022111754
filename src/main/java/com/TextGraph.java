package com;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;

public class TextGraph {
    private DirectedGraph graph;
    private static final double DAMPING_FACTOR = 0.85;

    public TextGraph() {
        graph = new DirectedGraph();
    }

    // 从文件读取文本并构建图
    public void buildGraphFromFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            String previousWord = null;
            while ((line = reader.readLine()) != null) {
                // 将文本转换为小写并分割成单词
                String[] words = line.toLowerCase().split("[^a-zA-Z]+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        if (previousWord != null) {
                            graph.addEdge(previousWord, word);
                        }
                        previousWord = word;
                    }
                }
            }
        }
    }

    // 生成DOT文件
    public void generateDotFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("digraph G {");
            writer.println("    rankdir=LR;");
            writer.println("    node [shape=circle, style=filled, fillcolor=lightblue];");
            
            // 添加节点
            for (String node : graph.getNodes()) {
                writer.println("    \"" + node + "\";");
            }
            
            // 添加边
            for (String from : graph.getNodes()) {
                Map<String, Integer> edges = graph.getOutgoingEdges(from);
                for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                    writer.println("    \"" + from + "\" -> \"" + edge.getKey() + 
                                 "\" [label=\"" + edge.getValue() + "\"];");
                }
            }
            
            writer.println("}");
        }
    }

    // 展示有向图
    public void showDirectedGraph() {
        // 创建并显示图形界面
        SwingUtilities.invokeLater(() -> {
            GraphVisualizer visualizer = new GraphVisualizer(graph);
            visualizer.setVisible(true);
        });
        
        // 同时显示文本形式
        System.out.println("\n文本形式的有向图：");
        for (String node : graph.getNodes()) {
            System.out.print(node + " -> ");
            Map<String, Integer> edges = graph.getOutgoingEdges(node);
            List<String> edgeList = new ArrayList<>();
            for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                edgeList.add(edge.getKey() + "(" + edge.getValue() + ")");
            }
            System.out.println(String.join(", ", edgeList));
        }
    }

    // 查询桥接词
    public String queryBridgeWords(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        if (!graph.containsNode(word1) || !graph.containsNode(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        List<String> bridgeWords = new ArrayList<>();
        Map<String, Integer> word1Edges = graph.getOutgoingEdges(word1);
        
        for (String potentialBridge : word1Edges.keySet()) {
            if (graph.getOutgoingEdges(potentialBridge).containsKey(word2)) {
                bridgeWords.add(potentialBridge);
            }
        }

        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        }

        if (bridgeWords.size() == 1) {
            return "The bridge word from " + word1 + " to " + word2 + " is: " + bridgeWords.get(0);
        } else {
            return "The bridge words from " + word1 + " to " + word2 + " are: " + 
                   String.join(", ", bridgeWords.subList(0, bridgeWords.size() - 1)) + 
                   " and " + bridgeWords.get(bridgeWords.size() - 1);
        }
    }

    // 生成新文本
    public String generateNewText(String inputText) {
        String[] words = inputText.toLowerCase().split("\\s+");
        List<String> result = new ArrayList<>();
        
        for (int i = 0; i < words.length; i++) {
            result.add(words[i]);
            if (i < words.length - 1) {
                String bridgeWords = queryBridgeWords(words[i], words[i + 1]);
                if (bridgeWords.startsWith("The bridge word")) {
                    String bridgeWord = bridgeWords.substring(bridgeWords.lastIndexOf(": ") + 2);
                    result.add(bridgeWord);
                } else if (bridgeWords.startsWith("The bridge words")) {
                    String[] bridges = bridgeWords.substring(bridgeWords.lastIndexOf(": ") + 2).split(", ");
                    String lastBridge = bridges[bridges.length - 1].replace(" and ", "");
                    result.add(lastBridge);
                }
            }
        }
        
        return String.join(" ", result);
    }

    // 计算最短路径
    // git test B2
    public String calcShortestPath(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        if (!graph.containsNode(word1) || !graph.containsNode(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(
            Comparator.comparingInt(distances::get)
        );

        for (String node : graph.getNodes()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(word1, 0);
        queue.add(word1);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(word2)) break;

            for (Map.Entry<String, Integer> edge : graph.getOutgoingEdges(current).entrySet()) {
                String neighbor = edge.getKey();
                int weight = edge.getValue();
                int newDist = distances.get(current) + weight;

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        if (distances.get(word2) == Integer.MAX_VALUE) {
            return "No path from " + word1 + " to " + word2 + "!";
        }

        List<String> path = new ArrayList<>();
        for (String node = word2; node != null; node = previous.get(node)) {
            path.add(0, node);
        }

        return "Shortest path: " + String.join(" -> ", path) + 
               " (length: " + distances.get(word2) + ")";
    }

    // 计算PageRank
    public double calPageRank(String word) {
        word = word.toLowerCase();
        if (!graph.containsNode(word)) {
            return 0.0;
        }
        graph.calculatePageRank(DAMPING_FACTOR);
        return graph.getPageRank(word);
    }

    // 随机游走
    public String randomWalk() {
        List<String> nodes = new ArrayList<>(graph.getNodes());
        if (nodes.isEmpty()) {
            return "Graph is empty!";
        }

        Random random = new Random();
        String current = nodes.get(random.nextInt(nodes.size()));
        List<String> path = new ArrayList<>();
        Set<String> visitedEdges = new HashSet<>();
        path.add(current);

        while (true) {
            Map<String, Integer> edges = graph.getOutgoingEdges(current);
            if (edges.isEmpty()) {
                break;
            }

            List<String> nextNodes = new ArrayList<>(edges.keySet());
            String next = nextNodes.get(random.nextInt(nextNodes.size()));
            String edge = current + "->" + next;

            if (visitedEdges.contains(edge)) {
                break;
            }

            visitedEdges.add(edge);
            path.add(next);
            current = next;
        }

        return String.join(" ", path);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide input file path");
            return;
        }

        TextGraph textGraph = new TextGraph();
        try {
            textGraph.buildGraphFromFile(args[0]);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n请选择功能：");
                System.out.println("1. 展示有向图");
                System.out.println("2. 查询桥接词");
                System.out.println("3. 生成新文本");
                System.out.println("4. 计算最短路径");
                System.out.println("5. 计算PageRank");
                System.out.println("6. 随机游走");
                System.out.println("0. 退出");

                int choice = scanner.nextInt();
                scanner.nextLine(); // 消耗换行符

                switch (choice) {
                    case 1:
                        textGraph.showDirectedGraph();
                        break;
                    case 2:
                        System.out.println("请输入两个单词：");
                        String word1 = scanner.nextLine();
                        String word2 = scanner.nextLine();
                        System.out.println(textGraph.queryBridgeWords(word1, word2));
                        break;
                    case 3:
                        System.out.println("请输入文本：");
                        String inputText = scanner.nextLine();
                        System.out.println(textGraph.generateNewText(inputText));
                        break;
                    case 4:
                        System.out.println("请输入两个单词：");
                        word1 = scanner.nextLine();
                        word2 = scanner.nextLine();
                        System.out.println(textGraph.calcShortestPath(word1, word2));
                        break;
                    case 5:
                        System.out.println("请输入单词：");
                        String word = scanner.nextLine();
                        System.out.println("PageRank: " + textGraph.calPageRank(word));
                        break;
                    case 6:
                        System.out.println(textGraph.randomWalk());
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("无效的选择！");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
} 