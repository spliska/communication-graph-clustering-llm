package pliska.communicationgraphclusteringbackend.db.graph;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface GraphRepository extends JpaRepository<GraphEntity, Long> {

    @Query("SELECT g FROM GraphEntity g WHERE g.id = :graphId")
    GraphEntity findGraphById(@Param("graphId") Long graphId);

    @Query("SELECT g FROM GraphEntity g WHERE g.createdAt >= :date ORDER BY g.createdAt DESC")
    List<GraphEntity> findGraphsByCreationDate(@Param("date") Instant date);

    @Query("SELECT g FROM GraphEntity g WHERE g.alogrithm = :algorithm")
    List<GraphEntity> findGraphsByAlgorithm(@Param("algorithm") String algorithm);
}

