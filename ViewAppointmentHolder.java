package org.rpms;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

 class ViewAppointmentsHandler {

    private static final String URL = "jdbc:mysql://localhost:3306/rpms";
    private static final String USER = "root";
    private static final String PASSWORD = "Seeulateralligator1234";

    public void showAppointmentsForPatient(int patientUserId) {
        Stage stage = new Stage();
        stage.setTitle("Your Appointments");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        ListView<String> appointmentList = new ListView<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM appointment WHERE patient_user_id = ? ORDER BY appointment_date DESC")) {

            stmt.setInt(1, patientUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Timestamp date = rs.getTimestamp("appointment_date");
                int doctorId = rs.getInt("doctor_user_id");
                String reason = rs.getString("reason");
                String status = rs.getString("status");

                appointmentList.getItems().add(date + " | Doctor: " + doctorId + " | " + reason + " | Status: " + status);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        layout.getChildren().addAll(new Label("Your Upcoming Appointments:"), appointmentList);

        Scene scene = new Scene(layout, 600, 400);
        stage.setScene(scene);
        stage.show();
    }
}
