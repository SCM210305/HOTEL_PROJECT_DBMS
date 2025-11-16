package hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class ManageRoomsPanel extends JPanel {

    private Connection conn;

    public ManageRoomsPanel(Connection conn) {
        this.conn = conn;

        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        buildUI();
    }

    private void buildUI() {

        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Add Room");
        JButton edit = new JButton("Edit Room");
        JButton del = new JButton("Delete Room");
        JButton refresh = new JButton("Refresh");

        top.add(add); top.add(edit); top.add(del); top.add(refresh);

        add(top, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        refresh.addActionListener(e -> loadRooms(model));
        refresh.doClick();

        add.addActionListener(e -> addRoom(model));
        edit.addActionListener(e -> editRoom(table, model));
        del.addActionListener(e -> deleteRoom(table, model));
    }

    private void loadRooms(DefaultTableModel model) {

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ROOM_NO, ROOM_TYPE, STATUS, PRICE, HOTEL_ID FROM ROOMS")) {

            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            String[] colNames = new String[cols];

            for (int i = 1; i <= cols; i++) {
                colNames[i - 1] = md.getColumnName(i);
            }

            model.setColumnIdentifiers(colNames);
            model.setRowCount(0);

            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int i = 1; i <= cols; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void addRoom(DefaultTableModel model) {

        JTextField no = new JTextField();
        JTextField type = new JTextField();
        JTextField status = new JTextField("Available");
        JTextField price = new JTextField();
        JTextField hid = new JTextField();

        JPanel p = new JPanel(new GridLayout(0, 2));
        p.add(new JLabel("Room No:")); p.add(no);
        p.add(new JLabel("Room Type:")); p.add(type);
        p.add(new JLabel("Status:")); p.add(status);
        p.add(new JLabel("Price:")); p.add(price);
        p.add(new JLabel("Hotel ID:")); p.add(hid);

        int res = JOptionPane.showConfirmDialog(this, p, "Add Room", JOptionPane.OK_CANCEL_OPTION);

        if (res == JOptionPane.OK_OPTION) {
            try (PreparedStatement pst =
                         conn.prepareStatement("INSERT INTO ROOMS VALUES (?, ?, ?, ?, ?)")) {

                pst.setString(1, no.getText());
                pst.setString(2, type.getText());
                pst.setString(3, status.getText());
                pst.setDouble(4, Double.parseDouble(price.getText()));
                pst.setString(5, hid.getText());
                pst.executeUpdate();

                loadRooms(model);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding room: " + ex.getMessage());
            }
        }
    }

    private void editRoom(JTable table, DefaultTableModel model) {

        int sel = table.getSelectedRow();

        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select a room.");
            return;
        }

        String rno = table.getValueAt(sel, 0).toString();
        String rtype = table.getValueAt(sel, 1).toString();
        String rstatus = table.getValueAt(sel, 2).toString();
        String rprice = table.getValueAt(sel, 3).toString();
        String rhid = table.getValueAt(sel, 4).toString();

        JTextField no = new JTextField(rno); no.setEnabled(false);
        JTextField type = new JTextField(rtype);
        JTextField status = new JTextField(rstatus);
        JTextField price = new JTextField(rprice);
        JTextField hid = new JTextField(rhid);

        JPanel p = new JPanel(new GridLayout(0, 2));
        p.add(new JLabel("Room No:")); p.add(no);
        p.add(new JLabel("Room Type:")); p.add(type);
        p.add(new JLabel("Status:")); p.add(status);
        p.add(new JLabel("Price:")); p.add(price);
        p.add(new JLabel("Hotel ID:")); p.add(hid);

        int r = JOptionPane.showConfirmDialog(this, p, "Edit Room", JOptionPane.OK_CANCEL_OPTION);

        if (r == JOptionPane.OK_OPTION) {
            try (PreparedStatement pst =
                         conn.prepareStatement("UPDATE ROOMS SET ROOM_TYPE=?, STATUS=?, PRICE=?, HOTEL_ID=? WHERE ROOM_NO=?")) {

                pst.setString(1, type.getText());
                pst.setString(2, status.getText());
                pst.setDouble(3, Double.parseDouble(price.getText()));
                pst.setString(4, hid.getText());
                pst.setString(5, no.getText());
                pst.executeUpdate();

                loadRooms(model);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating room: " + ex.getMessage());
            }
        }
    }

    private void deleteRoom(JTable table, DefaultTableModel model) {

        int sel = table.getSelectedRow();

        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select a room.");
            return;
        }

        String rno = table.getValueAt(sel, 0).toString();

        int conf = JOptionPane.showConfirmDialog(this,
                "Delete room " + rno + "?",
                "Confirm",
                JOptionPane.YES_NO_OPTION);

        if (conf == JOptionPane.YES_OPTION) {

            try (PreparedStatement pst =
                         conn.prepareStatement("DELETE FROM ROOMS WHERE ROOM_NO=?")) {

                pst.setString(1, rno);
                pst.executeUpdate();

                loadRooms(model);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Delete error: " + ex.getMessage());
            }
        }
    }
}
