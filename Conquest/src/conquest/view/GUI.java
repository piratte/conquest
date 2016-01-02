package conquest.view;

import java.awt.Button;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import conquest.game.GameMap;
import conquest.game.RegionData;
import conquest.game.Team;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.PlaceArmiesMove;
import conquest.game.world.Continent;
import conquest.game.world.Region;
import conquest.view.TriButton.ClickListener;



public class GUI extends JFrame implements MouseListener, KeyListener
{
	private static final long serialVersionUID = 2116436198852146401L;
	private static final String RESOURCE_IMAGE_FILE = "resources/images/conquest-map.jpg";
	private static final int WIDTH = 1239;
	private static final int HEIGHT = 664;
	
	public static int[][] positionsAIvsAI = new int[][]{
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
	
	//Position of each region's info (team color & number of armies)
	public static final int[][] positionsHuman = new int[][]{
				{95, 150},  //1.  Alaska
				{209, 143}, //2.  Northwest Territory
				{441, 96},  //3.  Greenland
				{190, 202}, //4.  Alberta
				{257, 203}, //5.  Ontario
				{355, 203}, //6.  Quebec
				{214, 263}, //7.  Western United States
				{295,273},  //8.  Eastern United States
				{255,333},  //9.  Central America
				{350,373},  //10. Venezuela
				{344,445},  //11. Peru
				{425,434},  //12. Brazil
				{374,511},  //13. Argentina
				{520,135},  //14. Iceland
				{538,195},  //15. Great Britain
				{627,130},  //16. Scandinavia
				{719,195},  //17. Ukraine
				{560,255},  //18. Western Europe
				{628,196},  //19. Northern Europe
				{650,255},  //20. Southern Europe
				{576,339},  //21. North Africa
				{654,316},  //22. Egypt
				{740,375},  //23. East Africa
				{654,408},  //24. Congo
				{657,478},  //25. South Africa
				{736,465},  //26. Madagascar
				{800,178},  //27. Ural
				{865,116},  //28. Siberia
				{972,110},  //29. Yakutsk
				{1080,150}, //30. Kamchatka
				{942,175},  //31. Irkutsk
				{798,242},  //32. Kazakhstan
				{895,279},  //33. China
				{975,250},  //34. Mongolia
				{1057,279}, //35. Japan
				{730,295},  //36. Middle East
				{815,316},  //37. India
				{908,348},  //38. Siam
				{930,412},  //39. Indonesia
				{1035,422}, //40. New Guinea
				{971,484},  //41. Western Australia
				{1055,500}, //42. Eastern Australia
			};
	
	public static int[][] positions = positionsAIvsAI;
	
	private GUINotif notification;
	
	private JLabel roundNumTxt;
	private JLabel actionTxt;
	
	private RegionInfo[] regions;
	private boolean clicked = false;
	private boolean rightClick = false;
	private boolean nextRound = false;
	private boolean continual = false;
	private int continualTime = 800;
	
	private String playerName1;
	private String playerName2;
	
	private RegionInfo p1;
	private RegionInfo p2;	
	
	private JLayeredPane mainLayer;
	
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
		
        mainLayer = new JLayeredPane();
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
		
		notification = new GUINotif(mainLayer, 1015, 45, 200, 50);		
		
		//Finish
        this.pack();
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
                
	}
	
