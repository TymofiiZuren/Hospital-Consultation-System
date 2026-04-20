package ie.setu.hcs.util;

import org.junit.jupiter.api.Test;

import javax.swing.table.DefaultTableModel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TableModelUtilTest {

    @Test
    void emptyCopyPreservesColumnStructureWithoutRows() {
        DefaultTableModel source = sampleModel();

        DefaultTableModel copy = TableModelUtil.emptyCopy(source);

        assertEquals(source.getColumnCount(), copy.getColumnCount());
        assertEquals(source.getColumnName(0), copy.getColumnName(0));
        assertEquals(source.getColumnName(1), copy.getColumnName(1));
        assertEquals(0, copy.getRowCount());
    }

    @Test
    void rowValueHelpersReturnDataByColumnName() {
        DefaultTableModel source = sampleModel();

        assertArrayEquals(new Object[]{7, "Dr. Jay", "Pending"}, TableModelUtil.rowValues(source, 0));
        assertEquals(7, TableModelUtil.intValue(source, 0, "appointment_id"));
        assertEquals("Dr. Jay", TableModelUtil.stringValue(source, 0, "doctor"));
        assertEquals("Pending", TableModelUtil.value(source, 0, "status"));
        assertNull(TableModelUtil.value(source, 0, "missing"));
    }

    private DefaultTableModel sampleModel() {
        DefaultTableModel model = new DefaultTableModel(new Object[]{
                "appointment_id", "doctor", "status"
        }, 0);
        model.addRow(new Object[]{7, "Dr. Jay", "Pending"});
        return model;
    }
}
