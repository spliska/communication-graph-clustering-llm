package pliska.communicationgraphclusteringbackend.db.graph;

import jakarta.persistence.*;
import pliska.communicationgraphclusteringbackend.db.person.PersonEntity;

@Entity
@Table(
        name = "node",
        uniqueConstraints = @UniqueConstraint(columnNames = {"graph_id", "person_id"}))
public class NodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "graph_id")
    private GraphEntity graph;

    @ManyToOne(optional = false)
    @JoinColumn(name = "person_id")
    private PersonEntity person;

    @Column(name = "cluster_id")
    private Integer clusterId;
    
    @Column(name = "hierarchy_level_global")
    private Double hierarchyLevelGlobal;

    @Column(name = "hierarchy_level_cluster")
    private Double hierarchyLevelCluster;
    
    @Column(name = "department", nullable = true)
    private Integer department;
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

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }

    public Double getHierarchyLevelGlobal() {
        return hierarchyLevelGlobal;
    }

    public void setHierarchyLevelGlobal(double hierarchyLevelGlobal) {
        this.hierarchyLevelGlobal = hierarchyLevelGlobal;
    }

    public Double getHierarchyLevelCluster() {
        return hierarchyLevelCluster;
    }

    public void setHierarchyLevelCluster(double hierarchyLevelCluster) {
        this.hierarchyLevelCluster = hierarchyLevelCluster;
    }

    public Integer getDepartment() {
        return department;
    }

    public void setDepartment(Integer department) {
        this.department = department;
    }
    
}
