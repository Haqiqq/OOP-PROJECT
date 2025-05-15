package org.rpms;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScheduleAppointmentHandler {

    private static final String URL = "jdbc:mysql://localhost:3306/rpms";
    private static final String USER = "root";
    private static final String PASSWORD = "Seeulateralligator1234";

    private final Map<String, Integer> patientMap = new HashMap<>();

    public void showAppointmentForm(int doctorUserId) {
        Stage formStage = new Stage();
        formStage.setTitle("Schedule Appointment");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        // Fields
        Label patientLabel = new Label("Select Patient:");
        ComboBox<String> patientComboBox = new ComboBox<>();
        loadPatients(patientComboBox);

        Label reasonLabel = new Label("Reason:");
        TextField reasonField = new TextField();

        Label dateLabel = new Label("Select Date:");
        DatePicker datePicker = new DatePicker();

        Label timeLabel = new Label("Select Time:");
        ComboBox<String> timeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00"
        ));

        Button submitButton = new Button("Schedule");
        Label feedbackLabel = new Label();

        submitButton.setOnAction(e -> {
            String selectedPatient = patientComboBox.getValue();
            String reason = reasonField.getText();
            var date = datePicker.getValue();
            String time = timeComboBox.getValue();

            if (selectedPatient == null || reason.isBlank() || date == null || time == null) {
                feedbackLabel.setText("All fields are required.");
                return;
            }

            try {
                int patientUserId = patientMap.get(selectedPatient);
                String dateTimeStr = date.toString() + " " + time;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date appointmentDate = sdf.parse(dateTimeStr);
                Date now = new Date();

                if (appointmentDate.before(now)) {
                    feedbackLabel.setText("Cannot schedule appointment in the past.");
                    return;
                }

                // Insert into DB
                insertAppointment(new java.sql.Timestamp(appointmentDate.getTime()), patientUserId, doctorUserId, reason);
                feedbackLabel.setText("Appointment scheduled successfully!");
            } catch (Exception ex) {
                feedbackLabel.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        Button viewAppointmentsButton = new Button("View Appointments");
        viewAppointmentsButton.setOnAction(e -> showAppointmentsForDoctor(doctorUserId));

        // Layout
        grid.add(patientLabel, 0, 0);
        grid.add(patientComboBox, 1, 0);
        grid.add(reasonLabel, 0, 1);
        grid.add(reasonField, 1, 1);
        grid.add(dateLabel, 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(timeLabel, 0, 3);
        grid.add(timeComboBox, 1, 3);
        grid.add(submitButton, 1, 4);
        grid.add(viewAppointmentsButton, 1, 5);
        grid.add(feedbackLabel, 1, 6);

        Scene scene = new Scene(grid, 500, 400);
        formStage.setScene(scene);
        formStage.show();
    }

    private void loadPatients(ComboBox<String> comboBox) {
        String sql = "SELECT id, name FROM users WHERE user_type = 'PATIENT'";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String display = name + " (ID: " + id + ")";
                patientMap.put(display, id);
                comboBox.getItems().add(display);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertAppointment(Timestamp appointmentDate, int patientId, int doctorId, String reason) {
        String sql = "INSERT INTO appointment (appointment_date, patient_user_id, doctor_user_id, reason, status, reminder_sent, created_at, created_by, last_modified, modified_by) " +
                "VALUES (?, ?, ?, ?, 'Pending', FALSE, NOW(), ?, NOW(), ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, appointmentDate);
            pstmt.setInt(2, patientId);
            pstmt.setInt(3, doctorId);
            pstmt.setString(4, reason);
            pstmt.setString(5, "Doctor_" + doctorId);
            pstmt.setString(6, "Doctor_" + doctorId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAppointmentsForDoctor(int doctorUserId) {
        Stage stage = new Stage();
        stage.setTitle("Appointments for Doctor " + doctorUserId);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        ListView<String> appointmentList = new ListView<>();
        Button deleteButton = new Button("Delete Selected Appointment");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT appointment_id, appointment_date, patient_user_id, reason FROM appointment WHERE doctor_user_id = ?")) {

            stmt.setInt(1, doctorUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("appointment_id");
                Timestamp date = rs.getTimestamp("appointment_date");
                int patient = rs.getInt("patient_user_id");
                String reason = rs.getString("reason");

                appointmentList.getItems().add(id + " | " + date + " | Patient: " + patient + " | " + reason);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        deleteButton.setOnAction(e -> {
            String selected = appointmentList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int appointmentId = Integer.parseInt(selected.split(" \\| ")[0]);
                deleteAppointmentById(appointmentId);
                appointmentList.getItems().remove(selected);
            }
        });

        layout.getChildren().addAll(new Label("Appointments:"), appointmentList, deleteButton);

        Scene scene = new Scene(layout, 600, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void deleteAppointmentById(int id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM appointment WHERE appointment_id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
