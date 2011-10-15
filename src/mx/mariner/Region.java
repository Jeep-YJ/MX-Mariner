package mx.mariner;

public class Region {
	public String name;
	public String description;
	public int iconResourceId;
	
	public Region() {
	}
	
	public Region(String name, String description, int iconResourceId) {
		this.name = name;
		this.description = description;
		this.iconResourceId = iconResourceId;
	}
	
	public String getName() {
		return this.name;
	}
	
}
