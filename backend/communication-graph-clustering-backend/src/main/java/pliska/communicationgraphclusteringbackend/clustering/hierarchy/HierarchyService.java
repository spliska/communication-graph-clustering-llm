package pliska.communicationgraphclusteringbackend.clustering.hierarchy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pliska.communicationgraphclusteringbackend.clustering.hierarchy.lllm.HierarchyCalculatorLlm;
import pliska.communicationgraphclusteringbackend.clustering.hierarchy.meta.HierarchyCalculatorMeta;
import pliska.communicationgraphclusteringbackend.db.graph.EdgeDto;
import pliska.communicationgraphclusteringbackend.db.graph.GraphRepository;
import pliska.communicationgraphclusteringbackend.db.graph.NodeDto;
import pliska.communicationgraphclusteringbackend.db.graph.NodeRepository;
import java.util.List;

import pliska.communicationgraphclusteringbackend.db.graph.NodeEntity;

@Service
public class HierarchyService {

    private final GraphRepository graphRepository;
    private final NodeRepository nodeRepository;
    @Autowired
    private final HierarchyCalculatorMeta hierarchyCalculatorMeta;
    @Autowired
    private final HierarchyCalculatorLlm hierarchyCalculatorLlm;

    public HierarchyService(GraphRepository graphRepository, NodeRepository nodeRepository, HierarchyCalculatorMeta hierarchyCalculatorMeta, HierarchyCalculatorLlm hierarchyCalculatorLlm) {
        this.graphRepository = graphRepository;
        this.nodeRepository = nodeRepository;
        this.hierarchyCalculatorMeta = hierarchyCalculatorMeta;
        this.hierarchyCalculatorLlm = hierarchyCalculatorLlm;
    }

    public void calculateHierarchy(Long graphId, List<NodeDto> nodes, List<EdgeDto> edges) {
        HierarchyCalculator hierarchyCalculator;
        if (graphRepository.findGraphById(graphId).isLlm()) {
            hierarchyCalculator = hierarchyCalculatorLlm;
        } else {
            hierarchyCalculator = hierarchyCalculatorMeta;
        }

        List<NodeEntity> nodeEntities = nodeRepository.findAllNodesByGraphId(graphId);

        for (NodeDto nodeDto : nodes) {
            NodeEntity nodeEntity = nodeEntities.stream()
                    .filter(entity -> entity.getId().equals(nodeDto.getNodeId()))
                    .findFirst()
                    .orElse(null);

            if (nodeEntity != null) {
                double globalHierarchyScore = hierarchyCalculator.calculateGlobalHierarchy(graphId,nodeDto, nodes, edges);
                nodeEntity.setHierarchyLevelGlobal(globalHierarchyScore);
            }
        }

        nodeRepository.saveAll(nodeEntities);
    }
}