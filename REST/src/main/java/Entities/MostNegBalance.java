package Entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MostNegBalance {
    @Id
    private Long aggregate;

    private Long value;

    public MostNegBalance() {
    }

    public MostNegBalance(Long aggregate, Long value) {
        this.aggregate = aggregate;
        this.value = value;
    }

    public Long getAggregate() {
        return aggregate;
    }

    public void setAggregate(Long aggregate) {
        this.aggregate = aggregate;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
