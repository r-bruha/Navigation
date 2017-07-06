package cz.uhk.fim.kikm.navigation.model;

/**
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
public class Location {
	private String floor;
	private int x, y, count;
	
	public Location(String floor, int x, int y) {
		this.floor = floor;
		this.x = x;
		this.y = y;
	}
	
	public Location(String floor, int x, int y, int count) {
		this.floor = floor;
		this.x = x;
		this.y = y;
		this.count = count;
	}
	
	public String getFloor() {
		return floor;
	}
	
	public Location setFloor(String floor) {
		this.floor = floor;
		return this;
	}
	
	public int getX() {
		return x;
	}
	
	public Location setX(int x) {
		this.x = x;
		return this;
	}
	
	public int getY() {
		return y;
	}
	
	public Location setY(int y) {
		this.y = y;
		return this;
	}
	
	public int getCount() {
		return count;
	}
	
	public Location setCount(int count) {
		this.count = count;
		return this;
	}
}
