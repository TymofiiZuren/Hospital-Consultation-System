package ie.setu.hcs.util;

import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public final class TableModelUtil {

    private TableModelUtil() {
    }

    public static DefaultTableModel buildTableModel(ResultSet rs)
            throws SQLException {

        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        DefaultTableModel model = new DefaultTableModel();

        for (int i = 1; i <= columnCount; i++) {
            model.addColumn(meta.getColumnName(i));
        }

        while (rs.next()) {

            Object[] row = new Object[columnCount];

            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getObject(i);
            }

            model.addRow(row);
        }

        return model;
    }

    public static DefaultTableModel emptyCopy(DefaultTableModel source) {
        DefaultTableModel copy = new DefaultTableModel();
        for (int column = 0; column < source.getColumnCount(); column++) {
            copy.addColumn(source.getColumnName(column));
        }
        return copy;
    }

    public static Object[] rowValues(DefaultTableModel source, int row) {
        Object[] values = new Object[source.getColumnCount()];
        for (int column = 0; column < source.getColumnCount(); column++) {
            values[column] = source.getValueAt(row, column);
        }
        return values;
    }

    public static int findColumnIndex(DefaultTableModel model, String columnName) {
        return model.findColumn(columnName);
    }

    public static Object value(DefaultTableModel model, int row, String columnName) {
        int column = findColumnIndex(model, columnName);
        return column < 0 ? null : model.getValueAt(row, column);
    }

    public static String stringValue(DefaultTableModel model, int row, String columnName) {
        Object value = value(model, row, columnName);
        return value == null ? "" : value.toString();
    }

    public static Integer intValue(DefaultTableModel model, int row, String columnName) {
        Object value = value(model, row, columnName);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }
}
