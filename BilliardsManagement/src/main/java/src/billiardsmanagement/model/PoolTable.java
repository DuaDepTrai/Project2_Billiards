package src.billiardsmanagement.model;

public class PoolTable {
    private int tableId; // ID của bàn
    private String name; // Tên bàn
    private String status; // Trạng thái bàn
    // ( Current : Available, Ordered, Playing )
    private int catePooltableId;
    private String catePooltableName;
    private double price; // Giá thuê bàn

    // Constructor
    public PoolTable(int tableId, String name, String status, int catePooltableId, String catePooltableName, double price) {
        this.tableId = tableId;
        this.name = name;
        setStatus(status);
        this.catePooltableId = catePooltableId;
        this.catePooltableName = catePooltableName;
        setPrice(price);
    }

    public PoolTable(int tableId, String name, double price, String status, String categoryName) {
        this.tableId = tableId;
        this.name = name;
        this.price = price;
        this.status = status;
        this.catePooltableName = categoryName;
    }

    // Getters
    public int getTableId() {
        return tableId;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getCatePooltableId() {
        return catePooltableId;
    }

    public String getCatePooltableName() {
        return catePooltableName;
    }

    public double getPrice() {
        return price;
    }

    // Setters
    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        // Check valid status
        if (!status.equals("Available") && !status.equals("Ordered") && !status.equals("Playing")) {
            throw new IllegalArgumentException(
                    "Invalid status. Must be 'Available', 'Ordered', or 'Playing'.");
        }
        this.status = status;
    }

    public void setCatePooltableId(int catePooltableId) {
        this.catePooltableId = catePooltableId;
    }

    public void setCatePooltableName(String catePooltableName) {
        this.catePooltableName = catePooltableName;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.price = price;
    }

    // Phương thức toString() để dễ dàng in ra thông tin của đối tượng
    @Override
    public String toString() {
        return "PoolTable{" +
                "tableId=" + tableId +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", catePooltableId=" + catePooltableId +
                ", catePooltableName='" + catePooltableName + '\'' +
                ", price=" + price +
                '}';
    }
}