package pliska.communicationgraphclusteringbackend.db.graph;

import jakarta.persistence.Column;

public class NodeDto {
    Long nodeId;
    String firstName;
    String lastName;
    String email;
    String clusterId;

    public NodeDto(Long nodeId, String firstName, String lastName, String email, String clusterId) {
        this.nodeId = nodeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.clusterId = clusterId;
    }

    public NodeDto() {}

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }
}
