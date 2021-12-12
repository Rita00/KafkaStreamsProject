package Entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@SuppressWarnings("unused")
@Entity
public class CreditsPerClient {
    @Id
    private int client_id;

    private double total_credits;

    public CreditsPerClient() {
    }

    public CreditsPerClient(int client_id, double total_credits) {
        this.client_id = client_id;
        this.total_credits = total_credits;
    }

    public double getTotal_credits() {
        return total_credits;
    }

    public void setTotal_credits(double total_credits) {
        this.total_credits = total_credits;
    }

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }
}
