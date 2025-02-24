package src.billiardsmanagement.model;

public class RolePermission {
    private Role role;
    private Permission permission;

    public RolePermission(Role role, Permission permission) {
        this.role = role;
        this.permission = permission;
    }

    public Role getRole() { return role; }
    public Permission getPermission() { return permission; }
}

