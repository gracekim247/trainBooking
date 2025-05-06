import java.sql.*;
import java.util.Scanner;


public class elana {
    public static void main(String[] args) {
        nelson db = new nelson();
        //read input from console
        Scanner scanner = new Scanner(System.in);

        //prompts choice
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        //newline char after
        scanner.nextLine(); 

        //option generate report
        if(choice == 5) {
            System.out.print("Enter your ID number: ");
            int passengerID = scanner.nextInt();
            scanner.nextLine();
            System.out.println("===\n");

            generateReport(db, passengerID);

        } else {
            System.out.println("Other option placeholder.");
        }
        scanner.close(); 
    }

    public static void generateReport(nelson db, int passengerID) {
        String sql = "SELECT p.first_name, p.last_name, b.bookingID, b.bookDate, b.seatNum, " +
                     "t.trainID, t.travelDate, b.confirmed, b.waiting, b.invalid " +
                     "FROM Passenger p " +
                     "JOIN Booking b ON p.passengerID = b.passengerID " +
                     "JOIN Ticket t ON b.ticketID = t.ticketID " +
                     "WHERE p.passengerID = ?";

        try {
        //prepares sql statement
        //pid = ?
        PreparedStatement stmt = db.connection.prepareStatement(sql);
        stmt.setInt(1, passengerID);
        ResultSet rs = stmt.executeQuery();

        //matching rows?
        boolean found = false;
        
        String first = rs.getString("first_name");
        String last = rs.getString("last_name");

        while (rs.next()) {
            if (!found) {
                System.out.println("Ticket Report for " + first + " " + last + ":");
                found = true;
            }

            String status = "Unknown";
            if (rs.getInt("confirmed") == 1) status = "Confirmed";
            else if (rs.getInt("waiting") == 1) status = "Waiting";
            else if (rs.getInt("invalid") == 1) status = "Invalid";

            //need to add destination
            System.out.println("Trip: Departure Date: " + rs.getString("travelDate") +
                               ", Destination: __?__" +
                               ", trainID: " + rs.getInt("trainID") +
                               ", seat #: " + rs.getInt("seatNum") +
                               ", booking ID #: " + rs.getInt("bookingID") +
                               ", booking status: " + status);
        }

        if (!found) {
            System.out.println(first + last + "has not booked any flights.");
        }

    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
    }
   
}
