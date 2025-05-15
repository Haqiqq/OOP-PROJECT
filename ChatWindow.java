package org.rpms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatWindow {
    private int senderId;
    private Connection conn;
    private String senderName;
    private Stage chatStage;
    private TextArea chatArea;
    private Map<String, Integer> userMap;
    private int selectedReceiverId = -1;
    private String selectedUserName = "";
    private String userType;

    public ChatWindow(int senderId, String senderName, String userType, Connection conn) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.userType = userType;
        this.conn = conn;
        this.userMap = new HashMap<>();

        initializeUI();
    }

    private void initializeUI() {
        chatStage = new Stage();
        chatStage.setTitle("RPMS Chat - " + senderName + " (" + userType + ")");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: #f0f5ff;");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label selectUserLabel = new Label("Select user to chat with:");
        selectUserLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<String> userComboBox = new ComboBox<>();
        userComboBox.setPrefWidth(200);
        userComboBox.setPromptText("Select user");

        Button refreshButton = new Button("↻");
        refreshButton.setTooltip(new Tooltip("Refresh user list"));
        refreshButton.setOnAction(e -> populateUserList(userComboBox));

        headerBox.getChildren().addAll(selectUserLabel, userComboBox, refreshButton);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefHeight(400);
        chatArea.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);

        TextField messageInput = new TextField();
        messageInput.setPromptText("Type your message...");
        messageInput.setPrefWidth(350);
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #4286f4; -fx-text-fill: white;");

        Button callButton = new Button("Start Call");
        callButton.setStyle("-fx-background-color: #42b972; -fx-text-fill: white;");
        callButton.setDisable(true);

        inputBox.getChildren().addAll(messageInput, sendButton, callButton);

        HBox statusBar = new HBox(10);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Ready to chat");
        statusBar.getChildren().add(statusLabel);

        layout.getChildren().addAll(headerBox, chatArea, inputBox, statusBar);

        populateUserList(userComboBox);

        sendButton.setOnAction(e -> {
            String message = messageInput.getText().trim();
            if (!message.isEmpty() && selectedReceiverId != -1) {
                sendMessage(message, selectedReceiverId, statusLabel);
                chatArea.appendText("You: " + message + "\n");
                messageInput.clear();
            } else if (selectedReceiverId == -1) {
                showAlert("Please select a user to chat with first.");
            }
        });

        messageInput.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                sendButton.fire();
            }
        });

        userComboBox.setOnAction(e -> {
            String selectedUser = userComboBox.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                selectedUserName = selectedUser;
                selectedReceiverId = userMap.get(selectedUser);
                loadChatHistory(selectedReceiverId, selectedUser);
                callButton.setDisable(false);
                statusLabel.setText("Chatting with " + selectedUser);
            }
        });

        callButton.setOnAction(e -> {
            if (selectedReceiverId != -1) {
                generateZoomCallLink(selectedReceiverId, selectedUserName);
            }
        });

        chatStage.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && selectedReceiverId != -1) {
                loadChatHistory(selectedReceiverId, selectedUserName);
            }
        });

        setupPeriodicUpdates();

        chatStage.setOnCloseRequest(e -> {});

        Scene scene = new Scene(layout, 500, 600);
        chatStage.setScene(scene);
        chatStage.show();
    }

    private void populateUserList(ComboBox<String> userComboBox) {
        userComboBox.getItems().clear();
        userMap.clear();

        try {
            String filterQuery = "";
            if ("doctor".equalsIgnoreCase(userType)) {
                filterQuery = " AND user_type = 'patient'";
            } else if ("patient".equalsIgnoreCase(userType)) {
                filterQuery = " AND user_type = 'doctor'";
            }

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, name, user_type FROM users WHERE id != ?" + filterQuery
            );
            ps.setInt(1, senderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("user_type");

                String displayName = name + " (" + type + ")";
                userComboBox.getItems().add(displayName);
                userMap.put(displayName, id);
            }

            userComboBox.getItems().sort(String::compareTo);

        } catch (SQLException ex) {
            showAlert("Error loading user list: " + ex.getMessage());
        }
    }

    private void loadChatHistory(int receiverId, String receiverName) {
        chatArea.clear();

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT sender, message, timestamp FROM messages " +
                            "WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) " +
                            "ORDER BY timestamp"
            );
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setInt(3, receiverId);
            ps.setInt(4, senderId);

            ResultSet rs = ps.executeQuery();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            while (rs.next()) {
                int sender = rs.getInt("sender");
                String msg = rs.getString("message");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                String timeStr = timestamp != null ? "[" + formatter.format(timestamp.toLocalDateTime()) + "] " : "";
                String fromName = (sender == senderId) ? "You" : receiverName;

                if (msg.startsWith("Call Link:")) {
                    chatArea.appendText(timeStr + fromName + ": [ZOOM LINK] " + msg.substring(10).trim() + "\n");
                } else {
                    chatArea.appendText(timeStr + fromName + ": " + msg + "\n");
                }
            }

            chatArea.positionCaret(chatArea.getText().length());

        } catch (SQLException ex) {
            showAlert("Error loading chat history: " + ex.getMessage());
        }
    }

    private void sendMessage(String message, int receiverId, Label statusLabel) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO messages (sender, receiver, message, timestamp) VALUES (?, ?, ?, NOW())"
            );
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, message);

            int result = ps.executeUpdate();

            statusLabel.setText(result > 0 ? "Message sent successfully" : "Failed to send message");

        } catch (SQLException ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            showAlert("Error sending message: " + ex.getMessage());
        }
    }

    private void generateZoomCallLink(int receiverId, String receiverName) {
        try {
            // Simulate a Zoom link format
            String meetingId = String.valueOf((int) (100000000 + Math.random() * 900000000));
            String password = UUID.randomUUID().toString().substring(0, 6);
            String callLink = "https://zoom.us/j/" + meetingId + "?pwd=" + password;

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO messages (sender, receiver, message, timestamp) VALUES (?, ?, ?, NOW())"
            );
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, "Call Link: " + callLink);
            ps.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Zoom Call Initiated");
            alert.setHeaderText("Zoom call with " + receiverName);

            TextArea textArea = new TextArea("Zoom link:\n" + callLink +
                    "\n\nThis link has also been sent in the chat.");
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefWidth(400);
            textArea.setPrefHeight(150);

            VBox content = new VBox(10);
            content.getChildren().add(textArea);
            alert.getDialogPane().setContent(content);
            alert.showAndWait();

            chatArea.appendText("You: [ZOOM LINK SHARED]\n");

        } catch (SQLException ex) {
            showAlert("Error generating Zoom link: " + ex.getMessage());
        }
    }

    private void setupPeriodicUpdates() {
        Thread updateThread = new Thread(() -> {
            while (chatStage.isShowing()) {
                if (selectedReceiverId != -1) {
                    Platform.runLater(() -> loadChatHistory(selectedReceiverId, selectedUserName));
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        updateThread.setDaemon(true);
        updateThread.start();
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Chat Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void refreshChat() {
        if (selectedReceiverId != -1) {
            loadChatHistory(selectedReceiverId, selectedUserName);
        }
    }
}