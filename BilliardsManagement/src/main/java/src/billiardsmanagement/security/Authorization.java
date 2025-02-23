package src.billiardsmanagement.security;

import java.util.List;
import src.billiardsmanagement.model.Permission;

public class Authorization {
    private List<Permission> userPermissions;

    public Authorization(List<Permission> userPermissions) {
        this.userPermissions = userPermissions;
    }

    public boolean hasPermission(String permissionName) {
        return userPermissions.stream().anyMatch(p -> p.getName().equals(permissionName));
    }
}
