package game;

import java.awt.Point;
import java.util.ArrayList;

import arrays.ArrayMethods;

public class PlayerGrid extends Grid
{
	@SuppressWarnings("unchecked")
    private static ArrayList<Point[]> getAvaliables(Grid g, ArrayList<Point> heighers)
	{
		ArrayList<Point[]> result = new ArrayList<Point[]>();
		if (heighers == null)
		{
			heighers = new ArrayList<Point>();
		}
		
		for (int x = 0; x < 3; x++)
		{
			for (int y = 0; y < 3; y++)
			{
				if (g.griddedObjects[x][y] instanceof Grid)
				{
					ArrayList<Point> clone = (ArrayList<Point>) heighers.clone();
					clone.add(g.griddedObjects[x][y].getLocation());
					result.addAll(getAvaliables((Grid) g.griddedObjects[x][y], clone));
				}
			}
		}
		
		if (g instanceof GridObject && ((GridObject) g).getSelected() == AVALIABLE)
		{
			result.add(heighers.toArray(new Point[0]));
		}
		return result;
	}
    
    private static ArrayList<Point[]> getSelected(Grid g, ArrayList<Point> heighers)
	{
		ArrayList<Point[]> result = new ArrayList<Point[]>();
		if (g.getDepth() == 0)
		{
			for (int x = 0; x < g.griddedObjects.length; x++)
			{
				for (int y = 0; y < g.griddedObjects[x].length; y++)
				{
					if (g.griddedObjects[x][y] == null)
					{
						result.add(new Point[] {new Point(x, y)});
					}
				}
			}
			return result;
		}
		if (heighers == null)
		{
			heighers = new ArrayList<Point>();
		}
		else
		{
			heighers.add(((GridObject) g).getLocation());
		}
		
		for (int x = 0; x < 3; x++)
		{
			for (int y = 0; y < 3; y++)
			{
				if (g.griddedObjects[x][y] instanceof Grid)
				{
				    @SuppressWarnings("unchecked")
					ArrayList<Point> clone = (ArrayList<Point>) heighers.clone();
					result.addAll(getSelected((Grid) g.griddedObjects[x][y], clone));
				}
			}
		}
		
		if (g instanceof GridObject && ((GridObject) g).getSelected() == SELECTED)
		{
			Point p;
			for (int x = 0; x < 3; x++)
			{
				for (int y = 0; y < 3; y++)
				{
					if (g.griddedObjects[x][y] == null)
					{
						p = new Point(x, y);
						heighers.add(p);
						result.add(heighers.toArray(new Point[0]));
						heighers.remove(p);
					}
				}
			}
		}
		return result;
	}

    public static interface Player
	{
    	public void setGrid(Grid g);
		public Point[] placeMarker(Point[][] arrayList);
		public Point[] pickSector(Point[][] options);
		public void markerPlaced(Point[] position, int player);
	}
	
	private Player[] players;
	private int turnCount;
	
	public PlayerGrid(int levels, Player... players)
    {
	    super(levels);
	    this.players = players;
	    for (Player p : players)
	    {
	    	p.setGrid(this);
	    }
	    turnCount = 0;
    }
	
	private Grid getGrid(Point[] loc, int n)
	{
		Grid result = this;
		for (int i = 0; i < n; i++)
		{
			result = (Grid) result.griddedObjects[loc[i].x][loc[i].y];
		}
		return result;
	}
	
	public Thread runGame()
	{
		Thread result = new Thread()
		{
			@Override
			public void run()
			{
				while (getOwner() < 0 && !isInterrupted())
				{
					Point[] loc = getCurrentPlayer().placeMarker(getSelected(PlayerGrid.this, null).toArray(new Point[0][0]));
					int n = loc.length-1;
					boolean result = getGrid(loc, n).addMarker(loc[n].x, loc[n].y, turnCount % players.length);
					Point[][] selectableAreas = getAvaliables(PlayerGrid.this, null).toArray(new Point[0][0]);
					while (selectableAreas.length > 0)
					{
						loc = getCurrentPlayer().pickSector(selectableAreas);
						GridObject chosen = ((GridObject) getGrid(loc, loc.length));
						if (!chosen.isFilled())
						{
							clearSelection();
							chosen.setSelected(SELECTED);
							selectableAreas = new Point[0][0];
							for (Player player : players)
							{
								player.markerPlaced(loc, getCurrentPlayerId());
							}
						}
					}
					if (result)
					{
						turnCount++;
					}
				}
			}
		};
		result.start();
		return result;
	}

	public int getCurrentTurn()
	{
		return turnCount;
	}
	
	public Player getPlayer(int i)
	{
		return players[i];
	}

	public int getPlayerId(Player p)
	{
		return ArrayMethods.getIndex(players, p);
	}
	
	public int getCurrentPlayerId()
	{
		return turnCount % players.length;
	}
	
	public Player getCurrentPlayer()
	{
		return players[turnCount % players.length];
	}
}