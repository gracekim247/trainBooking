import java.sql.*;

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

    public void listBookings() {
        String sql = "SELECT * FROM Booking";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String holder = "";
                    if (rs.getInt("confirmed") == 1) {
                        holder = "confirmed";
                    } else if (rs.getInt("waiting") == 1) {
                        holder = "waiting";
                    } else {
                        holder = "invalid";
                    }
                    System.out.println(
                        "Passenger ID: " + rs.getInt("passengerID") +
                        ", " + holder
                    );
            }
        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
        }
    }

    public void confirmBooking(int bookingID) {
        String sql = "UPDATE Booking SET confirmed = 1, waiting = 0 WHERE bookingID = " + bookingID + ";";
        try (Statement stmt = connection.createStatement()) {
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected > 0) {
                System.out.println("Booking Updated to Confirmed");
            } else {
                System.out.println("No booking found with ID: " + bookingID);
            }
        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
        }
    }

    public void invalidBooking(int bookingID) {
        String sql = "UPDATE Booking SET waiting = 0, invalid = 1 WHERE bookingID = " + bookingID + ";";
        try (Statement stmt = connection.createStatement()) {
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected > 0) {
                System.out.println("Booking Updated to Invalid");
            } else {
                System.out.println("No booking found with ID: " + bookingID);
            }
        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
        }
    }

    public void createPassenger(int passengerID, String firstName, String lastName, char sex, int age, String address) {
        String sql = "INSERT INTO Passenger (passengerID, first_name, last_name, sex, age, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, passengerID);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, String.valueOf(sex));  // sex is a char, convert to string
            pstmt.setInt(5, age);
            pstmt.setString(6, address);
    
            pstmt.executeUpdate();
            System.out.println("Passenger created");
        } catch (SQLException e) {
            System.err.println("Error creating passenger: " + e.getMessage());
        }
    }
    
    public void listPassengers(){
        String sql = "SELECT * FROM Passenger";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(
                    "Passenger ID: " + rs.getInt("passengerID") +
                    " Name: " + rs.getString("first_name") + " " + rs.getString("last_name")  +
                    " Sex: " + rs.getString("sex") +
                    " age: " + rs.getInt("age") + 
                    " address " + rs.getString("address")
                );
            }
        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
        }
    }

    public void createBooking(int ticketID, int bookingID, String bookDate, int seatNum, int confirmed, int waiting, int invalid, int passengerID) {
        String sql = "INSERT INTO Booking (ticketID, bookingID, bookDate, seatNum, confirmed, waiting, invalid, passengerID) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, ticketID);
            pstmt.setInt(2, bookingID);
            pstmt.setString(3, bookDate);
            pstmt.setInt(4, seatNum);
            pstmt.setInt(5, confirmed);
            pstmt.setInt(6, waiting);
            pstmt.setInt(7, invalid);
            pstmt.setInt(8, passengerID);
    
            pstmt.executeUpdate();
            System.out.println("Booking created");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void createTicket(int ticketID, int bookingID, int trainID, String travelDate, int print, int electronic) {
        String selectSQL = "SELECT seatNum FROM Booking WHERE bookingID = ? AND confirmed = 1";
        String insertSQL = "INSERT INTO Ticket (ticketID, print, electronic, trainID, travelDate, seatNum) VALUES (?, ?, ?, ?, ?, ?)";
    
        try (
            PreparedStatement selectStmt = connection.prepareStatement(selectSQL);
        ) {
            selectStmt.setInt(1, bookingID);
            ResultSet rs = selectStmt.executeQuery();
    
            if (rs.next()) {
                int seatNum = rs.getInt("seatNum");
    
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
                    insertStmt.setInt(1, ticketID);
                    insertStmt.setInt(2, print);
                    insertStmt.setInt(3, electronic);
                    insertStmt.setInt(4, trainID);
                    insertStmt.setString(5, travelDate);
                    insertStmt.setInt(6, seatNum);
    
                    insertStmt.executeUpdate();
                    System.out.println("Ticket created");
                }
    
            } else {
                System.out.println("Ticket not created");
            }
    
        } catch (SQLException e) {
            System.err.println("Error creating ticket: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        nelson db = new nelson();
        int bookingID = Integer.parseInt(args[0]);
        db.confirmBooking(bookingID);
        db.invalidBooking(bookingID);
        db.createPassenger(5, "Alice", "Smith", 'F', 28, "123 Elm Street");
        db.listPassengers();
        db.createBooking(1, 111, "2025-05-05", 12, 0, 1, 0, 1);
        db.listBookings();
        db.createTicket(4, 111, 5001, "2025-06-01", 1, 0);
        System.out.println("Hello, World!");
    }
}
