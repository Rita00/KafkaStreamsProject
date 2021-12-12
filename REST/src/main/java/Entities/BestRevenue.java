package Entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BestRevenue {
    @Id
    private String primary_key;

    private long manager_id;

    private double revenue;

    public BestRevenue() {
    }

    public BestRevenue(String primary_key, long manager_id, double revenue) {
        this.primary_key = primary_key;
        this.manager_id = manager_id;
        this.revenue = revenue;
    }

    public String getPrimary_key() {
        return primary_key;
    }

    public void setPrimary_key(String primary_key) {
        this.primary_key = primary_key;
    }

    public long getManager_id() {
        return manager_id;
    }

    public void setManager_id(long manager_id) {
        this.manager_id = manager_id;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
