package ie.setu.hcs.ui;

import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.Supplier;

public class DataTableFrame extends JFrame {
    @FunctionalInterface
    public interface TableLoader {
        DefaultTableModel load() throws Exception;
    }

    @FunctionalInterface
    public interface SelectedRowAction {
        void run(Integer selectedId) throws Exception;
    }

    private final String idColumn;
    private final TableLoader loader;
    private final JTable table;
    private final JPanel actionPanel = UIHelper.actionBar();
    private JButton navigationButton;
    private Supplier<JFrame> backSupplier;

    public DataTableFrame(String title, String subtitle, String idColumn, TableLoader loader) {
        this.idColumn = idColumn;
        this.loader = loader;
        this.table = UIHelper.table(new DefaultTableModel());
        initUI(title, subtitle);
        loadTable();
    }

    public DataTableFrame withBack(Supplier<JFrame> backSupplier) {
        this.backSupplier = backSupplier;
        if (navigationButton != null) {
            navigationButton.setText("Back");
        }
        return this;
    }

    private void initUI(String title, String subtitle) {
        setTitle(title);
        setSize(920, 620);
        setMinimumSize(new Dimension(800, 540));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(HCS_Colors.LIGHT_BG);
        root.add(UIHelper.pageHeader(title, subtitle), BorderLayout.NORTH);

        JPanel content = UIHelper.pageBody(new BorderLayout(0, 16));
        content.setBackground(HCS_Colors.LIGHT_BG);

        JButton refresh = UIHelper.actionButton("Refresh", HCS_Colors.BUTTON_BLUE);
        refresh.addActionListener(e -> loadTable());
        JButton view = UIHelper.detailsButton(this, table, title + " Details");
        navigationButton = UIHelper.secondaryButton("Close");
        navigationButton.addActionListener(e -> AppNavigator.backOrClose(
                this,
                backSupplier == null ? null : backSupplier.get()
        ));
        actionPanel.add(refresh);
        actionPanel.add(view);
        actionPanel.add(navigationButton);

        content.add(UIHelper.tableAlignedSection(actionPanel), BorderLayout.NORTH);
        content.add(UIHelper.tableScrollPane(table), BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);

        setContentPane(root);
    }

    public DataTableFrame addSelectedAction(String text, Color color, SelectedRowAction action) {
        JButton button = UIHelper.actionButton(text, color);
        button.addActionListener(e -> {
            try {
                action.run(UIHelper.selectedId(table, idColumn));
                loadTable();
            } catch (Exception ex) {
                UIHelper.showError(this, ex);
            }
        });
        actionPanel.add(button, Math.max(0, actionPanel.getComponentCount() - 2));
        actionPanel.revalidate();
        actionPanel.repaint();
        return this;
    }

    private void loadTable() {
        try {
            DefaultTableModel model = loader.load();
            table.setModel(model);
            UIHelper.hideColumns(table, idColumn);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }
}
