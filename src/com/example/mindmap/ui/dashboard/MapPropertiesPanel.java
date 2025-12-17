package com.example.mindmap.ui.dashboard;

import com.example.mindmap.entities.Category;
import com.example.mindmap.entities.MindMap;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MapPropertiesPanel extends JPanel {

    private DashboardMediator mediator;

    private final JTextField titleField = new JTextField(18);
    private final JTextArea descriptionArea = new JTextArea(6, 18);
    private final JButton saveMetaBtn = new JButton("Save");

    private final JToggleButton favBtn = new JToggleButton("☆ Important");
    private final JComboBox<Category> categoryBox = new JComboBox<>();
    private final JButton newCategoryBtn = new JButton("New category");

    private boolean internalUpdate = false;

    private final Category NO_CATEGORY;

    public MapPropertiesPanel() {
        super(new BorderLayout());

        NO_CATEGORY = new Category();
        NO_CATEGORY.setId(0);
        NO_CATEGORY.setTitle("No category");
        NO_CATEGORY.setColor("#B0B0B0");

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        card.setBackground(Color.WHITE);

        JLabel header = new JLabel("Map properties");
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Title
        JLabel titleLbl = new JLabel("Title:");
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Description
        JLabel descLbl = new JLabel("Description:");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);

        // Save meta button
        saveMetaBtn.setFocusPainted(false);
        saveMetaBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveMetaBtn.addActionListener(e -> {
            if (internalUpdate) return;
            if (mediator != null) mediator.onMetadataSaved(titleField.getText(), descriptionArea.getText());
        });

        // Favorite
        styleToggle(favBtn);
        favBtn.addActionListener(e -> {
            if (internalUpdate) return;
            if (mediator != null) mediator.onFavoriteToggled(favBtn.isSelected());
        });

        // Category combobox rendering (with colored dot)
        categoryBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category c) {
                    lbl.setText(c.getTitle());
                    Color col = ColorUtil.fromHex(c.getColor(), new Color(140, 140, 140));
                    lbl.setIcon(new DotIcon(col, 10));
                }
                return lbl;
            }
        });

        categoryBox.addActionListener(e -> {
            if (internalUpdate) return;
            Category selected = (Category) categoryBox.getSelectedItem();
            if (selected != null && selected.getId() == 0) selected = null;
            if (mediator != null) mediator.onCategorySelected(selected);
        });

        // Create category dialog
        newCategoryBtn.setFocusPainted(false);
        newCategoryBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        newCategoryBtn.addActionListener(e -> {
            if (mediator == null) return;

            JTextField name = new JTextField(18);
            int ok = JOptionPane.showConfirmDialog(this, name, "Category name", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;

            String title = name.getText().trim();
            if (title.isBlank()) {
                JOptionPane.showMessageDialog(this, "Category title is required");
                return;
            }

            Color chosen = JColorChooser.showDialog(this, "Choose category color", new Color(88, 101, 242));
            if (chosen == null) return;

            mediator.onCreateCategoryRequested(title, chosen);
        });

        // Layout
        card.add(header);
        card.add(Box.createVerticalStrut(10));

        card.add(titleLbl);
        card.add(titleField);
        card.add(Box.createVerticalStrut(8));

        card.add(descLbl);
        card.add(descScroll);
        card.add(Box.createVerticalStrut(8));

        JPanel saveRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        saveRow.setOpaque(false);
        saveRow.add(saveMetaBtn);
        card.add(saveRow);

        card.add(Box.createVerticalStrut(12));
        card.add(favBtn);
        card.add(Box.createVerticalStrut(12));

        JPanel catRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        catRow.setOpaque(false);
        catRow.add(new JLabel("Category:"));
        catRow.add(categoryBox);
        catRow.add(newCategoryBtn);

        card.add(catRow);

        add(card, BorderLayout.NORTH);
        setBackground(new Color(245, 247, 252));

        // default state: no map selected
        showMap(null, List.of());
    }

    public void setMediator(DashboardMediator mediator) {
        this.mediator = mediator;
    }

    public void setCategories(List<Category> categories) {
        internalUpdate = true;

        categoryBox.removeAllItems();
        categoryBox.addItem(NO_CATEGORY);
        for (Category c : categories) categoryBox.addItem(c);

        internalUpdate = false;
    }

    public void showMap(MindMap map, List<Category> categories) {
        internalUpdate = true;

        if (map == null) {
            titleField.setText("");
            descriptionArea.setText("");

            titleField.setEnabled(false);
            descriptionArea.setEnabled(false);
            saveMetaBtn.setEnabled(false);

            favBtn.setEnabled(false);
            favBtn.setSelected(false);
            favBtn.setText("☆ Important");

            categoryBox.setEnabled(false);
            newCategoryBtn.setEnabled(false);

            setCategories(categories);
            categoryBox.setSelectedItem(NO_CATEGORY);

            internalUpdate = false;
            return;
        }

        titleField.setEnabled(true);
        descriptionArea.setEnabled(true);
        saveMetaBtn.setEnabled(true);

        titleField.setText(map.getTitle() == null ? "" : map.getTitle());
        descriptionArea.setText(map.getDescription() == null ? "" : map.getDescription());

        favBtn.setEnabled(true);
        favBtn.setSelected(map.isFavorite());
        favBtn.setText(map.isFavorite() ? "★ Important" : "☆ Important");

        categoryBox.setEnabled(true);
        newCategoryBtn.setEnabled(true);

        setCategories(categories);

        Category current = map.getCategory();
        if (current == null) {
            categoryBox.setSelectedItem(NO_CATEGORY);
        } else {
            Category found = null;
            for (int i = 0; i < categoryBox.getItemCount(); i++) {
                Category item = categoryBox.getItemAt(i);
                if (item != null && item.getId() == current.getId()) {
                    found = item;
                    break;
                }
            }
            categoryBox.setSelectedItem(found != null ? found : NO_CATEGORY);
        }

        internalUpdate = false;
    }

    private static void styleToggle(AbstractButton b) {
        b.setBackground(new Color(245, 245, 245));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
