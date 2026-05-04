package pliska.communicationgraphclusteringbackend.db.person;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<PersonEntity, Long> {
    boolean existsByEmail(String email);

    boolean existsByFirstNameAndLastName(String firstName, String lastName);

    PersonEntity findByEmail(String email);

    List<PersonEntity> findAllByEmailIn(List<String> emails);


}
