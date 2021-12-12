package Entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@SuppressWarnings("unused")
@Entity
public class TotalResults {
    @Id
    private String aggregate;

    private double value;

    public TotalResults() {
    }

    public TotalResults(String aggregate, double value) {
        this.aggregate = aggregate;
        this.value = value;
    }

    public String getAggregate() {
        return aggregate;
    }

    public void setAggregate(String aggregate) {
        this.aggregate = aggregate;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
