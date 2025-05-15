package org.rpms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class PatientDashboard {
    private Stage primaryStage;
    private int userId;

    public PatientDashboard(Stage primaryStage, int userId) {
        this.primaryStage = primaryStage;
        this.userId = userId;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");

        VBox headerBox = createHeader();
        VBox contentBox = createContent();

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        root.setTop(headerBox);
        root.setCenter(scrollPane);

        return new Scene(root, 1024, 768);
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(10);
        headerBox.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 20px;");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8px;");
        logoutButton.setOnAction(e -> handleLogout());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label patientInfoLabel = getPatientInfo();
        patientInfoLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #1b5e20; -fx-font-weight: bold;");

        topBar.getChildren().addAll(patientInfoLabel, spacer, logoutButton);
        headerBox.getChildren().addAll(topBar);
        return headerBox;
    }

    private VBox createContent() {
        VBox contentBox = new VBox(30);
        contentBox.setPadding(new Insets(30));

        Label welcomeLabel = new Label("Welcome to Your Patient Portal");
        welcomeLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #33691e;");

        VBox quickActions = createQuickActions();
        VBox doctorList = createDoctorList();

        contentBox.getChildren().addAll(welcomeLabel, quickActions, doctorList);
        return contentBox;
    }

    private VBox createQuickActions() {
        VBox quickActions = new VBox(25);
        quickActions.setPadding(new Insets(25));
        quickActions.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-border-color: #c5e1a5; " +
                        "-fx-border-width: 1; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);"
        );

        Label quickActionsLabel = new Label("Quick Actions");
        quickActionsLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        // Button styles
        String buttonStyle = "-fx-background-color: #33691e; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 10px; -fx-min-width: 160px; -fx-pref-width: 200px; -fx-pref-height: 45px;";

        // Buttons
        Button addVitalsButton = new Button("Add Vitals");
        addVitalsButton.setOnAction(e -> new AddVitalsForm(new VitalDataBase(), userId));
        addVitalsButton.setStyle(buttonStyle);

        Button viewReportsButton = new Button("View Reports");
        viewReportsButton.setOnAction(e -> new PatientReportGenerator().showPatientReport(userId));
        viewReportsButton.setStyle(buttonStyle);

        Button requestAppointmentButton = new Button("Request Appointment");
        requestAppointmentButton.setOnAction(e -> new AppointmentRequestHandler().showRequestForm(userId));
        requestAppointmentButton.setStyle(buttonStyle);

        Button chatButton = new Button("Chat with Doctor");
        chatButton.setOnAction(e -> {
            try {
                Connection conn = DatabaseConnection.getConnection();
                new ChatWindow(userId, "PATIENT", "patient", conn);
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Chat Error");
                alert.setHeaderText("Could not open chat window");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });
        chatButton.setStyle(buttonStyle);

        // Grid for 2x2 buttons
        GridPane gridPane = new GridPane();
        gridPane.setHgap(25);
        gridPane.setVgap(25);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.add(addVitalsButton, 0, 0);
        gridPane.add(viewReportsButton, 1, 0);
        gridPane.add(chatButton, 0, 1);
        gridPane.add(requestAppointmentButton, 1, 1);

        // Emergency button centered below
        Button emergencyButton = new Button("EMERGENCY");
        emergencyButton.setStyle("-fx-background-color: #b71c1c; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 12px; -fx-padding: 12px 30px;");
        emergencyButton.setOnAction(e -> new EmergencyAlertHandler().handleEmergencyAlert(userId));

        HBox emergencyBox = new HBox(emergencyButton);
        emergencyBox.setAlignment(Pos.CENTER);
        emergencyBox.setPadding(new Insets(20, 0, 0, 0));

        quickActions.getChildren().addAll(quickActionsLabel, gridPane, emergencyBox);
        return quickActions;
    }


    private VBox createDoctorList() {
        VBox doctorListBox = new VBox(15);
        doctorListBox.setPadding(new Insets(20));
        doctorListBox.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-border-color: #c8e6c9; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 4);");

        Label doctorListLabel = new Label("Available Doctors");
        doctorListLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        VBox doctorInfoContainer = new VBox(10);
        doctorInfoContainer.setPadding(new Insets(10));

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.name, d.speciality FROM users u " +
                    "JOIN doctors d ON u.id = d.user_id WHERE u.user_type = 'doctor'";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String doctorName = rs.getString("name");
                String speciality = rs.getString("speciality");

                VBox doctorCard = new VBox(4);
                doctorCard.setPadding(new Insets(10));
                doctorCard.setStyle("-fx-background-color: #f1f8e9; -fx-background-radius: 8px; -fx-border-color: #aed581; -fx-border-width: 1;");

                Label nameLabel = new Label("Dr. " + doctorName);
                nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #33691e;");

                Label specialityLabel = new Label("Speciality: " + speciality);
                specialityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #558b2f;");

                doctorCard.getChildren().addAll(nameLabel, specialityLabel);
                doctorInfoContainer.getChildren().add(doctorCard);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Unable to load doctors.");
            errorLabel.setStyle("-fx-text-fill: red;");
            doctorInfoContainer.getChildren().add(errorLabel);
        }

        doctorListBox.getChildren().addAll(doctorListLabel, doctorInfoContainer);
        return doctorListBox;
    }

    private Label getPatientInfo() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.name, p.gender FROM users u " +
                    "JOIN patients p ON u.id = p.user_id WHERE u.id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String gender = rs.getString("gender");
                return new Label(name + " - " + gender);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Label("Patient Information Unavailable");
    }

    private void handleLogout() {
        LoginPage loginPage = new LoginPage(primaryStage);
        primaryStage.setScene(loginPage.createLoginScene());
        primaryStage.setTitle("RPMS Login");
    }
}
