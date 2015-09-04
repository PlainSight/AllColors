
import java.util.Arrays;


/* This class is made to ease the task of finding targets
 * each unit held within should update itself regularly.
 */

//will use quadtree for each faction

public class Quadtree 
{
	static final int maxsize = 32;
	
	private int xlen;
	private int ylen;
	private int zlen;
	
	private int midx;
	private int midy;
	private int midz;
	
	int minx;
	int miny;
	int minz;
	
	int maxx;
	int maxy;
	int maxz;
	
	SuperColor[] colors;
	int size;
	
	private Quadtree parent;
	private Quadtree[] children = new Quadtree[8];
	
	public Quadtree(int x, int y, int z, int mx, int my, int mz, Quadtree p)
	{
		xlen = x;
		ylen = y;
		zlen = z;
		midx = mx;
		midy = my;
		midz = mz;
		parent = p;
		
		colors = new SuperColor[maxsize];
		
		minx = midx - xlen/2;
		maxx = midx + xlen/2;
		
		miny = midy - ylen/2;
		maxy = midy + ylen/2;
		
		minz = midz - zlen/2;
		maxz = midz + zlen/2;
	}
	
	public void GetAllColors(Quadtree allColors) {
		
		if(size == 0) {
			return;
		}
		
		for(int i = 0; i < maxsize; i++) {
			if(colors[i] != null) {
				if(colors[i].isAlive) {
					allColors.add(colors[i]);
				}
			} else {
				break;
			}
		}
		
		if(children[0] == null) {
			return;
		}
		
		for(int i = 0; i < 8; i++) {
			children[i].GetAllColors(allColors);
		}
	}
	
	private void split()
	{	
		int xlenover4 = xlen/4;
		int ylenover4 = ylen/4;
		int zlenover4 = zlen/4;
		
		int xlenover2 = xlen/2;
		int ylenover2 = ylen/2;
		int zlenover2 = zlen/2;
	
		children[0] = new Quadtree(xlenover2,ylenover2,zlenover2, midx - xlenover4, midy - ylenover4, midz - zlenover4, this);
		children[1] = new Quadtree(xlenover2,ylenover2,zlenover2, midx + xlenover4, midy - ylenover4, midz - zlenover4, this);
		children[2] = new Quadtree(xlenover2,ylenover2,zlenover2, midx - xlenover4, midy + ylenover4, midz - zlenover4, this);
		children[3] = new Quadtree(xlenover2,ylenover2,zlenover2, midx - xlenover4, midy - ylenover4, midz + zlenover4, this);
		children[4] = new Quadtree(xlenover2,ylenover2,zlenover2, midx + xlenover4, midy + ylenover4, midz - zlenover4, this);
		children[5] = new Quadtree(xlenover2,ylenover2,zlenover2, midx - xlenover4, midy + ylenover4, midz + zlenover4, this);
		children[6] = new Quadtree(xlenover2,ylenover2,zlenover2, midx + xlenover4, midy - ylenover4, midz + zlenover4, this);
		children[7] = new Quadtree(xlenover2,ylenover2,zlenover2, midx + xlenover4, midy + ylenover4, midz + zlenover4, this);
		
		//put the nodes in children nodes
		for(int i = 0; i < maxsize; i++)
		{
			putInChild(colors[i]);
		}

		//clean up node
		colors = new SuperColor[maxsize];
	}
		
	public void add(SuperColor u)
	{
		//if there are children then add to child
		
		if(children[0] != null)
		{
			putInChild(u);
		} else {
			if(size < colors.length)
			{
				colors[size] = u;
				u.whereIAm = this;
			} else {
				split();
				putInChild(u);
			}
		}
		size++;
	}
	
	private boolean putInChild(SuperColor u)
	{
		for(int c = 0; c < 8; c++)
		{
			if(children[c].hasUnitInside(u))
			{
				children[c].add(u);
				return true;
			}
		}
		
		//means the unit is not longer within the confines of the parent bounds
		return false;
	}
	
	public void remove(SuperColor u)
	{
		int index = 0;
		for(int i = 0; i < size; i++)
		{
			if(colors[i] == u)
			{
				index = i;
				break;
			}
		}
	
		colors[index] = colors[size-1];
		colors[size-1] = null;
		
		for(Quadtree t = this; t != null; t = t.parent)
		{
			t.size--;
			if(t.size < maxsize/2 && t.children[0] != null)
			{
				//combine child nodes
				t.colors = concatAll(t.children[0].size, t.children[0].colors, t.children[1].colors, t.children[2].colors, t.children[3].colors,
						t.children[4].colors, t.children[5].colors, t.children[6].colors, t.children[7].colors);
				t.children = new Quadtree[8];

				for(int i = 0; i < t.size; i++)
				{
					t.colors[i].whereIAm = t;
				}
			}
		}
	}
	
	public SuperColor[] concatAll(int offset, SuperColor[] first, SuperColor[]... rest)
	{
		SuperColor[] result = Arrays.copyOf(first, maxsize);
		for (SuperColor[] array : rest) {
			if(array.length == 0) continue;
			int i = 0;
	inner:	while(i < array.length)
			{
				if(array[i] == null)
				{
					break inner;
				}
				result[offset++] = array[i++];
			}
		}
		return result;
	}
	
	//checks whether a given unit would be inside the bounds of this quad
	public boolean hasUnitInside(SuperColor u)
	{	
		return (minx <= u.r && u.r < maxx && miny <= u.g && u.g < maxy && minz <= u.b && u.b < maxz);
	}
	
	
	public boolean shouldVisit(SuperColor u, SuperColor nearest)
	{
		int aa = u.r - midx;
		int bb = u.g - midy;
		int cc = u.b - midz;
		double dd = 0.71*xlen;
	
		double distancesqr = (aa*aa + bb*bb + cc*cc) - dd*dd;
		
		return (SuperColor.getDist(u, nearest) > distancesqr);
	}	
	
	public SuperColor findNearest(SuperColor u, SuperColor nearest)
	{
		if((nearest != null && !shouldVisit(u, nearest)) || size == 0) {
			return nearest;
		}
		
		if(children[0] == null)
		{
			for(int i = 0; i < size; i++)
			{
				if(nearest == null && colors[i].isAlive)
				{
					nearest = colors[i];
				} else {
					if(colors[i].isAlive && SuperColor.getDist(u, colors[i]) < SuperColor.getDist(u, nearest))
					{
						nearest = colors[i];
					}
				}
			}
			return nearest;
		} else {
			for(int c = 0; c < 8; c++)
			{
				SuperColor temp = children[c].findNearest(u, nearest);
				
				if(temp == null) continue;
				
				if(nearest == null)
				{
					nearest = temp;
				} else {
					if(SuperColor.getDist(u, temp) < SuperColor.getDist(u, nearest))
					{
						nearest = temp;
					}
				}
			}
			return nearest;
		}
	}
}
