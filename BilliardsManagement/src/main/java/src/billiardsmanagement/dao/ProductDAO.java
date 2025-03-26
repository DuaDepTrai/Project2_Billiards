package src.billiardsmanagement.dao;

import javafx.collections.ObservableList;
import src.billiardsmanagement.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ProductDAO {
    public static List<Pair<String, Integer>> getAllProductNameAndQuantity() {
        List<Pair<String, Integer>> productList = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.createStatement();
            String query = "SELECT name, quantity FROM products";
            resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int quantity = resultSet.getInt("quantity");
                productList.add(new Pair<>(name, quantity));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
                if (statement != null)
                    statement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return productList;
    }



    // Phương thức để lấy tất cả sản phẩm
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, c.category_id, c.category_name, p.quantity, p.price, p.unit " +
                "FROM products p " +
                "JOIN category c ON p.category_id = c.category_id"; // ✅ Lấy category_name

        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("product_id");
                String name = resultSet.getString("name");
                int categoryId = resultSet.getInt("category_id");
                String category = resultSet.getString("category_name"); // ✅ Lấy category_name
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");
                String unit = resultSet.getString("unit");

                products.add(new Product(id, name, categoryId, category, quantity, price, unit)); // ✅ Truyền category vào constructor
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }


    // Phương thức để thêm sản phẩm mới
    public void addProduct(String name, int categoryId, double price, String unit, int quantity) throws SQLException {
        String sql = "INSERT INTO products (name, category_id, price, unit, quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = TestDBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setInt(2, categoryId);
            statement.setDouble(3, price);
            statement.setString(4, unit);
            statement.setInt(5, quantity);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức để cập nhật thông tin sản phẩm
    public void updateProduct(int productId, String name, int categoryId, double price, String unit, int quantity) throws SQLException {
        String sql = "UPDATE products SET name = ?, category_id = ?, price = ?, unit = ?, quantity = ? WHERE product_id = ?";
        try (Connection connection = TestDBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setInt(2, categoryId);
            statement.setDouble(3, price);
            statement.setString(4, unit);
            statement.setInt(5, quantity);
            statement.setInt(6, productId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // dispatchItem : decrease quantity
    public static boolean dispatchItem(String productName, int quantity) {
        String sql = "UPDATE products SET quantity = quantity - ? WHERE name = ? AND quantity >= 0";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setString(2, productName);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Product quantity successfully reduced for " + productName);
                return true;
            } else {
                throw new Exception("Sell-off failed. Product not found or quantity insufficient.");
            }
        } catch (Exception e) {
            System.err.println("Error during sell-off: " + e.getMessage());
        }
        return false;
    }

    // replenishItem : increase quantity
    public static boolean replenishItem(String productName, int quantity) {
        String sql = "UPDATE products SET quantity = quantity + ? WHERE name = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setString(2, productName);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Successfully replenished " + quantity + " units of '" + productName + "'.");
                return true;
            } else {
                throw new Exception("Replenishment failed. Product '" + productName + "' not found.");
            }
        } catch (SQLException e) {
            System.err.println("SQL error during replenishment: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during replenishment: " + e.getMessage());
        }
        return false;
    }

    private static int categoryId = 2;
    public static boolean replenishMultipleItems(List<OrderItem> orderItemList) {
        String sqlSelect = "SELECT name FROM products p " +
                "JOIN category cat ON p.category_id = cat.category_id " +
                "WHERE cat.category_id = ?";
        List<String> rentCueNameList = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            // Retrieve names of products in the specified category
            try (PreparedStatement selectStatement = connection.prepareStatement(sqlSelect);
                 ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {
                    rentCueNameList.add(resultSet.getString("name"));
                }
            }

            boolean allReplenished = true;
            int rentCuesCount = 0;

            // Check orderItemList for matching items and replenish
            for (OrderItem orderItem : orderItemList) {
                if (rentCueNameList.contains(orderItem.getProductName())) {
                    rentCuesCount++;
                    boolean result = replenishItem(orderItem.getProductName(), orderItem.getQuantity());
                    if (!result) {
                        allReplenished = false; // If any replenishment fails
                        break;
                    }
                }
            }
            if (rentCuesCount==0){
                connection.rollback();
                System.out.println("From ProductDAO - replenishMultipleItems() - Don't panic bro, there's just no rent cues in this Order. Nothing to update.");
            }
            else if (allReplenished) {
                connection.commit(); // Commit the transaction if all succeeded
                System.out.println("All items replenished successfully.");
                return true;
            }
            else {
                connection.rollback(); // Rollback if any item failed to replenish
                System.err.println("-- ERROR -- From Product DAO - Replenishment failed for some items. Transaction rolled back.");
            }

        } catch (SQLException e) {
            System.err.println("SQL error during replenishment: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during replenishment: " + e.getMessage());
        }
        return false; // Return false if replenishment was not successful
    }

