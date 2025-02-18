package com.example.virtualpet2;

public class Food {
    private String name;
    private double price;
    private double expGain;
    private int photoResId;

    public Food() {
    }

    public Food(String name, double price, double expGain, int photoResId) {
        this.name = name;
        this.price = price;
        this.expGain = expGain;
        this.photoResId = photoResId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getPhotoResId() {
        return photoResId;
    }

    public double getExpGain() {
        return expGain;
    }
}
