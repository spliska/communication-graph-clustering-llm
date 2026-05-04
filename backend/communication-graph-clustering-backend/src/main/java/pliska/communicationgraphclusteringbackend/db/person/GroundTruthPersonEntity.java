package pliska.communicationgraphclusteringbackend.db.person;

import jakarta.persistence.*;

@Entity
@Table(name = "ground_truth_person")
public class GroundTruthPersonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id")
    private GroundTruthSource source;

    private String firstName;
    private String lastName;
    private String title;
    private String department;
    private String  longDepartment;

    public GroundTruthPersonEntity() {
    }

    public GroundTruthPersonEntity(Long id, GroundTruthSource source, String firstName, String lastName, String title, String department) {
        this.id = id;
        this.source = source;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.department = department;
    }

    public GroundTruthPersonEntity(Long id, GroundTruthSource source, String firstName, String lastName, String title, String department,String longDepartment) {
        this.id = id;
        this.source = source;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.department = department;
        this.longDepartment = longDepartment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GroundTruthSource getSource() {
        return source;
    }

    public void setSource(GroundTruthSource source) {
        this.source = source;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLongDepartment() {
        return longDepartment;
    }

    public void setLongDepartment(String longDepartment) {
        this.longDepartment = longDepartment;
    }

    public Integer getTitleAsLevel(){
       return TitleToLevelParser.parseTitleToLevel(this.title);
    }
}
