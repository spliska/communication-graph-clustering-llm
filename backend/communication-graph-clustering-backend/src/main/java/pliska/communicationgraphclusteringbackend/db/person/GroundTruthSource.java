package pliska.communicationgraphclusteringbackend.db.person;

import jakarta.persistence.*;

@Entity
@Table(name = "ground_truth_source")
public class GroundTruthSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public GroundTruthSource() {
    }

    public GroundTruthSource(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
