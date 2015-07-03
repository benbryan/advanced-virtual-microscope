package avl.sv.shared;

import avl.sv.shared.Permissions;

public class PermissionsSet {

    Permissions permission;
    private final int ID;

    public PermissionsSet(Permissions permission, int ID) {
        this.permission = permission;
        this.ID = ID;
    }

    public Permissions getPermission() {
        return permission;
    }

    public int getID() {
        return ID;
    }

    public void setPermission(Permissions permission) {
        this.permission = permission;
    }    
}
