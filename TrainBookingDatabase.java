import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class TrainBookingDatabase
{
   private Connection connection;

   public TrainBookingDatabase(){
      try{
         // create a database connection
         this.connection = DriverManager.getConnection("jdbc:sqlite:train_booking.db");
      }
      catch(SQLException e){
         System.err.println("Database connection error " + e.getMessage());
      }
   }

   public void deleteBooking(int ticketID, int bookingID, int passengerID){
      String sql =
         "DELETE FROM Booking "
         + "WHERE ticketID   = ? "
         + "  AND bookingID  = ? "
         + "  AND passengerID = ?;";

      try(PreparedStatement stmt = connection.prepareStatement(sql)) {
         stmt.setInt(1, ticketID);
         stmt.setInt(2, bookingID);
         stmt.setInt(3, passengerID);

         int affected = stmt.executeUpdate();
         if (affected > 0) {
               System.out.println("Booking cancelled successfully.");
         } else {
               System.out.printf(
                  "No booking found for ticket %d, booking %d, passenger %d.%n",
                  ticketID, bookingID, passengerID
               );
         }
      } catch (SQLException e) {
         System.err.println("Error cancelling booking: " + e.getMessage());
         e.printStackTrace();
     }
   }

   public void deleteBookingWithoutTicket(int bookingID, int passengerID) {
      String lookup = 
         "SELECT ticketID FROM Booking "
         + "WHERE bookingID = ? AND passengerID = ?";

      try(PreparedStatement ps = connection.prepareStatement(lookup)){ 
         ps.setInt(1, bookingID);
         ps.setInt(2, passengerID);
         ResultSet rs = ps.executeQuery();

         if (!rs.next()) {
            System.out.println("No such booking found.");
            return;
         }

         int ticketID = rs.getInt("ticketID");
         deleteBooking(ticketID, bookingID, passengerID);
      } catch (SQLException e) {
         System.err.println("Error looking up booking: " + e.getMessage());
         e.printStackTrace();
     }
   }

   public void addPassenger(String fn, String ln, String sex, int age, String addr) { /* … */ }
   public void createBooking(String bd, int pID, String dest, String dd)      { /* … */ }
   public void updateBooking(int bID, int pID, String change, String value)  { /* … */ }
   public void generateReport(int pID) {/* */}

   public static void main(String[] args){
      TrainBookingDatabase db = new TrainBookingDatabase();
      try (Scanner scanner = new Scanner(System.in)){
         while(true){
            System.out.println("Train Booking System:");
            System.out.println("  1. Add Passenger Profile");
            System.out.println("  2. Create Booking");
            System.out.println("  3. Update Booking");
            System.out.println("  4. Cancel Booking");
            System.out.println("  5. Generate Ticket Report");
            System.out.println("  6. Exit");

            System.out.print("Enter your choice: ");

            int ans = scanner.nextInt();
            scanner.nextLine();

            switch (ans){
               case 1:
                  System.out.print("Enter your first name: ");
                  String firstName = scanner.nextLine();
                  System.out.print("Enter your last name: ");
                  String lastName = scanner.nextLine();
                  System.out.print("Enter your sex (M/F): ");
                  String sex = scanner.nextLine();
                  System.out.print("Enter your age: ");
                  int age = scanner.nextInt();
                  System.out.print("Enter your address: ");
                  String address = scanner.nextLine();
                  System.out.println("===");

                  db.addPassenger(firstName, lastName, sex, age, address);
                  break;

               case 2:
                  System.out.print("Enter today's date (MM-DD-YY): ");
                  String bookingDate = scanner.nextLine();
                  System.out.print("Enter your ID number: ");
                  int pID = scanner.nextInt();
                  System.out.print("Enter your destination: ");
                  String dest = scanner.nextLine();
                  System.out.print("Enter your departure date: ");
                  String deptDate = scanner.nextLine();
                  System.out.println("===");

                  db.createBooking(bookingDate, pID, dest, deptDate);
                  break;

               case 3:
                  System.out.print("Enter your booking ID: ");
                  int bID = scanner.nextInt();
                  System.out.print("Enter your ID number: ");
                  int newpID = scanner.nextInt();
                  System.out.print("To update your destination, enter 'd'. To update your departure date, enter 'date': ");
                  String change = scanner.nextLine();
                  System.out.print("Enter your new destination/departure date: ");
                  String changeValue = scanner.nextLine();
                  System.out.println("===");

                  db.updateBooking(bID, newpID, change, changeValue);
                  break;

               case 4:
                  System.out.print("Enter your booking ID: ");
                  int newbID = scanner.nextInt();
                  System.out.print("Enter your ID number: ");
                  int uppID = scanner.nextInt();
                  System.out.println("===");

                  db.deleteBookingWithoutTicket(newbID, uppID);
                  break;
                  
               case 5:
                  System.out.print("Enter your ID number : ");
                  int cpID = scanner.nextInt();
                  System.out.println("===");

                  db.generateReport(cpID);
                  break;

               case 6:
                  System.out.println("Have a good day!");
                  return;

               default: 
                  System.out.println("Invalid choice.");
            }
         }
      } catch (Exception e) {
         System.err.println("An error occurred: " + e.getMessage());
         e.printStackTrace();
     }
   }
}