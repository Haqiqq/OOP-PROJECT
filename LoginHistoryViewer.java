package org.rpms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginHistoryViewer {

    public static Scene createScene() {
        TableView<LoginEntry> table = new TableView<>();

        TableColumn<LoginEntry, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<LoginEntry, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        TableColumn<LoginEntry, String> timeCol = new TableColumn<>("Login Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("loginTime"));

        table.getColumns().addAll(usernameCol, roleCol, timeCol);
        table.setItems(fetchLoginData());

        VBox root = new VBox(10, table);
        root.setPadding(new Insets(20));

        return new Scene(root, 600, 400);
    }

    private static ObservableList<LoginEntry> fetchLoginData() {
        ObservableList<LoginEntry> list = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username, role, login_time FROM login_history ORDER BY login_time DESC")) {

            while (rs.next()) {
                list.add(new LoginEntry(
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getTimestamp("login_time").toString()
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static class LoginEntry {
        private final String username;
        private final String role;
        private final String loginTime;

        public LoginEntry(String username, String role, String loginTime) {
            this.username = username;
            this.role = role;
            this.loginTime = loginTime;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        public String getLoginTime() {
            return loginTime;
        }
    }
}

