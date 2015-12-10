package conquest.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sun.awt.AWTUtilities;

import conquest.game.GameMap;
import conquest.game.RegionData;
import conquest.game.ContinentData;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.PlaceArmiesMove;
import conquest.game.world.Continent;
import conquest.game.world.Region;
import conquest.view.RegionInfo.Team;



public class GUI extends JFrame implements MouseListener, KeyListener
{
	private static final long serialVersionUID = 2116436198852146401L;
	private static final String RESOURCE_IMAGE_FILE = "resources/images/conquest-map.jpg";
	private static final int WIDTH = 1239;
	private static final int HEIGHT = 664;
	
	private JLabel roundNumTxt;
	private JLabel actionTxt;
	
	private RegionInfo[] regions;
	private boolean clicked = false;
	private boolean rightClick = false;
	private boolean nextRound = false;
	private boolean continual = false;
	private int continualTime = 600;
	
	private String playerName1;
	private String playerName2;
	
	private RegionInfo p1;
	private RegionInfo p2;	
	
	public GUI(String playerName1, String playerName2)
	{
		System.out.println("GUI: Click to advance to next round.");
		System.out.println("GUI: Hold right mouse button to QUICKLY advance through many rounds.");
		
		this.playerName1 = playerName1;
		this.playerName2 = playerName2;
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("Interactive Map");
		this.addMouseListener(this);
		this.addKeyListener(this);
		
		this.setLayout(null);
		
        JLayeredPane mainLayer = new JLayeredPane();
        mainLayer.setBounds(0, 0, WIDTH, HEIGHT);
        mainLayer.setSize(WIDTH, HEIGHT);
        mainLayer.setPreferredSize(mainLayer.getSize());
        mainLayer.setLocation(0, -19);
        this.add(mainLayer);

        //Map image
		JLabel labelForImage = new JLabel();
		labelForImage.setBounds(0, 0, WIDTH, HEIGHT);
		URL iconURL = this.getClass().getResource(RESOURCE_IMAGE_FILE);
		ImageIcon icon = new ImageIcon(iconURL);
		labelForImage.setIcon(icon);
		mainLayer.add(labelForImage, JLayeredPane.DEFAULT_LAYER);

		//Current round number
		roundNumTxt = new JLabel("Round #: --", JLabel.CENTER);
		roundNumTxt.setBounds(20, 20, 150, 15);
		roundNumTxt.setBackground(Color.gray);
		roundNumTxt.setOpaque(true);
		roundNumTxt.setForeground(Color.WHITE);
		mainLayer.add(roundNumTxt, JLayeredPane.DRAG_LAYER);
		
		actionTxt = new JLabel("ACTION", JLabel.CENTER);
		actionTxt.setBounds(20, 40, 150, 15);
		actionTxt.setBackground(Color.gray);
		actionTxt.setOpaque(true);
		actionTxt.setForeground(Color.WHITE);
		actionTxt.setSize(150, 15);
		actionTxt.setPreferredSize(actionTxt.getSize());
		actionTxt.setLocation(20, 35);
		mainLayer.add(actionTxt, JLayeredPane.DRAG_LAYER);
				
		//Position of each region's info (team color & number of armies)
		int[][] positions = new int[][]{
			{95, 150},  //1.  Alaska
			{209, 160}, //2.  Northwest Territory
			{441, 96},  //3.  Greenland
			{190, 205}, //4.  Alberta
			{257, 209}, //5.  Ontario
			{355, 203}, //6.  Quebec
			{224, 263}, //7.  Western United States
			{295,277},  //8.  Eastern United States
			{255,333},  //9.  Central America
			{350,373},  //10. Venezuela
			{344,445},  //11. Peru
			{415,434},  //12. Brazil
			{374,511},  //13. Argentina
			{514,158},  //14. Iceland
			{545,200},  //15. Great Britain
			{627,160},  //16. Scandinavia
			{699,205},  //17. Ukraine
			{556,266},  //18. Western Europe
			{618, 218}, //19. Northern Europe
			{650, 255}, //20. Southern Europe
			{576,339},  //21. North Africa
			{647,316},  //22. Egypt
			{698,379},  //23. East Africa
			{654,408},  //24. Congo
			{657,478},  //25. South Africa
			{726,465},  //26. Madagascar
			{800,178},  //27. Ural
			{890,146},  //28. Siberia
			{972,150},  //29. Yakutsk
			{1080,150}, //30. Kamchatka
			{942,205},  //31. Irkutsk
			{798,242},  //32. Kazakhstan
			{895,279},  //33. China
			{965,242},  //34. Mongolia
			{1030,279}, //35. Japan
			{716,295},  //36. Middle East
			{835,316},  //37. India
			{908,348},  //38. Siam
			{930,412},  //39. Indonesia
			{1035,422}, //40. New Guinea
			{983,484},  //41. Western Australia
			{1055,500}, //42. Eastern Australia
		};

		this.regions = new RegionInfo[42];
		
		for (int idx = 0; idx < 42; idx++) {
			this.regions[idx] = new RegionInfo();
			this.regions[idx].setLocation(positions[idx][0] - 50, positions[idx][1]);
			this.regions[idx].setRegion(Region.forId(idx+1));			
			mainLayer.add(this.regions[idx], JLayeredPane.PALETTE_LAYER);
		}
		
		//Legend
		p1 = new RegionInfo();
		p1.setLocation(45,50);
		p1.setTeam(Team.PLAYER_1);
		p1.setNameLabel(playerName1);
		p1.setText("P1");
		mainLayer.add(p1, JLayeredPane.PALETTE_LAYER);
		
		p2 = new RegionInfo();
		p2.setLocation(45,85);
		p2.setTeam(Team.PLAYER_2);
		p2.setNameLabel(playerName2);
		p2.setText("P2");
		mainLayer.add(p2, JLayeredPane.PALETTE_LAYER);
		
		//Finish
        this.pack();
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	public RegionInfo.Team getTeam(String player) {
		if (player.equals(playerName1)) return Team.PLAYER_1;
		if (player.equals(playerName2)) return Team.PLAYER_2;
		return Team.NEUTRAL;
	}
	
	// ==============
	// MOUSE LISTENER
	// ==============
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			clicked = true;
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
			rightClick = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			rightClick = false;
		}
	}
	
	private void waitForClick() {
		int time = continualTime;
		
		while(!clicked && !rightClick && !nextRound) { //wait for click, or skip if right button down
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			time -= 50;
			if (continual && time <= 0) break;
		}
		clicked = false;
	}
	
	// ============
	// KEY LISTENER
	// ============
	
	@Override
	public void keyTyped(KeyEvent e) {
		char c = e.getKeyChar();
		c = Character.toLowerCase(c);
		switch(c) {
		case 'n':
			nextRound = true;
			break;
		case 'c':
			continual = !continual;
			break;
		case '-':
			continualTime += 50;
			continualTime = Math.min(continualTime, 3000);
			break;
		case '+':
			continualTime -= 50;
			continualTime = Math.max(continualTime, 200);
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
	
	// =============
	// ENGINE EVENTS
	// =============
	
	private void updateStats() {
		
		int plr1Regions = 0;
		int plr2Regions = 0;
		
		int plr1Armies = 0;
		int plr2Armies = 0;
		
		int plr1Income = 5;
		int plr2Income = 5;
		
		for (RegionInfo region : this.regions) {
			switch(region.getTeam()) {
			case PLAYER_1:
				++plr1Regions;
				plr1Armies += region.getArmies();
				break;
			case PLAYER_2:
				++plr2Regions;
				plr2Armies += region.getArmies();
				break;
			}
		}
		
		for (Continent continent : Continent.values()) {
			boolean plr1 = true;
			boolean plr2 = true;
			for (Region region : continent.getRegions()) {
				RegionInfo info = regions[region.id-1];
				if (info.getTeam() == Team.PLAYER_1) plr2 = false;
				if (info.getTeam() == Team.PLAYER_2) plr1 = false;
				if (info.getTeam() == Team.NEUTRAL) {
					plr1 = false;
					plr2 = false;
				}
				if (!plr1 && !plr2) break;
			}
			if (plr1) plr1Income += continent.reward;
			if (plr2) plr2Income += continent.reward;
		}
		
		p1.setText("[" + plr1Regions + " / " + plr1Armies + " / +" + plr1Income + "]");
		p2.setText("[" + plr2Regions + " / " + plr2Armies + " / +" + plr2Income + "]");
	}
	
	public void updateAfterRound(int roundNum, GameMap map) { //called by Engine.playRound()
		//Update round number
		roundNumTxt.setText("Round #: " + Integer.toString(roundNum));
		
		//Update regions info
		for(RegionData region : map.regions) {
			int id = region.getId();
			this.regions[id-1].setArmies(region.getArmies());
			this.regions[id-1].setText(Integer.toString(region.getArmies()));			
			this.regions[id-1].setTeam(getTeam(region.getPlayerName()));
		}

		actionTxt.setText("NEW ROUND");
		
		nextRound = false;
		
		updateStats();
		
		//Wait for user to request next round
		waitForClick();
	}

	List<RegionData> pickableRegions = null;
	
	public void pickableRegions(List<RegionData> pickableRegions) {
		actionTxt.setText("PICKABLE REGIONS");
		
		for (RegionData regionData : pickableRegions) {
			int id = regionData.getId();
			RegionInfo region = this.regions[id-1];
			region.setHighlight();
		}
		
		this.pickableRegions = pickableRegions;
		
		waitForClick();
	}
	
	public void firstPlaceArmies(List<PlaceArmiesMove> placeArmiesMoves) {
		actionTxt.setText("CHOSEN REGIONS");
		
		for (PlaceArmiesMove move : placeArmiesMoves) {
			int id = move.getRegion().getId();
			RegionInfo region = this.regions[id-1];
			if (move.getPlayerName().equals(playerName1)) region.setTeam(Team.PLAYER_1);
			if (move.getPlayerName().equals(playerName2)) region.setTeam(Team.PLAYER_2);
			region.armiesPlus += move.getArmies();
			region.setText(region.getArmies() + "+" + region.armiesPlus);			
			region.setHighlight();
		}

		waitForClick();
		
		for (PlaceArmiesMove move : placeArmiesMoves) {
			int id = move.getRegion().getId();
			RegionInfo region = this.regions[id-1];
			region.setArmies(region.getArmies() + region.armiesPlus);
			region.armiesPlus = 0;
			region.setText("" + region.getArmies());
			region.setTeamColor();
		}
		
		for (RegionData regionData : pickableRegions) {
			int id = regionData.getId();
			RegionInfo region = this.regions[id-1];
			region.setTeamColor();
		}
		
		updateStats();
	}
	
	public void placeArmies(List<PlaceArmiesMove> placeArmiesMoves) {
		actionTxt.setText("PLACE ARMIES");
		
		for (PlaceArmiesMove move : placeArmiesMoves) {
			int id = move.getRegion().getId();
			RegionInfo region = this.regions[id-1];
			region.armiesPlus += move.getArmies();
			region.setText(region.getArmies() + "+" + region.armiesPlus);
			region.setHighlight();
		}
		
		waitForClick();
		
		for (PlaceArmiesMove move : placeArmiesMoves) {
			int id = move.getRegion().getId();
			RegionInfo region = this.regions[id-1];
			region.setArmies(region.getArmies() + region.armiesPlus);
			region.armiesPlus = 0;
			region.setText("" + region.getArmies());
			region.setTeamColor();
		}
		
		actionTxt.setText("---");
		
		updateStats();
	}	

	public void transfer(AttackTransferMove move) {
		actionTxt.setText("TRANSFER BY " + move.getPlayerName());
		
		RegionInfo fromRegion = this.regions[move.getFromRegion().getId() - 1];
		RegionInfo toRegion = this.regions[move.getToRegion().getId() - 1];
		int armies = move.getArmies();
		
		fromRegion.armiesPlus = -armies;
		fromRegion.setText(fromRegion.getArmies() + " - " + armies);
		fromRegion.setHighlight();
		
		toRegion.armiesPlus = armies;
		toRegion.setText(toRegion.getArmies() + " + " + armies);
		toRegion.setHighlight();
		
		waitForClick();
		
		fromRegion.setTeamColor();
		fromRegion.setArmies(fromRegion.getArmies() + fromRegion.armiesPlus);
		fromRegion.setText(String.valueOf(fromRegion.getArmies()));
		fromRegion.armiesPlus = 0;
		
		toRegion.setTeamColor();
		toRegion.setArmies(toRegion.getArmies() + toRegion.armiesPlus);
		toRegion.setText(String.valueOf(toRegion.getArmies()));
		toRegion.armiesPlus = 0;
		
		actionTxt.setText("---");
	}

	public void attack(AttackTransferMove move) {
		actionTxt.setText("ATTACK BY " + move.getPlayerName());
		
		RegionInfo fromRegion = this.regions[move.getFromRegion().getId() - 1];
		RegionInfo toRegion = this.regions[move.getToRegion().getId() - 1];
		int armies = move.getArmies();
		
		fromRegion.armiesPlus = -armies;
		fromRegion.setText(fromRegion.getArmies() + " > " + armies);
		fromRegion.setHighlight();
		
		toRegion.armiesPlus = armies;
		toRegion.setText(toRegion.getArmies() + " < " + armies);
		toRegion.setHighlight();
		
		waitForClick();		
	}

	public void attackResult(RegionData fromRegionData, RegionData toRegionData, int attackersDestroyed, int defendersDestroyed) {
		RegionInfo fromRegion = this.regions[fromRegionData.getId() - 1];
		RegionInfo toRegion = this.regions[toRegionData.getId() - 1];
		
		if (fromRegionData.getPlayerName().equals(toRegionData.getPlayerName())) {
			actionTxt.setText("SUCCESS [A:" + (attackersDestroyed > 0 ? "-" : "") + attackersDestroyed + " | D:" + (defendersDestroyed > 0 ? "-" : "") + defendersDestroyed + "]");
			toRegion.setTeam(getTeam(toRegionData.getPlayerName()));
			toRegion.setArmies((-fromRegion.armiesPlus) - attackersDestroyed);
			toRegion.armiesPlus = 0;
			toRegion.setText("" + toRegion.getArmies());
		} else {
			actionTxt.setText("FAILURE [A:" + (attackersDestroyed > 0 ? "-" : "") + attackersDestroyed + " | D:" + (defendersDestroyed > 0 ? "-" : "") + defendersDestroyed + "]");			
			toRegion.setArmies(toRegion.getArmies() - defendersDestroyed);
			toRegion.armiesPlus = 0;
			toRegion.setText("" + toRegion.getArmies());
		}
		
		fromRegion.setArmies(fromRegion.getArmies() + fromRegion.armiesPlus);		
		fromRegion.armiesPlus = 0;
		fromRegion.setText("" + fromRegion.getArmies());
		
		fromRegion.setHighlight();
		toRegion.setHighlight();
		
		waitForClick();		
		
		fromRegion.setTeamColor();
		toRegion.setTeamColor();
		
		actionTxt.setText("---");	
		
		updateStats();
	}	
	
} 

