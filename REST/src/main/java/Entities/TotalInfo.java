package Entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class TotalInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private float totalCredits;

    private float totalPayments;

    private float totalBalance;

    public TotalInfo() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(float totalCredits) {
        this.totalCredits = totalCredits;
    }

    public float getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(float totalPayments) {
        this.totalPayments = totalPayments;
    }

    public float getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(float totalBalance) {
        this.totalBalance = totalBalance;
    }
}
