package com.example.mindmap.entities;

public class Category {
    private int id;
    private String title;
    private String color; // "#RRGGBB"
    private User owner;

    public Category() {}

    public Category(int id, String title, String color, User owner) {
        this.id = id;
        this.title = title;
        this.color = color;
        this.owner = owner;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    @Override
    public String toString() {
        return title; // щоб ComboBox показував назву
    }
}
