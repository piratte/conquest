package view;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class RegionInfo extends JPanel {
	private int diam;
	private Color col;
	private JLabel txt;

	public RegionInfo(String team, int diam) {
		init(diam, team);
	}
	public RegionInfo(String team) {
		init(30, team);
	}
	public RegionInfo() {
		init(30, "a");
	}
	
	private void init(int diam, String team) {
		this.setTeam(team);
		
		this.setOpaque(false);
		this.setBounds(0,0,diam,diam);
		
		//Text
        this.txt = new JLabel("45", JLabel.CENTER);
        this.txt.setBounds(200, 400, diam, diam);
//        subLabel.setLocation(100, 200);
        this.txt.setOpaque(false);
        this.add(this.txt);
        
        //Circle
        this.diam = diam;
	}
	
	public void setText(String s) {
		this.txt.setText(s);
		this.revalidate();
		this.repaint();
	}
	public void setTeam(String team) {
		if (team == "player1") {
			this.col = Color.red;
		} else if (team == "player2") {
			this.col = Color.cyan;
		} else { //neutral
			this.col = Color.gray;
		}
		this.revalidate();
		this.repaint();
	}
	
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //g.getClipBounds().width
        //g.setColor(Color.BLACK);
        //g.drawString("44", 0, 0);
        g.setColor(this.col);
        g.fillOval(0, 0, this.diam, this.diam);
    }
}
