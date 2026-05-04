package pliska.communicationgraphclusteringbackend.db.graph;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "graph")
public class GraphEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;


    @Column(name = "algorithm")
    private String alogrithm;

    @Column(name = "source_id", nullable = false)
    private String sourceId;

    @Column(name = "llm")
    private boolean llm;

    @Column(name ="weightCalcFormula")
    private WeightFormula weightCalcFormula;


    public GraphEntity() {
    }

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private java.time.Instant createdAt;

    public GraphEntity(Long id, String alogrithm, String sourceId, boolean llm, Instant createdAt, WeightFormula weightCalcFormula) {
        this.id = id;
        this.alogrithm = alogrithm;
        this.sourceId = sourceId;
        this.llm = llm;
        this.createdAt = createdAt;
        this.weightCalcFormula = WeightFormula.valueOf(String.valueOf(weightCalcFormula));
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlogrithm() {

        return alogrithm;
    }

    public void setAlogrithm(String alogrithm) {

        this.alogrithm = alogrithm;
    }

    public String getSourceId() {

        return sourceId;
    }

    public void setSourceId(String sourceId) {

        this.sourceId = sourceId;
    }

    public boolean isLlm() {

        return llm;
    }

    public void setLlm(boolean llm) {

        this.llm = llm;
    }

    public Instant getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {

        this.createdAt = createdAt;
    }

    public void setWeightCalcFormula(WeightFormula weightCalcFormula) {
        this.weightCalcFormula = weightCalcFormula;
    }

    public WeightFormula getWeightCalcFormula() {
        return weightCalcFormula;
    }
}
