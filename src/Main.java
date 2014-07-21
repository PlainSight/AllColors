import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;


public class Main {
	
	int REDBITDEPTH = 8;
	int GREENBITDEPTH = 8;
	int BLUEBITDEPTH = 8;
	
	int width = 4096;
	int height = 4096;
	int topcount = width*height;
	
	SuperColor[][] pixilValues = new SuperColor[width][height];
	
	int rq = 256;
	int gq = 256;
	int bq = 256;
	
	Quadtree edgeColors = new Quadtree(rq, gq, bq, rq/2, gq/2, bq/2, null);
	
	public static void main(String[] args) {
		new Main();
	}
	
	int nextColor = 0;
	int sourceColors = 0;
	int sizeOfSource = (int) Math.pow(2, REDBITDEPTH) * (int) Math.pow(2, GREENBITDEPTH) * (int) Math.pow(2, BLUEBITDEPTH);
	SuperColor[] colorSource = new SuperColor[sizeOfSource];
	
	private SuperColor getNextColor() {
		int indexToGet = nextColor + ((int) (Math.random() * (colorSource.length - nextColor)));
		
		SuperColor result = colorSource[indexToGet];
		
		colorSource[indexToGet] = colorSource[nextColor];
		
		nextColor++;
		
		return result;
	}
		
	public Main() {
		
		long starttime = System.currentTimeMillis();
		
		int redinc = 256 / ((int) Math.pow(2, REDBITDEPTH));
		int greeninc = 256 / ((int) Math.pow(2, GREENBITDEPTH));
		int blueinc = 256 / ((int) Math.pow(2, BLUEBITDEPTH));
		
		int sourceColors = 0;
		for(int r = 0; r < 256; r += redinc) {
			for(int g = 0; g < 256; g += greeninc) {
				for(int b = 0; b < 256; b += blueinc) {
					colorSource[sourceColors++] = (new SuperColor(r, g, b));
				}
			}
		}
				
		long aftercolortime = System.currentTimeMillis();
		
		//place first pixil
		SuperColor poppedColour = getNextColor();
		setPixil(width/2, height/2, poppedColour);
		edgeColors.add(poppedColour);
		
		int destructed = 0;
			
		for(int counter = 1; counter < topcount; counter++) {
			poppedColour = getNextColor();
			
			boolean set = false;
			
			while(!set) {
				
				SuperColor closestNeighbour = edgeColors.findNearest(poppedColour, null);
								
				int minx = closestNeighbour.x - 1;
				int maxx = closestNeighbour.x + 1;
				int miny = closestNeighbour.y - 1;
				int maxy = closestNeighbour.y + 1;
				
				minx = minx < 0 ? 0 : minx;
				maxx = maxx >= width ? width-1 : maxx;
				miny = miny < 0 ? 0 : miny;
				maxy = maxy >= height ? height-1 : maxy;
				
				int numopen = 0;
				int[][] open = new int[8][2];
				
				for(int x = minx; x <= maxx; x++) {
					for(int y = miny; y <= maxy; y++) {
						if(getPixil(x, y) == null) {
							open[numopen][0] = x;
							open[numopen][1] = y;
							numopen++;
							set = true;
						}
					}
				}
				
				
				
				
				if(!set) {
					closestNeighbour.destruct();
					destructed++;
					if(destructed > 1000) {
						cleanQuad();
						destructed = 0;
					}
					
				} else {
					int placement = (int) (Math.random()*numopen);
					
					setPixil(open[placement][0], open[placement][1], poppedColour);
					edgeColors.add(poppedColour);
				}
			}
			
			
//			if(counter % 10000 == 0) {
//				render(""+counter);
//			}
		}
		
		long finishtime = System.currentTimeMillis();
		
		System.out.println("Start up time: " + (aftercolortime - starttime));
		System.out.println("Calculation time: " + (finishtime - aftercolortime));

		render(""+topcount);
		
	}
	
	private void cleanQuad() {
		
		Quadtree allColors = new Quadtree(rq, gq, bq, rq/2, gq/2, bq/2, null);
		
		edgeColors.GetAllColors(allColors);
		
		edgeColors = allColors;
	
	}
	
	private void render(String label) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		
		for(int drawx = 0; drawx < width; drawx++) {			
			for(int drawy = 0; drawy < height; drawy++) {
				SuperColor elem = getPixil(drawx, drawy);
				if(elem != null) {
					g2.setColor(new Color(elem.r, elem.g, elem.b));
				} else {
					g2.setColor(Color.black);
				}
				g2.fillRect(drawx, drawy, 1, 1);
			}			
		}
		
		
		try {
		    // retrieve image
		    File outputfile = new File(label + " painting " + (new Date()).toString().replace(':', '-') + ".png");
		    ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
		
	private void setPixil(int x, int y, SuperColor sc) {
		sc.x = x;
		sc.y = y;
		pixilValues[x][y] = sc;
	}
	
	private SuperColor getPixil(int x, int y) {
		return pixilValues[x][y];
	}
}
