package pliska.communicationgraphclusteringbackend.db.graph;

import jakarta.persistence.*;

public class EdgeDto {

    private Long id;
    private String sourceNodeId;
    private String targetNode;
    private Double weight;

    public EdgeDto(Long id, String sourceNodeId, String targetNode, Double weight) {
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.targetNode = targetNode;
        this.weight = weight;
    }

    public EdgeDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(String sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public String getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(String targetNode) {
        this.targetNode = targetNode;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
