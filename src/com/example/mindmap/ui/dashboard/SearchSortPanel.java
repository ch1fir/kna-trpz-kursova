package com.example.mindmap.ui.dashboard;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class SearchSortPanel extends JPanel {

    private DashboardMediator mediator;

    private final JTextField searchField = new JTextField(18);
    private final JComboBox<SortMode> sortBox = new JComboBox<>(SortMode.values());

    public SearchSortPanel() {
        super(new FlowLayout(FlowLayout.LEFT, 10, 6));

        add(new JLabel("Search:"));
        add(searchField);

        add(new JLabel("Sort:"));
        sortBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == SortMode.BY_NAME) setText("By name");
                if (value == SortMode.BY_CATEGORY) setText("By category");
                return this;
            }
        });
        sortBox.setSelectedItem(SortMode.BY_CATEGORY);
        add(sortBox);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void fire() {
                if (mediator != null) mediator.onSearchChanged(searchField.getText());
            }
            @Override public void insertUpdate(DocumentEvent e) { fire(); }
            @Override public void removeUpdate(DocumentEvent e) { fire(); }
            @Override public void changedUpdate(DocumentEvent e) { fire(); }
        });

        sortBox.addActionListener(e -> {
            if (mediator != null) mediator.onSortModeChanged((SortMode) sortBox.getSelectedItem());
        });
    }

    public void setMediator(DashboardMediator mediator) {
        this.mediator = mediator;
    }
}
