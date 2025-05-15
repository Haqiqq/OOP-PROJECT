package org.rpms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginPage {
    private Stage primaryStage;
    private TextField emailField;
    private PasswordField passwordField;
    private ComboBox<String> userTypeComboBox;
    private Label statusLabel;

    private static final String SYSTEM_TIME = "2025-05-11 10:20:32";
    private static final String CURRENT_USER = "Haqiqq";

    public LoginPage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String userType = userTypeComboBox.getValue();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE email = ? AND user_type = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, userType);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (password.equals(storedPassword)) {
                    showSuccess("Login successful!");
                    int userId = rs.getInt("id");

                    // Log the login
                    LoginLogger.log(email, userType);

                    // Show dashboard
                    showDashboard(userType, userId);
                } else {
                    showError("Invalid email/password combination");
                }
            } else {
                showError("User not found!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database error: " + e.getMessage());
        }
    }

    private void showDashboard(String userType, int userId) {
        try {
            switch (userType) {
                case "Administrator":
                    AdminDashboard adminDash = new AdminDashboard(primaryStage);
                    primaryStage.setScene(adminDash.createScene());
                    break;
                case "Doctor":
                    DoctorDashboard doctorDash = new DoctorDashboard(primaryStage, userId, SYSTEM_TIME, CURRENT_USER);
                    primaryStage.setScene(doctorDash.createScene());
                    break;
                case "Patient":
                    PatientDashboard patientDash = new PatientDashboard(primaryStage, userId);
                    primaryStage.setScene(patientDash.createScene());
                    break;
            }
            primaryStage.setTitle(userType + " Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.web("#d32f2f"));
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.web("#388e3c"));  // Darker green for success
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
    }

    public Scene createLoginScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #c8e6c9, #81c784);");  // Darker green gradient

        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50));

        Label title = new Label("Remote Patient Monitoring System");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#388e3c"));  // Darker green for title

        GridPane form = new GridPane();
        form.setAlignment(Pos.CENTER);
        form.setHgap(20);
        form.setVgap(20);
        form.setPadding(new Insets(30));
        form.setMaxWidth(400);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        form.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.15)));

        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        emailField = new TextField();
        emailField.setPromptText("Enter email");
        emailField.setPrefWidth(250);

        Label passLabel = new Label("Password:");
        passLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        Label typeLabel = new Label("User Type:");
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        userTypeComboBox = new ComboBox<>();
        userTypeComboBox.getItems().addAll("Administrator", "Doctor", "Patient");
        userTypeComboBox.setValue("Administrator");

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(100);
        loginButton.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-font-weight: bold;");
        loginButton.setOnAction(e -> handleLogin());

        // Button hover effect
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #2c6f33; -fx-text-fill: white; -fx-font-weight: bold;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-font-weight: bold;"));

        statusLabel = new Label("");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));

        form.add(emailLabel, 0, 0);
        form.add(emailField, 1, 0);
        form.add(passLabel, 0, 1);
        form.add(passwordField, 1, 1);
        form.add(typeLabel, 0, 2);
        form.add(userTypeComboBox, 1, 2);
        form.add(loginButton, 1, 3);
        form.add(statusLabel, 0, 4, 2, 1);

        container.getChildren().addAll(title, form);
        root.setCenter(container);

        return new Scene(root, 900, 600);
    }
}
