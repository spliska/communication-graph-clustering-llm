package pliska.communicationgraphclusteringbackend.db.graph;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EdgeRepository extends JpaRepository<EdgeEntity, Long> {

    @Query("SELECT CASE WHEN COUNT(edge) > 0 THEN TRUE ELSE FALSE END FROM EdgeEntity edge WHERE edge.graph.id = :graphId AND edge.sourceNode.id = :sourceNodeId AND edge.targetNode.id = :targetNodeId")
    boolean existsByGraphAndNodes(
            @Param("graphId") Long graphId,
            @Param("sourceNodeId") Long sourceNodeId,
            @Param("targetNodeId") Long targetNodeId
    );

    @Query("SELECT edge FROM EdgeEntity edge WHERE edge.graph.id = :graphId")
    List<EdgeEntity> findAllEdgesByGraphId(@Param("graphId") Long graphId);

    @Query("SELECT MAX(edge.interactions) FROM EdgeEntity edge WHERE edge.graph.id = :graphId")
    Integer findMaxInteractionsByGraphId(@Param("graphId") Long graphId);
}
