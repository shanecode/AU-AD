/*
 * This file is part of aion-unique <aion-unique.org>.
 *
 *  aion-unique is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-unique is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.skillengine.model;

/**
 * @author ATracer
 *
 */
public enum SpellStatus
{
	/**
	 * Spell Status
	 * 
	 * 1 : stumble
	 * 2 : knockback
	 * 4 : open aerial
	 * 8 : close aerial
	 * 16 : spin
	 * 32 : block
	 * 64 : parry
	 * 128 : dodge
	 * 256 : resist
	 */
	NONE(0),
	STUMBLE(1),
	STAGGER(2),
	OPENAERIAL(4);
	
	private int id;

	private SpellStatus(int id)
	{
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}
	
	

}
