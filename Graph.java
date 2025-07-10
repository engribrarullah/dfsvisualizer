import java.util.*;

public class Graph {
    private final Map<String, List<String>> adjList = new HashMap<>();

    public void addNode(String node) {
        adjList.putIfAbsent(node, new ArrayList<>());
    }

    public void addEdge(String from, String to) {
        addNode(from);
        addNode(to);
        if (!adjList.get(from).contains(to)) {
            adjList.get(from).add(to);
        }
        if (!adjList.get(to).contains(from)) {
            adjList.get(to).add(from);
        }
    }

    public List<String> getNeighbors(String node) {
        return adjList.getOrDefault(node, new ArrayList<>());
    }

    public Set<String> getNodes() {
        return adjList.keySet();
    }

    public boolean hasNode(String node) {
        return adjList.containsKey(node);
    }

    public void dfs(String start, Set<String> visited, List<String> result) {
        if (!adjList.containsKey(start) || visited.contains(start)) return;
        visited.add(start);
        result.add(start);
        for (String neighbor : adjList.get(start)) {
            dfs(neighbor, visited, result);
        }
    }

    public void removeNode(String node) {
        if (!adjList.containsKey(node)) return;
        for (String neighbor : adjList.get(node)) {
            adjList.get(neighbor).remove(node);
        }
        adjList.remove(node);
    }

    public void removeEdge(String from, String to) {
        if (adjList.containsKey(from)) {
            adjList.get(from).remove(to);
        }
    }
}
