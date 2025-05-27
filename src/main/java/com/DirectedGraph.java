package com;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 有向图类.
 */
public class DirectedGraph {
  private Map<String, Map<String, Integer>> graph; // 存储图的邻接表
  private Map<String, Double> pageRank; // 存储PageRank值

  /**
   * 有向图类.
   */
  public DirectedGraph() {
    graph = new HashMap<>();
    pageRank = new HashMap<>();
  }

  /**
   * 复制构造函数（深拷贝）.
   * @param other 要复制的图对象
   */
  public DirectedGraph(DirectedGraph other) {
    this(); // 调用默认构造函数初始化

    // 深拷贝邻接表
    if (other.graph != null) {
      for (Map.Entry<String, Map<String, Integer>> entry : other.graph.entrySet()) {
        Map<String, Integer> edges = new HashMap<>(entry.getValue());
        this.graph.put(entry.getKey(), edges);
      }
    }

    // 深拷贝PageRank
    if (other.pageRank != null) {
      this.pageRank = new HashMap<>(other.pageRank);
    }
  }

  /**
   * 添加边.
   */
  public void addEdge(String from, String to) {
    graph.putIfAbsent(from, new HashMap<>());
    graph.putIfAbsent(to, new HashMap<>());

    Map<String, Integer> edges = graph.get(from);
    edges.put(to, edges.getOrDefault(to, 0) + 1);
  }

  /**
   * 获取边的权重.
   */
  public int getEdgeWeight(String from, String to) {
    return graph.getOrDefault(from, new HashMap<>()).getOrDefault(to, 0);
  }

  // 获取所有节点
  public Set<String> getNodes() {
    return graph.keySet();
  }

  /**
   * 获取节点的出边.
   */
  public Map<String, Integer> getOutgoingEdges(String node) {
    return graph.getOrDefault(node, new HashMap<>());
  }

  /**
   * 获取节点的入边.
   */
  public Map<String, Integer> getIncomingEdges(String node) {
    Map<String, Integer> incoming = new HashMap<>();
    for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
      if (entry.getValue().containsKey(node)) {
        incoming.put(entry.getKey(), entry.getValue().get(node));
      }
    }
    return incoming;
  }

  /**
   * 检查节点是否存在.
   */
  public boolean containsNode(String node) {
    return graph.containsKey(node);
  }

  /**
   * 计算PageRank.
   */
  public void calculatePageRank(double d) {
    int n = graph.size();
    // 初始化PageRank值
    for (String node : graph.keySet()) {
      pageRank.put(node, 1.0 / n);
    }

    // 迭代计算PageRank
    for (int i = 0; i < 100; i++) { // 迭代100次
      Map<String, Double> newPageRank = new HashMap<>();
      for (String node : graph.keySet()) {
        double sum = 0;
        for (Map.Entry<String, Integer> inEdge : getIncomingEdges(node).entrySet()) {
          String from = inEdge.getKey();
          int outDegree = 0;
          for (Integer integer : getOutgoingEdges(from).values()) {
            int intValue = integer;
            outDegree += intValue;
          }
          sum += pageRank.get(from) * inEdge.getValue() / outDegree;
        }
        newPageRank.put(node, (1 - d) / n + d * sum);
      }
      pageRank = newPageRank;
    }
  }

  /**
   * 获取节点的PageRank值.
   */
  public double getPageRank(String node) {
    return pageRank.getOrDefault(node, 0.0);
  }
} 