package org.rpms;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class DoctorApprovalHandler {

    private static final String URL = "jdbc:mysql://localhost:3306/rpms";
    private static final String USER = "root";
    private static final String PASSWORD = "Seeulateralligator1234";

    public void showPendingRequests(int doctorUserId) {
        Stage stage = new Stage();
        stage.setTitle("Pending Appointment Requests");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        ListView<String> requestList = new ListView<>();
        Button approveButton = new Button("Approve Selected");
        Button rejectButton = new Button("Reject Selected");
        Label statusLabel = new Label();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM appointment WHERE doctor_user_id = ? AND status = 'Requested'")) {

            stmt.setInt(1, doctorUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int appointmentId = rs.getInt("appointment_id");
                Timestamp apptDate = rs.getTimestamp("appointment_date");
                int patientId = rs.getInt("patient_user_id");
                String reason = rs.getString("reason");

                requestList.getItems().add(appointmentId + " | " + apptDate + " | Patient: " + patientId + " | " + reason);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        approveButton.setOnAction(e -> {
            String selected = requestList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int appointmentId = Integer.parseInt(selected.split(" \\| ")[0]);
                handleRequestUpdate(appointmentId, "Approved");
                requestList.getItems().remove(selected);
                statusLabel.setText("Request approved.");
            }
        });

        rejectButton.setOnAction(e -> {
            String selected = requestList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int appointmentId = Integer.parseInt(selected.split(" \\| ")[0]);
                handleRequestUpdate(appointmentId, "Rejected");
                requestList.getItems().remove(selected);
                statusLabel.setText("Request rejected.");
            }
        });

        layout.getChildren().addAll(new Label("Pending Requests:"), requestList, approveButton, rejectButton, statusLabel);

        Scene scene = new Scene(layout, 600, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void handleRequestUpdate(int appointmentId, String newStatus) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            // Get original appointment data
            PreparedStatement getStmt = conn.prepareStatement("SELECT * FROM appointment WHERE appointment_id = ?");
            getStmt.setInt(1, appointmentId);
            ResultSet rs = getStmt.executeQuery();

            if (rs.next()) {
                Timestamp date = rs.getTimestamp("appointment_date");
                int patientId = rs.getInt("patient_user_id");
                int doctorId = rs.getInt("doctor_user_id");
                String reason = rs.getString("reason");

                // If approved, update status and reminder_sent
                if (newStatus.equals("Approved")) {
                    PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE appointment SET status = ?, reminder_sent = TRUE, last_modified = NOW(), modified_by = ? WHERE appointment_id = ?");
                    updateStmt.setString(1, "Approved");
                    updateStmt.setString(2, "Doctor_" + doctorId); // Assuming the doctor modifies it
                    updateStmt.setInt(3, appointmentId);
                    updateStmt.executeUpdate();
                }

                // Update request status for rejected appointments
                if (newStatus.equals("Rejected")) {
                    PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE appointment SET status = ?, last_modified = NOW(), modified_by = ? WHERE appointment_id = ?");
                    updateStmt.setString(1, "Rejected");
                    updateStmt.setString(2, "Doctor_" + doctorId);
                    updateStmt.setInt(3, appointmentId);
                    updateStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
