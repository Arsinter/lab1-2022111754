package com;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Map;
import java.util.HashMap;

public class TextGraphBridgeWordsTest {
    private TextGraph textGraph;
    private DirectedGraph graph;

    @Before
    public void setUp() {
        textGraph = new TextGraph();
        // 使用反射获取私有成员graph
        try {
            java.lang.reflect.Field field = TextGraph.class.getDeclaredField("graph");
            field.setAccessible(true);
            graph = (DirectedGraph) field.get(textGraph);
        } catch (Exception e) {
            fail("无法访问graph字段: " + e.getMessage());
        }
    }

    @Test
    public void testCaseInsensitive() {
        // 设置测试数据
        graph.addEdge("word1", "bridge");
        graph.addEdge("bridge", "word2");

        // 测试不同大小写组合
        String result1 = textGraph.queryBridgeWords("WORD1", "word2");
        String result2 = textGraph.queryBridgeWords("word1", "WORD2");
        String result3 = textGraph.queryBridgeWords("WORD1", "WORD2");

        assertTrue("应该找到桥接词", result1.contains("bridge"));
        assertTrue("应该找到桥接词", result2.contains("bridge"));
        assertTrue("应该找到桥接词", result3.contains("bridge"));
    }

    @Test
    public void testNodeExistence() {
        // 测试不存在的节点
        String result = textGraph.queryBridgeWords("nonexistent1", "nonexistent2");
        assertEquals("应该返回节点不存在的信息", 
            "No nonexistent1 or nonexistent2 in the graph!", result);

        // 测试一个存在一个不存在的情况
        graph.addEdge("existing", "word");
        result = textGraph.queryBridgeWords("existing", "nonexistent");
        assertEquals("应该返回节点不存在的信息", 
            "No existing or nonexistent in the graph!", result);
    }

    @Test
    public void testNoBridgeWords() {
        // 设置测试数据：word1 -> bridge1 -> other
        // word2 没有到 other 的路径
        graph.addEdge("word1", "bridge1");
        graph.addEdge("bridge1", "other");
        graph.addEdge("word2", "bridge2");

        String result = textGraph.queryBridgeWords("word2", "other");
        assertEquals("应该返回无桥接词信息", 
            "No bridge words from word2 to other!", result);
    }

    @Test
    public void testSingleBridgeWord() {
        // 设置测试数据：word1 -> bridge -> word2
        graph.addEdge("word1", "bridge");
        graph.addEdge("bridge", "word2");

        String result = textGraph.queryBridgeWords("word1", "word2");
        assertEquals("应该返回单个桥接词信息", 
            "The bridge word from word1 to word2 is: bridge", result);
    }

    @Test
    public void testMultipleBridgeWords() {
        // 设置测试数据：
        // word1 -> bridge1 -> word2
        // word1 -> bridge2 -> word2
        // word1 -> bridge3 -> word2
        graph.addEdge("word1", "bridge1");
        graph.addEdge("bridge1", "word2");
        graph.addEdge("word1", "bridge2");
        graph.addEdge("bridge2", "word2");
        graph.addEdge("word1", "bridge3");
        graph.addEdge("bridge3", "word2");

        String result = textGraph.queryBridgeWords("word1", "word2");
        assertTrue("应该包含多个桥接词", result.startsWith("The bridge words from word1 to word2 are:"));
        assertTrue("应该包含bridge1", result.contains("bridge1"));
        assertTrue("应该包含bridge2", result.contains("bridge2"));
        assertTrue("应该包含bridge3", result.contains("bridge3"));
    }

