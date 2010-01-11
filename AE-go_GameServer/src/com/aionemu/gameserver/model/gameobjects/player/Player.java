/**
 * This file is part of aion-emu <aion-emu.com>.
 *
 *  aion-emu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-emu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.model.gameobjects.player;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.commons.callbacks.Enhancable;
import com.aionemu.gameserver.controllers.PlayerController;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.listeners.PlayerLoggedInListener;
import com.aionemu.gameserver.model.gameobjects.player.listeners.PlayerLoggedOutListener;
import com.aionemu.gameserver.model.gameobjects.stats.PlayerGameStats;
import com.aionemu.gameserver.model.gameobjects.stats.PlayerLifeStats;
import com.aionemu.gameserver.model.group.PlayerGroup;
import com.aionemu.gameserver.model.templates.stats.PlayerStatsTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_NEARBY_QUESTS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.PlayerService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * This class is representing Player object, it contains all needed data.
 * 
 * 
 * @author -Nemesiss-
 * @author SoulKeeper
 * @author alexa026
 * 
 */
public class Player extends Creature
{
	private PlayerAppearance	playerAppearance;
	private PlayerCommonData	playerCommonData;
	private MacroList			macroList;
	private SkillList			skillList;
	private FriendList			friendList;
	private BlockList			blockList;
	private ResponseRequester	requester;
	private boolean				lookingForGroup	= false;
	private Inventory			inventory;
	private PlayerStore			store;
	private ExchangeList		exchangeList;
	private PlayerStatsTemplate	playerStatsTemplate;
	private TitleList			titleList;
	private byte[]				uiSettings;
	private byte[]				shortcuts;

	private QuestStateList		questStateList;
	private List<Integer>		nearbyQuestList	= new ArrayList<Integer>();
	private ZoneInstance		zoneInstance;
	private PlayerGroup			playerGroup;
	private PlayerState 		state =  PlayerState.STANDING;

	/** When player enters game its char is in kind of "protection" state, when is blinking etc */
	private boolean				protectionActive;

	/**
	 * Connection of this Player.
	 */
	private AionConnection		clientConnection;

	public Player(PlayerController controller, PlayerCommonData plCommonData, PlayerAppearance appereance)
	{
		super(plCommonData.getPlayerObjId(), controller, null, plCommonData.getPosition());

		this.playerCommonData = plCommonData;
		this.playerAppearance = appereance;

		this.requester = new ResponseRequester(this);
		this.questStateList = new QuestStateList();
		this.titleList = new TitleList();
		controller.setOwner(this);

	}

	public PlayerCommonData getCommonData()
	{
		return playerCommonData;
	}

	@Override
	public String getName()
	{
		return playerCommonData.getName();
	}

	public PlayerAppearance getPlayerAppearance()
	{
		return playerAppearance;
	}

	/**
	 * Set connection of this player.
	 * 
	 * @param clientConnection
	 */
	public void setClientConnection(AionConnection clientConnection)
	{
		this.clientConnection = clientConnection;
	}

	/**
	 * Get connection of this player.
	 * 
	 * @return AionConnection of this player.
	 * 
	 */
	public AionConnection getClientConnection()
	{
		return this.clientConnection;
	}

	public boolean isProtectionActive()
	{
		return protectionActive;
	}

	/**
	 * After entering game player char is "blinking" which means that it's in under some protection, after making an
	 * action char stops blinking.
	 * 
	 * @param protectionActive
	 */
	public void setProtectionActive(boolean protectionActive)
	{
		this.protectionActive = protectionActive;
		if(!protectionActive)
			PacketSendUtility.sendPacket(this, new SM_PLAYER_STATE(this, 0));
	}

	public MacroList getMacroList()
	{
		return macroList;
	}

	public void setMacroList(MacroList macroList)
	{
		this.macroList = macroList;
	}

	public SkillList getSkillList()
	{
		return skillList;
	}

	public void setSkillList(SkillList skillList)
	{
		this.skillList = skillList;
	}

	/**
	 * Gets this players Friend List
	 * 
	 * @return
	 */
	public FriendList getFriendList()
	{
		return friendList;
	}

	/**
	 * Is this player looking for a group
	 * 
	 * @return
	 */
	public boolean isLookingForGroup()
	{
		return lookingForGroup;
	}

	/**
	 * Sets whether or not this player is looking for a group
	 * 
	 * @param lookingForGroup
	 */
	public void setLookingForGroup(boolean lookingForGroup)
	{
		this.lookingForGroup = lookingForGroup;
	}

	/**
	 * Sets this players friend list. <br />
	 * Remember to send the player the <tt>SM_FRIEND_LIST</tt> packet.
	 * 
	 * @param list
	 */
	public void setFriendList(FriendList list)
	{
		this.friendList = list;
	}

	public ExchangeList getExchangeList()
	{
		return exchangeList;
	}

	public void setExchangeList(ExchangeList list)
	{
		this.exchangeList = list;
	}

	public BlockList getBlockList()
	{
		return blockList;
	}

	public void setBlockList(BlockList list)
	{
		this.blockList = list;
	}

	/**
	 * @return the playerLifeStats
	 */
	public PlayerLifeStats getLifeStats()
	{
		return (PlayerLifeStats) super.getLifeStats();
	}

	/**
	 * @param lifeStats
	 *            the lifeStats to set
	 */
	public void setLifeStats(PlayerLifeStats lifeStats)
	{
		super.setLifeStats(lifeStats);
	}

	/**
	 * @return the gameStats
	 */
	public PlayerGameStats getGameStats()
	{
		return (PlayerGameStats) super.getGameStats();
	}

	/**
	 * @param gameStats
	 *            the gameStats to set
	 */
	public void setGameStats(PlayerGameStats gameStats)
	{
		super.setGameStats(gameStats);
	}

