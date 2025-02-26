package src.billiardsmanagement.model;

public class Permission {
    private int id;
    private String name;

    public Permission(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Permission(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

