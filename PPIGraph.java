package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PPIGraph {
    private List<Interaction> edgeList;
    private Map<String, Protein> vertices;

    public PPIGraph() {
        this.edgeList = new ArrayList<>();
        this.vertices = new HashMap<>();
    }

    public void loadGraph(String filePath, double threshold) {
        System.out.println("Loading file: " + filePath);
        System.out.println("Threshold: " + threshold);
        
        edgeList.clear();
        vertices.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineCount = 0;
            
            while ((line = br.readLine()) != null) {
                lineCount++;
                String[] parts = line.trim().split("\\s+"); 

                if (parts.length < 3) continue;
                if (!isNumeric(parts[2])) continue;

                try {
                    String p1Id = parts[0];
                    String p2Id = parts[1];
                    double score = Double.parseDouble(parts[2]);

                    if (score > 1.0) {
                        score = score / 1000.0;
                    }

                    if (score >= threshold) {
                        Protein p1 = vertices.computeIfAbsent(p1Id, Protein::new);
                        Protein p2 = vertices.computeIfAbsent(p2Id, Protein::new);

                        Interaction edge = new Interaction(p1, p2, score);
                        edgeList.add(edge);
                    }
                } catch (NumberFormatException e) {
                }
            }
            System.out.println("Loading Completed!");
            System.out.println("Total Lines Read: " + lineCount);
            System.out.println("Total Vertices (Proteins): " + vertices.size());
            System.out.println("Total Edges (Interactions): " + edgeList.size());

        } catch (IOException e) {
            System.out.println("ERROR: Could not read file. Check the file path.");
            System.out.println("Details: " + e.getMessage());
        }
    }

    private boolean isNumeric(String str) {
        if (str == null) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void BFS(String startId) {
        if (vertices.isEmpty()) {
            System.out.println("WARNING: Graph is empty. Please load the file first.");
            return;
        }
        
        if (!vertices.containsKey(startId)) {
            System.out.println("ERROR: Protein ID '" + startId + "' not found in the graph.");
            return;
        }

        Protein startNode = vertices.get(startId);
        Set<Protein> visited = new HashSet<>();
        Queue<Protein> queue = new LinkedList<>();

        visited.add(startNode);
        queue.add(startNode);

        System.out.println("\nStarting BFS Traversal from: " + startId);
        System.out.println("-----------------------------------");
        
        int count = 0;

        while (!queue.isEmpty()) {
            Protein current = queue.poll();
            count++;
            
            //if (count <= 50) {
                System.out.println(count + ". " + current.getId());
            //} else if (count == 51) {
           //     System.out.println("... (List truncated for readability) ...");
           // }

            for (Interaction edge : edgeList) {
                if (edge.getSource().equals(current)) {
                    Protein neighbor = edge.getDestination();
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
        System.out.println("-----------------------------------");
        System.out.println("BFS Completed.");
        System.out.println("Total Visited Nodes: " + visited.size());
    }
    
    public void printMetrics() {
        System.out.println("\n--- Graph Metrics ---");
        int vCount = vertices.size();
        int eCount = edgeList.size();
        
        System.out.println("Vertex Count: " + vCount);
        System.out.println("Edge Count: " + eCount);
        
        if (vCount > 0) {
            double avgDegree = (double) eCount / vCount;
            System.out.println(String.format("Average Degree: %.4f", avgDegree));
        } else {
            System.out.println("Graph is empty.");
        }
    }
}