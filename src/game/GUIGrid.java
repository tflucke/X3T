package game;

import game.Grid.GridObject;
import game.PlayerGrid.Player;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import ai.RandomAI;
import arrays.ArrayMethods;
import name.tomflucke.layouts.TableLayout;

public class GUIGrid extends JPanel implements GridConstants
{
	private static final long serialVersionUID = -1636143083725158877L;

	public static class GUIPlayer implements Player
	{
		private final Color playerColor;
		private final Player play;

		public GUIPlayer(Player play, Color color)
		{
			playerColor = color;
			this.play = play;
		}
		
		public Color getColor()
		{
			return playerColor;
		}
		
		@Override
        public Point[] placeMarker(Point[][] options)
        {
			return play.placeMarker(options);
        }

		@Override
        public Point[] pickSector(Point[][] options)
        {
			return play.pickSector(options);
        }

		@Override
        public void setGrid(Grid g)
        {
	        play.setGrid(g);
        }

		@Override
        public void markerPlaced(Point[] position, int player)
        {
			play.markerPlaced(position, player);
        }
		
	}
	
	public static class ClickPlayer implements Player
	{
		private static Placeholder clicked;
		private static boolean hasClicked;
		
		public static void click(Placeholder ph)
		{
			hasClicked = true;
			clicked = ph;
		}
		
		private void waitForClick()
		{
			while (!hasClicked)
			{
				try
                {
	                Thread.sleep(100);
                }
                catch (InterruptedException ie)
                {
	                // TODO Auto-generated catch block
	                ie.printStackTrace();
                }
			}
			hasClicked = false;
		}
		
		@Override
        public Point[] placeMarker(Point[][] options)
        {
			waitForClick();
	        return clicked.getGridLocation();
        }

		@Override
        public Point[] pickSector(Point[][] options)
        {
			waitForClick();
			Point[] result = clicked.getGridLocation();
	        return ArrayMethods.subarray(result, 0, result.length -1);
        }

		@Override
        public void setGrid(Grid g)
        {}

		@Override
        public void markerPlaced(Point[] position, int player)
        {}
	}
	
	public static void main(String... strs)
	{
		int gridNum = 2;
		boolean ai = true;
		int i = 0;
		while (i < strs.length)
		{
			switch (strs[i])
			{
				case "-h":
				case "--help":
					System.out.println("Welcome to X3T! (Extreme Tic-Tac-Toe!)");
					System.out.println("This is only a basic demonstration of the core of the soon-to-be program.");
					System.out.println("We currently accept the following arguements:");
					System.out.println("-a --ai\t\t\tCauses player two to become an AI player");
					System.out.println("\t\t\tCurrently, the only AI avaliable is the randomm AI.");
					System.out.println("-m --human\t\tCauses player two to place moves based on user input. (Default)");
					System.out.println("-g [n] --grid [n]\tSets the depth of recurssion. (Default n=2)");
					return;
				case "-a":
				case "--ai":
					ai = true;
					System.out.println("Using Random Number AI for player two.");
					break;
				case "-m":
				case "--human":
					ai = false;
					System.out.println("Using human input for player two.");
					break;
				case "-g":
				case "--grid":
					i++;
					if (i+1 == strs.length || !strs[i+1].matches("\\d+"))
					{
						System.out.println("Invalid grid depth specified.  Defaulting to two.");
					}
					gridNum = Integer.valueOf(strs[i + 1]);
					break;
				default:
					System.out.println("Invalid arguement: "+strs[i]);
			}
			i++;
		}
		JFrame window = new JFrame();
		Player p = ai?new RandomAI():new ClickPlayer();
		PlayerGrid g = new PlayerGrid(gridNum, new GUIPlayer(new RandomAI(), Color.RED), new GUIPlayer(p, Color.BLUE));
		GUIGrid guig = new GUIGrid(g);
		g.runGame();
		window.add(guig);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.repaint();
		window.pack();
	}
	
	private final Grid gameGrid;
	private final ArrayList<GUIGrid> subgrids;
	private final Point loc;
	private int borderSize = 5;
	private int ownerId;
	private byte selected = DESELECTED;
	private Stroke gridStroke;
	
	public GUIGrid(PlayerGrid g)
	{
		this(g, null);
	}

