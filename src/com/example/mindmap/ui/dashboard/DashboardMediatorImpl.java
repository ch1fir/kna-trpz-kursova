package com.example.mindmap.ui.dashboard;

import com.example.mindmap.entities.Category;
import com.example.mindmap.entities.MindMap;
import com.example.mindmap.entities.User;
import com.example.mindmap.services.MindMapService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardMediatorImpl implements DashboardMediator {

    private final User user;
    private final MindMapService service;

    private MapsListPanel mapsListPanel;
    private MapPropertiesPanel propertiesPanel;
    @SuppressWarnings("unused")
    private SearchSortPanel searchSortPanel;

    private List<MindMap> allMaps = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    private MindMap selected;
    private String search = "";
    private SortMode sortMode = SortMode.BY_CATEGORY;

    public DashboardMediatorImpl(User user, MindMapService service) {
        this.user = user;
        this.service = service;
    }

    public void register(MapsListPanel listPanel, MapPropertiesPanel propsPanel, SearchSortPanel filterPanel) {
        this.mapsListPanel = listPanel;
        this.propertiesPanel = propsPanel;
        this.searchSortPanel = filterPanel;

        listPanel.setMediator(this);
        propsPanel.setMediator(this);
        filterPanel.setMediator(this);

        refresh();
    }

    @Override
    public void refresh() {
        int selectedId = (selected != null) ? selected.getId() : -1;

        categories = service.getCategoriesByUser(user);
        allMaps = service.getMapsByUser(user);

        // Пере-знайдемо selected після перечитування з БД
        if (selectedId != -1) {
            selected = allMaps.stream().filter(m -> m.getId() == selectedId).findFirst().orElse(null);
        }

        applyFilterAndSort();
        propertiesPanel.showMap(selected, categories);
    }

    @Override
    public void onMapSelected(MindMap map) {
        selected = map;
        propertiesPanel.showMap(selected, categories);
    }

    @Override
    public void onFavoriteToggled(boolean favorite) {
        if (selected == null) return;

        if (favorite) selected.markAsFavorite();
        else selected.unmarkFavorite();

        service.updateMap(selected);

        int keepId = selected.getId();
        refresh();
        mapsListPanel.selectById(keepId);
    }

    @Override
    public void onCategorySelected(Category category) {
        if (selected == null) return;

        selected.setCategory(category);
        service.updateMap(selected);

        int keepId = selected.getId();
        refresh();
        mapsListPanel.selectById(keepId);
    }

    @Override
    public void onCreateCategoryRequested(String title, Color color) {
        String hex = ColorUtil.toHex(color);
        Category created = service.createCategory(user, title, hex);

        if (selected != null) {
            selected.setCategory(created);
            service.updateMap(selected);
        }

        int keepId = (selected != null ? selected.getId() : -1);
        refresh();
        if (keepId != -1) mapsListPanel.selectById(keepId);
    }

    @Override
    public void onMetadataSaved(String newTitle, String newDescription) {
        if (selected == null) return;

        String t = (newTitle == null) ? "" : newTitle.trim();
        if (t.isBlank()) {
            JOptionPane.showMessageDialog(propertiesPanel, "Назва мапи обов'язкова");
            return;
        }

        selected.setTitle(t);
        selected.setDescription(newDescription == null ? "" : newDescription.trim());

        service.updateMap(selected);

        int keepId = selected.getId();
        refresh();
        mapsListPanel.selectById(keepId);
    }

    @Override
    public void onSearchChanged(String query) {
        search = (query == null) ? "" : query.trim();
        applyFilterAndSort();
    }

    @Override
    public void onSortModeChanged(SortMode mode) {
        sortMode = (mode == null) ? SortMode.BY_CATEGORY : mode;
        applyFilterAndSort();
    }

    private void applyFilterAndSort() {
        String q = search.toLowerCase(Locale.ROOT);

        List<MindMap> filtered = allMaps.stream()
                .filter(m -> q.isBlank() || (m.getTitle() != null && m.getTitle().toLowerCase(Locale.ROOT).contains(q)))
                .collect(Collectors.toList());

        Comparator<MindMap> byName =
                Comparator.comparing(m -> m.getTitle() == null ? "" : m.getTitle(), String.CASE_INSENSITIVE_ORDER);

        Comparator<MindMap> byCategory =
                Comparator.comparing((MindMap m) -> {
                            if (m.getCategory() == null || m.getCategory().getTitle() == null) return "~~~";
                            return m.getCategory().getTitle().toLowerCase(Locale.ROOT);
                        })
                        .thenComparing(byName);

        Comparator<MindMap> base = (sortMode == SortMode.BY_NAME) ? byName : byCategory;

        filtered.sort(Comparator.comparing(MindMap::isFavorite).reversed().thenComparing(base));

        mapsListPanel.setMaps(filtered);
    }
}
