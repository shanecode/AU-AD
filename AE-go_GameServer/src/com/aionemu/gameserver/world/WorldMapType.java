/*
 * This file is part of aion-unique <aion-unique.org>.
 *
 * aion-unique is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aion-unique is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.world;

public enum WorldMapType
{
	PRISON(510010000),
	PANDAEMONIUM(120010000),
	ISHALGEN(220010000),
	MORHEIM(220020000),
	ALTGARD(220030000),
	BELUSLAN(220040000),
	BRUSTHONIN(220050000),

	SANCTUM(110010000),
	POETA(210010000),
	VERTERON(210030000),
	ELTNEN(210020000),
	HEIRON(210040000),
	THEOMOBOS(210060000),

        //instances
        SKYTEMPLEINTERIOR(320050000),
        DARKPOETA(300040000),
        ADMASTRONGHOLD(320130000),
        ASTERIACHAMBER(300050000),
        CHAMBEROFROAH(300070000),
        DREDGION(300110000),
        KROTANCHAMBER(300140000), 
        KYSISCHAMBER(300120000),
        LEFTWINGCHAMBER(300080000),
        MINERCHAMBER(300130000),
        NOCHSANATRAININGCAMP(300030000),
        RINGWINGCHAMBER(300090000),
        STEELRAKE(300100000),
        SULFURTREENEST(300060000),
        THEOBOMOSLAB(310110000),
		DROPNIR(320080000),

	RESHANTA(400010000),
	
	//Instances
	NOCHSANA_TRAINING_CAMP(300030000),
	FIRE_TEMPLE(320100000);

	private final int worldId;

	WorldMapType(int worldId )
	{
		this.worldId = worldId;
	}

	public int getId()
	{
		return worldId;
	}
	
	/**
	 * @param id of world
	 */
	public static WorldMapType getWorld(int id)
	{
		for (WorldMapType type : WorldMapType.values())
		{
			if (type.getId() == id)
			{
				return type;
			}
		}
		return null;
	}
}