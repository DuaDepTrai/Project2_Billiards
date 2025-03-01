package src.billiardsmanagement.model;

public class PoolTable {
    private int tableId; // ID của bàn
    private String name; // Tên bàn
    private double price; // Giá thuê bàn
    private String status; // Trạng thái bàn (Available, Occupied, Under Maintenance)

    // Constructor
    public PoolTable(int tableId, String name, double price, String status) {
        this.tableId = tableId;
        this.name = name;
        setPrice(price); // Sử dụng setter để đảm bảo tính hợp lệ
        setStatus(status); // Sử dụng setter để đảm bảo tính hợp lệ
    }

    // Getters
    public int getTableId() {
        return tableId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.price = price;
    }

    public void setStatus(String status) {
        // Kiểm tra trạng thái hợp lệ
        if (!status.equals("Available") && !status.equals("Occupied") && !status.equals("Under Maintenance")) {
            throw new IllegalArgumentException(
                    "Invalid status. Must be 'Available', 'Occupied', or 'Under Maintenance'.");
        }
        this.status = status;
    }

    // Phương thức toString() để dễ dàng in ra thông tin của đối tượng
    @Override
    public String toString() {
        return "PoolTable{" +
                "tableId=" + tableId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", status='" + status + '\'' +
                '}';
    }
}