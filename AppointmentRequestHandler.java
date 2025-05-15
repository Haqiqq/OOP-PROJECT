package org.rpms;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppointmentRequestHandler {

    private static final String URL = "jdbc:mysql://localhost:3306/rpms";
    private static final String USER = "root";
    private static final String PASSWORD = "Seeulateralligator1234";

    public void showRequestForm(int patientUserId) {
        Stage formStage = new Stage();
        formStage.setTitle("Request Appointment");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        Label doctorLabel = new Label("Select Doctor:");
        ComboBox<String> doctorComboBox = new ComboBox<>();
        loadDoctors(doctorComboBox);

        Label reasonLabel = new Label("Reason:");
        TextField reasonField = new TextField();

        Label dateLabel = new Label("Select Date:");
        DatePicker datePicker = new DatePicker();

        Label timeLabel = new Label("Select Time:");
        ComboBox<String> timeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00"
        ));

        Button submitButton = new Button("Request");
        Label feedbackLabel = new Label();

        submitButton.setOnAction(e -> {
            String selectedDoctorInfo = doctorComboBox.getValue();
            String reason = reasonField.getText();
            var date = datePicker.getValue();
            String time = timeComboBox.getValue();

            if (selectedDoctorInfo == null || reason.isBlank() || date == null || time == null) {
                feedbackLabel.setText("All fields are required.");
                return;
            }

            try {
                // Extract the doctor ID from the selected string (name and speciality)
                int doctorUserId = extractDoctorId(selectedDoctorInfo);

                String dateTimeStr = date + " " + time;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date appointmentDate = sdf.parse(dateTimeStr);
                insertAppointment(new Timestamp(appointmentDate.getTime()), patientUserId, doctorUserId, reason);
                feedbackLabel.setText("Appointment request sent!");
            } catch (Exception ex) {
                feedbackLabel.setText("Error: " + ex.getMessage());
            }
        });

        grid.add(doctorLabel, 0, 0);
        grid.add(doctorComboBox, 1, 0);
        grid.add(reasonLabel, 0, 1);
        grid.add(reasonField, 1, 1);
        grid.add(dateLabel, 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(timeLabel, 0, 3);
        grid.add(timeComboBox, 1, 3);
        grid.add(submitButton, 1, 4);
        grid.add(feedbackLabel, 1, 5);

        Scene scene = new Scene(grid, 500, 400);
        formStage.setScene(scene);
        formStage.show();
    }

    private void loadDoctors(ComboBox<String> comboBox) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT u.id, u.name, d.speciality FROM users u JOIN doctors d ON u.id = d.user_id WHERE u.user_type = 'doctor'"
             );
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int doctorId = rs.getInt("id");
                String doctorName = rs.getString("name");
                String speciality = rs.getString("speciality");
                String doctorInfo = doctorName + " (" + speciality + ")";  // Combine name and speciality
                comboBox.getItems().add(doctorInfo);
                // Optionally, store the doctor ID in a map or elsewhere if needed for further processing
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int extractDoctorId(String doctorInfo) {
        // Extract the doctor ID from the selected ComboBox string
        // Assumes doctorInfo is in the format: "Doctor Name (Speciality)"
        String[] parts = doctorInfo.split(" \\(");
        String doctorName = parts[0].trim();
        String speciality = parts[1].replace(")", "").trim();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT id FROM users WHERE name = ? AND user_type = 'doctor'"
             )) {
            pstmt.setString(1, doctorName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Default error value if the ID cannot be found
    }

    private void insertAppointment(Timestamp appointmentDate, int patientId, int doctorId, String reason) {
        String sql = "INSERT INTO appointment (appointment_date, patient_user_id, doctor_user_id, reason, status, reminder_sent, created_at, created_by, last_modified, modified_by) " +
                "VALUES (?, ?, ?, ?, 'Requested', FALSE, NOW(), ?, NOW(), ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, appointmentDate);
            pstmt.setInt(2, patientId);
            pstmt.setInt(3, doctorId);
            pstmt.setString(4, reason);
            pstmt.setString(5, "Patient_" + patientId);
            pstmt.setString(6, "Patient_" + patientId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
