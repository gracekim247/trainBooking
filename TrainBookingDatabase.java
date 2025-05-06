import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class TrainBookingDatabase
{
   public Connection connection;

   public TrainBookingDatabase(){
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
      String sql = "UPDATE Booking SET confirmed = 1, waiting = 0, invalid = 0 WHERE bookingID = " + bookingID + ";";
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
      String sql = "UPDATE Booking SET waiting = 0, invalid = 1, confirmed = 0 WHERE bookingID = " + bookingID + ";";
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

   public void generateReport(int passengerID) {
      String sql = "SELECT p.first_name, p.last_name, b.bookingID, b.bookDate, b.seatNum, " +
                   "t.trainID, t.travelDate, b.confirmed, b.waiting, b.invalid " +
                   "FROM Passenger p " +
                   "JOIN Booking b ON p.passengerID = b.passengerID " +
                   "JOIN Ticket t ON b.ticketID = t.ticketID " +
                   "WHERE p.passengerID = ?";

      try(PreparedStatement stmt = connection.prepareStatement(sql)) {
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
            System.out.println("trainID: " + rs.getInt("trainID") +
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
                  System.out.print("Enter passenger's ID number: ");
                  int pID = scanner.nextInt();
                  scanner.nextLine();
                  System.out.print("Enter passenger's first name: ");
                  String firstName = scanner.nextLine();
                  System.out.print("Enter passenger's last name: ");
                  String lastName = scanner.nextLine();
                  System.out.print("Enter passenger's sex (M/F): ");
                  char sex = scanner.nextLine().charAt(0);
                  System.out.print("Enter passenger's age: ");
                  // int age = scanner.nextInt();
                  // scanner.nextLine();
                  String age = scanner.nextLine();
                  int intAge = Integer.parseInt(age);
                  System.out.print("Enter passenger's address: ");
                  String address = scanner.nextLine();
                  
                  System.out.println();
                  System.out.println("===");


                  db.createPassenger(pID, firstName, lastName, sex, intAge, address);
                  break;

               case 2:
                  // 2
                  System.out.print("Enter today's date (MM-DD-YY): ");
                  String bookingDate = scanner.nextLine();
                  System.out.print("Enter booking ID number: ");
                  int bID = scanner.nextInt();
                  scanner.nextLine();
                  System.out.print("Enter passenger's ID number: ");
                  int cBpID = scanner.nextInt();
                  scanner.nextLine();
                  System.out.print("Enter seat number: ");
                  int seatNum = scanner.nextInt();
                  scanner.nextLine();
                  System.out.print("Enter if confirmed (0/1): ");
                  int confirmed = scanner.nextInt();
                  scanner.nextLine();
                  System.out.print("Enter if waiting (0/1): ");
                  int waiting = scanner.nextInt();
                  scanner.nextLine();
                  System.out.print("Enter if invalid (0/1): ");
                  int invalid = scanner.nextInt();
                  scanner.nextLine();
                  System.out.println("===");

                  db.createBooking(1, bID, bookingDate, seatNum, confirmed, waiting, invalid, cBpID);
                  break;

               case 3:
                  System.out.print("Enter booking ID: ");
                  int bookingID = scanner.nextInt();
                  scanner.nextLine();
                  System.out.print("To update to confirmed, enter 'c'. To update to invalid, enter 'i': ");
                  String change = scanner.nextLine();
                  System.out.println("===");
                  
                  if("c".equals(change)){
                     db.confirmBooking(bookingID);
                  } else{
                     db.invalidBooking(bookingID);
                  }
                  break;

               case 4:
                  System.out.print("Enter your booking ID: ");
                  int newbID = scanner.nextInt();
                  scanner.nextLine();
                  System.out.print("Enter your ID number: ");
                  int uppID = scanner.nextInt();
                  scanner.nextLine();
                  System.out.println("===");

                  db.deleteBookingWithoutTicket(newbID, uppID);
                  break;
                  
               case 5:
                  System.out.print("Enter your ID number : ");
                  int cpID = scanner.nextInt();
                  scanner.nextLine();
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