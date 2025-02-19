package com.example.virtualpet2;

public class Food {
    private String name;
    private double price;
    private double expGain;

    public Food() {
    }

    public Food(String name, double price, double expGain) {
        this.name = name;
        this.price = price;
        this.expGain = expGain;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getExpGain() {
        return expGain;
    }
}
