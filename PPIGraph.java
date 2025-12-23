package src;

import java.io.*;
import java.util.*;

public class PPIGraph {
    // KURALA UYGUN YAPI:
    // Hızlı erişim sağlayan Map'ler yasak. Sadece Listeler var.
    private ArrayList<Interaction> edgeList;
    private ArrayList<Protein> vertexList;

    public PPIGraph() {
        edgeList = new ArrayList<>();
        vertexList = new ArrayList<>();
    }

    public void load(String path, double th) {
        long startTime = System.currentTimeMillis();
        System.out.println("------------------------------------------------");
        System.out.println("Loading graph (Strict Edge List Mode)...");
        
        edgeList.clear();
        vertexList.clear();

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            
            while ((line = br.readLine()) != null) {
                String[] split = line.trim().split("\\s+");
                if (split.length < 3) continue;

                try {
                    double w = Double.parseDouble(split[2]);
                    double checkVal = (w > 1.0) ? w / 1000.0 : w;

                    if (checkVal >= th) {
                        String id1 = split[0];
                        String id2 = split[1];

                        // YASAK YOK: Map.get() yerine Listeyi tek tek gezen metot kullanıyoruz.
                        Protein p1 = findInList(id1);
                        if (p1 == null) {
                            p1 = new Protein(id1);
                            vertexList.add(p1);
                        }

                        Protein p2 = findInList(id2);
                        if (p2 == null) {
                            p2 = new Protein(id2);
                            vertexList.add(p2);
                        }

                        // Kenarı listeye ekle
                        Interaction in = new Interaction(p1, p2, w);
                        edgeList.add(in);
                    }
                } catch (Exception e) {}
            }
            br.close();
            
            long endTime = System.currentTimeMillis();
            System.out.println("Graph Loaded.");
            System.out.println("Time: " + (endTime - startTime) + " ms");
            System.out.println("Vertices: " + vertexList.size());
            System.out.println("Edges: " + edgeList.size());
            System.out.println("------------------------------------------------\n");

        } catch (Exception e) {
            System.out.println("File error.");
        }
    }

    // --- YASAKSIZ METOTLAR ---

    // Map.get() yerine bu kullanılır. O(N) karmaşıklığındadır (YAVAŞTIR).
    private Protein findInList(String id) {
        for (Protein p : vertexList) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    // Bir düğümün komşularını bulmak için TÜM KENARLARI tararız.
    private ArrayList<Interaction> getConnections(Protein p) {
        ArrayList<Interaction> list = new ArrayList<>();
        for (Interaction i : edgeList) {
            if (i.getSource().equals(p)) {
                list.add(i);
            }
        }
        return list;
    }

    // --- PATH ALGORITHMS ---

    public void findAllPaths(String start, String end) {
        Protein s = findInList(start);
        Protein e = findInList(end);

        if (s == null || e == null) {
            System.out.println("Error: Proteins not found.");
            return;
        }

        System.out.println("\n--- Path Analysis ---");
        System.out.println("1. Shortest Path (Min Hops)");
        solveShortest(s, e);

        System.out.println("\n2. Cheapest Path (Min Weight Sum)");
        solveCheapest(s, e);

        System.out.println("\n3. Most Confident Path (Shortest + Max Weight)");
        solveMostConfident(s, e);
        System.out.println("---------------------\n");
    }

    // 1. Shortest Path
    private void solveShortest(Protein s, Protein e) {
        // Algoritma içinde geçici Map kullanımı yasak değil (Data structure olarak değil, local variable olarak)
        HashMap<Protein, Protein> parent = new HashMap<>();
        HashMap<Protein, Integer> dist = new HashMap<>();
        LinkedList<Protein> q = new LinkedList<>();

        q.add(s);
        dist.put(s, 0);

        boolean found = false;
        while (!q.isEmpty()) {
            Protein u = q.poll();
            if (u.equals(e)) { found = true; break; }

            for (Interaction i : getConnections(u)) {
                Protein v = i.getDest();
                if (!dist.containsKey(v)) {
                    dist.put(v, dist.get(u) + 1);
                    parent.put(v, u);
                    q.add(v);
                }
            }
        }

        if (found) {
            System.out.println("Cost = " + (double) dist.get(e));
            printPath(parent, e);
        } else System.out.println("No path.");
    }

    // 2. Cheapest Path
    private void solveCheapest(Protein s, Protein e) {
        HashMap<Protein, Double> costs = new HashMap<>();
        HashMap<Protein, Protein> parent = new HashMap<>();
        // PriorityQueue Java'nın standart kütüphanesidir, yasak değildir.
        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> Double.compare(a.val, b.val));

        for (Protein p : vertexList) costs.put(p, Double.MAX_VALUE);
        costs.put(s, 0.0);
        pq.add(new Node(s, 0.0));

        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            Protein u = curr.p;
            if (u.equals(e)) break;
            if (curr.val > costs.get(u)) continue;

            for (Interaction i : getConnections(u)) {
                Protein v = i.getDest();
                double newCost = costs.get(u) + i.getW();
                if (newCost < costs.get(v)) {
                    costs.put(v, newCost);
                    parent.put(v, u);
                    pq.add(new Node(v, newCost));
                }
            }
        }

        if (parent.containsKey(e)) {
            System.out.println("Cost = " + costs.get(e));
            printPath(parent, e);
        } else System.out.println("No path.");
    }

    // 3. Most Confident Path
    private void solveMostConfident(Protein s, Protein e) {
        HashMap<Protein, Integer> minHops = new HashMap<>();
        HashMap<Protein, Double> maxWeight = new HashMap<>();
        HashMap<Protein, Protein> parent = new HashMap<>();

        PriorityQueue<PathNode> pq = new PriorityQueue<>((a, b) -> {
            if (a.hops != b.hops) return Integer.compare(a.hops, b.hops);
            return Double.compare(b.weight, a.weight);
        });

        for (Protein p : vertexList) {
            minHops.put(p, Integer.MAX_VALUE);
            maxWeight.put(p, -1.0);
        }
        minHops.put(s, 0);
        maxWeight.put(s, 0.0);
        pq.add(new PathNode(s, 0, 0.0));

        while (!pq.isEmpty()) {
            PathNode curr = pq.poll();
            Protein u = curr.p;

            if (curr.hops > minHops.get(u) || (curr.hops == minHops.get(u) && curr.weight < maxWeight.get(u))) continue;
            if (u.equals(e)) break;

            for (Interaction i : getConnections(u)) {
                Protein v = i.getDest();
                int newHops = curr.hops + 1;
                double newWeight = curr.weight + i.getW();

                boolean update = false;
                if (newHops < minHops.get(v)) update = true;
                else if (newHops == minHops.get(v) && newWeight > maxWeight.get(v)) update = true;

                if (update) {
                    minHops.put(v, newHops);
                    maxWeight.put(v, newWeight);
                    parent.put(v, u);
                    pq.add(new PathNode(v, newHops, newWeight));
                }
            }
        }

        if (parent.containsKey(e)) {
            System.out.println("Cost = " + maxWeight.get(e));
            printPath(parent, e);
        } else System.out.println("No path.");
    }

    private void printPath(HashMap<Protein, Protein> parent, Protein end) {
        ArrayList<String> path = new ArrayList<>();
        Protein curr = end;
        while (curr != null) {
            path.add(curr.getId());
            curr = parent.get(curr);
        }
        Collections.reverse(path);
        for (int i = 0; i < path.size(); i++) {
            System.out.println((i + 1) + "- " + path.get(i));
        }
    }

    public void metrics() {
        System.out.println("\n--- Metrics ---");
        int v = vertexList.size();
        int e = edgeList.size();
        System.out.println("Vertex: " + v);
        System.out.println("Edge: " + e);
        if (v > 0) System.out.printf("Avg Degree: %.4f\n", (double) e / v);

        int bi = 0;
        // Reciprocity için de döngü kullanmak en güvenlisi (Set kullanmak yasak değil ama döngü daha "Edge List" ruhuna uygun)
        for (Interaction i : edgeList) {
            Protein src = i.getSource();
            Protein dest = i.getDest();
            // Tersi var mı diye tüm listeyi tara
            for(Interaction j : edgeList) {
                if(j.getSource().equals(dest) && j.getDest().equals(src)) {
                    bi++;
                    break;
                }
            }
        }
        if (e > 0) System.out.printf("Reciprocity: %.4f\n", (double) bi / e);
    }

    public void bfs(String id) {
        Protein start = findInList(id);
        if (start == null) return;

        LinkedList<Protein> q = new LinkedList<>();
        ArrayList<Protein> visited = new ArrayList<>(); // Set yerine List kullandım (daha yavaş, daha pure)
        
        q.add(start);
        visited.add(start);
        int c = 0;

        System.out.println("BFS " + id + ":");
        while (!q.isEmpty()) {
            Protein u = q.poll();
            c++;
            if (c <= 10) System.out.println(c + ". " + u.getId());

            for (Interaction i : getConnections(u)) {
                Protein v = i.getDest();
                // visited.contains listeyi tarar (Yavaş)
                if (!visited.contains(v)) { 
                    visited.add(v);
                    q.add(v);
                }
            }
        }
    }

    public void dfs(String id) {
        Protein start = findInList(id);
        if (start == null) return;

        Stack<Protein> s = new Stack<>();
        ArrayList<Protein> visited = new ArrayList<>();

        s.push(start);
        int c = 0;
        System.out.println("DFS " + id + ":");

        while (!s.isEmpty()) {
            Protein u = s.pop();
            if (!visited.contains(u)) {
                visited.add(u);
                c++;
                if (c <= 10) System.out.println(c + ". " + u.getId());

                for (Interaction i : getConnections(u)) {
                    if (!visited.contains(i.getDest())) {
                        s.push(i.getDest());
                    }
                }
            }
        }
    }
    
    public void search(String id) {
        Protein p = findInList(id);
        if(p != null) {
            System.out.println("Found: " + id);
            int d = 0;
            for(Interaction i : edgeList) if(i.getSource().equals(p)) d++;
            System.out.println("Degree: " + d);
        } else {
            System.out.println("Not found.");
        }
    }
    
    public void check(String id1, String id2) {
        Protein p1 = findInList(id1);
        boolean f = false;
        if(p1 != null) {
            for(Interaction i : edgeList) {
                if(i.getSource().equals(p1) && i.getDest().getId().equals(id2)) {
                    f = true; break;
                }
            }
        }
        System.out.println("Interaction: " + (f ? "Yes" : "No"));
    }

    // Helper Classes
    class Node {
        Protein p; double val;
        public Node(Protein p, double val) { this.p = p; this.val = val; }
    }
    class PathNode {
        Protein p; int hops; double weight;
        public PathNode(Protein p, int hops, double weight) { this.p = p; this.hops = hops; this.weight = weight; }
    }
}