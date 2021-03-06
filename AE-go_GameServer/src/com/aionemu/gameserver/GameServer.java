/*
 * This file is part of aion-emu <aion-emu.com>.
 *
 * aion-emu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aion-emu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.log4j.exceptions.Log4jInitializationError;
import com.aionemu.commons.network.NioServer;
import com.aionemu.commons.services.LoggingService;
import com.aionemu.commons.utils.AEInfos;
import com.aionemu.gameserver.configs.Config;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.TaskManagerConfig;
import com.aionemu.gameserver.configs.main.ThreadConfig;
import com.aionemu.gameserver.controllers.BannedChatController;
import com.aionemu.gameserver.dao.NpcSpawnDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dataholders.loadingutils.XmlServiceProxy;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.spawn.NpcSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawn.SpawnTemplate;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.services.FortressService;
import com.aionemu.gameserver.services.ServiceProxy;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.taskmanager.tasks.PacketBroadcaster;
import com.aionemu.gameserver.unishell.Unishell;
import com.aionemu.gameserver.utils.AEVersions;
import com.aionemu.gameserver.utils.AutoAnnounce;
import com.aionemu.gameserver.utils.DeadlockDetector;
import com.aionemu.gameserver.utils.InfluenceManager;
import com.aionemu.gameserver.utils.LocaleManager;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.ThreadUncaughtExceptionHandler;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.gametime.GameTimeManager;
import com.aionemu.gameserver.utils.guice.DataInjectionModule;
import com.aionemu.gameserver.utils.guice.IDFactoriesInjectionModule;
import com.aionemu.gameserver.utils.guice.NetworkInjectionModule;
import com.aionemu.gameserver.utils.guice.ObjectFactoryInjectionModule;
import com.aionemu.gameserver.world.World;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * <tt>GameServer</tt> is the main class of the application and represents the whole game server.<br>
 * This class is also an entry point with main() method.
 * 
 * @author -Nemesiss-
 * @author SoulKeeper
 */
public class GameServer
{
	/** Logger for gameserver */
	private static final Logger	log	= Logger.getLogger(GameServer.class);

	private Injector			injector;
	
	private static String 				configurationFileLocation;

	/**
	 * Creates instance of GameServer, which includes loading static data, initializing world.
	 */
	private GameServer()
	{
		// Inits the injection module and injector itself
		// This will trigger creating singletons that are defined
		// in InjectionModule with asEagerSingleton() call
		DataInjectionModule dataIM = new DataInjectionModule();
		NetworkInjectionModule networkIM = new NetworkInjectionModule();
		ObjectFactoryInjectionModule controllerIM = new ObjectFactoryInjectionModule();

		injector = Guice.createInjector(dataIM, networkIM, new IDFactoriesInjectionModule(), controllerIM);
		dataIM.setInjector(injector);
		networkIM.setInjector(injector);

		// after all data is loaded need to set service proxy to xml service adapter
		injector.getInstance(XmlServiceProxy.class).setServiceProxy(injector.getInstance(ServiceProxy.class));
	}

	/**
	 * Launching method for GameServer
	 * 
	 * @param args
	 *            arguments, not used
	 */
	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();
		
		configurationFileLocation = args[0];
		
		initUtilityServicesAndConfig();

		GameServer gs = new GameServer();
		// Set all players is offline
		DAOManager.getDAO(PlayerDAO.class).setPlayersOffline(false);
		gs.spawnMonsters();
		gs.initQuests();
		gs.initBroker();
		
		Util.printSection("TaskManagers");
		PacketBroadcaster.getInstance();

		Util.printSection("System");
		AEVersions.printFullVersionInfo();
		AEInfos.printAllInfos();
		
		// Load chat bans
		BannedChatController.load();
		
		Util.printSection("GameServerLog");
		log.info("AE Game Server started in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");

		gs.startServers();
		GameTimeManager.startClock();

		if(TaskManagerConfig.DEADLOCK_DETECTOR_ENABLED)
		{
			log.info("Starting deadlock detector");
			new Thread(new DeadlockDetector(TaskManagerConfig.DEADLOCK_DETECTOR_INTERVAL)).start();
		}

		Runtime.getRuntime().addShutdownHook(gs.injector.getInstance(ShutdownHook.class));
		
		//Thread unishell = new Thread(new Unishell(8675));
		//unishell.start();
		
		World world = gs.injector.getInstance(World.class);
		Thread autoAnnounce = new Thread(new AutoAnnounce(world));
		autoAnnounce.start();
		
		if(gs.injector.getInstance(FortressService.class) instanceof FortressService)
		{
			log.info("Successfully loaded FortressService");
		}
		
		gs.injector.getInstance(FortressService.class).initialize();
		
