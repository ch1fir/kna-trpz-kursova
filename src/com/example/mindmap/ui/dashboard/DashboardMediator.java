package com.example.mindmap.ui.dashboard;

import com.example.mindmap.entities.Category;
import com.example.mindmap.entities.MindMap;

import java.awt.*;

public interface DashboardMediator {
    void onMapSelected(MindMap map);

    void onFavoriteToggled(boolean favorite);
    void onCategorySelected(Category category);

    void onCreateCategoryRequested(String title, Color color);

    // збереження назви/опису
    void onMetadataSaved(String newTitle, String newDescription);

    void onSearchChanged(String query);
    void onSortModeChanged(SortMode mode);

    void refresh();
}
