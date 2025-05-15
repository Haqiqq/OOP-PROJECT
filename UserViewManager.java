package org.rpms;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.*;

public class UserViewManager {

    private Stage stage;

    public void show(Stage parentStage) {
        // Initialize the modal stage for the user management window
        stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);
        stage.setTitle("View Users");

        // Main container for the layout
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: white;");

        // Title Label
        Label titleLabel = new Label("User Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // TextArea to display user data
        TextArea userTextArea = new TextArea();
        userTextArea.setEditable(false);
        userTextArea.setWrapText(true);
        userTextArea.setPrefHeight(400);

        // Button to load users
        Button loadButton = new Button("Load Users");
        loadButton.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        loadButton.setOnAction(e -> loadUsers(userTextArea));

        // Button to close the window
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
        closeButton.setOnAction(e -> stage.close());

        // Add components to the main container
        mainContainer.getChildren().addAll(titleLabel, loadButton, userTextArea, closeButton);

        // Create and set the scene
        Scene scene = new Scene(mainContainer, 600, 500);
        stage.setScene(scene);
        stage.show();
    }

    // Method to load users from the database and display in text area
    private void loadUsers(TextArea userTextArea) {
        StringBuilder usersText = new StringBuilder();

        // Database connection and data retrieval
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Loop through the ResultSet and append data to StringBuilder
            while (rs.next()) {
                usersText.append("ID: ").append(rs.getInt("id")).append("\n")
                        .append("Name: ").append(rs.getString("name")).append("\n")
                        .append("Email: ").append(rs.getString("email")).append("\n")
                        .append("Password: ").append(rs.getString("password")).append("\n")
                        .append("Address: ").append(rs.getString("address")).append("\n")
                        .append("Phone: ").append(rs.getString("phone_number")).append("\n")
                        .append("User Type: ").append(rs.getString("user_type")).append("\n\n");
            }

            // Update the TextArea on the JavaFX Application thread
            Platform.runLater(() -> userTextArea.setText(usersText.toString()));

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading users: " + e.getMessage());
        }
    }

    // Method to display error alerts
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
