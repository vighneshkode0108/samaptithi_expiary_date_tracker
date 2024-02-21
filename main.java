import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public class main {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/expiry_tracker";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in);
             Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Main menu loop
            boolean exit = false;
            while (!exit) {
                System.out.println("Menu:");
                System.out.println("1. Add data");
                System.out.println("2. Display data");
                System.out.println("3. Check items expiring within 7 days");
                System.out.println("4. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        addData(scanner, connection);
                        break;
                    case 2:
                        displayData(connection);
                        break;
                    case 3:
                        checkExpiryWithin7Days(connection);
                        break;
                    case 4:
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 4.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to add data to the database
    private static void addData(Scanner scanner, Connection connection) throws SQLException {
        System.out.print("Enter item name: ");
        String itemName = scanner.next();

        System.out.print("Enter item category: ");
        String itemCategory = scanner.next();

        System.out.print("Enter manufacturing date (YYYY-MM-DD): ");
        String manufacturingDateString = scanner.next();
        LocalDate manufacturingDate = LocalDate.parse(manufacturingDateString);

        System.out.print("Enter shelf life (in days): ");
        int shelfLifeDays = scanner.nextInt();

        
        LocalDate expiryDate = manufacturingDate.plusDays(shelfLifeDays);

        // Insert data into the database
        String insertQuery = "INSERT INTO products (item_name, item_category, manufacturing_date, shelf_life_days, expiry_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, itemName);
            preparedStatement.setString(2, itemCategory);
            preparedStatement.setString(3, manufacturingDateString);
            preparedStatement.setInt(4, shelfLifeDays);
            preparedStatement.setString(5, expiryDate.toString());
            preparedStatement.executeUpdate();
            System.out.println("Data inserted successfully!");
        }
    }

    // Method to display data from the database
    private static void displayData(Connection connection) throws SQLException {
        String query = "SELECT * FROM products";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                System.out.println("Item Name: " + resultSet.getString("item_name") +
                                   ", Item Category: " + resultSet.getString("item_category") +
                                   ", Manufacturing Date: " + resultSet.getString("manufacturing_date") +
                                   ", Shelf Life: " + resultSet.getInt("shelf_life_days") +
                                   ", Expiry Date: " + resultSet.getString("expiry_date"));
            }
        }
    }

 // Method to check items expiring within 7 days
    private static void checkExpiryWithin7Days(Connection connection) throws SQLException {
        LocalDate currentDate = LocalDate.now();
        LocalDate expiryThreshold = currentDate.plusDays(7);

        // Print out current date and  threshold for debugging
        System.out.println("Current Date: " + currentDate);
        System.out.println("Expiry Threshold: " + expiryThreshold);

        String query = "SELECT item_name FROM products WHERE expiry_date BETWEEN ? AND ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, currentDate.toString());
            preparedStatement.setString(2, expiryThreshold.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if there are any items expiring within 7 days
                boolean hasExpiringItems = false;
                while (resultSet.next()) {
                    hasExpiringItems = true;
                    System.out.println(resultSet.getString("item_name"));
                }
                if (!hasExpiringItems) {
                    System.out.println("No items are expiring within 7 days.");
                }
            }
        }
    }



//Authentication method
private static boolean authenticateUser(Connection connection, String username, String password) throws SQLException {
 String query = "SELECT * FROM user_login WHERE username = ? AND password = ?";
 try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
     preparedStatement.setString(1, username);
     preparedStatement.setString(2, password); // Remember to hash the password before querying
     try (ResultSet resultSet = preparedStatement.executeQuery()) {
         return resultSet.next(); // If a row is returned, the user is authenticated
     }
 }
}

//Registration method
private static void registerUser(Connection connection, String username, String password, String email, String role) throws SQLException {
 String query = "INSERT INTO user_login (username, password, email, role) VALUES (?, ?, ?, ?)";
 try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
     preparedStatement.setString(1, username);
     preparedStatement.setString(2, password); // Remember to hash the password before storing
     preparedStatement.setString(3, email);
     preparedStatement.setString(4, role);
     preparedStatement.executeUpdate();
 }
}
}

