package pliska.communicationgraphclusteringbackend.clustering.leiden;

import org.springframework.stereotype.Service;
import pliska.communicationgraphclusteringbackend.clustering.ClusteringService;
import pliska.communicationgraphclusteringbackend.clustering.hierarchy.HierarchyService;
import pliska.communicationgraphclusteringbackend.db.graph.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeidenService implements ClusteringService
{
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final HierarchyService hierarchyService;


    public LeidenService(NodeRepository nodeRepository, EdgeRepository edgeRepository, HierarchyService hierarchyService) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.hierarchyService = hierarchyService;
    }

    public Map<String, Object> clusterGraph(Long graphId, Map<String, Object> parameters
    ) {
        Integer maxIterations = (Integer) parameters.get("maxIterations");
        List<NodeEntity> nodes = nodeRepository.findAllNodesByGraphId(graphId);
        List<EdgeEntity> edges = edgeRepository.findAllEdgesByGraphId(graphId);

        Leiden leiden = new Leiden(nodes, edges);

        Map<Long, Integer> clusters = leiden.execute(maxIterations);
        Map<String, Object> clusteredGraph = new HashMap<>();
        List<NodeDto> nodeDtos = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : clusters.entrySet()) {
            Long nodeId = entry.getKey();
            Integer clusterId = entry.getValue();

            NodeEntity node = nodes.stream()
                    .filter(n -> n.getId().equals(nodeId))
                    .findFirst()
                    .orElse(null);

            if (node != null) {
                node.setClusterId(clusterId);
                NodeDto nodeDto = new NodeDto(
                        node.getId(),
                        node.getPerson().getFirstName(),
                        node.getPerson().getLastName(),
                        node.getPerson().getEmail(),
                        clusterId.toString());
                nodeDtos.add(nodeDto);
            }
        }

        ArrayList<EdgeDto> edgeDtos = new ArrayList<>();
        for (EdgeEntity edge : edges) {
            EdgeDto edgeDto = new EdgeDto(
                    edge.getId(),
                    edge.getSourceNode().getId().toString(),
                    edge.getTargetNode().getId().toString(),
                    edge.getWeight()
            );
            edgeDtos.add(edgeDto);
        }

        hierarchyService.calculateHierarchy(graphId, nodeDtos, edgeDtos);

        clusteredGraph.put("nodes", nodeDtos);
        clusteredGraph.put("edges", edgeDtos);

        return clusteredGraph;
    }
}

