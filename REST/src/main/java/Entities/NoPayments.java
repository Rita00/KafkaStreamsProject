package Entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@SuppressWarnings("unused")
@Entity
public class NoPayments {
    @Id
    private long client_id;

    private boolean paid;

    public NoPayments() {
    }

    public NoPayments(long client_id, boolean paid) {
        this.client_id = client_id;
        this.paid = paid;
    }

    public long getClient_id() {
        return client_id;
    }

    public void setClient_id(long client_id) {
        this.client_id = client_id;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