    @Test
    public void testInternalDataStructure() {
        // 测试内部数据结构
        graph.addEdge("word1", "bridge1");
        graph.addEdge("bridge1", "word2");
        graph.addEdge("word1", "bridge2");
        graph.addEdge("bridge2", "word2");

        // 验证word1的出边
        Map<String, Integer> word1Edges = graph.getOutgoingEdges("word1");
        assertEquals("word1应该有2个出边", 2, word1Edges.size());
        assertTrue("word1应该连接到bridge1", word1Edges.containsKey("bridge1"));
        assertTrue("word1应该连接到bridge2", word1Edges.containsKey("bridge2"));

        // 验证bridge1的出边
        Map<String, Integer> bridge1Edges = graph.getOutgoingEdges("bridge1");
        assertEquals("bridge1应该有1个出边", 1, bridge1Edges.size());
        assertTrue("bridge1应该连接到word2", bridge1Edges.containsKey("word2"));
    }

    @Test
    public void testEmptyGraph() {
        // 测试空图的情况
        String result = textGraph.queryBridgeWords("word1", "word2");
        assertEquals("空图应该返回节点不存在的信息", 
            "No word1 or word2 in the graph!", result);
    }

    @Test
    public void testSelfLoop() {
        // 测试自环的情况
        graph.addEdge("word1", "word1");
        String result = textGraph.queryBridgeWords("word1", "word1");
        assertEquals("自环应该返回无桥接词信息", 
            "No bridge words from word1 to word1!", result);
    }

    @Test
    public void testDirectConnection() {
        // 测试直接连接的情况（没有桥接词）
        graph.addEdge("word1", "word2");
        String result = textGraph.queryBridgeWords("word1", "word2");
        assertEquals("直接连接应该返回无桥接词信息", 
            "No bridge words from word1 to word2!", result);
    }

    @Test
    public void testMultiplePaths() {
        // 测试多条路径的情况
        // word1 -> bridge1 -> word2
        // word1 -> bridge2 -> word2
        // word1 -> bridge3 -> bridge4 -> word2
        graph.addEdge("word1", "bridge1");
        graph.addEdge("bridge1", "word2");
        graph.addEdge("word1", "bridge2");
        graph.addEdge("bridge2", "word2");
        graph.addEdge("word1", "bridge3");
        graph.addEdge("bridge3", "bridge4");
        graph.addEdge("bridge4", "word2");

        String result = textGraph.queryBridgeWords("word1", "word2");
        assertTrue("应该只包含直接桥接词", result.contains("bridge1"));
        assertTrue("应该只包含直接桥接词", result.contains("bridge2"));
        assertFalse("不应该包含间接桥接词", result.contains("bridge3"));
        assertFalse("不应该包含间接桥接词", result.contains("bridge4"));
    }

    @Test
    public void testEdgeWeights() {
        // 测试边的权重
        graph.addEdge("word1", "bridge1");
        graph.addEdge("bridge1", "word2");
        graph.addEdge("word1", "bridge1"); // 增加权重
        graph.addEdge("bridge1", "word2"); // 增加权重

        String result = textGraph.queryBridgeWords("word1", "word2");
        assertTrue("应该找到桥接词", result.contains("bridge1"));
        
        // 验证边的权重
        Map<String, Integer> word1Edges = graph.getOutgoingEdges("word1");
        assertEquals("word1到bridge1的权重应该为2", 2, word1Edges.get("bridge1").intValue());
        
        Map<String, Integer> bridge1Edges = graph.getOutgoingEdges("bridge1");
        assertEquals("bridge1到word2的权重应该为2", 2, bridge1Edges.get("word2").intValue());
    }

    @Test
    public void testSpecialCharacters() {
        // 测试特殊字符的处理
        graph.addEdge("word-1", "bridge_1");
        graph.addEdge("bridge_1", "word.2");

        String result = textGraph.queryBridgeWords("word-1", "word.2");
        assertTrue("应该找到桥接词", result.contains("bridge_1"));
    }

    @Test
    public void testLongPath() {
        // 测试长路径的情况
        graph.addEdge("word1", "bridge1");
        graph.addEdge("bridge1", "bridge2");
        graph.addEdge("bridge2", "bridge3");
        graph.addEdge("bridge3", "word2");

        String result = textGraph.queryBridgeWords("word1", "word2");
        assertEquals("应该返回无桥接词信息", 
            "No bridge words from word1 to word2!", result);
    }
} 