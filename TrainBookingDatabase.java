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
         connection = DriverManager.getConnection("jdbc:sqlite:train_booking copy.db");
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

    public Integer createBooking(int bookingID, String bookDate, int seatNum, int confirmed, int waiting, int invalid, int passengerID) throws SQLException {
        if (confirmed != 1) {
            String noTicketSql = """
                INSERT INTO Booking
                (bookingID, bookDate, seatNum, confirmed, waiting, invalid, passengerID)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
            try (PreparedStatement p = connection.prepareStatement(noTicketSql)) {
                p.setInt(1, bookingID);
                p.setString(2, bookDate);
                p.setInt(3, seatNum);
                p.setInt(4, confirmed);
                p.setInt(5, waiting);
                p.setInt(6, invalid);
                p.setInt(7, passengerID);
                p.executeUpdate();
            }
            return null;
        }
        else{
            String sql = """
                INSERT INTO Booking
                (bookingID, bookDate, seatNum, confirmed, waiting, invalid, passengerID)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
            try (PreparedStatement p = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                p.setInt(1, bookingID);
                p.setString(2, bookDate);
                p.setInt(3, seatNum);
                p.setInt(4, confirmed);
                p.setInt(5, waiting);
                p.setInt(6, invalid);
                p.setInt(7, passengerID);

                int affected = p.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Creating booking failed, no rows affected.");
                }

                try (ResultSet rs = p.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newTicketID = rs.getInt(1);
                        return newTicketID;
                    } else {
                        throw new SQLException("Creating booking failed, no ticketID obtained.");
                    }
                }
            }
        }
    }   

   public void createTicket(int ticketID, int bookingID, /*int trainID,*/ String travelDate, int print, int electronic) {
      String selectSQL = "SELECT seatNum FROM Booking WHERE bookingID = ? AND confirmed = 1";
      String insertSQL = "INSERT INTO Ticket (ticketID, print, electronic, travelDate, seatNum) VALUES (?, ?, ?, ?, ?)";
  
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
                  insertStmt.setString(4, travelDate);
                  insertStmt.setInt(5, seatNum);
  
                  insertStmt.executeUpdate();
              }
  
          } 
  
      } catch (SQLException e) {
          System.err.println("Error creating ticket: " + e.getMessage());
      }
    }
    public void updateBookingWithTicket(int bookingID, int ticketID) throws SQLException {
        String sql = """
        UPDATE Booking
            SET ticketID = ?
        WHERE bookingID = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ticketID);
            ps.setInt(2, bookingID);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("No Booking found for bookingID=" + bookingID);
            }
        }
    }


   public void bookAndIssueTicket(int bookingID,String bookDate,int seatNum,int confirmed,int waiting,int invalid,int passengerID,int trainID,String travelDate,int print,int electronic) throws SQLException {
        connection.setAutoCommit(false);
        try {
            Integer ticketID = createBooking(
                bookingID, bookDate, seatNum,
                confirmed, waiting, invalid, passengerID
            );

            if (ticketID != null) {
                createTicket(
                    ticketID,
                    bookingID,
                    travelDate,
                    print,
                    electronic
                );
                updateBookingWithTicket(bookingID, ticketID);
                
            } 
            System.out.println("Booking created.");

            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
        }
    }

   public void deleteBooking(int bookingID, int passengerID){
      String sql =
         "DELETE FROM Booking "
         + "WHERE bookingID  = ? "
         + "  AND passengerID = ?;";

      try(PreparedStatement stmt = connection.prepareStatement(sql)) {
         stmt.setInt(1, bookingID);
         stmt.setInt(2, passengerID);

         int affected = stmt.executeUpdate();
         if (affected > 0) {
               System.out.println("Booking cancelled successfully.");
         } else {
               System.out.println("No such booking found.");
         }
      } catch (SQLException e) {
         System.err.println("Error cancelling booking: " + e.getMessage());
         e.printStackTrace();
     }
   }

   public void generateReport(int passengerID) {
      String sql = "SELECT p.first_name, p.last_name, b.bookingID, b.bookDate, b.seatNum, " +
                   "t.trainID, t.travelDate, b.confirmed, b.waiting, b.invalid, b.ticketID " +
                   "FROM Passenger p " +
                   "JOIN Booking b ON p.passengerID = b.passengerID " +
                   "JOIN Ticket t ON b.ticketID = t.ticketID " +
                   "WHERE p.passengerID = ?";

      try(PreparedStatement stmt = connection.prepareStatement(sql)) {
         stmt.setInt(1, passengerID);
         ResultSet rs = stmt.executeQuery();

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
            System.out.println("ticketID: " + rs.getInt("ticketID") +
                              ", booking ID #: " + rs.getInt("bookingID") +
                              ", booking status: " + status +
                              ", seat #: " + rs.getInt("seatNum") +
                              ", travel date: " + rs.getString("travelDate"));
         }

         if (!found) {
            System.out.println("No flights booked.");
         }

   } catch (SQLException e) {
         System.out.println("Error: " + e.getMessage());
   }
  }

   public static void main(String[] args){
      TrainBookingDatabase db = new TrainBookingDatabase();

      try (Scanner scanner = new Scanner(System.in)){
        System.out.println("===");
         while(true){
            System.out.println("Train Booking System:");
            System.out.println("  1. Add Passenger Profile");
            System.out.println("  2. Create Booking");
            System.out.println("  3. Update Booking");
            System.out.println("  4. Cancel Booking");
            System.out.println("  5. Generate Ticket Report");
            System.out.println("  6. Exit");

            System.out.print("\nEnter your choice: ");

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
                    String age = scanner.nextLine();
                    int intAge = Integer.parseInt(age);
                    System.out.print("Enter passenger's address: ");
                    String address = scanner.nextLine();
                    
                    System.out.println("===");

                    db.createPassenger(pID, firstName, lastName, sex, intAge, address);
                    System.out.println("===\n");
                    break;

               case 2:
                  System.out.print("Enter today's date (MM-DD-YY): ");
                  String bookingDate = scanner.nextLine();
                  System.out.print("Enter travel date (MM-DD-YY): ");
                  String travelDate = scanner.nextLine();
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
                  System.out.println("===");

                  db.bookAndIssueTicket(bID, bookingDate, seatNum, confirmed, waiting, invalid, cBpID, 1, travelDate, 0, 1);
                  System.out.println("===\n");
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
                  System.out.println("===\n");
                  break;

               case 4:
                  System.out.print("Enter your booking ID: ");
                  int newbID = scanner.nextInt();
                  scanner.nextLine();
                  System.out.print("Enter your ID number: ");
                  int uppID = scanner.nextInt();
                  scanner.nextLine();
                  System.out.println("===");

                  db.deleteBooking(newbID, uppID);
                  System.out.println("===\n");
                  break;
                  
               case 5:
                  System.out.print("Enter your ID number : ");
                  int cpID = scanner.nextInt();
                  scanner.nextLine();
                  System.out.println("===");

                  db.generateReport(cpID);
                  System.out.println("===\n");
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