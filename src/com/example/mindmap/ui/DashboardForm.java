package com.example.mindmap.ui;

import com.example.mindmap.entities.MindMap;
import com.example.mindmap.entities.User;
import com.example.mindmap.services.AuthService;
import com.example.mindmap.services.MindMapService;
import com.example.mindmap.ui.dashboard.*;

import javax.swing.*;
import java.awt.*;

public class DashboardForm extends JFrame {

    private final User user;
    private final AuthService authService;
    private final MindMapService mindMapService;

    private final DashboardMediatorImpl mediator;

    private final MapsListPanel mapsListPanel = new MapsListPanel();
    private final MapPropertiesPanel propertiesPanel = new MapPropertiesPanel();
    private final SearchSortPanel searchSortPanel = new SearchSortPanel();

    public DashboardForm(User user, AuthService authService, MindMapService mindMapService) {
        super("Mind Map - Dashboard");
        this.user = user;
        this.authService = authService;
        this.mindMapService = mindMapService;

        this.mediator = new DashboardMediatorImpl(user, mindMapService);

        initComponents();
        mediator.register(mapsListPanel, propertiesPanel, searchSortPanel);
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(980, 520);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 252));
        add(mainPanel);

        // ===== Top bar =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        mainPanel.add(topBar, BorderLayout.NORTH);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel hi = new JLabel("Hi, " + user.getUsername());
        hi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hi.setForeground(new Color(120, 120, 145));

        JLabel title = new JLabel("Your mind maps");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(40, 40, 70));

        left.add(hi);
        left.add(Box.createVerticalStrut(4));
        left.add(title);

        topBar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton newMapBtn = new JButton("New map");
        JButton logoutBtn = new JButton("Logout");

        styleNewMapButton(newMapBtn);
        styleLogoutButton(logoutBtn);

        right.add(newMapBtn);
        right.add(logoutBtn);
        topBar.add(right, BorderLayout.EAST);

        // ===== Center: split =====
        JPanel leftCenter = new JPanel(new BorderLayout());
        leftCenter.setOpaque(false);
        leftCenter.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 8));
        leftCenter.add(searchSortPanel, BorderLayout.NORTH);
        leftCenter.add(mapsListPanel, BorderLayout.CENTER);

        JPanel rightCenter = new JPanel(new BorderLayout());
        rightCenter.setOpaque(false);
        rightCenter.setBorder(BorderFactory.createEmptyBorder(10, 8, 16, 16));
        rightCenter.add(propertiesPanel, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCenter, rightCenter);
        split.setDividerLocation(560);
        split.setBorder(null);
        mainPanel.add(split, BorderLayout.CENTER);

        // ===== Handlers =====
        newMapBtn.addActionListener(e -> onCreateMap());
        logoutBtn.addActionListener(e -> onLogout());

        // Double click open
        mapsListPanel.getList().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    MindMap selected = mapsListPanel.getSelected();
                    if (selected != null) openEditor(selected);
                }
            }
        });
    }

    private void onCreateMap() {
        String title = JOptionPane.showInputDialog(this, "Enter map title:");
        if (title == null) return;
        if (title.isBlank()) {
            JOptionPane.showMessageDialog(this, "Назва мапи обов'язкова");
            return;
        }

        mindMapService.createMap(title.trim(), user);
        mediator.refresh();
    }

    private void onLogout() {
        dispose();
        SwingUtilities.invokeLater(() ->
                new LoginForm(authService, mindMapService).setVisible(true)
        );
    }

    private void openEditor(MindMap map) {
        SwingUtilities.invokeLater(() ->
                new MindMapEditorForm(map, mindMapService).setVisible(true)
        );
    }

    private void styleNewMapButton(JButton btn) {
        btn.setBackground(new Color(76, 175, 80));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 26, 10, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
    }

    private void styleLogoutButton(JButton btn) {
        btn.setBackground(new Color(244, 67, 54));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
    }
}
