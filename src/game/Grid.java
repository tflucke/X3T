package game;

import java.awt.Point;
import java.util.ArrayList;

public class Grid implements GridConstants
{
	public static interface GridObject
	{
		public Point getLocation();
		
		public int getOwner();
		
		public boolean isFilled();
		
		public void setSelected(byte state);
		
		public byte getSelected();
		
		public int getDepth();
	}
	
	protected class Subgrid extends Grid implements GridObject
	{
		private final Point loc;
		private byte selected;
		private Grid parent;
		
		public Subgrid(int levels, int x, int y, Grid parent)
		{
			super(levels);
			selected = levels == 1 ? SELECTED : DESELECTED;
			loc = new Point(x, y);
			owner = -1;
			this.parent = parent;
		}
		
		@Override
		public Point getLocation()
		{
			return loc;
		}
		
		@Override
		protected boolean checkVictory()
		{
			boolean result = super.checkVictory();
			if (result)
			{
				parent.checkVictory();
			}
			return result;
		}
		
		@Override
		public boolean addMarker(int x, int y, int id)
		{
			if (selected == SELECTED)
			{
				boolean result = super.addMarker(x, y, id);
				if (result)
				{
					ArrayList<Point> points = new ArrayList<Point>();
					points.add(0, new Point(x, y));
					passSelected(points);
				}
				return result;
			}
			else
			{
				return false;
			}
		}
		
		private void passSelected(ArrayList<Point> points)
		{
			points.add(0, loc);
			if (parent instanceof Subgrid)
			{
				((Subgrid) parent).passSelected(points);
			}
			else
			{
				parent.setSelected(points, SELECTED);
			}
		}
		
		@Override
		public void setSelected(byte state)
		{
			selected = state;
			for (GridObject[] gos : griddedObjects)
			{
				for (GridObject go : gos)
				{
					if (go != null)
					{
						go.setSelected(state);
					}
				}
			}
			GridEvent ge = new GridEvent(this);
			for (GridListener gl : listeners)
			{
				gl.gridSelected(ge, state);
			}
		}
		
		@Override
		public byte getSelected()
		{
			return selected;
		}
	}
	
	private static class Marker implements GridObject
	{
		private final int playerId;
		private final Point loc;
		
		public Marker(int id, int x, int y)
		{
			playerId = id;
			loc = new Point(x, y);
		}
		
		@Override
		public Point getLocation()
		{
			return loc;
		}
		
		@Override
		public int getOwner()
		{
			return playerId;
		}
		
		@Override
		public boolean isFilled()
		{
			return true;
		}
		
		@Override
		public void setSelected(byte state)
		{
		}
		
		@Override
		public byte getSelected()
		{
			return DESELECTED;
		}
		
		@Override
		public int getDepth()
		{
			return 0;
		}
	}
	
	protected final GridObject[][] griddedObjects;
	protected transient final ArrayList<GridListener> listeners;
	private int owner;
	
	public Grid(int levels)
	{
		griddedObjects = new GridObject[3][3];
		listeners = new ArrayList<GridListener>();
		if (levels > 1)
		{
			for (int x = 0; x < griddedObjects.length; x++)
			{
				for (int y = 0; y < griddedObjects[x].length; y++)
				{
					griddedObjects[x][y] = new Subgrid(levels - 1, x, y, this);
				}
			}
		}
		owner = -1;
	}
	
	protected boolean checkVictory()
	{
		if (owner == -1)
		{
			for (int c = 0; c < 3; c++)
			{
				if (griddedObjects[c][0] != null
				        && griddedObjects[c][1] != null
				        && griddedObjects[c][2] != null
				        && griddedObjects[c][0].getOwner() >= 0
				        && griddedObjects[c][0].getOwner() == griddedObjects[c][1]
				                .getOwner()
				        && griddedObjects[c][0].getOwner() == griddedObjects[c][2]
				                .getOwner())
				{
					owner = griddedObjects[c][0].getOwner();
					for (GridListener gl : listeners)
					{
						gl.gridOwned(owner);
					}
					return true;
				}
				else if (griddedObjects[0][c] != null
				        && griddedObjects[1][c] != null
				        && griddedObjects[2][c] != null
				        && griddedObjects[0][c].getOwner() >= 0
				        && griddedObjects[0][c].getOwner() == griddedObjects[1][c]
				                .getOwner()
				        && griddedObjects[0][c].getOwner() == griddedObjects[2][c]
				                .getOwner())
				{
					owner = griddedObjects[0][c].getOwner();
					for (GridListener gl : listeners)
					{
						gl.gridOwned(owner);
					}
					return true;
				}
			}
			if ((griddedObjects[0][0] != null
			        && griddedObjects[1][1] != null
			        && griddedObjects[2][2] != null
			        && griddedObjects[1][1].getOwner() >= 0
			        && griddedObjects[0][0].getOwner() == griddedObjects[1][1]
			                .getOwner() && griddedObjects[1][1].getOwner() == griddedObjects[2][2]
			        .getOwner())
			        || (griddedObjects[0][2] != null
			                && griddedObjects[1][1] != null
			                && griddedObjects[2][0] != null
			                && griddedObjects[1][1].getOwner() >= 0
			                && griddedObjects[0][2].getOwner() == griddedObjects[1][1]
			                        .getOwner() && griddedObjects[1][1]
			                .getOwner() == griddedObjects[2][0].getOwner()))
			{
				owner = griddedObjects[1][1].getOwner();
				for (GridListener gl : listeners)
				{
					gl.gridOwned(owner);
				}
				return true;
			}
		}
		return false;
	}
	
