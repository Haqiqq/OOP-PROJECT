package org.rpms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MailComposer {
    private Stage stage;
    private ComboBox<String> recipientTypeComboBox;
    private ListView<String> recipientsListView;
    private TextField subjectField;
    private TextArea messageArea;

    public MailComposer(Stage primaryStage) {
        this.stage = new Stage();
        this.stage.initOwner(primaryStage);
        this.stage.setTitle("Email Composer");
    }

    public void show() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Recipient selection
        GridPane recipientPane = new GridPane();
        recipientPane.setHgap(10);
        recipientPane.setVgap(10);

        recipientTypeComboBox = new ComboBox<>();
        recipientTypeComboBox.getItems().addAll("All Users", "Administrators", "Doctors", "Patients");
        recipientTypeComboBox.setValue("All Users");
        recipientTypeComboBox.setOnAction(e -> updateRecipientsList());

        recipientsListView = new ListView<>();
        recipientsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Button loadRecipientsBtn = new Button("Load Recipients");
        loadRecipientsBtn.setOnAction(e -> updateRecipientsList());

        recipientPane.add(new Label("Recipient Type:"), 0, 0);
        recipientPane.add(recipientTypeComboBox, 1, 0);
        recipientPane.add(loadRecipientsBtn, 2, 0);
        recipientPane.add(new Label("Recipients:"), 0, 1);
        recipientPane.add(recipientsListView, 1, 1, 2, 1);

        // Email content
        subjectField = new TextField();
        subjectField.setPromptText("Subject");

        messageArea = new TextArea();
        messageArea.setPromptText("Your message here...");
        messageArea.setPrefRowCount(10);

        Button sendButton = new Button("Send Email");
        sendButton.setOnAction(e -> sendEmail());

        root.getChildren().addAll(
                new Label("Compose Email"),
                recipientPane,
                new Label("Subject:"),
                subjectField,
                new Label("Message:"),
                messageArea,
                sendButton
        );

        Scene scene = new Scene(root, 600, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void updateRecipientsList() {
        String recipientType = recipientTypeComboBox.getValue();
        ObservableList<String> recipients = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT email FROM users";

            if (!recipientType.equals("All Users")) {
                query += " WHERE user_type = ?";
                PreparedStatement stmt = conn.prepareStatement(query);

                switch (recipientType) {
                    case "Administrators":
                        stmt.setString(1, "ADMINISTRATOR");
                        break;
                    case "Doctors":
                        stmt.setString(1, "DOCTOR");
                        break;
                    case "Patients":
                        stmt.setString(1, "PATIENT");
                        break;
                }

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    recipients.add(rs.getString("email"));
                }
            } else {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    recipients.add(rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not load recipients: " + e.getMessage());
        }

        recipientsListView.setItems(recipients);
    }

    private void sendEmail() {
        String subject = subjectField.getText();
        String message = messageArea.getText();
        ObservableList<String> selectedRecipients = recipientsListView.getSelectionModel().getSelectedItems();

        if (subject.isEmpty() || message.isEmpty()) {
            showAlert("Error", "Subject and message cannot be empty");
            return;
        }

        if (selectedRecipients.isEmpty()) {
            showAlert("Error", "No recipients selected");
            return;
        }

        // Use your existing MyJavaEmail class to send emails
        try {
            MyJavaEmail emailSender = new MyJavaEmail();

            // For each recipient, send an email
            for (String recipient : selectedRecipients) {
                emailSender.createAndSendEmail(recipient, subject, message);

                // Also create a notification (using your Notifiable interface)
                Notifiable emailNotifier = new EmailNotification(recipient);
                emailNotifier.sendNotification(message);
            }

            showAlert("Success", "Emails sent successfully to " + selectedRecipients.size() + " recipients");
        } catch (Exception e) {
            showAlert("Error", "Failed to send emails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
