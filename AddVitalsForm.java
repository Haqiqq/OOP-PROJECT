package org.rpms;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class AddVitalsForm {

    private VitalDataBase vitalDataBase;
    private int patientId;

    public AddVitalsForm(VitalDataBase vitalDataBase, int patientId) {
        this.vitalDataBase = vitalDataBase;
        this.patientId = patientId;
        showForm();
    }

    private void showForm() {
        Stage stage = new Stage();
        stage.setTitle("Add Vitals");

        // Grid layout
        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(12);
        grid.setPadding(new Insets(20));

        // ComboBoxes or Spinners for vital entries
        Spinner<Double> pulseSpinner = createDoubleSpinner(40, 200, 70, 1);
        Spinner<Double> respSpinner = createDoubleSpinner(8, 40, 16, 1);
        TextField bpField = new TextField();
        Spinner<Double> oxygenSpinner = createDoubleSpinner(50, 100, 98, 1);
        Spinner<Double> tempSpinner = createDoubleSpinner(95, 105, 98.6, 0.1);
        Spinner<Double> bmiSpinner = createDoubleSpinner(10, 50, 22, 0.5);

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Optional notes...");
        notesArea.setPrefRowCount(3);

        DatePicker datePicker = new DatePicker(LocalDate.now());

        // Labels + fields
        grid.add(new Label("Pulse (bpm):"), 0, 0); grid.add(pulseSpinner, 1, 0);
        grid.add(new Label("Respiratory Rate:"), 0, 1); grid.add(respSpinner, 1, 1);
        grid.add(new Label("Blood Pressure (e.g., 120/80):"), 0, 2); grid.add(bpField, 1, 2);
        grid.add(new Label("Oxygen Saturation (%):"), 0, 3); grid.add(oxygenSpinner, 1, 3);
        grid.add(new Label("Temperature (°F):"), 0, 4); grid.add(tempSpinner, 1, 4);
        grid.add(new Label("BMI:"), 0, 5); grid.add(bmiSpinner, 1, 5);
        grid.add(new Label("Date:"), 0, 6); grid.add(datePicker, 1, 6);
        grid.add(new Label("Notes:"), 0, 7); grid.add(notesArea, 1, 7);

        Button saveButton = new Button("💾 Save Vitals");
        saveButton.setStyle("-fx-font-weight: bold; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        grid.add(saveButton, 1, 8);

        Button uploadButton = new Button("📤 Upload Vitals");
        uploadButton.setStyle("-fx-font-weight: bold; -fx-background-color: #2196F3; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, uploadButton);
        grid.add(buttonBox, 1, 9); // Placed under row 8 (save)

        uploadButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Upload functionality not implemented yet.");
            alert.showAndWait();
        });

        uploadButton.setOnAction(e -> {
            UploadVitals.uploadFromCSV(stage, patientId);
        });

        saveButton.setOnAction(e -> {
            try {
                double pulse = pulseSpinner.getValue();
                double respiratoryRate = respSpinner.getValue();
                String bloodPressure = bpField.getText().trim();
                double oxygenSaturation = oxygenSpinner.getValue();
                double temperature = tempSpinner.getValue();
                double bmi = bmiSpinner.getValue();
                String notes = notesArea.getText().trim();
                LocalDate date = datePicker.getValue();

                if (bloodPressure.isEmpty()) {
                    showError("Blood pressure cannot be empty");
                    return;
                }

                VitalSign vital = new VitalSign(pulse, respiratoryRate, bloodPressure,
                        oxygenSaturation, temperature, bmi);
                vital.setDate(date);
                vital.setNotes(notes);

                vitalDataBase.add_vital_sign(vital);
                saveToDatabase(vital);

                new Alert(Alert.AlertType.INFORMATION, "Vitals saved successfully!").showAndWait();
                stage.close();

            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().add(grid);

        stage.setScene(new Scene(root, 450, 550));
        stage.show();
    }

    private Spinner<Double> createDoubleSpinner(double min, double max, double initial, double step) {
        Spinner<Double> spinner = new Spinner<>(min, max, initial, step);
        spinner.setEditable(true);
        spinner.setPrefWidth(200);
        return spinner;
    }

    private void saveToDatabase(VitalSign vital) throws SQLException {
        String sql = "INSERT INTO vitals (patient_id, pulse, respiratory_rate, blood_pressure, " +
                "oxygen_saturation, temperature, bmi, date, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            pstmt.setDouble(2, vital.getPulse());
            pstmt.setDouble(3, vital.getRespiratoryRate());
            pstmt.setString(4, vital.getBloodPressure());
            pstmt.setDouble(5, vital.getOxygenSaturation());
            pstmt.setDouble(6, vital.getTemp());
            pstmt.setDouble(7, vital.getbmi());
            pstmt.setDate(8, java.sql.Date.valueOf(vital.getDate()));
            pstmt.setString(9, vital.getNotes());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating vital failed, no rows affected.");
            }
            System.out.println("Vitals saved to database.");
        }
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
