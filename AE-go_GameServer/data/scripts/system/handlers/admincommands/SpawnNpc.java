/*
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
package admincommands;

import com.aionemu.gameserver.configs.AdminConfig;
import com.aionemu.gameserver.dataholders.SpawnData;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.SpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.google.inject.Inject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Luno
 */

public class SpawnNpc extends AdminCommand {

    private final SpawnData spawnData;

    private final SpawnEngine spawnService;

    /**
     * @param spawnData
     * @param spawnService
     */

    @Inject
    public SpawnNpc(SpawnData spawnData, SpawnEngine spawnService) {
        super("spawn");
        this.spawnData = spawnData;
        this.spawnService = spawnService;
    }

    @Override
    public void executeCommand(Player admin, String[] params) {
        if (admin.getCommonData().getAdminRole() < AdminConfig.COMMAND_SPAWNNPC)
        {
            PacketSendUtility.sendMessage(admin, "You dont have enough rights to execute this command");
            return;
        }

        if (params.length < 1)
        {
            PacketSendUtility.sendMessage(admin, "syntax //spawn <template_id>");
            return;
        }

        int templateId = Integer.parseInt(params[0]);
        float x = admin.getX();
        float y = admin.getY();
        float z = admin.getZ();
        byte heading = admin.getHeading();
        int worldId = admin.getWorldId();

        SpawnTemplate spawn = spawnData.addNewSpawn(worldId, templateId, x, y, z, heading, 0, 0);

        if (spawn == null)
        {
            PacketSendUtility.sendMessage(admin, "There is no template with id " + templateId);
            return;
        }

        VisibleObject visibleObject = spawnService.spawnObject(spawn);
        String objectName = "";
        if (visibleObject instanceof Npc)
        {
            objectName = ((Npc) visibleObject).getTemplate().getName();
        }
        else if (visibleObject instanceof Gatherable)
        {
            objectName = ((Gatherable) visibleObject).getTemplate().getName();
        }

        String file = "data/static_data/spawns/new/" + worldId + ".txt";
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(file, true));

            StringBuilder sb = new StringBuilder();
            sb.append("#").append(objectName).append(" (spawned from game)\n");
            sb.append(worldId).append(" ");
            sb.append(templateId).append(" ");
            sb.append(x).append(" ").append(y).append(" ").append(z).append(" ").append(heading).append("0\n");
            out.write(sb.toString());
            out.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        PacketSendUtility.sendMessage(admin, objectName + " spawned and save spawndata to file");
    }
}
