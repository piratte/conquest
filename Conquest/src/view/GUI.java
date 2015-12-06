package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import main.Map;
import main.Region;

public class GUI extends JFrame implements MouseListener
{
	private static final long serialVersionUID = 2116436198852146401L;
	private static final String RESOURCE_IMAGE_FILE = "resources/images/conquest-map.jpg";
	private static final int WIDTH = 1239;
	private static final int HEIGHT = 664;
	
	private JLabel roundNumTxt;
	
	private RegionInfo[] regions;
	private boolean goNextRound = false;
	private boolean goNextRound_hold = false;
	
	public GUI()
	{
		System.out.println("GUI: Click to advance to next round.");
		System.out.println("GUI: Hold right mouse button to QUICKLY advance through many rounds.");
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("Interactive Map");
		this.addMouseListener(this);
		
        JLayeredPane mainLayer = new JLayeredPane();
        this.add(mainLayer);

        //Map image
		JLabel labelForImage = new JLabel();
		labelForImage.setBounds(0, 0, WIDTH, HEIGHT);
		URL iconURL = this.getClass().getResource(RESOURCE_IMAGE_FILE);
		ImageIcon icon = new ImageIcon(iconURL);
		labelForImage.setIcon(icon);
		mainLayer.add(labelForImage, JLayeredPane.DEFAULT_LAYER);

		//Current round number
		JPanel subLayer = new JPanel(new BorderLayout());
		roundNumTxt = new JLabel("Round #: --", JLabel.CENTER);
		roundNumTxt.setBounds(20, 20, 75, 15);
		roundNumTxt.setBackground(Color.gray);
		roundNumTxt.setOpaque(true);
		subLayer.add(roundNumTxt, BorderLayout.SOUTH);
		mainLayer.add(roundNumTxt, JLayeredPane.DRAG_LAYER);
				
		//Position of each region's info (team color & number of armies)
		int[][] positions = new int[][]{
			{84, 155}, //1. Alaska
			{202, 156}, //2. Northwest Territory
			{441, 96}, //3. Greenland
			{187, 205}, //4. Alberta
			{255, 209}, //5. Ontario
			{346, 209}, //6. Quebec
			{215, 263}, //7. Western United States
			{285,277}, //8. Eastern United States
			{246,326}, //9. Central America
			{346,382}, //10. Venezuela
			{344,445}, //11. Peru
			{410,439}, //12 Brazil
			{367,511}, //13. Argentina
			{514,158}, //14. Iceland
			{570,215}, //15. Great Britain
			{627,160}, //16. Scandinavia
			{699,205}, //17. Ukraine
			{556,269}, //18. Western Europe
			{616, 223}, //19. Northern Europe
			{640, 247}, //20. Southern Europe
			{576,339}, //21. North Africa
			{647,316}, //22. Egypt
			{691,378}, //23. East Africa
			{647,405}, //24. Congo
			{657,475}, //25. South Africa
			{726,469}, //26. Madagascar
			{794,178}, //27. Ural
			{880,146}, //28. Siberia
			{972,150}, //29. Yakutsk
			{1069,154}, //30. Kamchatka
			{942,205}, //31. Irkutsk
			{789,242}, //32. Kazakhstan
			{889,279}, //33. China
			{961,247}, //34. Mongolia
			{1024,279}, //35. Japan
			{716,295}, //36. Middle East
			{828,321}, //37. India
			{905,352}, //38. Siam
			{941,402}, //39. Indonesia
			{1030,422}, //40. New Guinea
			{983,484}, //41. Western Australia
			{1048,495}, //42. Eastern Australia
		};

		this.regions = new RegionInfo[42];
		
		for (int idx = 0; idx < 42; idx++) {
			this.regions[idx] = new RegionInfo();
			this.regions[idx].setLocation(positions[idx][0], positions[idx][1]);
			mainLayer.add(this.regions[idx], JLayeredPane.PALETTE_LAYER);
		}
		
		//Legend
		RegionInfo p1 = new RegionInfo();
		p1.setLocation(10,50);
		p1.setTeam("player1");
		p1.setText("p1");
		mainLayer.add(p1, JLayeredPane.PALETTE_LAYER);
		
		RegionInfo p2 = new RegionInfo();
		p2.setLocation(40,50);
		p2.setTeam("player2");
		p2.setText("p2");
		mainLayer.add(p2, JLayeredPane.PALETTE_LAYER);
		

		//Finish
        this.pack();
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	public void updateAfterRound(int roundNum, Map map) { //called by Engine.playRound()
		//Update round number
		roundNumTxt.setText("Round #: " + Integer.toString(roundNum));
		
		//Update regions info
		for(Region region : map.regions) {
			int id = region.getId();
			this.regions[id-1].setText(Integer.toString(region.getArmies()));
			this.regions[id-1].setTeam(region.getPlayerName());
		}

		//Wait for user to request next round
		while(!goNextRound && !goNextRound_hold) { //wait for click, or skip if right button down
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		goNextRound = false;
	}
	
	public static void main (String[] args) 
	{
		new GUI();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			goNextRound = true;
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			goNextRound_hold = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			goNextRound_hold = false;
		}
	} 
} 

