package Entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PaymentsPerClient {
    @Id
    private int client_id;

    private double total_payments;

    public PaymentsPerClient() {
    }

    public PaymentsPerClient(int client_id, double total_payments) {
        this.client_id = client_id;
        this.total_payments = total_payments;
    }

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    public double getTotal_payments() {
        return total_payments;
    }

    public void setTotal_payments(double total_payments) {
        this.total_payments = total_payments;
    }
}
