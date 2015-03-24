package game;

import game.Grid.GridObject;

public class GridEvent
{
	private final GridObject source;
	
	public GridEvent(GridObject source)
	{
		this.source = source;
	}
	
	public GridObject getSource()
	{
		return source;
	}
}
