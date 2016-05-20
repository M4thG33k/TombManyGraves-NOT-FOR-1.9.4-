package com.m4thg33k.tombmanygraves.client.gui;

import baubles.common.container.InventoryBaubles;
import com.m4thg33k.tombmanygraves.TombManyGraves;
import com.m4thg33k.tombmanygraves.core.util.LogHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GuiDeathItems extends GuiScreen {

    private EntityPlayer player;

    private ItemStack deathList;

    private List<String> mainItems;
    private List<String> baubleItems;

    private Scrollbar scrollbar;


    private int xSize;
    private int ySize;

    private static String MAIN = "Main Inventory";
    private static String BAUBLES = "Baubles";
    private static String LINE = "-----------------------------";
    private static String EOF = "END OF FILE";

    private static List<String> END_OF_FILE;

    public GuiDeathItems(EntityPlayer player, ItemStack deathList)
    {
        super();
        this.player = player;
        this.deathList = deathList.copy();
        createListOfItemsInMainInventory();
        createListOfItemsInBaublesInventory();

        END_OF_FILE = new ArrayList<>();
        END_OF_FILE.add(LINE);
        END_OF_FILE.add(EOF);
        END_OF_FILE.add(LINE);

        xSize = 200;
        ySize = 150;

        scrollbar = new Scrollbar(xSize - 12, 0, 12, ySize);
        scrollbar.setScrollDelta(1f);
    }



    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        bindTexture("DeathListBackground.png");
        drawTexture(getGuiLeft(),getGuiTop(),0,0,xSize,ySize);

        scrollbar.update(this,mouseX,mouseY);
        scrollbar.draw(this);

        int endHeight = drawMainItems();
        endHeight = drawBaubleItems(endHeight);

        drawEOF(endHeight);

//        this.fontRendererObj.drawString("this is a test", this.width/2, this.height/2, 0);

//        if (inBounds(xSize-12,0,12,150,mouseX,mouseY))
//        {
//            drawRect(xSize-12,0,12,150,0xFF0000);
//        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private int drawMainItems()
    {
        int height = 0;
        int gLeft = getGuiLeft();
        int gTop = getGuiTop();

        int counter = 0;

        for (int i=0; i<mainItems.size();i++)
        {
            height = 10*i + (int)scrollbar.getCurrentScroll()*(-10) + 10;
            counter += 1;
            if (height < 4 || height >= ySize - 12)
            {
                continue;
            }
            this.fontRendererObj.drawString(mainItems.get(i), gLeft + 12, gTop + height, 0);
        }

        return counter*10;
    }

    private int drawBaubleItems(int startHeight)
    {
        if (baubleItems.size() < 4)
        {
            return startHeight;
        }
        int height;
        int gLeft = getGuiLeft();
        int gTop = getGuiTop();

        int counter = 0;

        for (int i=0; i<baubleItems.size(); i++)
        {
            height = startHeight + 10*i + (int)scrollbar.getCurrentScroll()*(-10) + 10;
            counter += 1;
            if (height < 4 || height >= ySize - 12)
            {
                continue;
            }
            this.fontRendererObj.drawString(baubleItems.get(i), gLeft + 12, gTop + height, 0);
        }

        return startHeight + counter*10;
    }

    private void drawEOF(int startHeight)
    {
        int height;
        int gLeft = getGuiLeft();
        int gTop = getGuiTop();

        for (int i=0; i<END_OF_FILE.size(); i++)
        {
            height = startHeight + 10*i + (int)scrollbar.getCurrentScroll()*(-10) + 10;
            if (height < 4 || height >= ySize - 12)
            {
                continue;
            }
            this.fontRendererObj.drawString(END_OF_FILE.get(i), gLeft + 12, gTop + height, (i==1?0xFF0000:0));
        }
    }

    private void createListOfItemsInMainInventory()
    {
        NBTTagList tagList = deathList.getTagCompound().getTagList("Main",10);
        InventoryPlayer inventoryPlayer = new InventoryPlayer(player);
        inventoryPlayer.readFromNBT(tagList);

        mainItems = createListFromInventory(inventoryPlayer,MAIN);
    }

    private void createListOfItemsInBaublesInventory()
    {
        if (!TombManyGraves.isBaublesInstalled)
        {
            baubleItems = new ArrayList<String>();
        }
        NBTTagCompound tag = deathList.getTagCompound().getCompoundTag("Baubles");
        InventoryBaubles inventoryBaubles = new InventoryBaubles(player);
        inventoryBaubles.readNBT(tag);

        baubleItems = createListFromInventory(inventoryBaubles,BAUBLES);
    }

    private List<String> createListFromInventory(IInventory inventory,String sectionName)
    {
        List<String> stringList = new ArrayList<>();
        stringList.add(LINE);
        stringList.add(sectionName);
        stringList.add(LINE);

        int itemNumber = 1;

        for (int i=0; i<inventory.getSizeInventory();i++)
        {
            if (inventory.getStackInSlot(i)!=null && inventory.getStackInSlot(i).stackSize > 0)
            {
                String name = inventory.getStackInSlot(i).getDisplayName();
                if (name.length()>28)
                {
                    name = name.substring(0,25) + "...";
                }
                stringList.add(itemNumber + ") " + name + (inventory.getStackInSlot(i).stackSize>1 ? " x" + inventory.getStackInSlot(i).stackSize : ""));
                itemNumber += 1;
            }
        }

        return stringList;
    }

    public void bindTexture(String filename)
    {
        bindTexture(TombManyGraves.MODID, filename);
    }

    public void bindTexture(String base, String filename)
    {
        mc.getTextureManager().bindTexture(new ResourceLocation(base, "textures/gui/" + filename));
    }

    public void drawTexture(int x, int y, int textureX, int textureY, int width, int height)
    {
        drawTexturedModalRect(x,y,textureX,textureY,width,height);
    }

    public int getGuiLeft()
    {
        return (this.width - this.xSize) / 2;
    }

    public int getGuiTop()
    {
        return (this.height - this.ySize) / 2;
    }

    public boolean inBounds(int x, int y, int w, int h, int ox, int oy)
    {
//        LogHelper.info(x + "," + y + "," + w + "," + h + "," + ox + "," + oy + " ; top: " + getGuiTop() + ", left: " + getGuiLeft());
        return ox - getGuiLeft() >= + x && ox - getGuiLeft() <= x + w && oy - getGuiTop() >= y && oy - getGuiTop()<= y + h;
    }

    public int getxSize()
    {
        return xSize;
    }

    public int getySize()
    {
        return ySize;
    }
}
