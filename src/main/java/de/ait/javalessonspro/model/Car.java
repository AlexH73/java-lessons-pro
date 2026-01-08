package de.ait.javalessonspro.model;

public class Car {
    private Long id;
    private String brand;
    private String model;
    private int year;
    private int mileage;
    private int price;
    private String status;

    public Car(Long id, String brand, String model, int year, int mileage, int price, String status) {
        this.brand = brand;
        this.id = id;
        this.mileage = mileage;
        this.model = model;
        this.price = price;
        this.status = status;
        this.year = year;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

}
