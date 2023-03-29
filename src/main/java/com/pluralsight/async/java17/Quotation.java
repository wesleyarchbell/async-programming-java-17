package com.pluralsight.async.java17;

public class Quotation {
    private String server;
    private int amount;

    public Quotation(String server, int amount) {
        this.server = server;
        this.amount = amount;
    }

    public String getServer() {
        return server;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Quotation{" +
                "server='" + server + '\'' +
                ", amount=" + amount +
                '}';
    }
}
