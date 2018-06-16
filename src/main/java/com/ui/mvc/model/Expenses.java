package com.ui.mvc.model;

public class Expenses {

    private Long id;
    private int month;
    private int year;
    private String type;
    private String description;
    private String date;
    private double amount;

    private Expenses(Long id, int month, int year, String type, String description, String date,  double amount) {
        this.id = id;
        this.month = month;
        this.year = year;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.date = date;
    }

    public static Expenses of(Long id, int month, int year, String type, String description, String date, double amount) {
        return new Expenses(id, month, year, type, description, date, amount);
    }

    public Long getId() {
        return id;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Expenses{" +
                "id=" + id +
                ", month=" + month +
                ", year=" + year +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", amount=" + amount +
                '}';
    }
}
