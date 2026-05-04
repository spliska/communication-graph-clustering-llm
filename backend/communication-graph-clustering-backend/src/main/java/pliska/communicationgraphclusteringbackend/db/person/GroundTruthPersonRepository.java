package pliska.communicationgraphclusteringbackend.db.person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pliska.communicationgraphclusteringbackend.db.person.GroundTruthPersonEntity;
import pliska.communicationgraphclusteringbackend.db.person.GroundTruthSource;

import java.util.List;

public interface GroundTruthPersonRepository extends JpaRepository<GroundTruthPersonEntity, Long> {

    @Query("SELECT gt FROM GroundTruthPersonEntity gt WHERE gt.source = :source")
    List<GroundTruthPersonEntity> findBySource(GroundTruthSource source);

    @Query("SELECT gt FROM GroundTruthPersonEntity gt WHERE gt.firstName = :firstName AND gt.lastName = :lastName AND gt.source = :source")
    GroundTruthPersonEntity findByFirstNameAndLastName(String firstName, String lastName, String source);


}

