package com.m4thg33k.tombmanygraves.tiles;

import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import com.m4thg33k.tombmanygraves.TombManyGraves;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileDeathBlock extends TileEntity {

    private String playerName = "";
    private InventoryPlayer savedPlayerInventory = new InventoryPlayer(null);
    private NBTTagCompound baublesNBT = new NBTTagCompound();

    private int angle = 0;

    public void setPlayerName(String name)
    {
        playerName = name;
    }

    public void grabPlayer(EntityPlayer player)
    {

        angle = (int)player.rotationYawHead;

        if (worldObj.isRemote)
        {
            return;
        }
//        angle = TombManyGraves.rand.nextInt(360);
        setPlayerName(player.getName());
        setThisInventory(player.inventory);

        if (TombManyGraves.isBaublesInstalled)
        {
            setBaubleInventory(player);
        }

        worldObj.markAndNotifyBlock(pos, null, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 1);
    }

    public void setBaubleInventory(EntityPlayer player)
    {
        PlayerHandler.getPlayerBaubles(player).saveNBT(baublesNBT);
        PlayerHandler.clearPlayerBaubles(player);
    }

    public void setThisInventory(InventoryPlayer inventoryPlayer)
    {
        this.savedPlayerInventory.copyInventory(inventoryPlayer);
        inventoryPlayer.clear();
    }

    public boolean isSamePlayer(EntityPlayer player)
    {
        return player.getName().equals(playerName);
    }

    public String getPlayerName()
    {
        return playerName;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        playerName = compound.getString("PlayerName");
        savedPlayerInventory.readFromNBT(compound.getTagList("Inventory",10));
        baublesNBT = compound.getCompoundTag("BaublesNBT");
        angle = compound.getInteger("AngleOfDeath");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setString("PlayerName",playerName);

        NBTTagList tagList = new NBTTagList();
        savedPlayerInventory.writeToNBT(tagList);
        compound.setTag("Inventory",tagList);

        compound.setTag("BaublesNBT",baublesNBT);
        compound.setInteger("AngleOfDeath",angle);
    }

    public void onCollision(EntityPlayer player)
    {
        if (worldObj.isRemote || !isSamePlayer(player))
        {
            return;
        }

        replacePlayerInventory(player);

        if (TombManyGraves.isBaublesInstalled)
        {
            replaceBaublesInventory(player);
        }

        worldObj.setBlockToAir(pos);
    }

    public void replaceSpecificInventory(EntityPlayer player, IInventory playerInventory,IInventory savedInventory)
    {
        for (int i=0; i < playerInventory.getSizeInventory(); i++)
        {
            if (savedInventory.getStackInSlot(i) != null && savedInventory.getStackInSlot(i).stackSize > 0)
            {
                if (playerInventory.getStackInSlot(i) == null)
                {
                    playerInventory.setInventorySlotContents(i, savedInventory.getStackInSlot(i));
                }
                else
                {
                    EntityItem entityItem = new EntityItem(worldObj, player.posX, player.posY, player.posZ, savedInventory.getStackInSlot(i));
                    worldObj.spawnEntityInWorld(entityItem);
                }
            }
        }
    }

    public void replacePlayerInventory(EntityPlayer player)
    {
        replaceSpecificInventory(player,player.inventory,savedPlayerInventory);
        savedPlayerInventory = new InventoryPlayer(null);
//        for (int i=0; i < savedPlayerInventory.getSizeInventory(); i++)
//        {
//            if(savedPlayerInventory.getStackInSlot(i) != null && savedPlayerInventory.getStackInSlot(i).stackSize > 0)
//            {
//                if (player.inventory.getStackInSlot(i) == null)
//                {
//                    player.inventory.setInventorySlotContents(i, savedPlayerInventory.getStackInSlot(i));
//                }
//                else
//                {
//                    EntityItem entityItem = new EntityItem(worldObj, player.posX, player.posY, player.posZ, savedPlayerInventory.getStackInSlot(i));
//                    worldObj.spawnEntityInWorld(entityItem);
//                }
//            }
//        }
    }

    public void replaceBaublesInventory(EntityPlayer player)
    {
        IInventory currentBaubles = PlayerHandler.getPlayerBaubles(player);
        InventoryBaubles savedBaubles = new InventoryBaubles(player);
        savedBaubles.readNBT(baublesNBT);

        replaceSpecificInventory(player,currentBaubles,savedBaubles);

        baublesNBT = new NBTTagCompound();

    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        playerName = pkt.getNbtCompound().getString("PlayerName");
        angle = pkt.getNbtCompound().getInteger("AngleOfDeath");
    }

    @Override
    public Packet<?> getDescriptionPacket() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("PlayerName",playerName);
        compound.setInteger("AngleOfDeath",angle);
        return new SPacketUpdateTileEntity(pos,0,compound);
    }

    public int getAngle()
    {
        return angle;
    }

    public void dropAllItems()
    {
        InventoryHelper.dropInventoryItems(worldObj, pos, savedPlayerInventory);
        if (TombManyGraves.isBaublesInstalled)
        {
            InventoryBaubles baubles = new InventoryBaubles(null);
            baubles.readNBT(baublesNBT);
            InventoryHelper.dropInventoryItems(worldObj, pos, baubles);
        }
    }

    public static boolean isInventoryEmpty(EntityPlayer player)
    {
        boolean toReturn = isSpecificInventoryEmpty(player.inventory);

        if (TombManyGraves.isBaublesInstalled)
        {
            toReturn = toReturn && isSpecificInventoryEmpty(PlayerHandler.getPlayerBaubles(player));
        }

        return toReturn;
    }

    public static boolean isSpecificInventoryEmpty(IInventory inventory)
    {
        for (int i=0; i < inventory.getSizeInventory(); i++)
        {
            if (inventory.getStackInSlot(i) != null && inventory.getStackInSlot(i).stackSize > 0)
            {
                return false;
            }
        }

        return true;
    }
}
