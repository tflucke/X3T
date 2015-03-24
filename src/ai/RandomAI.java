package ai;

import game.Grid;

import java.awt.Point;

public class RandomAI implements game.PlayerGrid.Player
{
	@Override
    public Point[] placeMarker(Point[][] options)
    {
	    return options[(int) (Math.random()*options.length)];
    }

	@Override
    public Point[] pickSector(Point[][] options)
    {
	    return options[(int) (Math.random()*options.length)];
    }

	@Override
    public void setGrid(Grid g)
    {}

	@Override
    public void markerPlaced(Point[] position, int player)
    {}
}