	public Team getTeam(String player) {
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
		long time = System.currentTimeMillis() + continualTime;
		
		while(!clicked && !rightClick && !nextRound) { //wait for click, or skip if right button down
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (continual && time < System.currentTimeMillis()) break; // skip if continual action and time out
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
			showNotification("SKIP TO NEXT ROUND");
			break;
		case 'c':
			continual = !continual;
			showNotification( continual ? "Continual run enabled" : "Continual run disabled");
			break;
		case '+':
			continualTime += 50;
			continualTime = Math.min(continualTime, 3000);
			showNotification("Action visualized for: " + continualTime + " ms");
			break;
		case '-':
			continualTime -= 50;
			continualTime = Math.max(continualTime, 200);
			showNotification("Action visualized for: " + continualTime + " ms");
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
	
	// =====
	// NOTIF
	// =====
	
	public void showNotification(String txt) {
		notification.show(txt, 1500);
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
		this.requestFocusInWindow();
		
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
		this.requestFocusInWindow();
		
		actionTxt.setText("PICKABLE REGIONS");
		
		for (RegionData regionData : pickableRegions) {
			int id = regionData.getId();
			RegionInfo region = this.regions[id-1];
			region.setHighlight(true);
		}
		
		this.pickableRegions = pickableRegions;
		
		waitForClick();
	}
	
	public void updateRegions(List<RegionData> regions) {
		this.requestFocusInWindow();
		
		for (RegionData data : regions) {
			int id = data.getId();
			RegionInfo region = this.regions[id-1];
			region.setTeam(getTeam(data.getPlayerName()));
			region.setArmies(data.getArmies());
			region.setText("" + region.getArmies());
		}
	}
	
	public void regionsChosen(List<RegionData> regions) {
		this.requestFocusInWindow();
		
		actionTxt.setText("CHOSEN REGIONS");
		
		updateRegions(regions);
		
		for (RegionData data : regions) {
			int id = data.getId();
			RegionInfo region = this.regions[id-1];
			region.setHighlight(region.getTeam() != Team.NEUTRAL);
		}

		waitForClick();
		
		for (RegionData regionData : regions) {
			int id = regionData.getId();
			RegionInfo region = this.regions[id-1];
			region.setHighlight(false);
		}
		
		updateStats();
	}
	
	public void placeArmies(LinkedList<RegionData> regions, List<PlaceArmiesMove> placeArmiesMoves) {
		this.requestFocusInWindow();
		
		actionTxt.setText("PLACE ARMIES");
		
		updateRegions(regions);
		
		for (PlaceArmiesMove move : placeArmiesMoves) {
			int id = move.getRegion().getId();
			RegionInfo region = this.regions[id-1];	
			region.setArmies(region.getArmies() - move.getArmies());
			region.armiesPlus += move.getArmies();
			region.setText(region.getArmies() + "+" + region.armiesPlus);
			region.setHighlight(true);
		}
		
		waitForClick();
		
		for (PlaceArmiesMove move : placeArmiesMoves) {
			int id = move.getRegion().getId();
			RegionInfo region = this.regions[id-1];
			region.setArmies(region.getArmies() + region.armiesPlus);
			region.armiesPlus = 0;
			region.setText("" + region.getArmies());
			region.setHighlight(false);
		}
		
		actionTxt.setText("---");
		
		updateStats();
	}	

	public void transfer(AttackTransferMove move) {
		this.requestFocusInWindow();
		
		actionTxt.setText("TRANSFER BY " + move.getPlayerName());
		
		RegionInfo fromRegion = this.regions[move.getFromRegion().getId() - 1];
		RegionInfo toRegion = this.regions[move.getToRegion().getId() - 1];
		int armies = move.getArmies();
		
		fromRegion.armiesPlus = -armies;
		fromRegion.setText(fromRegion.getArmies() + " - " + armies);
		fromRegion.setHighlight(true);
		
		toRegion.armiesPlus = armies;
		toRegion.setText(toRegion.getArmies() + " + " + armies);
		toRegion.setHighlight(true);
		
		waitForClick();
		
		fromRegion.setHighlight(false);
		fromRegion.setArmies(fromRegion.getArmies() + fromRegion.armiesPlus);
		fromRegion.setText(String.valueOf(fromRegion.getArmies()));
		fromRegion.armiesPlus = 0;
		
		toRegion.setHighlight(false);
		toRegion.setArmies(toRegion.getArmies() + toRegion.armiesPlus);
		toRegion.setText(String.valueOf(toRegion.getArmies()));
		toRegion.armiesPlus = 0;
		
		actionTxt.setText("---");
	}

	public void attack(AttackTransferMove move) {
		this.requestFocusInWindow();
		
		actionTxt.setText("ATTACK BY " + move.getPlayerName());
		
		RegionInfo fromRegion = this.regions[move.getFromRegion().getId() - 1];
		RegionInfo toRegion = this.regions[move.getToRegion().getId() - 1];
		int armies = move.getArmies();
		
		fromRegion.armiesPlus = -armies;
		fromRegion.setText(fromRegion.getArmies() + " > " + armies);
		fromRegion.setHighlight(true);
		
		toRegion.armiesPlus = armies;
		toRegion.setText(toRegion.getArmies() + " < " + armies);
		toRegion.setHighlight(true);
		
		waitForClick();		
	}

	public void attackResult(RegionData fromRegionData, RegionData toRegionData, int attackersDestroyed, int defendersDestroyed) {
		this.requestFocusInWindow();
		
		RegionInfo fromRegion = this.regions[fromRegionData.getId() - 1];
		RegionInfo toRegion = this.regions[toRegionData.getId() - 1];
		
		if (fromRegionData.getPlayerName().equals(toRegionData.getPlayerName())) {
			actionTxt.setText("SUCCESS [A:" + (attackersDestroyed > 0 ? "-" : "") + attackersDestroyed + " | D:" + (defendersDestroyed > 0 ? "-" : "") + defendersDestroyed + "]");
			fromRegion.setArmies(fromRegion.getArmies() + fromRegion.armiesPlus);
			toRegion.setTeam(getTeam(toRegionData.getPlayerName()));
			toRegion.setArmies((-fromRegion.armiesPlus) - attackersDestroyed);
		} else {
			actionTxt.setText("FAILURE [A:" + (attackersDestroyed > 0 ? "-" : "") + attackersDestroyed + " | D:" + (defendersDestroyed > 0 ? "-" : "") + defendersDestroyed + "]");
			fromRegion.setArmies(fromRegion.getArmies() - attackersDestroyed);
			toRegion.setArmies(toRegion.getArmies() - defendersDestroyed);
		}
					
		
		fromRegion.armiesPlus = 0;
		fromRegion.setText("" + fromRegion.getArmies());
		
		toRegion.armiesPlus = 0;
		toRegion.setText("" + toRegion.getArmies());
		
		fromRegion.setHighlight(true);
		toRegion.setHighlight(true);
		
		waitForClick();		
		
		fromRegion.setHighlight(false);
		toRegion.setHighlight(false);
		
		actionTxt.setText("---");	
		
		updateStats();
	}
	
	// --------------
	// ==============
	// HUMAN CONTROLS
	// ==============
	// --------------
	
	// ======================
	// CHOOSE INITIAL REGIONS
	// ======================
	
	private CountDownLatch chooseRegionsAction;
	
	private List<Region> chosenRegions;
	
	private List<Region> availableRegions;
	
	private List<Button> regionButtons;
	
	private Button finishedButton;
	
	private String chooseRegionsPlayerName;
	
	public List<Region> chooseRegionsHuman(String playerName, List<Region> availableRegions) {
		this.requestFocusInWindow();
		
		continual = true;
		
		this.chooseRegionsPlayerName = playerName;
		chooseRegionsAction = new CountDownLatch(1);
		
		this.availableRegions = availableRegions;
		this.chosenRegions = new ArrayList<Region>();
		
		actionTxt.setText(chooseRegionsPlayerName + ": choose " + (6-chosenRegions.size()) + " regions");
		
		regionButtons = new ArrayList<Button>();
		
		for (Region region : availableRegions) {
			Button button = new Button("+");
			button.setForeground(Color.WHITE);
			button.setBackground(Color.BLACK);
			button.setSize(30, 20);
			int[] regionPos = positions[region.id-1];
			button.setLocation(regionPos[0] - 15, regionPos[1] + 40);
			regionButtons.add(button);
			
			mainLayer.add(button, JLayeredPane.PALETTE_LAYER);
			
			final Region targetRegion = region;
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					regionClicked(targetRegion);
					GUI.this.requestFocusInWindow();
				}
			});
		}
		
