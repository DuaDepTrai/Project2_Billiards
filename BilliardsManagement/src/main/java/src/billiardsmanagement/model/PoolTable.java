package src.billiardsmanagement.model;
public class PoolTable {

    private int tableId; // Tương ứng với cột 'table_id'
    private String name; // Tương ứng với cột 'name'
    private Double price; // Tương ứng với cột 'price'
    private String status; // Tương ứng với cột 'status'

    // Constructor không tham số
    public PoolTable(int tableId, String name, double price, String status) {
    }

    // Constructor với tất cả các tham số
    public PoolTable(int tableId, String name, Double price, String status) {
        this.tableId = tableId;
        this.name = name;
        this.price = price;
        this.status = status;
    }

    // Getter và Setter cho từng thuộc tính
    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Phương thức toString để hiển thị thông tin của đối tượng
    @Override
    public String toString() {
        return "PoolTable{" +
                "tableId=" + tableId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", status=" + status +
                '}';
    }
}
