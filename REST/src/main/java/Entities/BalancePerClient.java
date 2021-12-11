package Entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class BalancePerClient {
    @Id
    private int client_id;

    private double current_balance;

    public BalancePerClient() {
    }

    public BalancePerClient(int client_id, double current_balance) {
        this.client_id = client_id;
        this.current_balance = current_balance;
    }

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    public double getCurrent_balance() {
        return current_balance;
    }

    public void setCurrent_balance(double current_balance) {
        this.current_balance = current_balance;
    }
}
