package Entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@SuppressWarnings("unused")
@Entity
public class WindowedCreditPerClient {
    @Id
    private String primary_key;

    private long client_id;

    private double total_credit_lastmonth;

    public WindowedCreditPerClient() {
    }

    public WindowedCreditPerClient(String primary_key, long client_id, double total_credit_lastmonth) {
        this.primary_key = primary_key;
        this.client_id = client_id;
        this.total_credit_lastmonth = total_credit_lastmonth;
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

    public double getTotal_credit_lastmonth() {
        return total_credit_lastmonth;
    }

    public void setTotal_credit_lastmonth(double total_credit_lastmonth) {
        this.total_credit_lastmonth = total_credit_lastmonth;
    }
}
