package org.rpms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

class DoctorDashboard {
    private Stage primaryStage;
    private int userId;
    private String currentTime;
    private String currentUser;

    public DoctorDashboard(Stage primaryStage, int userId, String currentTime, String currentUser) {
        this.primaryStage = primaryStage;
        this.userId = userId;
        this.currentTime = currentTime;
        this.currentUser = currentUser;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

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
        headerBox.setStyle("-fx-background-color: #f1f8e9; -fx-padding: 20px;");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8px;");
        logoutButton.setOnAction(e -> handleLogout());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label doctorInfoLabel = getDoctorInfo();
        doctorInfoLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #558b2f; -fx-font-weight: bold;");

        topBar.getChildren().addAll(doctorInfoLabel, spacer, logoutButton);

        headerBox.getChildren().addAll(topBar);
        return headerBox;
    }

    private VBox createContent() {
        VBox contentBox = new VBox(30);
        contentBox.setPadding(new Insets(30));

        Label welcomeLabel = new Label("Welcome to Your Doctor Dashboard");
        welcomeLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #7cb342;");

        VBox quickActions = createQuickActions();

        contentBox.getChildren().addAll(welcomeLabel, quickActions);
        return contentBox;
    }

    private VBox createQuickActions() {
        VBox quickActions = new VBox(15);
        quickActions.setPadding(new Insets(20));
        quickActions.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-border-color: #dcedc8; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label quickActionsLabel = new Label("Quick Actions");
        quickActionsLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #7cb342;");

        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(20);
        buttonGrid.setVgap(20);
        buttonGrid.setAlignment(Pos.CENTER);

        Button viewPatientsButton = new Button("View Patients");
        viewPatientsButton.setOnAction(e -> ViewPatientsHandler.showPatientsList());

        Button scheduleAppointmentButton = new Button("Schedule Appointment");
        scheduleAppointmentButton.setOnAction(e -> new ScheduleAppointmentHandler().showAppointmentForm(userId));

        Button approveRequestsButton = new Button("Approve Requests");
        approveRequestsButton.setOnAction(e -> new DoctorApprovalHandler().showPendingRequests(userId));

        Button feedbackButton = new Button("Give Feedback");
        feedbackButton.setOnAction(e -> GiveFeedbackHandler.showFeedbackForm(userId));

        Button chatButton = new Button("Chat with Patient");
        chatButton.setOnAction(e -> {
            try {
                Connection conn = DatabaseConnection.getConnection();
                new ChatWindow(userId, currentUser, "doctor", conn);
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Chat Error", "Could not open chat window", ex.getMessage());
            }

    });
        for (Button btn : new Button[]{viewPatientsButton, scheduleAppointmentButton, approveRequestsButton, chatButton, feedbackButton}) {
            btn.setStyle("-fx-background-color: #aed581; -fx-text-fill: #33691e; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-min-width: 160px; -fx-pref-width: 200px;");
        }

        buttonGrid.add(viewPatientsButton, 0, 0);
        buttonGrid.add(scheduleAppointmentButton, 1, 0);
        buttonGrid.add(approveRequestsButton, 0, 1);
        buttonGrid.add(chatButton, 1, 1);
        buttonGrid.add(feedbackButton, 0, 2, 2, 1); // Centered across both columns

        quickActions.getChildren().addAll(quickActionsLabel, buttonGrid);
        return quickActions;
    }


    private Label getDoctorInfo() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.name, d.speciality FROM users u JOIN doctors d ON u.id = d.user_id WHERE u.id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String speciality = rs.getString("speciality");
                return new Label(name + " - " + speciality);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Label("Doctor Information Unavailable");
    }

    private void handleLogout() {
        LoginPage loginPage = new LoginPage(primaryStage);
        primaryStage.setScene(loginPage.createLoginScene());
        primaryStage.setTitle("RPMS Login");
    }

    private void showError(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