		finishedButton = new Button("DONE");
		finishedButton.setForeground(Color.WHITE);
		finishedButton.setBackground(Color.BLACK);
		finishedButton.setSize(60, 30);
		finishedButton.setLocation(175, 20);
		finishedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseRegionsAction.countDown();
				GUI.this.requestFocusInWindow();
			}
		});
		finishedButton.setVisible(false);
		mainLayer.add(finishedButton, JLayeredPane.PALETTE_LAYER);
		
		try {
			chooseRegionsAction.await();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while awaiting user action.");
		}
		
		for (Button button : regionButtons) {
			mainLayer.remove(button);			
		}
		mainLayer.remove(finishedButton);
		
		for (Region region : availableRegions) {
			if (chosenRegions.size() == 6) break;
			if (chosenRegions.contains(region)) continue;
			chosenRegions.add(region);
		}
		
		return chosenRegions;
	}
	
	private void regionClicked(Region region) {
		if (chosenRegions.contains(region)) {
			chosenRegions.remove(region);
			renumberButtons();
			finishedButton.setVisible(chosenRegions.size() == 6);
			return;
		}
		
		if (chosenRegions.size() == 6) return;

		chosenRegions.add(region);
		renumberButtons();
		
		finishedButton.setVisible(chosenRegions.size() == 6);
	}
	
	private void renumberButtons() {
		actionTxt.setText(chooseRegionsPlayerName + ": choose " + (6-chosenRegions.size()) + " regions");
		for (int i = 0; i < regionButtons.size(); ++i) {
			Button button = regionButtons.get(i);
			if (chosenRegions.contains(availableRegions.get(i))) {
				int index = chosenRegions.indexOf(availableRegions.get(i));				
				button.setLabel(String.valueOf(index+1) + ".");
			} else {
				button.setLabel("+");
			}
		}
	}
	
	// ============
	// PLACE ARMIES
	// ============
	
	private CountDownLatch placeArmiesAction;
	
	private List<PlaceArmiesMove> placeArmies;
	
	private String placeArmiesPlayerName;
	
	private int totalArmies;
	
	private int armiesLeft;
	
	private List<Region> armyRegions;
	
	private List<TriButton> armyRegionButtons;
	
	private Button placeArmiesFinishedButton;
		
	public List<PlaceArmiesMove> placeArmiesHuman(String playerName, Team team, int startingArmies) {
		this.requestFocusInWindow();
		
		List<Region> availableRegions = new ArrayList<Region>();
		for (int i = 0; i < regions.length; ++i) {
			RegionInfo info = regions[i];
			if (info.getTeam() == team) {
				availableRegions.add(Region.values()[i]);
			}			
		}
		return placeArmiesHuman(playerName, startingArmies, availableRegions);
	}
	
	public List<PlaceArmiesMove> placeArmiesHuman(String playerName, int totalArmies, List<Region> availableRegions) {
		this.armyRegions = availableRegions;
		this.placeArmiesPlayerName = playerName;
		
		placeArmiesAction = new CountDownLatch(1);
		
		placeArmies = new ArrayList<PlaceArmiesMove>();
		
		actionTxt.setText(placeArmiesPlayerName + ": place " + totalArmies + " armies");
		
		this.totalArmies = totalArmies;
		this.armiesLeft = totalArmies;		
		
		armyRegionButtons = new ArrayList<TriButton>();
		
		int ch1 = 1;
		int ch2 = 4;
		int ch3 = 10;
		
		for (Region region : armyRegions) {
			int[] regionPos = positions[region.id-1];
			
			TriButton button = new TriButton(mainLayer, regionPos[0], regionPos[1] + 30, ch1, ch2, ch3);
			
			armyRegionButtons.add(button);
						
			final Region targetRegion = region;
			
			button.clickListener = new ClickListener() {
				@Override
				public void clicked(int change) {
					placeArmyRegionClicked(targetRegion, change);
					GUI.this.requestFocusInWindow();
				}
			};
		}
		
		if (placeArmiesFinishedButton == null) {
			placeArmiesFinishedButton = new Button("DONE");
			placeArmiesFinishedButton.setForeground(Color.WHITE);
			placeArmiesFinishedButton.setBackground(Color.BLACK);
			placeArmiesFinishedButton.setSize(60, 30);
			placeArmiesFinishedButton.setLocation(175, 20);
			placeArmiesFinishedButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (armiesLeft == 0) {
						placeArmiesAction.countDown();
					}
					GUI.this.requestFocusInWindow();
				}
			});
		}
		mainLayer.add(placeArmiesFinishedButton, JLayeredPane.PALETTE_LAYER);
		placeArmiesFinishedButton.setVisible(false);
		
		try {
			placeArmiesAction.await();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while awaiting user action.");
		}
		
		for (TriButton button : armyRegionButtons) {
			button.dispose();
		}
		mainLayer.remove(placeArmiesFinishedButton);
		
		List<PlaceArmiesMove> result = new ArrayList<PlaceArmiesMove>();
		
		for (Region region : availableRegions) {
			RegionInfo info = regions[region.id-1];
			if (info.armiesPlus > 0) {
				info.setArmies(info.getArmies() + info.armiesPlus);
				info.setText("" + info.getArmies());
				info.setHighlight(false);

				PlaceArmiesMove command = new PlaceArmiesMove(playerName, new RegionData(region, region.id, null), info.armiesPlus);
				info.armiesPlus = 0;
				
				result.add(command);
			}
		}
		
		return result;
	}
	
	private void placeArmyRegionClicked(Region region, int change) {		
		change = Math.min(armiesLeft, change);
		if (change == 0) return;
		
		RegionInfo info = regions[region.id-1];
		
		if (change < 0) {
			change = -Math.min(Math.abs(change), info.armiesPlus);
		}
		if (change == 0) return;
		
		info.armiesPlus += change;
		armiesLeft -= change;
		
		if (info.armiesPlus > 0) {
			info.setText(info.getArmies() + "+" + info.armiesPlus);
			info.setHighlight(true);
		} else {
			info.setText(String.valueOf(info.getArmies()));
			info.setHighlight(false);
		}
		
		actionTxt.setText(placeArmiesPlayerName + ": place " + armiesLeft + " armies");
		
		placeArmiesFinishedButton.setVisible(armiesLeft == 0);
	}

	// ===========
	// MOVE ARMIES
	// ===========
	
	private CountDownLatch moveArmiesAction;
	
	private List<AttackTransferMove> moveArmies;
	
	private String moveArmiesPlayerName;
	
	private List<Region> moveArmyPlayerRegions;
	private List<Region> moveArmyOtherRegions;
	
	private List<Button> moveArmyPlayerRegionButtons;
	private List<Button> moveArmyOtherRegionButtons;
	
	private List<TriButton> moveArmyPlayerRegionTriButtons;
	private List<TriButton> moveArmyOtherRegionTriButtons;
	
	private Button moveArmiesFinishedButton;
		
	public List<AttackTransferMove> moveArmiesHuman(String playerName, Team team) {
		this.requestFocusInWindow();
		
		List<Region> playerRegions = new ArrayList<Region>();
		List<Region> otherRegions = new ArrayList<Region>();
		for (int i = 0; i < regions.length; ++i) {
			RegionInfo info = regions[i];
			if (info.getTeam() == team) {
				playerRegions.add(Region.values()[i]);
			} else {
				otherRegions.add(Region.values()[i]);
			}
		}
		return moveArmiesHuman(playerName, playerRegions, otherRegions);
	}
	
	public List<AttackTransferMove> moveArmiesHuman(String playerName, List<Region> playerRegions, List<Region> otherRegions) {
		this.moveArmiesPlayerName = playerName;	
		this.moveArmyPlayerRegions = playerRegions;
		this.moveArmyOtherRegions = otherRegions;
		
		actionTxt.setText(moveArmiesPlayerName + ": move armies");
			
		moveArmiesAction = new CountDownLatch(1);
		
		moveArmies = new ArrayList<AttackTransferMove>();
		
		moveArmyPlayerRegionButtons = new ArrayList<Button>();
		
		for (Region region : moveArmyPlayerRegions) {
			Button button = new Button(">>");
			button.setForeground(Color.WHITE);
			button.setBackground(Color.BLACK);
			button.setSize(30, 20);			
			int[] regionPos = positions[region.id-1];
			button.setLocation(regionPos[0] - 15, regionPos[1] + 40);
			moveArmyPlayerRegionButtons.add(button);
			
			mainLayer.add(button, JLayeredPane.PALETTE_LAYER);
			
			final Region targetRegion = region;
			final Button targetButton = button;
			button.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						moveArmyRegionClicked(targetButton, targetRegion, 1);
					} else {
						moveArmyRegionClicked(targetButton, targetRegion, -1);
					}
					GUI.this.requestFocusInWindow();
				}
			});
		}
				
		showPlayerRegionButtons();
		
		moveArmyOtherRegionButtons = new ArrayList<Button>();
		
		for (Region region : moveArmyOtherRegions) {
			Button button = new Button("<<");
			button.setForeground(Color.WHITE);
			button.setBackground(Color.DARK_GRAY);
			button.setSize(30, 20);
			button.setVisible(false);
			int[] regionPos = positions[region.id-1];
			button.setLocation(regionPos[0] - 15, regionPos[1] + 40);
			moveArmyOtherRegionButtons.add(button);
			
			mainLayer.add(button, JLayeredPane.PALETTE_LAYER);
			
			final Region targetRegion = region;
			final Button targetButton = button;
			button.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						moveArmyRegionClicked(targetButton, targetRegion, 1);
					} else {
						moveArmyRegionClicked(targetButton, targetRegion, -1);
					}
					GUI.this.requestFocusInWindow();
				}
			});
		}
		
		hideAllOtherButtons(null);
		
		int ch1 = 1;
		int ch2 = 4;
		int ch3 = 10;
		
		moveArmyPlayerRegionTriButtons = new ArrayList<TriButton>();
		
		for (Region region : moveArmyPlayerRegions) {
			int[] regionPos = positions[region.id-1];
			
			TriButton button = new TriButton(mainLayer, regionPos[0], regionPos[1] + 30, ch1, ch2, ch3, false);			
			
			moveArmyPlayerRegionTriButtons.add(button);			
						
			button.clickListener = new ClickListener() {
				@Override
				public void clicked(int change) {
					updateMoveArmies(change);
					GUI.this.requestFocusInWindow();
				}
			};
		}
		
		moveArmyOtherRegionTriButtons = new ArrayList<TriButton>();
		
		for (Region region : moveArmyOtherRegions) {
			int[] regionPos = positions[region.id-1];
			
			TriButton button = new TriButton(mainLayer, regionPos[0], regionPos[1] + 30, ch1, ch2, ch3, false);			
			
			moveArmyOtherRegionTriButtons.add(button);			
						
			button.clickListener = new ClickListener() {
				@Override
				public void clicked(int change) {
					updateMoveArmies(change);
					GUI.this.requestFocusInWindow();
				}
			};
		}
		
		if (moveArmiesFinishedButton == null) {
			moveArmiesFinishedButton = new Button("DONE");
			moveArmiesFinishedButton.setForeground(Color.WHITE);
			moveArmiesFinishedButton.setBackground(Color.BLACK);
			moveArmiesFinishedButton.setSize(60, 30);
			moveArmiesFinishedButton.setLocation(175, 20);
			moveArmiesFinishedButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (armiesLeft == 0) {
						moveArmiesAction.countDown();
					}
					GUI.this.requestFocusInWindow();
				}
			});
		}
		mainLayer.add(moveArmiesFinishedButton, JLayeredPane.PALETTE_LAYER);
		
		try {
			moveArmiesAction.await();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while awaiting user action.");
		}
		
		for (Button button : moveArmyPlayerRegionButtons) {
			mainLayer.remove(button);			
		}
		moveArmyPlayerRegionButtons.clear();
		
		for (Button button : moveArmyOtherRegionButtons) {
			mainLayer.remove(button);			
		}
		moveArmyOtherRegionButtons.clear();
		
		for (TriButton button : moveArmyPlayerRegionTriButtons) {
			button.dispose();			
		}
		moveArmyPlayerRegionTriButtons.clear();
		
		for (TriButton button : moveArmyOtherRegionTriButtons) {
			button.dispose();			
		}
		moveArmyOtherRegionTriButtons.clear();
		
		mainLayer.remove(moveArmiesFinishedButton);
		
		for (RegionInfo info : regions) {
			info.setText("" + info.getArmies());
			info.armiesPlus = 0;
			info.setHighlight(false);
		}
		
		return moveArmies;
	}
	
	private Button getMoveArmyPlayerButton(Region region) {
		int index = moveArmyPlayerRegions.indexOf(region);
		if (index < 0) return null;
		return moveArmyPlayerRegionButtons.get(index);
	}
	
	private Button getMoveArmyOtherButton(Region region) {
		int index = moveArmyOtherRegions.indexOf(region);
		if (index < 0) return null;
		return moveArmyOtherRegionButtons.get(index);
	}
	
	private Button getMoveArmyButton(Region region) {
		Button result = getMoveArmyPlayerButton(region);
		if (result == null) return getMoveArmyOtherButton(region);
		return result;
	}
	
	private TriButton getMoveArmyPlayerTriButton(Region region) {
		int index = moveArmyPlayerRegions.indexOf(region);
		if (index < 0) return null;
		return moveArmyPlayerRegionTriButtons.get(index);
	}
	
	private TriButton getMoveArmyOtherTriButton(Region region) {
		int index = moveArmyOtherRegions.indexOf(region);
		if (index < 0) return null;
		return moveArmyOtherRegionTriButtons.get(index);
	}
		
	private TriButton getMoveArmyTriButton(Region region) {
		TriButton result = getMoveArmyPlayerTriButton(region);
		if (result == null) return getMoveArmyOtherTriButton(region);
		return result;
	}
	
	private void showPlayerRegionButtons() {
		for (int i = 0; i < moveArmyPlayerRegionButtons.size(); ++i) {
			Button button = moveArmyPlayerRegionButtons.get(i);
			Region region = moveArmyPlayerRegions.get(i);
			RegionInfo info = regions[region.id-1];
			button.setVisible(info.getArmies() > 1);
		}
	}
	
	private void hidePlayerRegionButtonsExcept(Region except) {
		for (int i = 0; i < moveArmyPlayerRegionButtons.size(); ++i) {			
			if (moveArmyPlayerRegions.get(i) == except) {
				continue;
			}
			Button button = moveArmyPlayerRegionButtons.get(i);
			button.setVisible(false);
		}
	}
	
	private void hideAllOtherButtons(Region except) {
		for (int i = 0; i < moveArmyOtherRegionButtons.size(); ++i) {
			Button button = moveArmyOtherRegionButtons.get(i);
			Region region = moveArmyOtherRegions.get(i);
			if (region == except) continue;
			RegionInfo info = regions[region.id-1];
			button.setVisible(false);
		}
	}
	
	private void showOtherButton(Region region) {
		int index = moveArmyOtherRegions.indexOf(region);
		Button button = moveArmyOtherRegionButtons.get(index);
		button.setVisible(true);
	}
	
	private void showNeighbors(Region playerRegion) {
		for (Region neighbour : playerRegion.getNeighbours()) {
			Button button = getMoveArmyButton(neighbour);
			button.setLabel("<<");
			button.setVisible(true);
		}
	}
	
	private Region moveFrom;
	private Region moveTo;
	private int movePlayerArmies;
	
	private void moveArmyRegionClicked(Button targetButton, Region region, int change) {
		if (targetButton.getLabel().equals("X")) {
			regions[moveFrom.id-1].setHighlight(false);
			
			moveFrom = null;
			showPlayerRegionButtons();
			hideAllOtherButtons(null);
			targetButton.setLabel(">>");	
			
			moveArmiesFinishedButton.setVisible(true);
			
			updateMoveArmiesHighlight();
			return;
		}
		
		if (targetButton.getLabel().equals(">>")) {
			moveFrom = region;
			
			regions[moveFrom.id-1].setHighlight(true);
			
			hidePlayerRegionButtonsExcept(region);
			hideAllOtherButtons(null);
			showNeighbors(region);
			targetButton.setLabel("X");
			
			moveArmiesFinishedButton.setVisible(false);
			
			updateMoveArmiesHighlight();			
			return;
		}
		
		if (targetButton.getLabel().equals("<<")) {
			moveTo = region;
			
			regions[moveTo.id-1].setHighlight(true);
			
			movePlayerArmies = 0;
			AttackTransferMove command = null;
			for (AttackTransferMove cmd : moveArmies) {
				if (cmd.getFromRegion().getRegion() == moveFrom && cmd.getToRegion().getRegion() == moveTo) {
					movePlayerArmies = cmd.getArmies();
					moveArmies.remove(cmd);
					command = cmd;
					break;
				}
			}
//			if (command == null) {
//				for (AttackTransferMove cmd : moveArmies) {
//					if (cmd.getFromRegion().getRegion() == moveFrom) {
//						movePlayerArmies = cmd.getArmies();
//						moveArmies.remove(cmd);
//						moveTo = cmd.getToRegion().getRegion();
//						updateMoveArmies(-cmd.getArmies());
//						moveTo = region;
//						break;
//					}
//				}
//			}
						
			hidePlayerRegionButtonsExcept(moveFrom);
			hideAllOtherButtons(null);
			
			getMoveArmyTriButton(moveTo).setVisible(true);
			
			Button fromButton = getMoveArmyPlayerButton(moveFrom);
			fromButton.setLabel("OK");
			targetButton.setLabel("+");
			return;
		}
		
		if (targetButton.getLabel().equals("OK")) {
			
			regions[moveFrom.id-1].setHighlight(false);
			regions[moveTo.id-1].setHighlight(false);
			
			Button fromButton = getMoveArmyButton(moveFrom);
			Button toButton = getMoveArmyButton(moveTo);
			
			fromButton.setLabel(">>");
			toButton.setLabel("<<");
			
			for (Button button : moveArmyPlayerRegionButtons) {
				button.setLabel(">>");
			}
			
			hideAllOtherButtons(null);
			getMoveArmyTriButton(moveTo).setVisible(false);
			showPlayerRegionButtons();
			
			if (change > 0) {
				// CONFIRM
				if (movePlayerArmies > 0) {
					AttackTransferMove cmd = new AttackTransferMove(moveArmiesPlayerName, new RegionData(moveFrom, moveFrom.id, null), new RegionData(moveTo, moveTo.id, null), movePlayerArmies);
					moveArmies.add(cmd);
				}
			} else {
				// CANCEL
				if (movePlayerArmies > 0) {
					updateMoveArmies(-movePlayerArmies);
				}
				// no need to remove command from the list ... already deleted in "<<"
			}
			
			moveArmiesFinishedButton.setVisible(true);
			
			moveFrom = null;
			updateMoveArmiesHighlight();
			
			return;
		}
		
		if (targetButton.getLabel().equals("+")) {
			updateMoveArmies(change);
			return;
		}
	}
	
	private void updateMoveArmies(int change) {
		RegionInfo from = regions[moveFrom.id-1];
		RegionInfo to   = regions[moveTo.id-1];
		
		int newMovePlayerArmies = movePlayerArmies + change;
		
		if (newMovePlayerArmies < 0) newMovePlayerArmies = 0;
		if (newMovePlayerArmies > from.getArmies() - 1) newMovePlayerArmies = from.getArmies() - 1;
		
		from.armiesPlus += movePlayerArmies;
		from.armiesPlus -= newMovePlayerArmies;
		
		to.armiesPlus -= movePlayerArmies;
		to.armiesPlus += newMovePlayerArmies;
		
		movePlayerArmies = newMovePlayerArmies;
				
		if (from.armiesPlus != 0) {
			from.setText(from.getArmies() + (from.armiesPlus > 0 ? " < " : " > ") + Math.abs(from.armiesPlus));
		} else {
			from.setText("" + from.getArmies());
		}
		
		if (to.armiesPlus != 0) {
			to.setText(to.getArmies() + (to.armiesPlus > 0 ? " < " : " > ") + Math.abs(to.armiesPlus));
		} else {
			to.setText("" + to.getArmies());
		}
	}
	
	private void updateMoveArmiesHighlight() {
		for (Region region : moveArmyPlayerRegions) {
			RegionInfo info = regions[region.id-1];
			boolean command = (moveFrom == region);
			if (!command) {
				for (AttackTransferMove move : moveArmies) {
					if (move.getFromRegion().getRegion() == region) {
						command = true;
						break;
					}
				}
			}
			info.setHighlight(command);
		}
	}
	
	
	
} 

