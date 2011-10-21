package mx.mariner;

public class Region {
	protected String name;
	protected String description;
	protected int iconResourceId;
	
	protected Region() {
	}
	
	protected Region(String name, String description, int iconResourceId) {
		this.name = name;
		this.description = description;
		this.iconResourceId = iconResourceId;
	}
	
	protected String getName() {
		return this.name;
	}
	
}
