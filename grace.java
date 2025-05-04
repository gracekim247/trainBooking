import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class grace
{
  public static void main(String[] args)
  {
   try{
      // create a database connection
      Connection connection = DriverManager.getConnection("jdbc:sqlite:train_booking.db");
      
      //example of how to access database
      Statement stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT * FROM Train");
      while (rs.next()) {
         System.out.println("Train: " + rs.getString("trainID"));
      }
   }
   catch(SQLException e){
      System.err.println("Database connection error " + e.getMessage());
   }
  }
}