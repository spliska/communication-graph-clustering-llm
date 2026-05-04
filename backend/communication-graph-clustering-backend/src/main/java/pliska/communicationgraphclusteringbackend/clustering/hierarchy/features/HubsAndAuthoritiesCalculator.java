package pliska.communicationgraphclusteringbackend.clustering.hierarchy.features;

import pliska.communicationgraphclusteringbackend.db.graph.EdgeDto;
import pliska.communicationgraphclusteringbackend.db.graph.NodeDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HubsAndAuthoritiesCalculator {

    private static final int MAX_ITERATIONS = 100;
    private static final double EPSILON = 1e-9;

    private final List<NodeDto> nodes;
    private final List<EdgeDto> edges;

    private final Map<String, Double> authorityScores = new HashMap<>();
    private final Map<String, Double> hubScores = new HashMap<>();

    public HubsAndAuthoritiesCalculator(List<NodeDto> nodes, List<EdgeDto> edges) {
        this.nodes = nodes;
        this.edges = edges;
        calculateHits();
    }

    public double getAuthorityScore(String nodeId) {
        return authorityScores.getOrDefault(nodeId, 0.0);
    }

    public double getHubScore(String nodeId) {
        return hubScores.getOrDefault(nodeId, 0.0);
    }

    public double getCombinedHitsScore(String nodeId) {
        double authority = getAuthorityScore(nodeId);
        double hub = getHubScore(nodeId);

        return 0.6 * authority + 0.4 * hub;
    }

    private void calculateHits() {
        Map<String, Double> authority = new HashMap<>();
        Map<String, Double> hub = new HashMap<>();

        for (NodeDto node : nodes) {
            String nodeId = node.getNodeId().toString();
            authority.put(nodeId, 1.0);
            hub.put(nodeId, 1.0);
        }

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            Map<String, Double> newAuthority = initializeZeroScores();
            Map<String, Double> newHub = initializeZeroScores();

            for (EdgeDto edge : edges) {
                String sourceId = edge.getSourceNodeId();
                String targetId = edge.getTargetNode();

                if (!authority.containsKey(sourceId) || !authority.containsKey(targetId)) {
                    continue;
                }

                double weight = edge.getWeight();

                if (weight <= 0.0) {
                    continue;
                }

                newAuthority.put(
                        targetId,
                        newAuthority.get(targetId) + weight * hub.get(sourceId)
                );

                newHub.put(
                        sourceId,
                        newHub.get(sourceId) + weight * authority.get(targetId)
                );
            }

            normalize(newAuthority);
            normalize(newHub);

            double diff = difference(authority, newAuthority)
                    + difference(hub, newHub);

            authority = newAuthority;
            hub = newHub;

            if (diff < EPSILON) {
                break;
            }
        }

        authorityScores.clear();
        authorityScores.putAll(authority);

        hubScores.clear();
        hubScores.putAll(hub);
    }

    private Map<String, Double> initializeZeroScores() {
        Map<String, Double> scores = new HashMap<>();

        for (NodeDto node : nodes) {
            scores.put(node.getNodeId().toString(), 0.0);
        }

        return scores;
    }

    private void normalize(Map<String, Double> scores) {
        double norm = 0.0;

        for (double value : scores.values()) {
            norm += value * value;
        }

        norm = Math.sqrt(norm);

        if (norm <= EPSILON) {
            return;
        }

        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            entry.setValue(entry.getValue() / norm);
        }
    }

    private double difference(
            Map<String, Double> oldScores,
            Map<String, Double> newScores
    ) {
        double diff = 0.0;

        for (String nodeId : oldScores.keySet()) {
            diff += Math.abs(
                    oldScores.getOrDefault(nodeId, 0.0)
                            - newScores.getOrDefault(nodeId, 0.0)
            );
        }

        return diff;
    }
}