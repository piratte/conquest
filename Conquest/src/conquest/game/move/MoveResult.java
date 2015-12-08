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

package conquest.game.move;

import conquest.game.GameMap;

public class MoveResult {
	
	private final Move move;
	private final GameMap map;
	
	public MoveResult(Move move, GameMap resultingMap)
	{
		this.move = move;
		this.map = resultingMap;
	}
	
	public Move getMove()
	{
		return this.move;
	}
	
	public GameMap getMap()
	{
		return this.map;
	}

}
