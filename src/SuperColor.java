
public class SuperColor {
	int x;
	int y;
	
	int r;
	int g;
	int b;
	
	Quadtree whereIAm;
	
	Boolean isAlive = true;
	
	public SuperColor(int red, int green, int blue) {
		r = red;
		g = green;
		b = blue;
	}
	
	public void destruct() {
	
		isAlive = false;
		
		if(whereIAm != null) {			
			whereIAm.remove(this);
		}
	}
	
	public static int getDist(SuperColor color1, SuperColor color2) {
		int rd = color1.r - color2.r;
		int gd = color1.g - color2.g;
		int bd = color1.b - color2.b;
	
		return (rd*rd) + (gd*gd) + (bd*bd);
	}
	
}
