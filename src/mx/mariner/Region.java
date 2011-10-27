package mx.mariner;

public class Region {
    protected String icon;
    protected String name;
    protected String desc;
    protected int bytes;
    protected String status;

    public Region() {
    }
    
    public Region(String icon, String name, String desc, int bytes, String status) {
        this.icon = icon;
        this.name = name;
        this.desc = desc;
        this.bytes = bytes;
        this.status = status;
    }
    
}
