package conquest.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import conquest.game.world.Region;
import conquest.view.RegionInfo.Team;

public class RegionInfo extends JPanel {
	
	public static final int[] PLAYER_1_COLOR_RGB = new int[] { 255, 160, 160 };
	public static final int[] PLAYER_2_COLOR_RGB = new int[] { 160, 160, 255 };
	public static final int[] NEUTRAL_COLOR_RGB = new int[] { 240, 240, 240 };
	
	public static final int[] PLAYER_1_HIGHLIGHT_COLOR_RGB = new int[] { 255, 120, 120 };
	public static final int[] PLAYER_2_HIGHLIGHT_COLOR_RGB = new int[] { 120, 120, 255 };
	public static final int[] NEUTRAL_HIGHLIGHT_COLOR_RGB = new int[] { 220, 220, 220 };
	
	public static final float[] PLAYER_1_COLOR_HSB = Color.RGBtoHSB(PLAYER_1_COLOR_RGB[0], PLAYER_1_COLOR_RGB[1], PLAYER_1_COLOR_RGB[2], null);
	public static final float[] PLAYER_2_COLOR_HSB = Color.RGBtoHSB(PLAYER_2_COLOR_RGB[0], PLAYER_2_COLOR_RGB[1], PLAYER_2_COLOR_RGB[2], null);
	public static final float[] NEUTRAL_COLOR_HSB = Color.RGBtoHSB(NEUTRAL_COLOR_RGB[0], NEUTRAL_COLOR_RGB[1], NEUTRAL_COLOR_RGB[2], null);
	public static final float[] PLAYER_1_HIGHLIGHT_COLOR_HSB = Color.RGBtoHSB(PLAYER_1_HIGHLIGHT_COLOR_RGB[0], PLAYER_1_HIGHLIGHT_COLOR_RGB[1], PLAYER_1_HIGHLIGHT_COLOR_RGB[2], null);
	public static final float[] PLAYER_2_HIGHLIGHT_COLOR_HSB = Color.RGBtoHSB(PLAYER_2_HIGHLIGHT_COLOR_RGB[0], PLAYER_2_HIGHLIGHT_COLOR_RGB[1], PLAYER_2_HIGHLIGHT_COLOR_RGB[2], null);
	public static final float[] NEUTRAL_HIGHLIGHT_COLOR_HSB = Color.RGBtoHSB(NEUTRAL_HIGHLIGHT_COLOR_RGB[0], NEUTRAL_HIGHLIGHT_COLOR_RGB[1], NEUTRAL_HIGHLIGHT_COLOR_RGB[2], null);
	
	
	public static enum Team {
		PLAYER_1(Color.getHSBColor(PLAYER_1_COLOR_HSB[0], PLAYER_1_COLOR_HSB[1], PLAYER_1_COLOR_HSB[2]),
				 Color.getHSBColor(PLAYER_1_HIGHLIGHT_COLOR_HSB[0], PLAYER_1_HIGHLIGHT_COLOR_HSB[1], PLAYER_1_HIGHLIGHT_COLOR_HSB[2])),
		PLAYER_2(Color.getHSBColor(PLAYER_2_COLOR_HSB[0], PLAYER_2_COLOR_HSB[1], PLAYER_2_COLOR_HSB[2]),
				 Color.getHSBColor(PLAYER_2_HIGHLIGHT_COLOR_HSB[0], PLAYER_2_HIGHLIGHT_COLOR_HSB[1], PLAYER_2_HIGHLIGHT_COLOR_HSB[2])),
				 
		NEUTRAL(Color.getHSBColor(NEUTRAL_COLOR_HSB[0], NEUTRAL_COLOR_HSB[1], NEUTRAL_COLOR_HSB[2]),
				Color.getHSBColor(NEUTRAL_HIGHLIGHT_COLOR_HSB[0], NEUTRAL_HIGHLIGHT_COLOR_HSB[1], NEUTRAL_HIGHLIGHT_COLOR_HSB[2]));
		
		public final Color color;
		public final Color highlight;
		
		private Team(Color c, Color highlight) {
			this.color = c;
			this.highlight = highlight;
		}		
	}
		
	private int diam;
	private Color circleColor;
	private JLabel txt;
	private JLabel name;
	private Region region;
	private int armies = 0;
	private Team team;
	private boolean highlight = false;

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
	
	public void setTeamColor() {
		this.circleColor = team.color;
		this.highlight = false;
		this.revalidate();
		this.repaint();
	}
	
	public void setHighlight() {		
		this.highlight = true;
		this.circleColor = team.highlight;
		
		//this.txt.setForeground(Color.LIGHT_GRAY);
		//this.name.setForeground(Color.LIGHT_GRAY);
		//this.revalidate();
		this.repaint();
	}
	
	public void setTeam(Team team) {
		this.team = team;
		this.circleColor = team.color;
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
        	g.setColor(Color.WHITE);
        	g.fillOval(width/2 - diam/2 - 4, 0, this.diam + 8, this.diam + 8);
        }
        
        g.setColor(this.circleColor);              
        g.fillOval(width/2 - diam/2, 4, this.diam, this.diam);
        
    }
	
	public Team getTeam() {
		return team;
	}
    
}
