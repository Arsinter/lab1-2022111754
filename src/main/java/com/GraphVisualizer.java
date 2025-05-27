package com;


import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.swing.JFrame;

/**
 * 图可视化类.
 */
public class GraphVisualizer extends JFrame {
  private DirectedGraph graph;
  private Map<String, Point2D.Double> nodePositions;
  private static final int NODE_RADIUS = 20;
  private static final int PADDING = 50;
  private static final int WIDTH = 1200;
  private static final int HEIGHT = 800;
  private static final int LEVEL_HEIGHT = 100;
  private static final int ARROW_SIZE = 10;

  /**
   * 图可视化类.
   */
  @SuppressWarnings("EI_EXPOSE_REP2")
  public GraphVisualizer(DirectedGraph graph) {
    this.graph = new DirectedGraph(graph);
    this.nodePositions = new HashMap<>();

    setTitle("有向图可视化");
    setSize(WIDTH, HEIGHT);

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    // 计算节点位置
    calculateNodePositions();
  }

  private void calculateNodePositions() {
    // 使用BFS计算每个节点的层级
    Map<String, Integer> levels = new HashMap<>();
    Queue<String> queue = new LinkedList<>();
    Set<String> visited = new HashSet<>();

    // 找到入度为0的节点作为根节点
    String root = null;
    for (String node : graph.getNodes()) {
      if (graph.getIncomingEdges(node).isEmpty()) {
        root = node;
        break;
      }
    }

    if (root == null) {
      // 如果没有入度为0的节点，选择第一个节点作为根
      root = graph.getNodes().iterator().next();
    }

    queue.offer(root);
    visited.add(root);
    levels.put(root, 0);

    while (!queue.isEmpty()) {
      String current = queue.poll();
      int currentLevel = levels.get(current);

      for (String neighbor : graph.getOutgoingEdges(current).keySet()) {
        if (!visited.contains(neighbor)) {
          visited.add(neighbor);
          levels.put(neighbor, currentLevel + 1);
          queue.offer(neighbor);
        }
      }
    }

    // 计算每个层级的节点数量
    Map<Integer, List<String>> levelNodes = new HashMap<>();
    for (Map.Entry<String, Integer> entry : levels.entrySet()) {
      levelNodes.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
    }

    // 计算节点位置
    for (Map.Entry<Integer, List<String>> entry : levelNodes.entrySet()) {
      int level = entry.getKey();
      List<String> nodes = entry.getValue();
      int nodeCount = nodes.size();

      for (int i = 0; i < nodeCount; i++) {
        String node = nodes.get(i);
        double x = PADDING + (double) ((WIDTH - 2 * PADDING) * (i + 1)) / (nodeCount + 1);
        double y = PADDING + level * LEVEL_HEIGHT;
        nodePositions.put(node, new Point2D.Double(x, y));
      }
    }
  }

  private void drawArrow(Graphics2D g2d, double x1, double y1, double x2, double y2) {
    double dx = x2 - x1;
    double dy = y2 - y1;
    double angle = Math.atan2(dy, dx);
    double length = Math.sqrt(dx * dx + dy * dy);

    // 计算箭头位置
    double arrowX = x1 + (dx * (length - NODE_RADIUS)) / length;
    double arrowY = y1 + (dy * (length - NODE_RADIUS)) / length;

    // 绘制边
    g2d.drawLine((int) x1, (int) y1, (int) arrowX, (int) arrowY);

    // 绘制箭头
    double arrowAngle1 = angle - Math.PI / 6;
    double arrowAngle2 = angle + Math.PI / 6;

    int[] xPoints = new int[3];
    int[] yPoints = new int[3];

    xPoints[0] = (int) arrowX;
    yPoints[0] = (int) arrowY;
    xPoints[1] = (int) (arrowX - ARROW_SIZE * Math.cos(arrowAngle1));
    yPoints[1] = (int) (arrowY - ARROW_SIZE * Math.sin(arrowAngle1));
    xPoints[2] = (int) (arrowX - ARROW_SIZE * Math.cos(arrowAngle2));
    yPoints[2] = (int) (arrowY - ARROW_SIZE * Math.sin(arrowAngle2));

    g2d.fillPolygon(xPoints, yPoints, 3);
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // 绘制边
    g2d.setColor(Color.BLACK);
    for (String from : graph.getNodes()) {
      Point2D.Double fromPos = nodePositions.get(from);
      Map<String, Integer> edges = graph.getOutgoingEdges(from);

      for (Map.Entry<String, Integer> edge : edges.entrySet()) {
        String to = edge.getKey();
        Point2D.Double toPos = nodePositions.get(to);

        // 绘制带箭头的边
        drawArrow(g2d, fromPos.x, fromPos.y, toPos.x, toPos.y);

        // 绘制权重
        String weight = String.valueOf(edge.getValue());
        g2d.drawString(weight,
                (int) ((fromPos.x + toPos.x) / 2),
                (int) ((fromPos.y + toPos.y) / 2));
      }
    }

    // 绘制节点
    // git test B2
    for (String node : graph.getNodes()) {
      Point2D.Double pos = nodePositions.get(node);

      // 绘制节点背景
      g2d.setColor(new Color(173, 216, 230)); // 浅蓝色
      g2d.fillOval((int) (pos.x - NODE_RADIUS),
              (int) (pos.y - NODE_RADIUS),
              NODE_RADIUS * 2,
              NODE_RADIUS * 2);

      // 绘制节点边框
      g2d.setColor(Color.BLACK);
      g2d.drawOval((int) (pos.x - NODE_RADIUS),
              (int) (pos.y - NODE_RADIUS),
              NODE_RADIUS * 2,
              NODE_RADIUS * 2);

      // 绘制节点标签
      g2d.setColor(Color.BLACK);
      FontMetrics fm = g2d.getFontMetrics();
      int textWidth = fm.stringWidth(node);
      g2d.drawString(node,
              (int) (pos.x - (double) textWidth / 2),
              (int) (pos.y + (double) fm.getAscent() / 2));
    }
  }
} 