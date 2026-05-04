package pliska.communicationgraphclusteringbackend.clustering.hierarchy.features;

import pliska.communicationgraphclusteringbackend.db.graph.EdgeDto;
import pliska.communicationgraphclusteringbackend.db.graph.NodeDto;
import pliska.communicationgraphclusteringbackend.db.graph.NodeEntity;

import java.util.*;

public class CliquesCalculator {

    private final List<NodeDto> nodes;
    private final List<EdgeDto> edges;

    private final Map<NodeEntity, Set<Set<NodeEntity>>> nodeCliqueMap = new HashMap<>();
    private final Collection<Set<NodeEntity>> cliques = new HashSet<>();

    public CliquesCalculator(List<NodeDto> nodes, List<EdgeDto> edges) {
        this.nodes = nodes;
        this.edges = edges;
        calculateCliques();
        buildNodeCliqueMap();
    }

    public Collection<Set<NodeEntity>> getAllMaximalCliques() {
        return cliques;
    }

    public Collection<Set<NodeEntity>> getBiggestMaximalCliques() {
        int maxSize = cliques.stream()
                .mapToInt(Set::size)
                .max()
                .orElse(0);

        Collection<Set<NodeEntity>> biggestCliques = new HashSet<>();

        for (Set<NodeEntity> clique : cliques) {
            if (clique.size() == maxSize) {
                biggestCliques.add(clique);
            }
        }

        return biggestCliques;
    }

    public int getNumberOfCliquesForNode(NodeDto node) {
        NodeEntity nodeEntity = convertNodeDtoToEntity(node);

        return nodeCliqueMap
                .getOrDefault(nodeEntity, Collections.emptySet())
                .size();
    }

    public int getNumberOfMembersInClique(Set<NodeEntity> clique) {
        return clique.size();
    }

    public List<Integer> getCliquesMembersAmountForNode(String nodeId) {
        NodeEntity nodeEntity = convertNodeIdToEntity(nodeId);

        if (nodeEntity == null) {
            throw new IllegalArgumentException("Node with ID " + nodeId + " not found.");
        }

        List<Integer> cliqueSizes = new ArrayList<>();

        for (Set<NodeEntity> clique : cliques) {
            if (clique.contains(nodeEntity)) {
                cliqueSizes.add(clique.size());
            }
        }

        return cliqueSizes;
    }

    private void calculateCliques() {
        Set<NodeEntity> allNodes = new HashSet<>(convertNodesToEntities(nodes));

        findCliques(
                new HashSet<>(),
                allNodes,
                new HashSet<>()
        );
    }

    private void findCliques(
            Set<NodeEntity> currentClique,
            Set<NodeEntity> possibleCandidates,
            Set<NodeEntity> alreadyProcessed
    ) {
        if (possibleCandidates.isEmpty() && alreadyProcessed.isEmpty()) {
            cliques.add(new HashSet<>(currentClique));
            return;
        }

        Set<NodeEntity> candidatesToCheck = new HashSet<>(possibleCandidates);

        for (NodeEntity candidateNode : candidatesToCheck) {
            Set<NodeEntity> candidateNeighbors = getNeighbors(candidateNode);

            Set<NodeEntity> extendedClique = new HashSet<>(currentClique);
            extendedClique.add(candidateNode);

            Set<NodeEntity> remainingCandidates = new HashSet<>(possibleCandidates);
            remainingCandidates.retainAll(candidateNeighbors);

            Set<NodeEntity> processedNeighbors = new HashSet<>(alreadyProcessed);
            processedNeighbors.retainAll(candidateNeighbors);

            findCliques(
                    extendedClique,
                    remainingCandidates,
                    processedNeighbors
            );

            possibleCandidates.remove(candidateNode);
            alreadyProcessed.add(candidateNode);
        }
    }

    private Set<NodeEntity> getNeighbors(NodeEntity node) {
        Set<NodeEntity> neighbors = new HashSet<>();

        for (NodeDto nodeDto : nodes) {
            NodeEntity otherNode = convertNodeDtoToEntity(nodeDto);

            if (!otherNode.equals(node) && isNeighbor(node, otherNode)) {
                neighbors.add(otherNode);
            }
        }

        return neighbors;
    }

    private boolean isNeighbor(NodeEntity firstNode, NodeEntity secondNode) {
        String firstNodeId = firstNode.getId().toString();
        String secondNodeId = secondNode.getId().toString();

        for (EdgeDto edge : edges) {
            boolean edgeFromFirstToSecond =
                    edge.getSourceNodeId().equals(firstNodeId)
                            && edge.getTargetNode().equals(secondNodeId);

            boolean edgeFromSecondToFirst =
                    edge.getSourceNodeId().equals(secondNodeId)
                            && edge.getTargetNode().equals(firstNodeId);

            if (edgeFromFirstToSecond || edgeFromSecondToFirst) {
                return true;
            }
        }

        return false;
    }

    private void buildNodeCliqueMap() {
        nodeCliqueMap.clear();

        for (Set<NodeEntity> clique : cliques) {
            for (NodeEntity node : clique) {
                nodeCliqueMap
                        .computeIfAbsent(node, ignored -> new HashSet<>())
                        .add(clique);
            }
        }
    }

    private List<NodeEntity> convertNodesToEntities(List<NodeDto> nodeDtos) {
        List<NodeEntity> entities = new ArrayList<>();

        for (NodeDto nodeDto : nodeDtos) {
            entities.add(convertNodeDtoToEntity(nodeDto));
        }

        return entities;
    }

    private NodeEntity convertNodeDtoToEntity(NodeDto nodeDto) {
        NodeEntity entity = new NodeEntity();
        entity.setId(nodeDto.getNodeId());
        return entity;
    }

    private NodeEntity convertNodeIdToEntity(String nodeId) {
        return nodes.stream()
                .filter(node -> node.getNodeId().toString().equals(nodeId))
                .map(this::convertNodeDtoToEntity)
                .findFirst()
                .orElse(null);
    }
}