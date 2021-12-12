package Entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MostNegBalance {
    @Id
    private String primary_key;

    private long client_id;

    private double current_balance;

    public MostNegBalance() {
    }

    public MostNegBalance(String primary_key, long client_id, double current_balance) {
        this.primary_key = primary_key;
        this.client_id = client_id;
        this.current_balance = current_balance;
    }

    public String getPrimary_key() {
        return primary_key;
    }

    public void setPrimary_key(String primary_key) {
        this.primary_key = primary_key;
    }

    public long getClient_id() {
        return client_id;
    }

    public void setClient_id(long client_id) {
        this.client_id = client_id;
    }

    public double getCurrent_balance() {
        return current_balance;
    }

    public void setCurrent_balance(double current_balance) {
        this.current_balance = current_balance;
    }
}
