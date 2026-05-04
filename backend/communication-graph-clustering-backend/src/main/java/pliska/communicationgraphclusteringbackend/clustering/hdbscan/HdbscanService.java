package pliska.communicationgraphclusteringbackend.clustering.hdbscan;

import org.springframework.stereotype.Service;
import pliska.communicationgraphclusteringbackend.clustering.ClusteringService;
import pliska.communicationgraphclusteringbackend.clustering.hierarchy.HierarchyService;
import pliska.communicationgraphclusteringbackend.db.graph.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class HdbscanService implements ClusteringService {

    private final EdgeRepository edgeRepository;
    private final NodeRepository nodeRepository;
    private final HierarchyService hierarchyService;

    public HdbscanService(EdgeRepository edgeRepository, NodeRepository nodeRepository, GraphRepository graphRepository, HierarchyService hierarchyService) {
        this.edgeRepository = edgeRepository;
        this.nodeRepository = nodeRepository;
        this.hierarchyService = hierarchyService;
    }

    @Override
    public Map<String, Object> clusterGraph(Long graphId, Map<String, Object> parameters) {
        List<NodeEntity> nodes = nodeRepository.findAllNodesByGraphId(graphId);
        List<EdgeEntity> edges = edgeRepository.findAllEdgesByGraphId(graphId);

        nodes.forEach(node -> node.setClusterId(null));
        nodeRepository.saveAll(nodes);

        Integer minPoints = (Integer) parameters.get("minPoints");
        Double densityThreshold = (Double) parameters.get("densityThreshold");

        Hdbscan hdbscan = new Hdbscan(nodes, edges);
        Map<Long, Double> coreDistances = hdbscan.calculateCoreDistances(minPoints);
        List<EdgeEntity> edgesWithMRD = hdbscan.computeMutualReachabilityDistances(coreDistances);
        List<EdgeEntity> mstEdges = hdbscan.constructMST(edgesWithMRD);
        List<Map<Integer, List<Long>>> clusters = hdbscan.extractClusters(mstEdges, densityThreshold);

        ArrayList<NodeDto> nodeDtos = new ArrayList<>();
        for (Map<Integer, List<Long>> cluster : clusters) {
            for (Map.Entry<Integer, List<Long>> entry : cluster.entrySet()) {
                Integer clusterId = entry.getKey();
                List<Long> nodeIds = entry.getValue();

                for (Long nodeId : nodeIds) {
                    NodeEntity nodeEntity = nodes.stream()
                            .filter(node -> node.getId().equals(nodeId))
                            .findFirst()
                            .orElse(null);
                    if (nodeEntity != null) {
                        nodeEntity.setClusterId(clusterId);

                        NodeDto nodeDto = new NodeDto(
                                nodeEntity.getId(),
                                nodeEntity.getPerson().getFirstName(),
                                nodeEntity.getPerson().getLastName(),
                                nodeEntity.getPerson().getEmail(),
                                clusterId.toString()
                        );
                        nodeDtos.add(nodeDto);
                    }
                }
            }
        }

        nodeRepository.saveAll(nodes);

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

        Map<String, Object> clusteredGraph = new HashMap<>();
        clusteredGraph.put("nodes", nodeDtos);
        clusteredGraph.put("edges", edgeDtos);

        return clusteredGraph;
    }
}