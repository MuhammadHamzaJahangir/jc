import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class CrudApplication extends JFrame {
    private JTextField idField, nameField, ageField;
    private JButton addButton, updateButton, deleteButton, fetchButton, showAllButton;
    private JTable recordTable;
    private DefaultTableModel tableModel;

    private Connection connection;
    private Statement statement;

    public CrudApplication() {
        super("CRUD Application");

        // Initialize the components
        idField = new JTextField(10);
        nameField = new JTextField(20);
        ageField = new JTextField(3);

        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        fetchButton = new JButton("Fetch");
        showAllButton = new JButton("Show All");

        // Add action listeners to the buttons
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addRecord();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateRecord();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteRecord();
            }
        });

        fetchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fetchRecord();
            }
        });

        showAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAllRecords();
            }
        });

        // Create panels
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(4, 2));
        topPanel.add(new JLabel("ID:"));
        topPanel.add(idField);
        topPanel.add(new JLabel("Name:"));
        topPanel.add(nameField);
        topPanel.add(new JLabel("Age:"));
        topPanel.add(ageField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(fetchButton);
        buttonPanel.add(showAllButton);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        tableModel = new DefaultTableModel();
        recordTable = new JTable(tableModel);
        recordTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = recordTable.getSelectedRow();
                    if (selectedRow != -1) {
                        setFieldsFromSelectedRow(selectedRow);
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(recordTable);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // Add panels to the main frame
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // Set frame properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);
    }

    private void addRecord() {
        try {
            getConnection();
            int id = Integer.parseInt(idField.getText());
            String name = nameField.getText();
            int age = Integer.parseInt(ageField.getText());

            String query = "INSERT INTO users (id, name, age) VALUES (" + id + ", '" + name + "', " + age + ")";

            statement.executeUpdate(query);

            clearFields();
            JOptionPane.showMessageDialog(this, "Record added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void updateRecord() {
        try {
            getConnection();

            int id = Integer.parseInt(idField.getText());
            String name = nameField.getText();
            int age = Integer.parseInt(ageField.getText());

            String query = "UPDATE users SET name = '" + name + "', age = " + age + " WHERE id = " + id;
            statement.executeUpdate(query);

            clearFields();
            JOptionPane.showMessageDialog(this, "Record updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void deleteRecord() {
        try {
            getConnection();

            int id = Integer.parseInt(idField.getText());

            String query = "DELETE FROM users WHERE id = " + id;
            statement.executeUpdate(query);

            clearFields();
            JOptionPane.showMessageDialog(this, "Record deleted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void fetchRecord() {
        try {
            getConnection();

            int id = Integer.parseInt(idField.getText());

            String query = "SELECT * FROM users WHERE id = " + id;
            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");

                nameField.setText(name);
                ageField.setText(String.valueOf(age));
            } else {
                clearFields();
                JOptionPane.showMessageDialog(this, "No record found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void showAllRecords() {
        try {
            getConnection();

            String query = "SELECT * FROM users";
            ResultSet resultSet = statement.executeQuery(query);

            // Clear the table
            tableModel.setRowCount(0);

            // Get the column names
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = metaData.getColumnName(i + 1);
            }
            tableModel.setColumnIdentifiers(columnNames);

            // Populate the table with data
            while (resultSet.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    rowData[i] = resultSet.getObject(i + 1);
                }
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void setFieldsFromSelectedRow(int selectedRow) {
        if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
            Object id = tableModel.getValueAt(selectedRow, 0);
            Object name = tableModel.getValueAt(selectedRow, 1);
            Object age = tableModel.getValueAt(selectedRow, 2);

            idField.setText(id.toString());
            nameField.setText(name.toString());
            ageField.setText(age.toString());
        }
    }

    private void getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/hamza";
        String username = "root";
        String password = "12345678";

        connection = DriverManager.getConnection(url, username, password);
        statement = connection.createStatement();
    }

    private void closeConnection() {
        try {
            if (statement != null)
                statement.close();

            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        ageField.setText("");
    }

    private boolean authenticateUser(String username, String password) {
        try {
            getConnection();

            String query = "SELECT * FROM login WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }

        return false;
    }

    private void showLoginScreen() {
        JFrame loginFrame = new JFrame("Login");
        JPanel loginPanel = new JPanel(new GridLayout(3, 2,10,10));
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (authenticateUser(username, password)) {
                    loginFrame.dispose();
                    setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Invalid username or password");
                }
            }
        });

        loginPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        loginPanel.add(usernameLabel);
        loginPanel.add(usernameField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);
        loginPanel.add(new JLabel());
        loginPanel.add(loginButton);


        loginFrame.add(loginPanel);
        loginFrame.pack();
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setVisible(true);
        loginFrame.setSize(300,200);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CrudApplication().showLoginScreen();
            }
        });
    }
}
