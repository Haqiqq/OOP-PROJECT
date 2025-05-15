package org.rpms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseTest {
    public static void main(String[] args) {
        testConnection();
        testUsers();
    }

    public static void testConnection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Test users table
            String query = "SELECT * FROM users";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nUsers in database:");
            System.out.println("----------------------------------------");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("User Type: " + rs.getString("user_type"));
                System.out.println("----------------------------------------");
            }

            // Test specific user
            query = "SELECT * FROM users WHERE email = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "admin@rpms.org");
            rs = pstmt.executeQuery();

            System.out.println("\nTesting admin user:");
            if (rs.next()) {
                System.out.println("Admin found!");
                System.out.println("Admin ID: " + rs.getInt("id"));
                System.out.println("Admin Name: " + rs.getString("name"));
            } else {
                System.out.println("Admin not found!");
            }

        } catch (SQLException e) {
            System.out.println("Error testing users!");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}