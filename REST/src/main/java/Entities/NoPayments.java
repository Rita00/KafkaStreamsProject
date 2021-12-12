package Entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@SuppressWarnings("unused")
@Entity
public class NoPayments {
    @Id
    private long client_id;

    private long number_payments_last_twomonths;

    public NoPayments() {
    }

    public NoPayments(int client_id, int number_payments_last_twomonths) {
        this.client_id = client_id;
        this.number_payments_last_twomonths = number_payments_last_twomonths;
    }

    public long getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    public long getNumber_payments_last_twomonths() {
        return number_payments_last_twomonths;
    }

    public void setNumber_payments_last_twomonths(long number_payments_last_twomonths) {
        this.number_payments_last_twomonths = number_payments_last_twomonths;
    }
}
