package pliska.communicationgraphclusteringbackend.api;

import pliska.communicationgraphclusteringbackend.db.graph.EdgeEntity;
import pliska.communicationgraphclusteringbackend.db.graph.EdgeRepository;
import pliska.communicationgraphclusteringbackend.db.graph.GraphEntity;
import pliska.communicationgraphclusteringbackend.db.graph.NodeEntity;

import java.util.HashMap;

public class GraphDto {
    GraphEntity graphEntity;
    HashMap<String, NodeEntity> nodes;
    HashMap<String, EdgeEntity> edges;

    public GraphEntity getGraphEntity() {
        return graphEntity;
    }

    public void setGraphEntity(GraphEntity graphEntity) {
        this.graphEntity = graphEntity;
    }

    public HashMap<String, NodeEntity> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<String, NodeEntity> nodes) {
        this.nodes = nodes;
    }

    public HashMap<String, EdgeEntity> getEdges() {
        return edges;
    }

    public void setEdges(HashMap<String, EdgeEntity> edges) {
        this.edges = edges;
    }
}