	/*
	 * protected boolean checkTie()
	 * {
	 * }
	 */
	
	public int getOwner()
	{
		return owner;
	}
	
	public void clearSelection()
	{
		for (GridObject[] gos : griddedObjects)
		{
			for (GridObject go : gos)
			{
				if (go != null)
				{
					go.setSelected(DESELECTED);
					if (go instanceof Grid)
					{
						((Grid) go).clearSelection();
					}
				}
			}
		}
	}
	
	public void addGridListener(GridListener gl)
	{
		listeners.add(gl);
	}
	
	public void removeGridListener(GridListener gl)
	{
		listeners.remove(gl);
	}
	
	public int getDepth()
	{
		return griddedObjects.length > 0 && griddedObjects[0].length > 0
		        && griddedObjects[0][0] != null ? griddedObjects[0][0]
		        .getDepth() + 1 : 1;
	}

	public GridObject getObjectAt(int x, int y)
	{
		if (x < 0 || y < 0 || x >= 3 || y >= 3)
		{
			throw new IllegalArgumentException();
		}
		else
		{
			return griddedObjects[x][y];
		}
	}
	
	private GridObject getObjectAt(Point[] loc, int i)
	{
		if (loc.length > i)
		{
			GridObject go = getObjectAt(loc[i].x, loc[i].y);
			if (go instanceof Grid)
			{
				return ((Grid) go).getObjectAt(loc, i+1);
			}
			else
			{
				throw new IllegalArgumentException();
			}
		}
		else
		{
			return getObjectAt(loc[i].x, loc[i].y);
		}
	}
	
	public GridObject getObjectAt(Point[] loc)
	{
		return getObjectAt(loc, 0);
	}
	
	public boolean addMarker(int x, int y, int id)
	{
		boolean result = !(griddedObjects[x][y] instanceof GridObject);
		if (result)
		{
			griddedObjects[x][y] = new Marker(id, x, y);
		}
		GridEvent ge = new GridEvent(griddedObjects[x][y]);
		for (GridListener gl : listeners)
		{
			gl.markerAdded(ge);
		}
		checkVictory();
		return result;
	}
	
	public int getOwner(int x, int y)
	{
		return griddedObjects[x][y].getOwner();
	}
	
	public boolean isFilled()
	{
		for (GridObject[] gos : griddedObjects)
		{
			for (GridObject go : gos)
			{
				if (go == null || !go.isFilled())
				{
					return false;
				}
			}
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public void setSelected(ArrayList<Point> p, byte state)
	{
		p.remove(0);
		ArrayList<Point> clone = (ArrayList<Point>) ((ArrayList<Point>) p)
		        .clone();
		if (griddedObjects[p.get(0).x][p.get(0).y].isFilled())
		{
			for (GridObject[] gos : griddedObjects)
			{
				for (GridObject go : gos)
				{
					if (go != null)
					{
						if (go.isFilled())
						{
							go.setSelected(DESELECTED);
						}
						else if (go instanceof Grid && p.size() > 1)
						{
							((Grid) go).setSelected(clone, AVALIABLE);
							clone = (ArrayList<Point>) ((ArrayList<Point>) p)
							        .clone();
						}
						else
						{
							go.setSelected(AVALIABLE);
						}
					}
				}
			}
		}
		else
		{
			for (int col = 0; col < 3; col++)
			{
				for (int row = 0; row < 3; row++)
				{
					if (col == p.get(0).x && row == p.get(0).y)
					{
						if (griddedObjects[col][row] instanceof Grid)
						{
							if (p.size() > 1)
							{
								((Grid) griddedObjects[col][row]).setSelected(
								        clone, state);
							}
							else
							{
								griddedObjects[col][row].setSelected(state);
							}
						}
					}
					else
					{
						griddedObjects[col][row].setSelected(DESELECTED);
					}
				}
			}
		}
	}
}