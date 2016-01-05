// Copyright 2014 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package conquest.bot;

import java.util.ArrayList;

import conquest.engine.robot.InternalRobot;
import conquest.game.RegionData;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.PlaceArmiesMove;
import conquest.view.GUI;


public interface Bot {
	
	/**
	 * CHOOSE REGIONS - called only at the beginning.
	 * @param state
	 * @param timeOut
	 * @return
	 */
	public ArrayList<RegionData> getPreferredStartingRegions(BotState state, Long timeOut);
	
	/**
	 * PLACE ARMIES - distribute armies between your regions.
	 * @param state
	 * @param timeOut
	 * @return
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut);
	
	/**
	 * MOVE ARMIES - attack opponents' regions or neutral ones ... or transfer armies between your regions.
	 * @param state
	 * @param timeOut
	 * @return
	 */
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut);

	/**
	 * Callback that is invoked only for {@link InternalRobot}s and games with {@link GUI}.
	 * @param gui
	 */
	public void setGUI(GUI gui);
	
}
