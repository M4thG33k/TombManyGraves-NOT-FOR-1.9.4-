package com.m4thg33k.tombmanygraves.core.handlers;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import com.m4thg33k.tombmanygraves.TombManyGraves;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeathInventory {

    private NBTTagCompound allNBT;

    public DeathInventory(EntityPlayer player) {
        allNBT = new NBTTagCompound();

        NBTTagList tagList = new NBTTagList();
        player.inventory.writeToNBT(tagList);
        allNBT.setTag("Main",tagList);

        NBTTagCompound baublesNBT = new NBTTagCompound();
        if (TombManyGraves.isBaublesInstalled) {
            PlayerHandler.getPlayerBaubles(player).saveNBT(baublesNBT);
        }
        allNBT.setTag("Baubles",baublesNBT);
    }

    public boolean writePortion(String fileName,String toWrite)
    {
        boolean didWork = true;

        try (FileWriter file = new FileWriter(fileName))
        {
            file.write(toWrite);
            file.close();
        }
        catch (Exception e)
        {
//            e.printStackTrace();
            didWork = false;
        }

        return didWork;
    }

    public boolean writeFile(EntityPlayer player)
    {
        boolean didWork;

        String filename = "/" + player.getName();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String filePostfix =  timeStamp + ".json";

        didWork = writePortion(TombManyGraves.file + DeathInventoryHandler.FILE_PREFIX + filename + "#" + filePostfix,allNBT.toString());
//        didWork = writePortion(TombManyGraves.file + DeathInventoryHandler.FILE_PREFIX + filename + "_inventory_" + filePostfix,mainNBT.toString());
//        didWork = didWork && writePortion(TombManyGraves.file + DeathInventoryHandler.FILE_PREFIX + filename + "_baubles_" + filePostfix,baublesNBT.toString());

        return didWork;
    }

    public boolean dropAll(EntityPlayer player, String timestamp)
    {
        boolean didWork = true;

        String filename = TombManyGraves.file + DeathInventoryHandler.FILE_PREFIX + "/" + player.getName() + "#" + timestamp + ".json";

        BufferedReader reader;

        try
        {
            reader = new BufferedReader(new FileReader(filename));
            String fileData = reader.readLine();
            allNBT = JsonToNBT.getTagFromJson(fileData);
            InventoryPlayer inventoryPlayer = new InventoryPlayer(player);
            inventoryPlayer.readFromNBT(allNBT.getTagList("Main",10));
            InventoryHelper.dropInventoryItems(player.worldObj, player.getPosition(), inventoryPlayer);

            if (TombManyGraves.isBaublesInstalled)
            {
                InventoryBaubles inventoryBaubles = new InventoryBaubles(player);
                inventoryBaubles.readNBT(allNBT.getCompoundTag("Baubles"));
                InventoryHelper.dropInventoryItems(player.worldObj, player.getPosition(), inventoryBaubles);
            }
        }
        catch (Exception e)
        {
//            e.printStackTrace();
            didWork = false;
        }

        return didWork;
    }

    public boolean restoreAll(EntityPlayer player, String timestamp)
    {
        boolean didWork = true;

        String filename = TombManyGraves.file + DeathInventoryHandler.FILE_PREFIX + "/" + player.getName() + "#" + timestamp + ".json";

        BufferedReader reader;

        try
        {
            reader = new BufferedReader(new FileReader(filename));
            String fileData = reader.readLine();
            allNBT = JsonToNBT.getTagFromJson(fileData);
            player.inventory.readFromNBT(allNBT.getTagList("Main",10));

            if (TombManyGraves.isBaublesInstalled)
            {
                InventoryBaubles inventoryBaubles = new InventoryBaubles(player);
                inventoryBaubles.readNBT(allNBT.getCompoundTag("Baubles"));
                PlayerHandler.setPlayerBaubles(player,inventoryBaubles);
            }
        }
        catch (Exception e)
        {
//            e.printStackTrace();
            didWork = false;
        }

        return didWork;
    }
}
