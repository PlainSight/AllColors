
public class SuperColor {
	int x;
	int y;
	
	int rgb;
	
	Quadtree whereIAm;
	
	public SuperColor(int trgb) {
		rgb = trgb;
	}
	
	public void destruct() {
		whereIAm.remove(this);
	}
	
	public static int getDist(SuperColor color1, SuperColor color2) {
		int sub = color1.rgb - color2.rgb;
		int rd = (sub & 0x00FF0000) >> 16;
		int gd = (sub & 0x0000FF00) >> 8;
		int bd = sub & 0x000000FF;
	
		return (rd*rd) + (gd*gd) + (bd*bd);
	}
	
}