	/**
	 * Gets the ResponseRequester for this player
	 * 
	 * @return
	 */
	public ResponseRequester getResponseRequester()
	{
		return requester;
	}

	public boolean isOnline()
	{
		return getClientConnection() != null;
	}

	public int getCubeSize()
	{
		return this.playerCommonData.getCubeSize();
	}

	public PlayerClass getPlayerClass()
	{
		return playerCommonData.getPlayerClass();
	}

	public Gender getGender()
	{
		return playerCommonData.getGender();
	}

	/**
	 * Return PlayerController of this Player Object.
	 * 
	 * @return PlayerController.
	 */
	@Override
	public PlayerController getController()
	{
		return (PlayerController) super.getController();
	}

	@Override
	public byte getLevel()
	{
		return (byte) playerCommonData.getLevel();
	}

	/**
	 * @return the inventory
	 */
	public Inventory getInventory()
	{
		return inventory;
	}

	/**
	 * @return the player private store
	 */
	public PlayerStore getStore()
	{
		return store;
	}

	/**
	 * @return the player private store
	 */
	public void setStore(PlayerStore store)
	{
		this.store = store;
		store.setOwner(this);
	}

	/**
	 * @return the questStatesList
	 */
	public QuestStateList getQuestStateList()
	{
		return questStateList;
	}

	/**
	 * @param questStateList
	 *            the QuestStateList to set
	 */
	public void setQuestStateList(QuestStateList questStateList)
	{
		this.questStateList = questStateList;
	}

	/**
	 * @return the playerStatsTemplate
	 */
	public PlayerStatsTemplate getPlayerStatsTemplate()
	{
		return playerStatsTemplate;
	}

	/**
	 * @param playerStatsTemplate
	 *            the playerStatsTemplate to set
	 */
	public void setPlayerStatsTemplate(PlayerStatsTemplate playerStatsTemplate)
	{
		this.playerStatsTemplate = playerStatsTemplate;
	}

	public void updateNearbyQuests()
	{
		nearbyQuestList.clear();
		for(VisibleObject obj : getKnownList())
		{
			if(obj instanceof Npc)
			{
				for (int questId : QuestEngine.getInstance().getNpcQuestData(((Npc)obj).getNpcId()).getOnQuestStart())
				{
					QuestEnv env = new QuestEnv(obj, this, questId, 0);
					if (QuestEngine.getInstance().getQuest(env).checkStartCondition())
					{
					    if (!nearbyQuestList.contains(questId))
					    {
					    	nearbyQuestList.add(questId);
					    }
					}
				}
			}
		}
		PacketSendUtility.sendPacket(this, new SM_NEARBY_QUESTS(nearbyQuestList));
	}

	public List<Integer> getNearbyQuests()
	{
		return nearbyQuestList;
	}

	/**
	 * @param inventory
	 *            the inventory to set Inventory should be set right after player object is created
	 */
	public void setInventory(Inventory inventory)
	{
		this.inventory = inventory;
		inventory.setOwner(this);
	}

	/**
	 * @param CubeUpgrade
	 *            int Sets the cubesize
	 */
	public void setCubesize(int cubesize)
	{
		this.playerCommonData.setCubesize(cubesize);
	}

	/**
	 * @return the shortcuts
	 */
	public byte[] getShortcuts()
	{
		return shortcuts;
	}

	/**
	 * @param shortcuts
	 *            the shortcuts to set
	 */
	public void setShortcuts(byte[] shortcuts)
	{
		this.shortcuts = shortcuts;
	}

	/**
	 * @return the uiSettings
	 */
	public byte[] getUiSettings()
	{
		return uiSettings;
	}

	/**
	 * @param uiSettings
	 *            the uiSettings to set
	 */
	public void setUiSettings(byte[] uiSettings)
	{
		this.uiSettings = uiSettings;
	}

	/**
	 * @return the zoneInstance
	 */
	public ZoneInstance getZoneInstance()
	{
		return zoneInstance;
	}

	/**
	 * @param zoneInstance the zoneInstance to set
	 */
	public void setZoneInstance(ZoneInstance zoneInstance)
	{
		this.zoneInstance = zoneInstance;
	}

	public TitleList getTitleList()
	{
		return titleList;
	}
	
	public void setTitleList(TitleList titleList)
	{
		this.titleList = titleList;
		titleList.setOwner(this);
	}

	/**
	 * @return the playerGroup
	 */
	public PlayerGroup getPlayerGroup()
	{
		return playerGroup;
	}

	/**
	 * @param playerGroup the playerGroup to set
	 */
	public void setPlayerGroup(PlayerGroup playerGroup)
	{
		this.playerGroup = playerGroup;
	}

	@Override
	public void initializeAi()
	{
		// TODO Auto-generated method stub

	}
	
	/**
	 * @return the state
	 */
	public PlayerState getState()
	{
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(PlayerState state)
	{
		this.state = state;
	}

	/**
	 * This method is called when player logs into the game. It's main responsibility is to call all registered
	 * listeners.<br>
	 * <br>
	 * 
	 * <b><font color='red'>NOTICE: </font>this method is supposed to be called only from
	 * {@link PlayerService#playerLoggedIn(Player)}</b>
	 */
	@Enhancable(callback = PlayerLoggedInListener.class)
	public void onLoggedIn()
	{

	}

	/**
	 * This method is called when player leaves the game. It's main responsibility is to call all registered listeners.<br>
	 * <br>
	 * 
	 * <b><font color='red'>NOTICE: </font>this method is supposed to be called only from
	 * {@link PlayerService#playerLoggedOut(Player)}</b>
	 */
	@Enhancable(callback = PlayerLoggedOutListener.class)
	public void onLoggedOut()
	{

	}
}
