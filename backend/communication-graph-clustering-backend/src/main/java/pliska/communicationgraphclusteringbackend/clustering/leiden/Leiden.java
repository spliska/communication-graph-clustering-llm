package pliska.communicationgraphclusteringbackend.clustering.leiden;

import pliska.communicationgraphclusteringbackend.db.graph.EdgeEntity;
import pliska.communicationgraphclusteringbackend.db.graph.NodeEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Leiden {
    private final List<NodeEntity> nodes;
    private final List<EdgeEntity> edges;

    public Leiden(List<NodeEntity> nodes, List<EdgeEntity> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Map<Long, Integer> execute(int maxIterations) {
        Map<Long, Integer> clustering = initializeClusters();

        int iteration = 0;
        boolean hasImproved;

        do {
            hasImproved = false;
            iteration++;

            for (NodeEntity node : nodes) {
                hasImproved |= optimizeClusterForNode(node, clustering);
            }

            if (hasImproved) {
                clustering = aggregateGraph(clustering);
            }

        } while (hasImproved && iteration < maxIterations);

        return clustering;
    }

    private Map<Long, Integer> initializeClusters() {
        Map<Long, Integer> clustering = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            clustering.put(nodes.get(i).getId(), i);
        }
        return clustering;
    }

    private boolean optimizeClusterForNode(NodeEntity node, Map<Long, Integer> clustering) {
        Long nodeId = node.getId();
        Integer currentCluster = clustering.get(nodeId);

        Map<Long, Double> neighborWeights = edges.stream()
                .filter(edge -> edge.getSourceNode().getId().equals(nodeId) || edge.getTargetNode().getId().equals(nodeId))
                .collect(Collectors.toMap(
                        edge -> edge.getSourceNode().getId().equals(nodeId) ? edge.getTargetNode().getId() : edge.getSourceNode().getId(),
                        EdgeEntity::getWeight
                ));

        Map<Integer, Double> modularityGain = new HashMap<>();
        for (Map.Entry<Long, Double> neighborEntry : neighborWeights.entrySet()) {
            Long neighborNodeId = neighborEntry.getKey();
            Integer neighborCluster = clustering.get(neighborNodeId);

            modularityGain.put(neighborCluster, modularityGain.getOrDefault(neighborCluster, 0.0) + neighborEntry.getValue());
        }

        Integer bestCluster = currentCluster;
        double maxGain = 0;

        for (Map.Entry<Integer, Double> entry : modularityGain.entrySet()) {
            if (entry.getValue() > maxGain) {
                maxGain = entry.getValue();
                bestCluster = entry.getKey();
            }
        }

        if (!bestCluster.equals(currentCluster)) {
            clustering.put(nodeId, bestCluster);
            return true;
        }

        return false;
    }

    private Map<Long, Integer> aggregateGraph(Map<Long, Integer> clustering) {
        Map<Integer, List<Long>> clusters = new HashMap<>();
        Map<Long, Integer> newClustering = new HashMap<>();
        int newClusterId = 0;

        for (Map.Entry<Long, Integer> entry : clustering.entrySet()) {
            clusters.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        for (Map.Entry<Integer, List<Long>> clusterEntry : clusters.entrySet()) {
            for (Long nodeId : clusterEntry.getValue()) {
                newClustering.put(nodeId, newClusterId);
            }
            newClusterId++;
        }

        return newClustering;
    }
}
