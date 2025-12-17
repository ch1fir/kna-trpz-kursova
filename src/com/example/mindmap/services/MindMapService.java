package com.example.mindmap.services;

import com.example.mindmap.db.JdbcConnectionProvider;
import com.example.mindmap.entities.*;
import com.example.mindmap.repositories.JdbcCategoryRepository;
import com.example.mindmap.repositories.JdbcMapElementRepository;
import com.example.mindmap.repositories.JdbcMapStrokeRepository;
import com.example.mindmap.repositories.JdbcMindMapRepository;
import com.example.mindmap.tools.drawing.DrawingStroke;

import java.util.List;

public class MindMapService {

    private final JdbcMindMapRepository mapRepository;
    private final JdbcMapElementRepository elementRepository;
    private final JdbcMapStrokeRepository strokeRepository;
    private final JdbcCategoryRepository categoryRepository;

    // старий конструктор лишаємо
    public MindMapService(JdbcMindMapRepository mapRepository,
                          JdbcMapElementRepository elementRepository) {
        this(mapRepository, elementRepository,
                new JdbcMapStrokeRepository(new JdbcConnectionProvider()),
                new JdbcCategoryRepository(new JdbcConnectionProvider()));
    }

    public MindMapService(JdbcMindMapRepository mapRepository,
                          JdbcMapElementRepository elementRepository,
                          JdbcMapStrokeRepository strokeRepository) {
        this(mapRepository, elementRepository,
                strokeRepository,
                new JdbcCategoryRepository(new JdbcConnectionProvider()));
    }

    public MindMapService(JdbcMindMapRepository mapRepository,
                          JdbcMapElementRepository elementRepository,
                          JdbcMapStrokeRepository strokeRepository,
                          JdbcCategoryRepository categoryRepository) {
        this.mapRepository = mapRepository;
        this.elementRepository = elementRepository;
        this.strokeRepository = strokeRepository;
        this.categoryRepository = categoryRepository;
    }

    // --- Мапи ---
    public MindMap createMap(String title, User owner) {
        MindMap map = new MindMap();
        map.setTitle(title);
        map.setOwner(owner);
        mapRepository.save(map);
        return map;
    }

    public void updateMap(MindMap map) {
        mapRepository.save(map);
    }

    public List<MindMap> getMapsByUser(User user) {
        return mapRepository.getAllByUser(user);
    }

    // --- Категорії ---
    public List<Category> getCategoriesByUser(User user) {
        return categoryRepository.getAllByUser(user);
    }

    public Category createCategory(User user, String title, String colorHex) {
        Category c = new Category();
        c.setTitle(title);
        c.setColor(colorHex);
        c.setOwner(user);
        return categoryRepository.save(c);
    }

    // --- Елементи ---
    public List<MapElement> getElementsForMap(MindMap map) {
        return elementRepository.findByMapId(map.getId());
    }

    public MapElement addTextNode(MindMap map, float x, float y, String text) {
        TextNode node = new TextNode(x, y, text);
        elementRepository.save(node, map.getId());
        return node;
    }

    public MapElement addImageNode(MindMap map, float x, float y, String imagePath) {
        ImageNode node = new ImageNode(x, y, imagePath);
        elementRepository.save(node, map.getId());
        return node;
    }

    public MapElement addImageNode(MindMap map, float x, float y, String imagePath, int width, int height) {
        ImageNode node = new ImageNode(x, y, imagePath);
        node.setWidthPx(width);
        node.setHeightPx(height);
        elementRepository.save(node, map.getId());
        return node;
    }

    public void updateElement(MapElement element) {
        elementRepository.update(element);
    }

    public void deleteElement(MapElement element) {
        if (element.getId() != 0) elementRepository.delete(element.getId());
    }

    // --- Strokes ---
    public List<DrawingStroke> getStrokesForMap(MindMap map) {
        return strokeRepository.findByMapId(map.getId());
    }

    public void saveStroke(MindMap map, DrawingStroke stroke) {
        strokeRepository.save(map.getId(), stroke);
    }

    public void deleteStroke(DrawingStroke stroke) {
        strokeRepository.deleteById(stroke.getId());
    }
}
