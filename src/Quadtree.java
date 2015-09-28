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
	
	Boolean hasChildren = false;
	
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
	
	private void split()
	{	
		int xlenover4 = xlen/4;
		int ylenover4 = ylen/4;
		int zlenover4 = zlen/4;
		
		int xlenover2 = xlen/2;
		int ylenover2 = ylen/2;
		int zlenover2 = zlen/2;
	
		if(children[0] == null) {
			children[0] = new Quadtree(xlenover2,ylenover2,zlenover2, midx - xlenover4, midy - ylenover4, midz - zlenover4, this);
			children[1] = new Quadtree(xlenover2,ylenover2,zlenover2, midx + xlenover4, midy - ylenover4, midz - zlenover4, this);
			children[2] = new Quadtree(xlenover2,ylenover2,zlenover2, midx - xlenover4, midy + ylenover4, midz - zlenover4, this);
			children[3] = new Quadtree(xlenover2,ylenover2,zlenover2, midx - xlenover4, midy - ylenover4, midz + zlenover4, this);
			children[4] = new Quadtree(xlenover2,ylenover2,zlenover2, midx + xlenover4, midy + ylenover4, midz - zlenover4, this);
			children[5] = new Quadtree(xlenover2,ylenover2,zlenover2, midx - xlenover4, midy + ylenover4, midz + zlenover4, this);
			children[6] = new Quadtree(xlenover2,ylenover2,zlenover2, midx + xlenover4, midy - ylenover4, midz + zlenover4, this);
			children[7] = new Quadtree(xlenover2,ylenover2,zlenover2, midx + xlenover4, midy + ylenover4, midz + zlenover4, this);
		}

		hasChildren = true;
		
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
		
		if(hasChildren)
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
		for(int i = 0; i < size; i++)
		{
			if(colors[i] == u)
			{
				colors[i] = colors[size-1];
				break;
			}
		}
		
		
		for(Quadtree t = this; t != null; t = t.parent)
		{
			t.size--;
			
			if(t.size < maxsize/2 && t.hasChildren)
			{
				int tColorIndex = 0;
				
				for(int i = 0; i < 8; i++) {
					for (int j = 0; j < t.children[i].size; j++) {
						t.colors[tColorIndex++] = t.children[i].colors[j];
					}
					t.children[i].hasChildren = false;
					t.children[i].size = 0;
				}

				for(int i = 0; i < t.size; i++)
				{
					t.colors[i].whereIAm = t;
				}
				
				t.hasChildren = false;
			}
		}
	}
		
	//checks whether a given unit would be inside the bounds of this quad
	public boolean hasUnitInside(SuperColor u)
	{	
		return (minx <= u.r && u.r < maxx && miny <= u.g && u.g < maxy && minz <= u.b && u.b < maxz);
	}
	
	
	public boolean shouldVisit(SuperColor u, SuperColor nearest)
	{
		int cx = (u.r < midx ? 1 : 0) * minx + (u.r > midx ? 1 : 0) * maxx;
		int cy = (u.g < midy ? 1 : 0) * miny + (u.g > midy ? 1 : 0) * maxy;
		int cz = (u.b < midz ? 1 : 0) * minz + (u.b > midz ? 1 : 0) * maxz;

		int isoutx = (u.r >= maxx ? 1 : 0) | (u.r < minx ? 1 : 0);
		int isouty = (u.g >= maxy ? 1 : 0) | (u.g < miny ? 1 : 0);
		int isoutz = (u.b >= maxz ? 1 : 0) | (u.b < minz ? 1 : 0);

		int dx = isoutx * (u.r - cx);
		int dy = isouty * (u.g - cy);
		int dz = isoutz * (u.b - cz);

		//this distance is accurate if the color value is not within any min max range
		int distancesqr = dx*dx + dy*dy + dz*dz;
		
		return SuperColor.getDist(u, nearest) > distancesqr;
	}	
	
	public SuperColor findNearest(SuperColor u, SuperColor nearest)
	{
		if(size == 0 || (nearest != null && !shouldVisit(u, nearest))) {
			return nearest;
		}
		
		if(!hasChildren)
		{
			for(int i = 0; i < size; i++)
			{
				if(nearest == null)
				{
					nearest = colors[i];
				} else {
					if(SuperColor.getDist(u, colors[i]) < SuperColor.getDist(u, nearest))
					{
						nearest = colors[i];
					}
				}
			}
			return nearest;
		} else {
			
			int bestChild = 4 * (u.r > midx ? 1 : 0)
						+	2 * (u.g > midy ? 1 : 0)
						+		(u.b > midz ? 1 : 0);
			
			nearest = children[bestChild].findNearest(u, nearest);
			
			for(int c = 0; c < 8; c++)
			{
				if(c == bestChild) continue;
				
				nearest = children[c].findNearest(u, nearest);
			}
			return nearest;
		}
	}
}
