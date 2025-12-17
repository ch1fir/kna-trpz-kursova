package com.example.mindmap.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MindMap {
    private int id;
    private String title;
    private String description;          // нове поле
    private LocalDateTime createdAt;
    private boolean favorite;
    private String previewImage;

    private User owner;
    private Category category;
    private final List<MapElement> elements = new ArrayList<>();

    public MindMap() {
        this.createdAt = LocalDateTime.now();
    }

    public MindMap(int id, String title, User owner) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsFavorite() {
        this.favorite = true;
    }

    public void unmarkFavorite() {
        this.favorite = false;
    }

    public void changeCategory(Category category) {
        this.category = category;
    }

    // Для сумісності з репозиторієм/медіатором
    public void setCategory(Category category) {
        changeCategory(category);
    }

    public void addElement(MapElement element) {
        elements.add(element);
    }

    public void removeElement(MapElement element) {
        elements.remove(element);
    }

    // getters / setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public String getPreviewImage() { return previewImage; }
    public void setPreviewImage(String previewImage) { this.previewImage = previewImage; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Category getCategory() { return category; }

    public List<MapElement> getElements() { return elements; }

    @Override
    public String toString() {
        return title;
    }
}
