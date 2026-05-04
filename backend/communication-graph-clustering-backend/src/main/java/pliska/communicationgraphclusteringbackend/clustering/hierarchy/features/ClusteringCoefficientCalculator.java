package pliska.communicationgraphclusteringbackend.clustering.hierarchy.features;

import pliska.communicationgraphclusteringbackend.db.graph.EdgeDto;
import pliska.communicationgraphclusteringbackend.db.graph.NodeDto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClusteringCoefficientCalculator {

    private List<NodeDto> nodes;
    private List<EdgeDto> edges;

    public ClusteringCoefficientCalculator(List<NodeDto> nodes, List<EdgeDto> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }


    public Integer calculateConnectionsOfNodeToOtherClusters(String nodeId) {
        NodeDto currentNode = nodes.stream()
                .filter(node -> node.getNodeId().toString().equals(nodeId))
                .findFirst()
                .orElse(null);

        if (currentNode == null) {
            throw new IllegalArgumentException("Node with ID " + nodeId + " not found.");
        }

        String currentClusterId = currentNode.getClusterId();
        Set<String> connectedClusters = new HashSet<>();

        for (EdgeDto edge : edges) {
            if (edge.getSourceNodeId().equals(nodeId)) {
                String targetNodeId = edge.getTargetNode();
                String targetClusterId = getNodeClusterId(targetNodeId);

                if (targetClusterId != null && !targetClusterId.equals(currentClusterId)) {
                    connectedClusters.add(targetClusterId);
                }

            } else if (edge.getTargetNode().equals(nodeId)) {
                String sourceNodeId = edge.getSourceNodeId();
                String sourceClusterId = getNodeClusterId(sourceNodeId);

                if (sourceClusterId != null && !sourceClusterId.equals(currentClusterId)) {
                    connectedClusters.add(sourceClusterId);
                }
            }
        }

        return connectedClusters.size();
    }

    private String getNodeClusterId(String nodeId) {
        return nodes.stream()
                .filter(node -> node.getNodeId().toString().equals(nodeId))
                .map(NodeDto::getClusterId)
                .findFirst()
                .orElse(null);
    }
}

