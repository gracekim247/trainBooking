import java.sql.*;
import java.util.*;

public class nelson {
    private Connection connection;
    // Constructor to establish database connection
    public nelson() {
        try {
            // Load the SQLite JDBC driver (optional for newer versions)
            Class.forName("org.sqlite.JDBC");

            // Connect to the database
            connection = DriverManager.getConnection("jdbc:sqlite:train_booking.db");
            System.out.println("Connected to database successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    public void listTrains() {
        String sql = "SELECT * FROM Train";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(
                    "Train ID: " + rs.getInt("trainID") +
                    ", Model: " + rs.getString("model") +
                    ", Company: " + rs.getString("company")
                );
            }
        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        nelson db = new nelson();
        db.listTrains();
    }
}
