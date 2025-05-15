package org.rpms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.*;

public class AdminDashboard {
    private Stage primaryStage;
    private VBox rootContainer;

    public AdminDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Scene createScene() {
        rootContainer = new VBox(20);
        rootContainer.setPadding(new Insets(20));
        rootContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #f1f8e9, #dcedc8);");

        rootContainer.getChildren().addAll(
                createHeader(),
                createTabPane()
        );

        return new Scene(rootContainer, 1200, 800);
    }


    private HBox createHeader() {
        Label title = new Label("Administrator Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#2e7d32"));

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutBtn.setOnAction(e -> handleLogout());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(20, title, spacer, logoutBtn);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #e8f5e9, #c8e6c9); -fx-border-color: #aed581;");
        header.setAlignment(Pos.CENTER_LEFT);

        return header;
    }


    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();

        // Add User Tab
        Tab addUserTab = new Tab("Add User");
        addUserTab.setContent(createAddUserForm());
        addUserTab.setClosable(false);

        // View Users Tab
        Tab viewUsersTab = new Tab("View Users");
        viewUsersTab.setContent(createUserListView());
        viewUsersTab.setClosable(false);

        // Login History Tab (simple working version, no listeners or dynamic reloads)
        Tab loginHistoryTab = new Tab("Login History");
        loginHistoryTab.setContent(LoginHistoryViewer.createScene().getRoot());
        loginHistoryTab.setClosable(false);

        // Add all tabs
        tabPane.getTabs().addAll(addUserTab, viewUsersTab, loginHistoryTab);

        return tabPane;
    }

    private VBox createAddUserForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Form fields
        TextField nameField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField addressField = new TextField();
        TextField phoneField = new TextField();
        ComboBox<String> userTypeBox = new ComboBox<>(
                FXCollections.observableArrayList("ADMINISTRATOR", "DOCTOR", "PATIENT")
        );
        userTypeBox.setValue("ADMINISTRATOR");

        // Add fields to grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Address:"), 0, 3);
        grid.add(addressField, 1, 3);
        grid.add(new Label("Phone:"), 0, 4);
        grid.add(phoneField, 1, 4);
        grid.add(new Label("User Type:"), 0, 5);
        grid.add(userTypeBox, 1, 5);

        // Additional fields for Doctor/Patient
        VBox additionalFields = new VBox(10);
        additionalFields.setVisible(false);

        TextField specialityField = new TextField();
        specialityField.setPromptText("Doctor Speciality");

        ComboBox<String> genderBox = new ComboBox<>(
                FXCollections.observableArrayList("Male", "Female", "Other")
        );
        TextField emergencyContactField = new TextField();
        emergencyContactField.setPromptText("Emergency Contact");

        userTypeBox.setOnAction(e -> {
            additionalFields.getChildren().clear();
            switch (userTypeBox.getValue()) {
                case "DOCTOR":
                    additionalFields.getChildren().add(specialityField);
                    break;
                case "PATIENT":
                    additionalFields.getChildren().addAll(genderBox, emergencyContactField);
                    break;
            }
            additionalFields.setVisible(!userTypeBox.getValue().equals("ADMINISTRATOR"));
        });

        // Submit button
        Button submitBtn = new Button("Add User");
        submitBtn.setStyle("-fx-background-color: #66bb6a; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 6 18 6 18;");

        Label statusLabel = new Label();

        submitBtn.setOnAction(e -> {
            try {
                Connection conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false);

                // Insert into users table
                String userSql = "INSERT INTO users (name, email, password, address, phone_number, user_type) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
                userStmt.setString(1, nameField.getText());
                userStmt.setString(2, emailField.getText());
                userStmt.setString(3, passwordField.getText());
                userStmt.setString(4, addressField.getText());
                userStmt.setString(5, phoneField.getText());
                userStmt.setString(6, userTypeBox.getValue());

                userStmt.executeUpdate();

                // Get generated user ID
                ResultSet rs = userStmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);

                    // Insert additional info based on user type
                    switch (userTypeBox.getValue()) {
                        case "DOCTOR":
                            String doctorSql = "INSERT INTO doctors (user_id, speciality) VALUES (?, ?)";
                            PreparedStatement doctorStmt = conn.prepareStatement(doctorSql);
                            doctorStmt.setInt(1, userId);
                            doctorStmt.setString(2, specialityField.getText());
                            doctorStmt.executeUpdate();
                            break;

                        case "PATIENT":
                            String patientSql = "INSERT INTO patients (user_id, gender, emergency_contact) VALUES (?, ?, ?)";
                            PreparedStatement patientStmt = conn.prepareStatement(patientSql);
                            patientStmt.setInt(1, userId);
                            patientStmt.setString(2, genderBox.getValue());
                            patientStmt.setString(3, emergencyContactField.getText());
                            patientStmt.executeUpdate();
                            break;
                    }

                    conn.commit();
                    statusLabel.setText("User added successfully!");
                    statusLabel.setStyle("-fx-text-fill: green;");
                    clearForm(nameField, emailField, passwordField, addressField, phoneField);
                    specialityField.clear();
                    emergencyContactField.clear();

                    // Refresh the user list tab
                    refreshUserListView();
                }
            } catch (SQLException ex) {
                statusLabel.setText("Error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        Button sendMailBtn = new Button("Send Mail");
        sendMailBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        sendMailBtn.setOnAction(e -> {
            MailComposer mailComposer = new MailComposer(primaryStage);
            mailComposer.show();
        });

        Label titleLabel = new Label("Add New User");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        form.getChildren().addAll(
                titleLabel,
                grid,
                additionalFields,
                submitBtn,
                sendMailBtn,
                statusLabel
        );
        return form;
    }

    private void clearForm(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    private VBox createUserListView() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white;");

        Label title = new Label("All Registered Users");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Create a scrollable area for the user list
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox userList = new VBox(10);
        userList.setPadding(new Insets(10));

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {

            while (rs.next()) {
                int userId = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String userType = rs.getString("user_type");
                String phone = rs.getString("phone_number");

                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-border-color: #ccc; -fx-padding: 10px; -fx-background-color: #f9f9f9;");

                Label nameLabel = new Label("Name: " + name);
                Label emailLabel = new Label("Email: " + email);
                Label typeLabel = new Label("Type: " + userType);
                Label phoneLabel = new Label("Phone: " + phone);

                nameLabel.setPrefWidth(200);
                emailLabel.setPrefWidth(250);
                typeLabel.setPrefWidth(150);
                phoneLabel.setPrefWidth(150);

                Button editBtn = new Button("Edit");
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                editBtn.setOnAction(e -> showEditForm(userId));

                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> {
                    boolean confirm = showConfirmationDialog("Delete User",
                            "Are you sure you want to delete this user?");
                    if (confirm) {
                        if (deleteUser(userId)) {
                            // Refresh the user list
                            refreshUserListView();
                        }
                    }
                });

                row.getChildren().addAll(nameLabel, emailLabel, typeLabel, phoneLabel, editBtn, deleteBtn);
                userList.getChildren().add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            userList.getChildren().add(new Label("Error loading users: " + e.getMessage()));
        }

        scrollPane.setContent(userList);
        container.getChildren().addAll(title, scrollPane);
        return container;
    }

    // Method to refresh the user list view
    private void refreshUserListView() {
        // Find the TabPane in the scene
        for (javafx.scene.Node node : rootContainer.getChildren()) {
            if (node instanceof TabPane) {
                TabPane tabPane = (TabPane) node;
                // Find the "View Users" tab
                for (Tab tab : tabPane.getTabs()) {
                    if (tab.getText().equals("View Users")) {
                        // Replace the content of the tab with a fresh user list
                        tab.setContent(createUserListView());
                        // Switch to the View Users tab
                        tabPane.getSelectionModel().select(tab);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void showEditForm(int userId) {
        // Don't use try-with-resources here as we need the connection to stay open for the save action
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Create a new stage for the edit form
                Stage editStage = new Stage();
                editStage.setTitle("Edit User");

                VBox editForm = new VBox(15);
                editForm.setPadding(new Insets(20));
                editForm.setStyle("-fx-background-color: white;");

                // Get user data
                String name = rs.getString("name");
                String email = rs.getString("email");
                String userType = rs.getString("user_type");
                String address = rs.getString("address");
                String phone = rs.getString("phone_number");

                // Create form fields
                TextField nameField = new TextField(name);
                TextField emailField = new TextField(email);
                TextField addressField = new TextField(address);
                TextField phoneField = new TextField(phone);
                ComboBox<String> userTypeBox = new ComboBox<>(
                        FXCollections.observableArrayList("ADMINISTRATOR", "DOCTOR", "PATIENT")
                );
                userTypeBox.setValue(userType);

                // Additional fields container
                VBox additionalFields = new VBox(10);

                // Create fields that will be used based on user type
                TextField specialityField = new TextField();
                ComboBox<String> genderBox = new ComboBox<>(
                        FXCollections.observableArrayList("Male", "Female", "Other")
                );
                TextField emergencyContactField = new TextField();

                // Load additional data based on user type
                String currentSpeciality = "";
                String currentGender = "";
                String currentEmergencyContact = "";

                if (userType.equals("DOCTOR")) {
                    PreparedStatement doctorStmt = conn.prepareStatement(
                            "SELECT speciality FROM doctors WHERE user_id = ?");
                    doctorStmt.setInt(1, userId);
                    ResultSet doctorRs = doctorStmt.executeQuery();
                    if (doctorRs.next()) {
                        currentSpeciality = doctorRs.getString("speciality");
                        specialityField.setText(currentSpeciality);
                        additionalFields.getChildren().add(new HBox(10, new Label("Speciality:"), specialityField));
                    }
                    doctorRs.close();
                    doctorStmt.close();
                } else if (userType.equals("PATIENT")) {
                    PreparedStatement patientStmt = conn.prepareStatement(
                            "SELECT gender, emergency_contact FROM patients WHERE user_id = ?");
                    patientStmt.setInt(1, userId);
                    ResultSet patientRs = patientStmt.executeQuery();
                    if (patientRs.next()) {
                        currentGender = patientRs.getString("gender");
                        currentEmergencyContact = patientRs.getString("emergency_contact");

                        genderBox.setValue(currentGender);
                        emergencyContactField.setText(currentEmergencyContact);

                        additionalFields.getChildren().addAll(
                                new HBox(10, new Label("Gender:"), genderBox),
                                new HBox(10, new Label("Emergency Contact:"), emergencyContactField)
                        );
                    }
                    patientRs.close();
                    patientStmt.close();
                }

                rs.close();
                stmt.close();

                // Final references to be used in the action handler
                final String finalSpeciality = currentSpeciality;
                final String finalGender = currentGender;
                final String finalEmergencyContact = currentEmergencyContact;

                // Store the connection as a final variable to be used in the event handler
                final Connection finalConn = conn;

                // Update additional fields when user type changes
                userTypeBox.setOnAction(e -> {
                    String selectedType = userTypeBox.getValue();
                    additionalFields.getChildren().clear();

                    switch (selectedType) {
                        case "DOCTOR":
                            specialityField.setText(finalSpeciality);
                            additionalFields.getChildren().add(new HBox(10, new Label("Speciality:"), specialityField));
                            break;
                        case "PATIENT":
                            genderBox.setValue(finalGender);
                            emergencyContactField.setText(finalEmergencyContact);
                            additionalFields.getChildren().addAll(
                                    new HBox(10, new Label("Gender:"), genderBox),
                                    new HBox(10, new Label("Emergency Contact:"), emergencyContactField)
                            );
                            break;
                    }
                });

                // Layout the form
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20));

                grid.add(new Label("Name:"), 0, 0);
                grid.add(nameField, 1, 0);
                grid.add(new Label("Email:"), 0, 1);
                grid.add(emailField, 1, 1);
                grid.add(new Label("Address:"), 0, 2);
                grid.add(addressField, 1, 2);
                grid.add(new Label("Phone:"), 0, 3);
                grid.add(phoneField, 1, 3);
                grid.add(new Label("User Type:"), 0, 4);
                grid.add(userTypeBox, 1, 4);

                // Save button
                Button saveBtn = new Button("Save Changes");
                saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                Label statusLabel = new Label();

                saveBtn.setOnAction(e -> {
                    try {
                        finalConn.setAutoCommit(false);

                        // Get the previous user type
                        String previousUserType = "";
                        PreparedStatement typeStmt = finalConn.prepareStatement("SELECT user_type FROM users WHERE id = ?");
                        typeStmt.setInt(1, userId);
                        ResultSet typeRs = typeStmt.executeQuery();
                        if (typeRs.next()) {
                            previousUserType = typeRs.getString("user_type");
                        }
                        typeRs.close();
                        typeStmt.close();

                        // Update user table
                        String updateSql = "UPDATE users SET name=?, email=?, address=?, phone_number=?, user_type=? WHERE id=?";
                        PreparedStatement updateStmt = finalConn.prepareStatement(updateSql);
                        updateStmt.setString(1, nameField.getText());
                        updateStmt.setString(2, emailField.getText());
                        updateStmt.setString(3, addressField.getText());
                        updateStmt.setString(4, phoneField.getText());
                        updateStmt.setString(5, userTypeBox.getValue());
                        updateStmt.setInt(6, userId);

                        int rowsAffected = updateStmt.executeUpdate();
                        updateStmt.close();

                        if (rowsAffected > 0) {
                            // Handle role change if needed
                            String newUserType = userTypeBox.getValue();

                            // If user type has changed, we need to delete old role data and create new
                            if (!previousUserType.equals(newUserType)) {
                                // Delete old role data
                                if (previousUserType.equals("DOCTOR")) {
                                    PreparedStatement deleteStmt = finalConn.prepareStatement("DELETE FROM doctors WHERE user_id = ?");
                                    deleteStmt.setInt(1, userId);
                                    deleteStmt.executeUpdate();
                                    deleteStmt.close();
                                } else if (previousUserType.equals("PATIENT")) {
                                    PreparedStatement deleteStmt = finalConn.prepareStatement("DELETE FROM patients WHERE user_id = ?");
                                    deleteStmt.setInt(1, userId);
                                    deleteStmt.executeUpdate();
                                    deleteStmt.close();
                                }

                                // Insert new role data
                                if (newUserType.equals("DOCTOR")) {
                                    PreparedStatement insertStmt = finalConn.prepareStatement(
                                            "INSERT INTO doctors (user_id, speciality) VALUES (?, ?)");
                                    insertStmt.setInt(1, userId);
                                    insertStmt.setString(2, specialityField.getText());
                                    insertStmt.executeUpdate();
                                    insertStmt.close();
                                } else if (newUserType.equals("PATIENT")) {
                                    PreparedStatement insertStmt = finalConn.prepareStatement(
                                            "INSERT INTO patients (user_id, gender, emergency_contact) VALUES (?, ?, ?)");
                                    insertStmt.setInt(1, userId);
                                    insertStmt.setString(2, genderBox.getValue());
                                    insertStmt.setString(3, emergencyContactField.getText());
                                    insertStmt.executeUpdate();
                                    insertStmt.close();
                                }
                            } else {
                                // User type hasn't changed, just update the role-specific data
                                if (newUserType.equals("DOCTOR")) {
                                    // Check if record exists first
                                    PreparedStatement checkStmt = finalConn.prepareStatement(
                                            "SELECT COUNT(*) FROM doctors WHERE user_id = ?");
                                    checkStmt.setInt(1, userId);
                                    ResultSet checkRs = checkStmt.executeQuery();
                                    boolean exists = checkRs.next() && checkRs.getInt(1) > 0;
                                    checkRs.close();
                                    checkStmt.close();

                                    if (exists) {
                                        PreparedStatement updateRoleStmt = finalConn.prepareStatement(
                                                "UPDATE doctors SET speciality = ? WHERE user_id = ?");
                                        updateRoleStmt.setString(1, specialityField.getText());
                                        updateRoleStmt.setInt(2, userId);
                                        updateRoleStmt.executeUpdate();
                                        updateRoleStmt.close();
                                    } else {
                                        PreparedStatement insertStmt = finalConn.prepareStatement(
                                                "INSERT INTO doctors (user_id, speciality) VALUES (?, ?)");
                                        insertStmt.setInt(1, userId);
                                        insertStmt.setString(2, specialityField.getText());
                                        insertStmt.executeUpdate();
                                        insertStmt.close();
                                    }
                                } else if (newUserType.equals("PATIENT")) {
                                    // Check if record exists first
                                    PreparedStatement checkStmt = finalConn.prepareStatement(
                                            "SELECT COUNT(*) FROM patients WHERE user_id = ?");
                                    checkStmt.setInt(1, userId);
                                    ResultSet checkRs = checkStmt.executeQuery();
                                    boolean exists = checkRs.next() && checkRs.getInt(1) > 0;
                                    checkRs.close();
                                    checkStmt.close();

                                    if (exists) {
                                        PreparedStatement updateRoleStmt = finalConn.prepareStatement(
                                                "UPDATE patients SET gender = ?, emergency_contact = ? WHERE user_id = ?");
                                        updateRoleStmt.setString(1, genderBox.getValue());
                                        updateRoleStmt.setString(2, emergencyContactField.getText());
                                        updateRoleStmt.setInt(3, userId);
                                        updateRoleStmt.executeUpdate();
                                        updateRoleStmt.close();
                                    } else {
                                        PreparedStatement insertStmt = finalConn.prepareStatement(
                                                "INSERT INTO patients (user_id, gender, emergency_contact) VALUES (?, ?, ?)");
                                        insertStmt.setInt(1, userId);
                                        insertStmt.setString(2, genderBox.getValue());
                                        insertStmt.setString(3, emergencyContactField.getText());
                                        insertStmt.executeUpdate();
                                        insertStmt.close();
                                    }
                                }
                            }

                            finalConn.commit();
                            statusLabel.setText("User updated successfully!");
                            statusLabel.setStyle("-fx-text-fill: green;");

                            // Refresh the user list view
                            refreshUserListView();
                        }
                    } catch (SQLException ex) {
                        try {
                            finalConn.rollback();
                        } catch (SQLException rollbackEx) {
                            rollbackEx.printStackTrace();
                        }
                        statusLabel.setText("Error updating user: " + ex.getMessage());
                        statusLabel.setStyle("-fx-text-fill: red;");
                        ex.printStackTrace();
                    }
                });

                // Handle closing the connection when window is closed
                editStage.setOnCloseRequest(windowEvent -> {
                    try {
                        if (finalConn != null && !finalConn.isClosed()) {
                            finalConn.close();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                editForm.getChildren().addAll(
                        new Label("Edit User (ID: " + userId + ")"),
                        grid,
                        additionalFields,
                        saveBtn,
                        statusLabel
                );

                Scene editScene = new Scene(editForm, 600, 500);
                editStage.setScene(editScene);
                editStage.show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load user data: " + e.getMessage());
            // Make sure we close the connection if there's an error
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert.showAndWait().get() == ButtonType.OK;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean deleteUser(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // First, check the user type
            String userType = getUserType(conn, userId);

            if (userType == null) {
                showAlert("Error", "User not found");
                return false;
            }

            // Delete from role-specific tables first
            if (userType.equals("DOCTOR")) {
                // Delete doctor-specific data
                PreparedStatement deleteDoctor = conn.prepareStatement(
                        "DELETE FROM doctors WHERE user_id = ?");
                deleteDoctor.setInt(1, userId);
                deleteDoctor.executeUpdate();
            } else if (userType.equals("PATIENT")) {
                // Delete patient-specific data
                PreparedStatement deletePatient = conn.prepareStatement(
                        "DELETE FROM patients WHERE user_id = ?");
                deletePatient.setInt(1, userId);
                deletePatient.executeUpdate();
            }

            // Then delete from users table
            PreparedStatement deleteUser = conn.prepareStatement(
                    "DELETE FROM users WHERE id = ?");
            deleteUser.setInt(1, userId);
            int rowsDeleted = deleteUser.executeUpdate();

            conn.commit();

            if (rowsDeleted > 0) {
                showAlert("Success", "User deleted successfully");
                return true;
            } else {
                showAlert("Error", "Failed to delete user");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to delete user: " + e.getMessage());
            return false;
        }
    }

    private String getUserType(Connection conn, int userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT user_type FROM users WHERE id = ?");
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getString("user_type");
        } else {
            return null;
        }
    }

    private void handleLogout() {
        LoginPage loginPage = new LoginPage(primaryStage);
        primaryStage.setScene(loginPage.createLoginScene());
        primaryStage.setTitle("RPMS Login");
    }
}