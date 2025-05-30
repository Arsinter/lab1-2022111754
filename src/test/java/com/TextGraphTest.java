package com;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TextGraphTest {
    private TextGraph textGraph;

    @Before
    public void setUp() {
        textGraph = new TextGraph();
        // 构建一个简单的测试图
        try {
            // 创建一个临时文件用于测试
            java.io.File tempFile = java.io.File.createTempFile("test", ".txt");
            java.io.PrintWriter writer = new java.io.PrintWriter(tempFile);
            // 构建一个有向图：a -> b -> c -> d
            writer.println("a b");
            writer.println("b c");
            writer.println("c d");
            writer.close();
            
            textGraph.buildGraphFromFile(tempFile.getAbsolutePath());
            tempFile.delete();
        } catch (Exception e) {
            fail("测试设置失败: " + e.getMessage());
        }
    }

    @Test
    public void testCalcShortestPath_ValidPath() {
        String result = textGraph.calcShortestPath("a", "d");
        assertTrue("应该找到从a到d的路径", result.startsWith("Shortest path:"));
        assertTrue("路径应该包含a", result.contains("a"));
        assertTrue("路径应该包含d", result.contains("d"));
        assertTrue("路径长度应该为3", result.contains("(length: 3)"));
    }

    @Test
    public void testCalcShortestPath_SameWord() {
        String result = textGraph.calcShortestPath("a", "a");
        assertEquals("相同单词的路径长度应该为0", "Shortest path: a (length: 0)", result);
    }

    @Test
    public void testCalcShortestPath_NoPath() {
        // 测试反向路径（在有向图中应该不存在）
        String result = textGraph.calcShortestPath("d", "a");
        assertEquals("应该返回无路径信息", "No path from d to a!", result);
    }

    @Test
    public void testCalcShortestPath_NonExistentWord() {
        String result = textGraph.calcShortestPath("nonexistent", "a");
        assertEquals("应该返回单词不存在的信息", "No nonexistent or a in the graph!", result);
    }

    @Test
    public void testCalcShortestPath_CaseInsensitive() {
        String result = textGraph.calcShortestPath("A", "D");
        assertTrue("应该忽略大小写并找到路径", result.startsWith("Shortest path:"));
    }

    @Test
    public void testCalcShortestPath_IntermediateNode() {
        String result = textGraph.calcShortestPath("a", "c");
        assertTrue("应该找到从a到c的路径", result.startsWith("Shortest path:"));
        assertTrue("路径应该包含b", result.contains("b"));
        assertTrue("路径长度应该为2", result.contains("(length: 2)"));
    }

    @Test
    public void testCalcShortestPath_IsolatedNode() {
        // 创建一个新的TextGraph实例来测试孤立节点
        TextGraph isolatedGraph = new TextGraph();
        try {
            // 创建一个临时文件用于测试
            java.io.File tempFile = java.io.File.createTempFile("test", ".txt");
            java.io.PrintWriter writer = new java.io.PrintWriter(tempFile);
            // 添加一个孤立的节点，确保它与其他节点没有连接
            writer.println("isolated word");
            writer.println("other word");
            writer.close();
            
            isolatedGraph.buildGraphFromFile(tempFile.getAbsolutePath());
            tempFile.delete();
        } catch (Exception e) {
            fail("测试设置失败: " + e.getMessage());
        }

        String result = isolatedGraph.calcShortestPath("isolated", "other");
        assertEquals("应该返回无路径信息", "No path from isolated to other!", result);
    }
}