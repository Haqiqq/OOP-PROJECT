package org.rpms;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.stage.FileChooser;

import java.io.File;


import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.io.PrintWriter;

public class ViewPatientsHandler {
    private static final String URL = "jdbc:mysql://localhost:3306/rpms";
    private static final String USER = "root";
    private static final String PASSWORD = "Seeulateralligator1234";

    public static void showPatientsList() {
        Stage stage = new Stage();
        stage.setTitle("Patients List");

        TableView<ObservableList<String>> patientTable = new TableView<>();
        patientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ObservableList<String>, String> idColumn = new TableColumn<>("Patient ID");
        idColumn.setCellValueFactory(param -> javafx.beans.binding.Bindings.valueAt(param.getValue(), 0));

        TableColumn<ObservableList<String>, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(param -> javafx.beans.binding.Bindings.valueAt(param.getValue(), 1));

        TableColumn<ObservableList<String>, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(param -> javafx.beans.binding.Bindings.valueAt(param.getValue(), 2));

        TableColumn<ObservableList<String>, String> phoneColumn = new TableColumn<>("Phone Number");
        phoneColumn.setCellValueFactory(param -> javafx.beans.binding.Bindings.valueAt(param.getValue(), 3));

        patientTable.getColumns().addAll(idColumn, nameColumn, emailColumn, phoneColumn);

        fetchPatients(patientTable);

        patientTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ObservableList<String> selectedPatient = patientTable.getSelectionModel().getSelectedItem();
                if (selectedPatient != null) {
                    showAllVitals(selectedPatient.get(0), selectedPatient.get(1)); // patientId and name
                }
            }
        });

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        Label instructionLabel = new Label("Double-click a patient to view all vitals");
        instructionLabel.setStyle("-fx-font-style: italic;");
        vbox.getChildren().addAll(instructionLabel, patientTable);

        stage.setScene(new Scene(vbox, 700, 500));
        stage.show();
    }

    private static void fetchPatients(TableView<ObservableList<String>> patientTable) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT id, name, email, phone_number FROM users WHERE user_type = 'PATIENT'";
            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    ObservableList<String> row = javafx.collections.FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("id")),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone_number")
                    );
                    patientTable.getItems().add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to fetch patients: " + e.getMessage());
        }
    }

    private static void showAllVitals(String patientId, String patientName) {
        Stage vitalsStage = new Stage();
        vitalsStage.setTitle("Vitals History for " + patientName);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane();
        VBox vitalsContainer = new VBox(15);
        vitalsContainer.setPadding(new Insets(10));
        scrollPane.setContent(vitalsContainer);
        scrollPane.setFitToWidth(true);

        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("Vitals History for ").append(patientName).append("\n")
                .append("====================================\n");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT * FROM vitals WHERE patient_id = ? ORDER BY date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, patientId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        String date = rs.getString("date");
                        int pulse = rs.getInt("pulse");
                        int respRate = rs.getInt("respiratory_rate");
                        String bp = rs.getString("blood_pressure");
                        int oxygen = rs.getInt("oxygen_saturation");
                        double temp = rs.getDouble("temperature");
                        double bmi = rs.getDouble("bmi");
                        String notes = rs.getString("notes");

                        VBox vitalsBox = new VBox(5);
                        vitalsBox.setStyle("-fx-border-color: #ccc; -fx-padding: 10;");
                        vitalsBox.getChildren().addAll(
                                new Label("Date: " + date),
                                new Label("Pulse: " + pulse + " bpm"),
                                new Label("Respiratory Rate: " + respRate + " breaths/min"),
                                new Label("Blood Pressure: " + bp),
                                new Label("Oxygen Saturation: " + oxygen + "%"),
                                new Label("Temperature: " + temp + " °C"),
                                new Label("BMI: " + bmi),
                                new Label("Notes: " + notes)
                        );
                        vitalsContainer.getChildren().add(vitalsBox);

                        reportBuilder.append("Date: ").append(date).append("\n")
                                .append("Pulse: ").append(pulse).append(" bpm\n")
                                .append("Respiratory Rate: ").append(respRate).append(" breaths/min\n")
                                .append("Blood Pressure: ").append(bp).append("\n")
                                .append("Oxygen Saturation: ").append(oxygen).append(" %\n")
                                .append("Temperature: ").append(temp).append(" °C\n")
                                .append("BMI: ").append(bmi).append("\n")
                                .append("Notes: ").append(notes).append("\n")
                                .append("------------------------------------\n");
                    }

                    if (!found) {
                        vitalsContainer.getChildren().add(new Label("No vitals data found for this patient."));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load vitals: " + e.getMessage());
        }

        Button trendButton = new Button("Show Vitals Trends");
        trendButton.setOnAction(e -> showTrends(patientId, patientName));

        Button downloadButton = new Button("Download Vitals Report");
        downloadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Vitals Report");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            fileChooser.setInitialFileName(patientName + "_Vitals_Report.txt");

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.write(reportBuilder.toString());
                    showAlert("Success", "Vitals report downloaded successfully.");
                } catch (Exception ex) {
                    showAlert("Error", "Failed to download report: " + ex.getMessage());
                }
            }
        });

        HBox buttonBox = new HBox(10, downloadButton, trendButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        vbox.getChildren().addAll(scrollPane, buttonBox);
        vitalsStage.setScene(new Scene(vbox, 500, 600));
        vitalsStage.show();
    }

    private static void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static void showTrends(String patientId, String patientName) {
        Stage chartStage = new Stage();
        chartStage.setTitle("Vitals Trends for " + patientName);

        TabPane tabPane = new TabPane();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT date, pulse, respiratory_rate, temperature, oxygen_saturation, bmi FROM vitals WHERE patient_id = ? ORDER BY date ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, patientId);
                ResultSet rs = pstmt.executeQuery();

                XYChart.Series<String, Number> pulseSeries = new XYChart.Series<>();
                pulseSeries.setName("Pulse");

                XYChart.Series<String, Number> respSeries = new XYChart.Series<>();
                respSeries.setName("Respiratory Rate");

                XYChart.Series<String, Number> tempSeries = new XYChart.Series<>();
                tempSeries.setName("Temperature");

                XYChart.Series<String, Number> oxygenSeries = new XYChart.Series<>();
                oxygenSeries.setName("Oxygen Saturation");

                XYChart.Series<String, Number> bmiSeries = new XYChart.Series<>();
                bmiSeries.setName("BMI");

                while (rs.next()) {
                    String date = rs.getString("date");
                    pulseSeries.getData().add(new XYChart.Data<>(date, rs.getInt("pulse")));
                    respSeries.getData().add(new XYChart.Data<>(date, rs.getInt("respiratory_rate")));
                    tempSeries.getData().add(new XYChart.Data<>(date, rs.getDouble("temperature")));
                    oxygenSeries.getData().add(new XYChart.Data<>(date, rs.getInt("oxygen_saturation")));
                    bmiSeries.getData().add(new XYChart.Data<>(date, rs.getDouble("bmi")));
                }

                tabPane.getTabs().add(createChartTab("Pulse", pulseSeries));
                tabPane.getTabs().add(createChartTab("Respiratory Rate", respSeries));
                tabPane.getTabs().add(createChartTab("Temperature", tempSeries));
                tabPane.getTabs().add(createChartTab("Oxygen Saturation", oxygenSeries));
                tabPane.getTabs().add(createChartTab("BMI", bmiSeries));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load trend data: " + e.getMessage());
        }

        Scene scene = new Scene(tabPane, 800, 500);
        chartStage.setScene(scene);
        chartStage.show();
    }

    private static Tab createChartTab(String label, XYChart.Series<String, Number> series) {
        NumberAxis yAxis = new NumberAxis();
        CategoryAxis xAxis = new CategoryAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(label + " Over Time");
        chart.getData().add(series);
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        VBox vbox = new VBox(chart);
        vbox.setPadding(new Insets(10));
        return new Tab(label, vbox);
    }



}