//    public static boolean replenishMultipleItems(ObservableList<RentCue> productNameList) {
//        String sql = "UPDATE products SET quantity = quantity + 1 WHERE name = ?";
//        Connection connection = DatabaseConnection.getConnection();
//        try (PreparedStatement statement = connection.prepareStatement(sql)) {
//
//            // Disable auto-commit to allow batch processing
//            connection.setAutoCommit(false);
//
//            for (RentCue product : productNameList) {
//                statement.setString(1, product.getProductName());
//                statement.addBatch(); // Add each update operation to the batch
//            }
//
//            int[] rowsAffected = statement.executeBatch(); // Execute the batch
//
//            // Check if all updates were successful
//            boolean allSuccessful = true;
//            for (int row : rowsAffected) {
//                if (row <= 0) {
//                    allSuccessful = false;
//                    break;
//                }
//            }
//
//            if (allSuccessful) {
//                System.out.println("Successfully replenished all products.");
//                connection.commit(); // Commit the transaction
//                return true;
//            } else {
//                throw new Exception("Replenishment failed. Not all products were found or updated.");
//            }
//        } catch (SQLException e) {
//            try {
//                if (connection != null) {
//                    connection.rollback(); // Rollback the transaction in case of error
//                }
//                e.printStackTrace();
//            } catch (SQLException ex) {
//                throw new RuntimeException(ex);
//            }
//            System.err.println("SQL error during replenishment: " + e.getMessage());
//        } catch (Exception e) {
//            try {
//                if (connection != null) {
//                    connection.rollback(); // Rollback the transaction in case of error
//                }
//                e.printStackTrace();
//            } catch (SQLException ex) {
//                throw new RuntimeException(ex);
//            }
//            System.err.println("Unexpected error during replenishment: " + e.getMessage());
//        } finally {
//            try {
//                if (connection != null) {
//                    connection.setAutoCommit(true); // Re-enable auto-commit
//                    connection.close(); // Close the connection
//                }
//            } catch (Exception e) {
//                System.out.println("Error : " + e.getMessage());
//            }
//        }
//
//        return false;
//    }


    // Phương thức để stockUp
    public void stockUp(int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity + ? WHERE product_id = ?";
        try (Connection connection = TestDBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setInt(2, productId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức để xóa sản phẩm
    public void removeProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection connection = TestDBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức để lấy sản phẩm theo danh mục
    public List<Product> getProductsByCategory(int categoryId) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, c.category_name, p.quantity, p.price, p.unit " +
                "FROM products p " +
                "JOIN category c ON p.category_id = c.category_id " + // ✅ JOIN để lấy category_name
                "WHERE p.category_id = ?";

        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("product_id");
                String name = resultSet.getString("name");
                String category = resultSet.getString("category_name"); // ✅ Lấy category_name
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");
                String unit = resultSet.getString("unit");

                products.add(new Product(id, name, categoryId, category, quantity, price, unit));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return products;
    }

    public Product getProductByName(String productName) {
        String sql = "SELECT p.product_id, p.name, c.category_id, c.category_name, p.quantity, p.price, p.unit " +
                "FROM products p " +
                "JOIN category c ON p.category_id = c.category_id " +
                "WHERE p.name = ?";

        try (Connection connection = TestDBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, productName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("product_id");
                String name = resultSet.getString("name");
                int categoryId = resultSet.getInt("category_id"); // ✅ Lấy category_id
                String category = resultSet.getString("category_name");
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");
                String unit = resultSet.getString("unit");

                return new Product(id, name, categoryId, category, quantity, price, unit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null; // Trả về null nếu không tìm thấy sản phẩm
    }

    public static ArrayList<String> getAllProductsName() {
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null)
                throw new SQLException("Connection Error : cannot connect to the Database !");
            ArrayList<String> list = new ArrayList<>();
            String query = "SELECT name FROM products";
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery(query);
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getProductQuantityByName(String productName) {
        String query = "SELECT quantity FROM products WHERE name = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, productName);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Pair<Integer, Double> getProductIdAndPriceByName(String productName) {
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con == null)
                throw new SQLException("Error connecting to the database!");
            String query = "SELECT product_id,price FROM products WHERE name = ?";
            PreparedStatement st = con.prepareStatement(query);
            st.setString(1, productName);
            ResultSet rs = st.executeQuery();
            if (!rs.next())
                throw new SQLException("Error: Unable to retrieve data from the database! \nThis might be because of some wrongness in your input. Please check again.");
            Pair<Integer, Double> productPair = new Pair<>();
            productPair.setFirstValue(rs.getInt("product_id"));
            productPair.setSecondValue(rs.getDouble("price"));
            return productPair;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getProductIdByName(String productName) {
        String query = "SELECT product_id FROM products WHERE name = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, productName);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("product_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String  getProductUnitById(int productId){

        String query = "SELECT unit FROM products WHERE product_id= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("unit");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "unit"; // Mặc định nếu không có đơn vị tính
    }

}
