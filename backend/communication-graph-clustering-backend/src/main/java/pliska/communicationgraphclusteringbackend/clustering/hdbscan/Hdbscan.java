package pliska.communicationgraphclusteringbackend.clustering.hdbscan;

import pliska.communicationgraphclusteringbackend.db.graph.EdgeEntity;
import pliska.communicationgraphclusteringbackend.db.graph.NodeEntity;

import java.util.*;
import java.util.stream.Collectors;

public class Hdbscan {
    private final List<NodeEntity> nodes;
    private final List<EdgeEntity> edges;

    public Hdbscan(List<NodeEntity> nodes, List<EdgeEntity> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Map<Long, Double> calculateCoreDistances(int minPoints) {
        Map<Long, Double> coreDistances = new HashMap<>();
        for (NodeEntity node : nodes) {
            List<Double> neighborDistances = edges.stream()
                    .filter(edge -> edge.getSourceNode().getId().equals(node.getId()) || edge.getTargetNode().getId().equals(node.getId()))
                    .map(EdgeEntity::getWeight)
                    .sorted()
                    .collect(Collectors.toList());

            if (neighborDistances.size() >= minPoints) {
                coreDistances.put(node.getId(), neighborDistances.get(minPoints - 1));
            } else {
                coreDistances.put(node.getId(), Double.MAX_VALUE);
            }
        }
        return coreDistances;
    }

    public List<EdgeEntity> computeMutualReachabilityDistances(Map<Long, Double> coreDistances) {
        List<EdgeEntity> updatedEdges = new ArrayList<>();

        for (EdgeEntity edge : edges) {
            Double sourceCoreDist = coreDistances.get(edge.getSourceNode().getId());
            Double targetCoreDist = coreDistances.get(edge.getTargetNode().getId());
            Double originalDist = edge.getWeight();

            Double mutualReachabilityDistance = Math.max(Math.max(sourceCoreDist, targetCoreDist), originalDist);

            EdgeEntity updatedEdge = new EdgeEntity(edge.getId(), edge.getGraph(),
                    edge.getSourceNode(), edge.getTargetNode(), mutualReachabilityDistance, edge.getInteractions());
            updatedEdges.add(updatedEdge);
        }
        return updatedEdges;
    }

    public List<EdgeEntity> constructMST(List<EdgeEntity> edgesWithMRD) {
        edgesWithMRD.sort(Comparator.comparingDouble(EdgeEntity::getWeight));

        Map<Long, Long> parent = new HashMap<>();
        for (NodeEntity node : nodes) {
            parent.put(node.getId(), node.getId());
        }

        List<EdgeEntity> mstEdges = new ArrayList<>();
        for (EdgeEntity edge : edgesWithMRD) {
            Long sourceSet = findSet(parent, edge.getSourceNode().getId());
            Long targetSet = findSet(parent, edge.getTargetNode().getId());

            if (!sourceSet.equals(targetSet)) {
                mstEdges.add(edge);
                unionSets(parent, sourceSet, targetSet);
            }
        }

        return mstEdges;
    }

    private Long findSet(Map<Long, Long> parent, Long node) {
        if (!parent.get(node).equals(node)) {
            parent.put(node, findSet(parent, parent.get(node)));
        }
        return parent.get(node);
    }

    private void unionSets(Map<Long, Long> parent, Long set1, Long set2) {
        parent.put(set1, set2);
    }

    public List<Map<Integer, List<Long>>> extractClusters(List<EdgeEntity> mstEdges, double densityThreshold) {
        List<EdgeEntity> filteredEdges = mstEdges.stream()
                .filter(edge -> edge.getWeight() <= densityThreshold)
                .collect(Collectors.toList());

        Map<Long, Set<NodeEntity>> nodeClusters = new HashMap<>();
        for (NodeEntity node : nodes) {
            nodeClusters.put(node.getId(), new HashSet<>(Collections.singletonList(node)));
        }

        for (EdgeEntity edge : filteredEdges) {
            Long sourceClusterId = edge.getSourceNode().getId();
            Long targetClusterId = edge.getTargetNode().getId();

            if (!nodeClusters.get(sourceClusterId).equals(nodeClusters.get(targetClusterId))) {
                Set<NodeEntity> sourceCluster = nodeClusters.get(sourceClusterId);
                Set<NodeEntity> targetCluster = nodeClusters.get(targetClusterId);

                sourceCluster.addAll(targetCluster);
                for (NodeEntity targetNode : targetCluster) {
                    nodeClusters.put(targetNode.getId(), sourceCluster);
                }
            }
        }

        Set<Set<NodeEntity>> uniqueClusters = new HashSet<>(nodeClusters.values());

        List<Map<Integer, List<Long>>> result = new ArrayList<>();
        int clusterId = 0;

        for (Set<NodeEntity> cluster : uniqueClusters) {
            List<Long> nodeIds = cluster.stream()
                    .map(NodeEntity::getId)
                    .sorted()
                    .collect(Collectors.toList());

            Map<Integer, List<Long>> clusterMap = new LinkedHashMap<>();
            clusterMap.put(clusterId, nodeIds);
            result.add(clusterMap);

            clusterId++;
        }

        return result;
    }



}
