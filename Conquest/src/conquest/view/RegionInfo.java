package conquest.view;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import conquest.game.Team;
import conquest.game.world.Region;

public class RegionInfo extends JPanel {
	
	public static final int[] HIGHLIGHT_RING_COLOR_RGB = new int[] { 255, 255, 255 };
	public static final float[] HIGHLIGHT_RING_COLOR_HSB = Color.RGBtoHSB(HIGHLIGHT_RING_COLOR_RGB[0], HIGHLIGHT_RING_COLOR_RGB[1], HIGHLIGHT_RING_COLOR_RGB[2], null);
	
	private int diam;
	//private Color circleColor;
	private JLabel txt;
	private JLabel name;
	private Region region;
	private int armies = 0;
	private Team team;
	private boolean highlight = false;
	private boolean selected = false;

	public int armiesPlus = 0;
	
	public RegionInfo(Team team, int diam) {
		init(diam, team);
	}
	public RegionInfo(Team team) {
		init(30, team);
	}
	public RegionInfo() {
		init(30, Team.NEUTRAL);
	}
	
	private void init(int diam, Team team) {
		this.setTeam(team);
		
		this.setOpaque(false);
		this.setBounds(0,0, 100, diam < 30 ? 34 : diam+8);
		
		BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		this.setLayout(layout);
		
		//Region name
        this.name = new JLabel("PLR", JLabel.CENTER);
        this.name.setSize(100, 15);
        this.name.setPreferredSize(this.name.getSize());
//        this.name.setBounds(200, 400, diam, diam);
        this.name.setOpaque(false);
        this.name.setAlignmentX(0.5f);
        this.name.setForeground(Color.BLACK);
        this.add(this.name);
		
		//Text
        this.txt = new JLabel("2", JLabel.CENTER);
        this.txt.setSize(100, 15);
        this.txt.setPreferredSize(this.txt.getSize());
//        this.txt.setBounds(200, 400, diam, diam);
        this.txt.setOpaque(false);
        this.txt.setAlignmentX(0.5f);
        this.txt.setForeground(Color.BLACK);
        this.add(this.txt);        
        
        //Circle
        this.diam = diam;
	}
	
	public void setNameLabel(String s) {
		this.name.setText(s);
	}
	
	public void setText(String s) {
		this.txt.setText(s);			
		this.revalidate();
		this.repaint();
	}
		
	public void setHighlight(boolean state) {		
		this.highlight = state;
		this.revalidate();
		this.repaint();
	}
	
	public void setTeam(Team team) {
		this.team = team;
		if (this.txt != null) {
			this.txt.setForeground(Color.BLACK);
		}
		if (this.name != null) {
			this.name.setForeground(Color.BLACK);
		}
		this.revalidate();
		this.repaint();
	}
	
    public void setRegion(Region region) {
		this.region = region;
		this.name.setText(region.id + ":" + region.mapName);
		this.revalidate();
		this.repaint();
	}
    
    public int getArmies() {
		return armies;
	}
	
	public void setArmies(int armies) {
		this.armies = armies;
	}
	
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //g.getClipBounds().width
//        g.setColor(Color.BLACK);
//        g.drawString("44", 0, 0);
        
        int width = this.getBounds().width; 
        
        if (highlight) {
        	g.setColor(Color.getHSBColor(HIGHLIGHT_RING_COLOR_HSB[0], HIGHLIGHT_RING_COLOR_HSB[1], HIGHLIGHT_RING_COLOR_HSB[2]));
        	g.fillOval(width/2 - diam/2 - 4, 0, this.diam + 8, this.diam + 8);
        }
        
        if (highlight) {
        	g.setColor(TeamView.getHighlightColor(team));        	
        } else {
        	g.setColor(TeamView.getColor(team));
        }
        g.fillOval(width/2 - diam/2, 4, this.diam, this.diam);
        
    }
	
	public Team getTeam() {
		return team;
	}
    
}
