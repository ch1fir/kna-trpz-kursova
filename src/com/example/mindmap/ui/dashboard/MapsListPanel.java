package com.example.mindmap.ui.dashboard;

import com.example.mindmap.entities.MindMap;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MapsListPanel extends JPanel {

    private DashboardMediator mediator;

    private final DefaultListModel<MindMap> model = new DefaultListModel<>();
    private final JList<MindMap> list = new JList<>(model);

    public MapsListPanel() {
        super(new BorderLayout());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new MindMapListCellRenderer());
        add(new JScrollPane(list), BorderLayout.CENTER);

        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            if (mediator != null) mediator.onMapSelected(list.getSelectedValue());
        });
    }

    public void setMediator(DashboardMediator mediator) {
        this.mediator = mediator;
    }

    public void setMaps(List<MindMap> maps) {
        model.clear();
        for (MindMap m : maps) model.addElement(m);
    }

    public JList<MindMap> getList() {
        return list;
    }

    public MindMap getSelected() {
        return list.getSelectedValue();
    }

    public void selectById(int id) {
        for (int i = 0; i < model.size(); i++) {
            MindMap m = model.get(i);
            if (m.getId() == id) {
                list.setSelectedIndex(i);
                list.ensureIndexIsVisible(i);
                break;
            }
        }
    }
}
