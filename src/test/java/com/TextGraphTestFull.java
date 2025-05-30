package com;

import org.junit.*;
import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

public class TextGraphTestFull {
    private TextGraph textGraph;
    private DirectedGraph graph;

    @Before
    public void setUp() throws Exception {
        textGraph = new TextGraph();
        // 反射获取私有graph
        java.lang.reflect.Field field = TextGraph.class.getDeclaredField("graph");
        field.setAccessible(true);
        graph = (DirectedGraph) field.get(textGraph);
    }

    @Test
    public void testBuildGraphFromFile() throws Exception {
        File temp = File.createTempFile("test", ".txt");
        try (PrintWriter pw = new PrintWriter(temp)) {
            pw.println("Hello world hello");
        }
        textGraph.buildGraphFromFile(temp.getAbsolutePath());
        assertTrue(graph.containsNode("hello"));
        assertTrue(graph.containsNode("world"));
        
        // 验证边的连接
        Map<String, Integer> helloEdges = graph.getOutgoingEdges("hello");
        assertTrue("hello应该连接到world", helloEdges.containsKey("world"));
        assertEquals("hello到world的权重应该是1", 1, helloEdges.get("world").intValue());
        
        Map<String, Integer> worldEdges = graph.getOutgoingEdges("world");
        assertTrue("world应该连接到hello", worldEdges.containsKey("hello"));
        assertEquals("world到hello的权重应该是1", 1, worldEdges.get("hello").intValue());
        
        temp.delete();
    }

    @Test
    public void testGenerateDotFile() throws Exception {
        graph.addEdge("a", "b");
        File temp = File.createTempFile("test", ".dot");
        textGraph.generateDotFile(temp.getAbsolutePath());
        String content = new String(java.nio.file.Files.readAllBytes(temp.toPath()));
        assertTrue(content.contains("\"a\" -> \"b\""));
        temp.delete();
    }

    @Test
    public void testShowDirectedGraphText() {
        graph.addEdge("a", "b");
        graph.addEdge("b", "c");
        // 捕获System.out
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(out));
        textGraph.showDirectedGraph();
        System.setOut(old);
        String output = out.toString();
        assertTrue(output.contains("a -> b(1)"));
        assertTrue(output.contains("b -> c(1)"));
    }

    @Test
    public void testQueryBridgeWords() {
        graph.addEdge("a", "b");
        graph.addEdge("b", "c");
        assertTrue(textGraph.queryBridgeWords("a", "c").contains("b"));
        assertTrue(textGraph.queryBridgeWords("a", "a").contains("No bridge words"));
        assertTrue(textGraph.queryBridgeWords("x", "c").contains("No x or c"));
    }

    @Test
    public void testGenerateNewText() {
        graph.addEdge("a", "b");
        graph.addEdge("b", "c");
        String result = textGraph.generateNewText("a c");
        // a和c之间有桥接词b
        assertTrue(result.contains("a b c"));
    }

    @Test
    public void testCalcShortestPath() {
        graph.addEdge("a", "b");
        graph.addEdge("b", "c");
        String path = textGraph.calcShortestPath("a", "c");
        assertTrue(path.contains("a -> b -> c"));
        // 不存在路径
        assertTrue(textGraph.calcShortestPath("a", "x").contains("No a or x"));
        assertTrue(textGraph.calcShortestPath("c", "a").contains("No path"));
    }

    @Test
    public void testCalPageRank() {
        graph.addEdge("a", "b");
        graph.addEdge("b", "a");
        double rankA = textGraph.calPageRank("a");
        double rankB = textGraph.calPageRank("b");
        assertTrue(rankA > 0 && rankB > 0);
        assertEquals(0.0, textGraph.calPageRank("notfound"), 1e-6);
    }

    @Test
    public void testRandomWalk() {
        graph.addEdge("a", "b");
        graph.addEdge("b", "c");
        String walk = textGraph.randomWalk();
        assertTrue(walk.startsWith("a") || walk.startsWith("b") || walk.startsWith("c"));
        // 空图
        TextGraph empty = new TextGraph();
        assertEquals("Graph is empty!", empty.randomWalk());
    }

    @Test
    public void testMainNoArgs() {
        // 捕获System.out
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(out));
        TextGraph.main(new String[0]);
        System.setOut(old);
        assertTrue(out.toString().contains("Please provide input file path"));
    }

    @Test
    public void testMainFileNotFound() {
        // 捕获System.err
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        System.setErr(new PrintStream(err));
        TextGraph.main(new String[]{"not_exist_file.txt"});
        System.setErr(oldErr);
        assertTrue(err.toString().contains("Error reading file"));
    }
} 