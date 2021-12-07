package data;

public class Currency {
    private int id;

    private String name;

    private double exchangeRate;

    public Currency() {}

    public Currency(String name, Float exchangeRate) {
        this.name = name;
        this.exchangeRate = exchangeRate;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
