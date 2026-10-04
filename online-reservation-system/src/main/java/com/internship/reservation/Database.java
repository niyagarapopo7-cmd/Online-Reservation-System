package com.internship.reservation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final String URL = "jdbc:sqlite:reservation.db";

    private Database() { }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS trains (train_number TEXT PRIMARY KEY, train_name TEXT NOT NULL, " +
                    "travel_class TEXT NOT NULL, source TEXT NOT NULL, destination TEXT NOT NULL)");
            statement.execute("CREATE TABLE IF NOT EXISTS reservations (pnr TEXT PRIMARY KEY, passenger_name TEXT NOT NULL, " +
                    "train_number TEXT NOT NULL, train_name TEXT NOT NULL, travel_class TEXT NOT NULL, journey_date TEXT NOT NULL, " +
                    "source TEXT NOT NULL, destination TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }
        seedTrain("12627", "Karnataka Express", "Sleeper", "Bengaluru", "New Delhi");
        seedTrain("12628", "Karnataka Express", "AC 3 Tier", "New Delhi", "Bengaluru");
        seedTrain("12007", "Shatabdi Express", "AC Chair Car", "Chennai", "Mysuru");
        seedTrain("12675", "Kovai Express", "AC Chair Car", "Chennai", "Coimbatore");
    }

    private static void seedTrain(String number, String name, String travelClass, String source, String destination)
            throws SQLException {
        String sql = "INSERT OR IGNORE INTO trains(train_number, train_name, travel_class, source, destination) VALUES(?,?,?,?,?)";
        try (Connection connection = connect(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, number); ps.setString(2, name); ps.setString(3, travelClass);
            ps.setString(4, source); ps.setString(5, destination); ps.executeUpdate();
        }
    }
}