		InfluenceManager.initialize(world);
		
		LocaleManager.initialize();

		// gs.injector.getInstance(com.aionemu.gameserver.utils.chathandlers.ChatHandlers.class);
		onStartup();
	}

	/**
	 * This method is basically responsible for triggering NPCs spawning with {@link SpawnEngine}.
	 */
	private void spawnMonsters()
	{
		Util.printSection("Spawns");

		SpawnEngine spawnEngine = injector.getInstance(SpawnEngine.class);
		spawnEngine.setInjector(injector);
		spawnEngine.spawnAll();	
		
		ArrayList<NpcSpawnTemplate> databaseTemplates = DAOManager.getDAO(NpcSpawnDAO.class).getAllTemplates();
		log.info("Successfully loaded " + databaseTemplates.size() + " database NPC spawn templates");
		
		DAOManager.getDAO(NpcSpawnDAO.class).clearCache();
		log.info("Successfully cleared the database NPC spawns cache.");
		
		Map<Integer,Integer> successfullySpawned = new HashMap<Integer,Integer>();
		
		for(NpcSpawnTemplate tpl : databaseTemplates)
		{
			SpawnTemplate spawnTemplate = spawnEngine.addNewSpawn(tpl.getMap(), 1, tpl.getNpcTemplateId(), tpl.getX(), tpl.getY(), tpl.getZ(), tpl.getHeading(), 0, 0, false, true);
			VisibleObject obj = spawnEngine.spawnObject(spawnTemplate, 1);
			if(obj != null)
			{
				DAOManager.getDAO(NpcSpawnDAO.class).insertCache(tpl.getSpawnTemplateId(), obj.getObjectId());
				int mapId = obj.getPosition().getMapId();
				if(successfullySpawned.get(mapId) == null)
				{
					successfullySpawned.put(mapId, 1);
				}
				else
				{
					int currentValue = successfullySpawned.get(mapId);
					successfullySpawned.remove(mapId);
					successfullySpawned.put(mapId, currentValue + 1);
				}
			}
			else
			{
				log.info("invalid spawn object id #" + tpl.getSpawnTemplateId());
			}
		}
		
		Set<Entry<Integer, Integer>> dta = successfullySpawned.entrySet();
		for(Entry<Integer, Integer> row : dta)
		{
			log.info("[XpawnEngine] Spawned " + row.getKey() + " : " + row.getValue());
		}
		
		//log.info("Successfully spawned " + databaseTemplates.size() + " NPC templates from database");
		
	}

	private void initQuests()
	{
		Util.printSection("Quests");

		QuestEngine questEngine = injector.getInstance(QuestEngine.class);
		questEngine.setInjector(injector);
		questEngine.load();
	}
	
	private void initBroker()
	{
		BrokerService brService = injector.getInstance(BrokerService.class);
		brService.initBrokerService();
	}

	/**
	 * Starts servers for connection with aion client and login server.
	 */
	private void startServers()
	{
		NioServer nioServer = injector.getInstance(NioServer.class);
		LoginServer loginServer = injector.getInstance(LoginServer.class);
		ChatServer chatServer = injector.getInstance(ChatServer.class);
		// Nio must go first
		nioServer.connect();
		loginServer.connect();
		
		if(!GSConfig.DISABLE_CHAT_SERVER)
			chatServer.connect();
	}

	/**
	 * Initialize all helper services, that are not directly related to aion gs, which includes:
	 * <ul>
	 * <li>Logging</li>
	 * <li>Database factory</li>
	 * <li>Thread pool</li>
	 * </ul>
	 * 
	 * This method also initializes {@link Config}
	 * 
	 * @throws Log4jInitializationError
	 */
	private static void initUtilityServicesAndConfig() throws Log4jInitializationError
	{
		// Set default uncaught exception handler
		Thread.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());
		// First of all we must initialize logging
		LoggingService.init();
		// init config
		Config.loadAionDream(configurationFileLocation);
		// Second should be database factory
		Util.printSection("DataBase");
		DatabaseFactory.init();
		// Initialize DAOs
		DAOManager.init();
		// Initialize thread pools
		Util.printSection("Threads");
		ThreadConfig.load();
		ThreadPoolManager.getInstance();
	}

	private static Set<StartupHook>	startUpHooks	= new HashSet<StartupHook>();

	public synchronized static void addStartupHook(StartupHook hook)
	{
		if(startUpHooks != null)
			startUpHooks.add(hook);
		else
			hook.onStartup();
	}

	private synchronized static void onStartup()
	{
		final Set<StartupHook> startupHooks = startUpHooks;

		startUpHooks = null;

		for(StartupHook hook : startupHooks)
			hook.onStartup();
	}

	public interface StartupHook
	{
		public void onStartup();
	}
}
