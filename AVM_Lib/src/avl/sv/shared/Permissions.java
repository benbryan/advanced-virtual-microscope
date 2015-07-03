package avl.sv.shared;

public enum Permissions {
    VIEW    (1<<1),   
    CRUD    (1<<2),
    DENIED  (1<<4), 
    ADMIN   (1<<8);

    private final int value;
    Permissions(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    } 
    
    public boolean canModify(){
        return this.equals(Permissions.CRUD) || this.equals(Permissions.ADMIN);        
    }
    public boolean canRead(){
        return this.equals(Permissions.CRUD) || this.equals(Permissions.VIEW) || this.equals(Permissions.ADMIN);
    }

    public static String PERMISSION_DENIED = "Permission Denied";

    public boolean isAdmin() {
        return this.equals(Permissions.ADMIN);
    }
}
