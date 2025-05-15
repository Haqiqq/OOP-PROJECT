package org.rpms;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UploadVitals {

    public static void uploadFromCSV(Stage parentStage, int patientId) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));

        File file = fileChooser.showOpenDialog(parentStage);
        if (file == null) return;

        try {
            List<VitalSign> vitalsList = parseCSV(file);
            insertVitals(patientId, vitalsList);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Vitals uploaded successfully: " + vitalsList.size() + " records.");
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Upload failed: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private static List<VitalSign> parseCSV(File file) throws IOException {
        List<VitalSign> vitalsList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        boolean isHeader = true;

        while ((line = reader.readLine()) != null) {
            if (isHeader) {
                isHeader = false; // skip header
                continue;
            }

            String[] tokens = line.split(",");
            if (tokens.length < 8) continue;

            double pulse = Double.parseDouble(tokens[0]);
            double resp = Double.parseDouble(tokens[1]);
            String bp = tokens[2];
            double oxygen = Double.parseDouble(tokens[3]);
            double temp = Double.parseDouble(tokens[4]);
            double bmi = Double.parseDouble(tokens[5]);
            LocalDate date = LocalDate.parse(tokens[6]);
            String notes = tokens[7];

            VitalSign v = new VitalSign(pulse, resp, bp, oxygen, temp, bmi);
            v.setDate(date);
            v.setNotes(notes);
            vitalsList.add(v);
        }

        reader.close();
        return vitalsList;
    }

    private static void insertVitals(int patientId, List<VitalSign> vitalsList) throws Exception {
        String sql = "INSERT INTO vitals (patient_id, pulse, respiratory_rate, blood_pressure, " +
                "oxygen_saturation, temperature, bmi, date, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            for (VitalSign vital : vitalsList) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, patientId);
                    pstmt.setDouble(2, vital.getPulse());
                    pstmt.setDouble(3, vital.getRespiratoryRate());
                    pstmt.setString(4, vital.getBloodPressure());
                    pstmt.setDouble(5, vital.getOxygenSaturation());
                    pstmt.setDouble(6, vital.getTemp());
                    pstmt.setDouble(7, vital.getbmi());
                    pstmt.setDate(8, java.sql.Date.valueOf(vital.getDate()));
                    pstmt.setString(9, vital.getNotes());
                    pstmt.executeUpdate();
                }
            }
        }
    }
}
