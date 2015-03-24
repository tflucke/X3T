package game;

public interface GridListener
{
	public void markerAdded(GridEvent ge);
	public void gridSelected(GridEvent ge, byte state);
	public void gridOwned(int id);
}
