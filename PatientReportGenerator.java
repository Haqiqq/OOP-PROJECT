package org.rpms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.sql.*;

public class PatientReportGenerator {

    public void showPatientReport(int patientId) {
        Stage reportStage = new Stage();
        reportStage.setTitle("Patient Medical Report");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f7f6;");

        Label titleLabel = new Label("Complete Medical Report");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e67e22;");

        VBox vitalsBox = getVitalsSection(patientId);
        VBox feedbackBox = getFeedbackSection(patientId);

        HBox downloadBox = createDownloadSection(patientId);

        ScrollPane scrollPane = new ScrollPane(new VBox(20, titleLabel, vitalsBox, feedbackBox, downloadBox));
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent;");

        root.getChildren().add(scrollPane);

        Scene scene = new Scene(root, 800, 650);
        reportStage.setScene(scene);
        reportStage.show();
    }

    private VBox getVitalsSection(int patientId) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #fef4e7; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e67e22; -fx-border-width: 2;");

        Label header = new Label("Vitals History");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #d35400;");

        String query = "SELECT * FROM vitals WHERE patient_id = ? ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            VBox vitalsList = new VBox(5);

            while (rs.next()) {
                String entry = String.format("Date: %s | Pulse: %s bpm | Resp. Rate: %s | BP: %s | O2 Sat: %s%% | Temp: %.1f°C | BMI: %.1f\nNotes: %s",
                        rs.getString("date"),
                        rs.getString("pulse"),
                        rs.getString("respiratory_rate"),
                        rs.getString("blood_pressure"),
                        rs.getString("oxygen_saturation"),
                        rs.getDouble("temperature"),
                        rs.getDouble("bmi"),
                        rs.getString("notes")
                );
                Label recordLabel = new Label(entry);
                recordLabel.setWrapText(true);
                recordLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
                vitalsList.getChildren().add(recordLabel);
            }

            if (vitalsList.getChildren().isEmpty()) {
                vitalsList.getChildren().add(new Label("No vitals data found."));
            }

            box.getChildren().addAll(header, vitalsList);

        } catch (SQLException e) {
            e.printStackTrace();
            box.getChildren().add(new Label("Error retrieving vitals data."));
        }

        return box;
    }

    private VBox getFeedbackSection(int patientId) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #fff0e0; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e67e22; -fx-border-width: 2;");

        Label header = new Label("Doctor Feedback");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #d35400;");

        String query = "SELECT f.*, u.name AS doctor_name FROM feedback f " +
                "JOIN users u ON f.doctor_id = u.id WHERE f.patient_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            VBox feedbackList = new VBox(5);

            while (rs.next()) {
                String entry = String.format("Date: %s | Doctor: %s\nComments: %s\nMedicine: %s | Frequency: %s",
                        rs.getString("created_at"),
                        rs.getString("doctor_name"),
                        rs.getString("comments"),
                        rs.getString("medicine"),
                        rs.getString("frequency")
                );
                Label feedbackLabel = new Label(entry);
                feedbackLabel.setWrapText(true);
                feedbackLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
                feedbackList.getChildren().add(feedbackLabel);
            }

            if (feedbackList.getChildren().isEmpty()) {
                feedbackList.getChildren().add(new Label("No feedback records found."));
            }

            box.getChildren().addAll(header, feedbackList);

        } catch (SQLException e) {
            e.printStackTrace();
            box.getChildren().add(new Label("Error retrieving feedback data."));
        }

        return box;
    }

    private HBox createDownloadSection(int patientId) {
        Button downloadButton = new Button("Download Report");
        downloadButton.setStyle("-fx-font-size: 16px; -fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 10 20;");

        downloadButton.setOnAction(e -> downloadReport(patientId));

        HBox downloadBox = new HBox(10, downloadButton);
        downloadBox.setAlignment(Pos.CENTER);
        downloadBox.setPadding(new Insets(10, 0, 0, 0));

        return downloadBox;
    }

    private void downloadReport(int patientId) {
        String reportContent = generateReportContent(patientId);

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("Patient_Report_" + patientId + ".txt");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(reportContent);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Download Successful");
                alert.setHeaderText("Your report has been downloaded successfully.");
                alert.show();
            } catch (IOException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Download Error");
                alert.setHeaderText("Failed to save the report.");
                alert.show();
            }
        }
    }

    private String generateReportContent(int patientId) {
        StringBuilder reportContent = new StringBuilder();

        // Vitals
        reportContent.append("Vitals History\n");
        reportContent.append("--------------------------------------------------\n");

        String vitalsQuery = "SELECT * FROM vitals WHERE patient_id = ? ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(vitalsQuery)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                reportContent.append(String.format("Date: %s | Pulse: %s bpm | Resp. Rate: %s | BP: %s | O2 Sat: %s%% | Temp: %.1f°C | BMI: %.1f\nNotes: %s\n\n",
                        rs.getString("date"),
                        rs.getString("pulse"),
                        rs.getString("respiratory_rate"),
                        rs.getString("blood_pressure"),
                        rs.getString("oxygen_saturation"),
                        rs.getDouble("temperature"),
                        rs.getDouble("bmi"),
                        rs.getString("notes")
                ));
            }

            if (reportContent.indexOf("Vitals History") == 0) {
                reportContent.append("No vitals data found.\n\n");
            }

        } catch (SQLException e) {
            reportContent.append("Error retrieving vitals data.\n\n");
        }

        // Feedback
        reportContent.append("Doctor Feedback\n");
        reportContent.append("--------------------------------------------------\n");

        String feedbackQuery = "SELECT f.*, u.name AS doctor_name FROM feedback f " +
                "JOIN users u ON f.doctor_id = u.id WHERE f.patient_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(feedbackQuery)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                reportContent.append(String.format("Date: %s | Doctor: %s\nComments: %s\nMedicine: %s | Frequency: %s\n\n",
                        rs.getString("created_at"),
                        rs.getString("doctor_name"),
                        rs.getString("comments"),
                        rs.getString("medicine"),
                        rs.getString("frequency")
                ));
            }

            if (reportContent.indexOf("Doctor Feedback") == 0) {
                reportContent.append("No feedback records found.\n\n");
            }

        } catch (SQLException e) {
            reportContent.append("Error retrieving feedback data.\n\n");
        }

        return reportContent.toString();
    }
}
