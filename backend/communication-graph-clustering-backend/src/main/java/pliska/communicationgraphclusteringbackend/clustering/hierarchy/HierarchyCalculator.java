package pliska.communicationgraphclusteringbackend.clustering.hierarchy;

import pliska.communicationgraphclusteringbackend.db.graph.EdgeDto;
import pliska.communicationgraphclusteringbackend.db.graph.NodeDto;

import java.util.List;

public interface HierarchyCalculator {

    public Double calculateGlobalHierarchy(Long graphId,NodeDto node, List<NodeDto> nodes, List<EdgeDto> edges);

}
