package pliska.communicationgraphclusteringbackend.clustering.hierarchy.features;

import pliska.communicationgraphclusteringbackend.db.graph.EdgeDto;
import pliska.communicationgraphclusteringbackend.db.graph.NodeDto;

import java.util.List;

public class BasicGraphInsightsCalculator {

    List<NodeDto> nodes;
    List<EdgeDto> edges;

    public BasicGraphInsightsCalculator(List<NodeDto> nodes, List<EdgeDto> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public int getLocalNodeCentrality(Long graphId, String email) {
        NodeDto currentNode = nodes.stream()
                .filter(node -> node.getEmail().equals(email))
                .findFirst()
                .orElse(null);

        if (currentNode == null) {
            throw new IllegalArgumentException("Node with email " + email + " not found.");
        }

        String currentClusterId = currentNode.getClusterId();
        int centrality = 0;

        for (EdgeDto edge : edges) {
            if (edge.getSourceNodeId().equals(currentNode.getNodeId().toString())) {
                String targetClusterId = getNodeClusterId(edge.getTargetNode());
                if (targetClusterId != null && targetClusterId.equals(currentClusterId)) {
                    centrality++;
                }
            } else if (edge.getTargetNode().equals(currentNode.getNodeId().toString())) {
                String sourceClusterId = getNodeClusterId(edge.getSourceNodeId());
                if (sourceClusterId != null && sourceClusterId.equals(currentClusterId)) {
                    centrality++;
                }
            }
        }
        return centrality;
    }

    public int getGlobalNodeCentrality(Long graphId, String email) {
        NodeDto currentNode = nodes.stream()
                .filter(node -> node.getEmail().equals(email))
                .findFirst()
                .orElse(null);

        if (currentNode == null) {
            throw new IllegalArgumentException("Node with email " + email + " not found.");
        }

        int centrality = 0;

        for (EdgeDto edge : edges) {
            if (edge.getSourceNodeId().equals(currentNode.getNodeId().toString())
                    || edge.getTargetNode().equals(currentNode.getNodeId().toString())) {
                centrality++;
            }
        }

        return centrality;
    }

    private String getNodeClusterId(String nodeId) {
        return nodes.stream()
                .filter(node -> node.getNodeId().toString().equals(nodeId))
                .map(NodeDto::getClusterId)
                .findFirst()
                .orElse(null);
    }

    public double getAverageDistanceToConnectedNodes(String email) {
        NodeDto currentNode = nodes.stream()
                .filter(node -> node.getEmail().equals(email))
                .findFirst()
                .orElse(null);

        if (currentNode == null) {
            throw new IllegalArgumentException("Node with email " + email + " not found.");
        }

        double totalDistance = 0.0;
        int connectionCount = 0;

        for (EdgeDto edge : edges) {
            if (edge.getSourceNodeId().equals(currentNode.getNodeId().toString())) {
                totalDistance += edge.getWeight();
                connectionCount++;
            } else if (edge.getTargetNode().equals(currentNode.getNodeId().toString())) {
                totalDistance += edge.getWeight();
                connectionCount++;
            }
        }

        return connectionCount > 0 ? totalDistance / connectionCount : -1;
    }


}
