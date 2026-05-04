package pliska.communicationgraphclusteringbackend.db.graph;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NodeRepository extends JpaRepository<NodeEntity, Long> {

    @Query("SELECT n FROM NodeEntity n WHERE n.graph.id = :graphId")
    List<NodeEntity> findAllNodesByGraphId(@Param("graphId") Long graphId);

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN TRUE ELSE FALSE END FROM NodeEntity n WHERE n.graph.id = :graphId AND n.person.id = :personId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    boolean existsByGraphAndPerson(@Param("graphId") Long graphId, @Param("personId") Long personId);

    @Query("SELECT MIN(n.hierarchyLevelGlobal), MAX(n.hierarchyLevelGlobal) FROM NodeEntity n WHERE n.graph.id = :graphId")
    Object[] findMinAndMaxHierarchyLevelGlobalByGraphId(@Param("graphId") Long graphId);



}
