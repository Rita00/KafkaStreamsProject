package Entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@SuppressWarnings("unused")
@Entity
public class WindowedCreditPerClient {
    @Id
    private long client_id;

    private double total_credits_lastmonth;

    public WindowedCreditPerClient() {
    }

    public WindowedCreditPerClient(long client_id, double total_credits_lastmonth) {
        this.client_id = client_id;
        this.total_credits_lastmonth = total_credits_lastmonth;
    }

    public long getClient_id() {
        return client_id;
    }

    public void setClient_id(long client_id) {
        this.client_id = client_id;
    }

    public double getTotal_credits_lastmonth() {
        return total_credits_lastmonth;
    }

    public void setTotal_credits_lastmonth(double total_credits_lastmonth) {
        this.total_credits_lastmonth = total_credits_lastmonth;
    }
}
