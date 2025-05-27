package com;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;


/**
 * 用户服务类，提供用户相关操作.
 */
public class Main {
  private static DirectedGraph graph;
  private static Scanner scanner;

  /**
   * 主程序入口.
   */
  public static void main(String[] args) {
    scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
    graph = new DirectedGraph();

    // 从资源文件读取文本并构建图
    try {
      String text = readResourceFile("Easy Test.txt");
      buildGraph(text);
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
      return;
    }

    boolean running = true;
    while (running) {
      System.out.println("\n请选择功能：");
      System.out.println("1. 展示有向图");
      System.out.println("2. 查询桥接词");
      System.out.println("3. 生成新文本");
      System.out.println("4. 计算最短路径");
      System.out.println("5. 计算PageRank");
      System.out.println("6. 随机游走");
      System.out.println("0. 退出");
      System.out.print("请输入选项 (0-6): ");

      String choice = scanner.nextLine().trim();

      try {
        switch (choice) {
          case "1":
            showDirectedGraph();
            break;
          case "2":
            queryBridgeWords();
            break;
          case "3":
            generateNewText();
            break;
          case "4":
            calculateShortestPath();
            break;
          case "5":
            calculatePageRank();
            break;
          case "6":
            randomWalk();
            break;
          case "0":
            running = false;
            System.out.println("感谢使用，再见！");
            break;
          default:
            System.out.println("无效的选择，请重试。");
        }
      } catch (Exception e) {
        System.out.println("操作出错：" + e.getMessage());
      }
    }
    scanner.close();
  }

  private static void buildGraph(String text) {
    String[] sentences = text.split("[.!?]");
    for (String sentence : sentences) {
      sentence = sentence.trim();
      if (sentence.isEmpty()) {
        continue;
      }

      String[] words = sentence.split("\\s+");
      for (int i = 0; i < words.length - 1; i++) {
        String from = words[i].toLowerCase();
        String to = words[i + 1].toLowerCase();
        graph.addEdge(from, to);
      }
    }
  }

  private static void showDirectedGraph() {
    System.out.println("\n正在生成有向图可视化...");
    GraphVisualizer visualizer = new GraphVisualizer(graph);
    visualizer.setVisible(true);
  }

  private static void queryBridgeWords() {
    System.out.print("请输入第一个词: ");
    String word1 = scanner.nextLine().trim().toLowerCase();
    System.out.print("请输入第二个词: ");
    String word2 = scanner.nextLine().trim().toLowerCase();

    if (!graph.containsNode(word1) || !graph.containsNode(word2)) {
      System.out.println("输入的词不在图中！");
      return;
    }

    List<String> bridgeWords = findBridgeWords(word1, word2);
    if (bridgeWords.isEmpty()) {
      System.out.println("没有找到桥接词！");
    } else {
      System.out.println("桥接词: " + String.join(", ", bridgeWords));
    }
  }

  private static List<String> findBridgeWords(String word1, String word2) {
    List<String> bridgeWords = new ArrayList<>();
    Map<String, Integer> word1Edges = graph.getOutgoingEdges(word1);
    Map<String, Integer> word2Incoming = graph.getIncomingEdges(word2);

    for (String bridge : word1Edges.keySet()) {
      if (word2Incoming.containsKey(bridge)) {
        bridgeWords.add(bridge);
      }
    }
    return bridgeWords;
  }

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private static void generateNewText() {
    System.out.print("请输入新文本: ");
    String input = scanner.nextLine().trim();
    String[] words = input.split("\\s+");
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < words.length - 1; i++) {
      result.append(words[i]).append(" ");
      List<String> bridgeWords = findBridgeWords(words[i].toLowerCase(),
              words[i + 1].toLowerCase());
      if (!bridgeWords.isEmpty()) {
        String bridge = bridgeWords.get(SECURE_RANDOM.nextInt(bridgeWords.size()));
        result.append(bridge).append(" ");
      }
    }
    result.append(words[words.length - 1]);
    System.out.println("生成的新文本: " + result);
  }

  private static void calculateShortestPath() {
    System.out.print("请输入起始词: ");
    String start = scanner.nextLine().trim().toLowerCase();
    System.out.print("请输入目标词: ");
    String end = scanner.nextLine().trim().toLowerCase();

    if (!graph.containsNode(start) || !graph.containsNode(end)) {
      System.out.println("输入的词不在图中！");
      return;
    }

    Map<String, Integer> distances = new HashMap<>();
    Map<String, String> previous = new HashMap<>();
    PriorityQueue<String> queue = new PriorityQueue<>(
            Comparator.comparingInt(distances::get)
    );

    for (String node : graph.getNodes()) {
      distances.put(node, Integer.MAX_VALUE);
    }
    distances.put(start, 0);
    queue.offer(start);

    while (!queue.isEmpty()) {
      String current = queue.poll();
      if (current.equals(end)) {
        break;
      }

      for (Map.Entry<String, Integer> edge : graph.getOutgoingEdges(current).entrySet()) {
        String neighbor = edge.getKey();
        int newDist = distances.get(current) + edge.getValue();
        if (newDist < distances.get(neighbor)) {
          distances.put(neighbor, newDist);
          previous.put(neighbor, current);
          queue.offer(neighbor);
        }
      }
    }

    if (distances.get(end) == Integer.MAX_VALUE) {
      System.out.println("没有找到路径！");
      return;
    }

    List<String> path = new ArrayList<>();
    String current = end;
    while (current != null) {
      path.add(0, current);
      current = previous.get(current);
    }
    System.out.println("最短路径: " + String.join(" -> ", path));
    System.out.println("路径长度: " + distances.get(end));
  }

  private static void calculatePageRank() {
    System.out.println("\n正在计算PageRank...");
    graph.calculatePageRank(0.85);

    List<Map.Entry<String, Double>> rankings = new ArrayList<>();
    for (String node : graph.getNodes()) {
      rankings.add(new AbstractMap.SimpleEntry<>(node, graph.getPageRank(node)));
    }

    rankings.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

    System.out.println("\nPageRank排名（所有单词）：");
    System.out.println("排名\t单词\t\tPageRank值");
    System.out.println("----------------------------------------");
    for (int i = 0; i < rankings.size(); i++) {
      Map.Entry<String, Double> entry = rankings.get(i);
      System.out.printf("%d\t%-15s\t%.6f%n",
              i + 1,
              entry.getKey(),
              entry.getValue());
    }
  }

  private static void randomWalk() {
    System.out.println("\n开始随机游走...");
    List<String> path = new ArrayList<>();
    Set<String> visitedEdges = new HashSet<>();
    String current = graph.getNodes().iterator().next();
    path.add(current);

    while (true) {
      Map<String, Integer> edges = graph.getOutgoingEdges(current);
      if (edges.isEmpty()) {
        break;
      }

      // For non-security purposes, predictable randomness is acceptable here
      List<String> neighbors = new ArrayList<>(edges.keySet());
      String next = neighbors.get(SECURE_RANDOM.nextInt(neighbors.size()));
      String edge = current + "->" + next;

      if (visitedEdges.contains(edge)) {
        System.out.println("检测到重复边，停止游走");
        break;
      }

      visitedEdges.add(edge);
      path.add(next);
      current = next;
    }

    System.out.println("随机游走路径: " + String.join(" -> ", path));
  }

  private static String readResourceFile(String fileName) throws Exception {
    StringBuilder content = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(Main.class.getClassLoader().getResourceAsStream(fileName), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line).append("\n");
      }
    }
    return content.toString();
  }
} 