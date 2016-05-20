package com.m4thg33k.tombmanygraves.core.handlers;

import com.m4thg33k.tombmanygraves.TombManyGraves;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerDataHandler {

    public static final String NBT_ROOT = TombManyGraves.MODID + "Data";

    private static Map<Integer, PlayerData> playerData = new HashMap();

    public static PlayerData getData(EntityPlayer player)
    {
        int key = getKey(player);

        if (!playerData.containsKey(key))
        {
            playerData.put(key,new PlayerData(player));
        }

        PlayerData data = playerData.get(key);

        if (data.playerWR.get() != player)
        {
            NBTTagCompound tags = new NBTTagCompound();
            data.writeToNBT(tags);
            playerData.remove(key);
            data = getData(player);
            data.readFromNBT(tags);
        }

        return data;
    }

    public static void cleanup()
    {
        List<Integer> remove = new ArrayList();

        for (int i : playerData.keySet())
        {
            PlayerData data = playerData.get(i);
            if (data != null && data.playerWR.get() == null)
            {
                remove.add(i);
            }
        }

        for (int i : remove)
        {
            playerData.remove(i);
        }
    }

    private static int getKey(EntityPlayer player)
    {
        return player.hashCode() << 1 + (player.worldObj.isRemote ? 1 : 0);
    }

    public static NBTTagCompound getDataCompoundForPlayer(EntityPlayer player)
    {
        NBTTagCompound forgeData = player.getEntityData();
        if (!forgeData.hasKey(EntityPlayer.PERSISTED_NBT_TAG))
        {
            forgeData.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        }

        NBTTagCompound persistentData = forgeData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (!persistentData.hasKey(NBT_ROOT))
        {
            persistentData.setTag(NBT_ROOT, new NBTTagCompound());
        }

        return persistentData.getCompoundTag(NBT_ROOT);
    }

    public static class EventHandler
    {

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event)
        {
            if (event.phase == TickEvent.Phase.END)
            {
                PlayerDataHandler.cleanup();
            }
        }
    }

    public static class PlayerData {

        public WeakReference<EntityPlayer> playerWR;
        public final boolean isClient;

        private List<String> friends;

        public PlayerData(EntityPlayer player)
        {
            playerWR = new WeakReference<EntityPlayer>(player);
            isClient = player.worldObj.isRemote;

            friends = new ArrayList<String>();

            load();
        }

        public void writeToNBT(NBTTagCompound tag)
        {
            NBTTagCompound friendList = new NBTTagCompound();
            for (int i=0; i < friends.size(); i++)
            {
                friendList.setString(""+i, friends.get(i));
            }
            tag.setTag("Friends", friendList);
        }

        public void readFromNBT(NBTTagCompound tag)
        {
            friends = new ArrayList<String>();
            NBTTagCompound friendList = tag.getCompoundTag("Friends");
            int i=0;
            while (friendList.hasKey(""+i))
            {
                friends.add(friendList.getString(""+i));
                i++;
            }
        }

        public void load()
        {
            if (!isClient)
            {
                EntityPlayer player = playerWR.get();

                if (player != null)
                {
                    NBTTagCompound tags = getDataCompoundForPlayer(player);
                    readFromNBT(tags);
                }
            }
        }

        public List<String> getFriends()
        {
            return friends;
        }

        public boolean addFriend(String friendName)
        {
            String name = friendName.toLowerCase();
            if (!friends.contains(name))
            {
                friends.add(name);
                save();
                return true;
            }
            return false;
        }

        public boolean removeFriend(String friendName)
        {
            String name = friendName.toLowerCase();
            if (friends.contains(name))
            {
                friends.remove(name);
                save();
                return true;
            }
            return false;
        }

        public void clearFriends()
        {
            friends = new ArrayList<String>();
            save();
        }

        public void save()
        {
            if (!isClient)
            {
                EntityPlayer player = playerWR.get();

                if (player != null)
                {
                    NBTTagCompound tags = getDataCompoundForPlayer(player);
                    writeToNBT(tags);
                }
            }
        }

        public boolean isFriend(String playerName)
        {
            String name = playerName.toLowerCase();

            return friends.contains(name);
        }
    }
}
