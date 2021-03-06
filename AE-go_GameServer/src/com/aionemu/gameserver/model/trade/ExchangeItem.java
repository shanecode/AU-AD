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
package com.aionemu.gameserver.model.trade;

import com.aionemu.gameserver.model.gameobjects.Item;

/**
 * @author ATracer
 *
 */
public class ExchangeItem
{
	private int itemObjId;
	private int itemCount;
	private int itemDesc;
	private Item item;

	/**
	 * Used when exchange item != original item
	 * 
	 * @param itemObjId
	 * @param itemCount
	 * @param item
	 */
	public ExchangeItem(int itemObjId, int itemCount, Item item)
	{
		this.itemObjId = itemObjId;
		this.itemCount = itemCount;
		this.item = item;
		this.itemDesc = item.getItemTemplate().getNameId();
	}
	
	/**
	 * @param item the item to set
	 */
	public void setItem(Item item)
	{
		this.item = item;
	}

	/**
	 * @param countToAdd
	 */
	public void addCount(int countToAdd)
	{
		this.itemCount += countToAdd;
		this.item.setItemCount(itemCount);
	}

	/**
	 * @return the newItem
	 */
	public Item getItem()
	{
		return item;
	}

	/**
	 * @return the itemObjId
	 */
	public int getItemObjId()
	{
		return itemObjId;
	}

	/**
	 * @return the itemCount
	 */
	public int getItemCount()
	{
		return itemCount;
	}

	/**
	 * @return the itemDesc
	 */
	public int getItemDesc()
	{
		return itemDesc;
	}
}
