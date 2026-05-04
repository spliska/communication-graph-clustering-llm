package pliska.communicationgraphclusteringbackend.clustering.hierarchy;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "node_hierarchy")
public class NodeHierarchyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long graphId;
    private Long nodeId;
    private Double globalHierarchyScore;
    private Double clusterHierarchyScore;

    public NodeHierarchyEntity() {
    }

    public NodeHierarchyEntity(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGraphId() {
        return graphId;
    }

    public void setGraphId(Long graphId) {
        this.graphId = graphId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Double getGlobalHierarchyScore() {
        return globalHierarchyScore;
    }

    public void setGlobalHierarchyScore(Double globalHierarchyScore) {
        this.globalHierarchyScore = globalHierarchyScore;
    }

    public Double getClusterHierarchyScore() {
        return clusterHierarchyScore;
    }

    public void setClusterHierarchyScore(Double clusterHierarchyScore) {
        this.clusterHierarchyScore = clusterHierarchyScore;
    }
}