	private GUIGrid(Grid g, Point p)
	{
		super(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
				{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL}
			}));
		setBackground(new Color(0, 0, 0, 0));
		subgrids = new ArrayList<GUIGrid>();
		setBorder(BorderFactory.createEmptyBorder(borderSize, borderSize, borderSize, borderSize));
		gameGrid = g;
		ownerId = -1;
		loc = p;
		gameGrid.addGridListener(new GridListener()
		{
			@Override
			public void markerAdded(GridEvent ge)
			{
				Point loc = ge.getSource().getLocation();
				Player p = ((PlayerGrid) getHighestParent().gameGrid).getPlayer(ge.getSource().getOwner());
				if (p instanceof GUIPlayer)
				{
					add(new MarkerGUI(((GUIPlayer) p).getColor(), loc.x, loc.y), loc.x+", "+loc.y);
				}
				revalidate();
				repaint();
            }

			@Override
            public void gridSelected(GridEvent ge, byte state)
            {
				selected = state;
				getHighestParent().repaint();
            }

			@Override
            public void gridOwned(int id)
            {
				if (getParent() instanceof GUIGrid)
				{
					ownerId = id;
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Player "+(id+1)+" Won!");
				}
            }
		});
		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 3; col++)
			{
				GridObject go = g.getObjectAt(col, row);
				if (go == null)
				{
					add(new Placeholder(col, row), col+", "+row);
				}
				else if (go instanceof Grid)
				{
					GUIGrid guig = new GUIGrid((Grid) go, new Point(col, row));
					subgrids.add(guig);
					add(guig, col+", "+row);
				}
			}
		}
		gridStroke = new BasicStroke(gameGrid.getDepth());
		if (subgrids.size() == 0)
		{
			setPreferredSize(new Dimension(100, 100));
		}
		else
		{
			int largest = 100;
			for (Component c : getComponents())
			{
				largest = Math.max(largest, c.getPreferredSize().height);
			}
			setPreferredSize(new Dimension(largest, largest));
		}
	}
	
	private GUIGrid getHighestParent()
	{
		if (getParent() instanceof GUIGrid)
		{
			return ((GUIGrid) getParent()).getHighestParent();
		}
		else
		{
			return this;
		}
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		g.clearRect(1, 1, getWidth()-2, getHeight()-2);
		if (selected == SELECTED)
		{
			g.setColor(new Color(200, 200, 0, 75));
			g.fillRect(2, 2, getWidth()-3, getHeight()-3);
		}
		else if (selected == AVALIABLE)
		{
			g.setColor(new Color(0, 200, 200, 75));
			g.fillRect(2, 2, getWidth()-3, getHeight()-3);
			
		}
		if (ownerId != -1)
		{
			((Graphics2D) g).setStroke(new BasicStroke(gameGrid.getDepth()+1));
			Player p = ((PlayerGrid) getHighestParent().gameGrid).getPlayer(ownerId);
			if (p instanceof GUIPlayer)
			{
				g.setColor(((GUIPlayer) p).getColor());
			}
			g.drawRect(1, 1, getWidth()-2, getHeight()-2);
		}
		((Graphics2D) g).setStroke(gridStroke);
		super.paintComponent(g);
		((TableLayout) getLayout()).drawGrid(this, g, true, false);
	}
	
	public ArrayList<Point> getGridLocation()
	{
		if (getParent() instanceof GUIGrid)
		{
			ArrayList<Point> result = ((GUIGrid) getParent()).getGridLocation();
			result.add(loc);
			return result;
		}
		else
		{
			return new ArrayList<Point>();
		}
	}
	
	private class MarkerGUI extends Placeholder
	{
		private static final long serialVersionUID = 593200601971906754L;
		
		public MarkerGUI(Color col, int x, int y)
		{
			super(x, y);
			setBackground(col);
		}
		
		@Override
		public void paint(Graphics g)
		{
			super.paint(g);
			g.setColor(getBackground());
			int doubleBorder = 2*borderSize;
			g.fillRect(borderSize, borderSize, getWidth()-doubleBorder, getHeight()-doubleBorder);
		}
	}
	private class Placeholder extends Component
	{
		private static final long serialVersionUID = -4243594387683315602L;

		private final Point loc;
		
		public Placeholder(int x, int y)
		{
			loc = new Point(x, y);
			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent me)
				{
					ClickPlayer.click(Placeholder.this);
				}
			});
		}

		public Point[] getGridLocation()
		{
			ArrayList<Point> result = GUIGrid.this.getGridLocation();
			result.add(loc);
			return result.toArray(new Point[0]);
		}
	}
}