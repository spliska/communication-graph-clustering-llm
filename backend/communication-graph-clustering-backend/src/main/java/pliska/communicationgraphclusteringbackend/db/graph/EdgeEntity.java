package pliska.communicationgraphclusteringbackend.db.graph;

import jakarta.persistence.*;

@Entity
@Table(name = "edge")
public class EdgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="graph_id")
    private GraphEntity graph;

    @ManyToOne(optional=false)
    @JoinColumn(name="source_node_id")
    private NodeEntity sourceNode;

    @ManyToOne(optional=false)
    @JoinColumn(name="target_node_id")
    private NodeEntity targetNode;

    private Integer interactions;

    private Double weight;


    public EdgeEntity(Long id, GraphEntity graph, NodeEntity sourceNode, NodeEntity targetNode, Double weight, Integer interactions) {
        this.id = id;
        this.graph = graph;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.weight = weight;
        this.interactions = interactions;

    }

    public EdgeEntity(Long id, GraphEntity graph, NodeEntity sourceNode, NodeEntity targetNode, Integer interactions) {
        this.id = id;
        this.graph = graph;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.weight = 0.0;
        this.interactions = interactions;
    }

    public EdgeEntity() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GraphEntity getGraph() {
        return graph;
    }

    public void setGraph(GraphEntity graph) {
        this.graph = graph;
    }

    public NodeEntity getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(NodeEntity sourceNode) {
        this.sourceNode = sourceNode;
    }

    public NodeEntity getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(NodeEntity targetNode) {
        this.targetNode = targetNode;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public void setInteractions(Integer interactions){
        this.interactions=interactions;
    }

    public Integer getInteractions(){
        return this.interactions;
    }
}
