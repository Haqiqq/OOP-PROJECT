package org.rpms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class GiveFeedbackHandler {
    private static final String URL = "jdbc:mysql://localhost:3306/rpms";
    private static final String USER = "root";
    private static final String PASSWORD = "Seeulateralligator1234";

    public static void showFeedbackForm(int doctorId) {
        Stage stage = new Stage();
        stage.setTitle("Give Feedback");

        // Outer card-style layout
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-radius: 10; -fx-background-radius: 10;");
        card.setAlignment(Pos.CENTER_LEFT);

        Label header = new Label("Provide Feedback");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Patient selection
        ComboBox<String> patientComboBox = new ComboBox<>();
        HashMap<String, Integer> patientMap = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT u.id, u.name FROM users u JOIN patients p ON u.id = p.user_id");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String display = id + ": " + name;
                patientComboBox.getItems().add(display);
                patientMap.put(display, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        VBox patientBox = createLabeledBox("Select Patient:", patientComboBox);

        // Comments field
        TextArea commentField = new TextArea();
        commentField.setPrefRowCount(3);
        commentField.setWrapText(true);
        VBox commentBox = createLabeledBox("Comments:", commentField);

        // Medicine input
        TextField medicineField = new TextField();
        VBox medicineBox = createLabeledBox("Medicine:", medicineField);

        // Frequency input
        TextField frequencyField = new TextField();
        VBox frequencyBox = createLabeledBox("Frequency:", frequencyField);

        // Submit button
        Button submitButton = new Button("Submit Feedback");
        submitButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        submitButton.setPrefWidth(200);
        submitButton.setOnAction(e -> {
            String selectedPatient = patientComboBox.getValue();
            String comments = commentField.getText().trim();
            String medicine = medicineField.getText().trim();
            String frequency = frequencyField.getText().trim();

            if (selectedPatient == null || comments.isEmpty() || medicine.isEmpty() || frequency.isEmpty()) {
                showAlert("Error", "All fields must be filled.");
                return;
            }

            int patientId = patientMap.get(selectedPatient);
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String sql = "INSERT INTO feedback (patient_id, doctor_id, comments, medicine, frequency) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, patientId);
                pstmt.setInt(2, doctorId);
                pstmt.setString(3, comments);
                pstmt.setString(4, medicine);
                pstmt.setString(5, frequency);
                pstmt.executeUpdate();

                showAlert("Success", "Feedback submitted successfully.");
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Database Error", "Could not submit feedback.");
            }
        });

        HBox buttonBox = new HBox(submitButton);
        buttonBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(header, patientBox, commentBox, medicineBox, frequencyBox, buttonBox);

        Scene scene = new Scene(new StackPane(card), 500, 500);
        stage.setScene(scene);
        stage.show();
    }

    private static VBox createLabeledBox(String labelText, Control inputControl) {
        Label label = new Label(labelText);
        VBox box = new VBox(5, label, inputControl);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
