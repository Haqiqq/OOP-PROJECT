package org.rpms;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmergencyAlertHandler {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/rpms";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Seeulateralligator1234";

    public void handleEmergencyAlert(int patientUserId) {
        // Fetching administrator emails to send the alert to
        List<String> recipientEmails = getAdministratorEmails();

        // If no administrators are found, notify the user via GUI alert
        if (recipientEmails.isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Emergency Alert Failed");
                alert.setHeaderText(null);
                alert.setContentText("❌ No administrators found to send the emergency alert to.");
                alert.showAndWait();
            });
            return;
        }

        String patientName = getPatientName(patientUserId);

        // Construct the subject and message for the emergency alert email
        String subject = "🚨 Emergency Alert from Patient";
        String message = "<h2>Emergency Alert</h2>"
                + "<p>Patient <strong>" + patientName + "</strong> (User ID: " + patientUserId + ") "
                + "has triggered an emergency alert. Immediate attention required.</p>";

        // Send the email to all administrators
        for (String email : recipientEmails) {
            MyJavaEmail emailSender = new MyJavaEmail();
            emailSender.createAndSendEmail(email, subject, message);
        }

        // Show GUI alert indicating success
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Emergency Alert Sent");
            alert.setHeaderText(null);
            alert.setContentText("✅ Emergency alert has been sent successfully to administrators!");
            alert.showAndWait();
        });
    }

    // Fetch the email addresses of all administrators
    private List<String> getAdministratorEmails() {
        List<String> emails = new ArrayList<>();
        String query = "SELECT email FROM users WHERE user_type = 'ADMINISTRATOR'";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                emails.add(rs.getString("email"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return emails;
    }

    // Fetch the name of the patient based on the userId
    private String getPatientName(int userId) {
        String name = "Unknown Patient";
        String query = "SELECT name FROM users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                name = rs.getString("name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }
}
