package com.m4thg33k.tombmanygraves.client.render.tiles;

import com.m4thg33k.tombmanygraves.blocks.ModBlocks;
import com.m4thg33k.tombmanygraves.tiles.TileDeathBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;

import java.util.Random;

public class TileDeathBlockRenderer extends TileEntitySpecialRenderer{

    private String playerName = null;
    private int deathAngle = -1;

    public TileDeathBlockRenderer()
    {
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {


        TileDeathBlock tileDeathBlock = (TileDeathBlock)te;
        this.deathAngle = tileDeathBlock.getAngle();
        this.playerName = tileDeathBlock.getPlayerName();

        boolean isLocked = tileDeathBlock.isLocked();
        boolean renderGround = true;

        ItemStack groundType = tileDeathBlock.getGroundMaterial();
        if (groundType == null)
        {
            groundType = new ItemStack(Blocks.dirt,1);
        }
        else if (groundType.getItem() == Item.getItemFromBlock(ModBlocks.blockDeath))
        {
            renderGround = false;
        }

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0f,1.0f,1.0f,1.0f);

        GlStateManager.translate(x+0.5,y+0.5,z+0.5);

        ItemStack skull = new ItemStack(Items.skull,1,3);
        skull.setTagCompound(new NBTTagCompound());
        skull.getTagCompound().setTag("SkullOwner",new NBTTagString(playerName));

//        ItemStack dirt = new ItemStack(Blocks.dirt,1);

        RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();

        EntityItem entityItem = new EntityItem(te.getWorld(),0.0,0.0,0.0,skull);
        entityItem.hoverStart = 0.0f;

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);


        if (renderGround) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-deathAngle,0,1,0);
            if (isLocked)
            {
                GlStateManager.translate(0,-0.1,0);
                GlStateManager.rotate(-90,1,0,0);
            }
            else
            {
                GlStateManager.rotate(-45,1,0,0);
            }
            GlStateManager.scale(0.75,0.75,0.75);
            GlStateManager.pushAttrib();
            RenderHelper.enableStandardItemLighting();
            itemRenderer.renderItem(entityItem.getEntityItem(), ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, -0.25, 0);
            GlStateManager.scale(2, 1, 2);
            itemRenderer.renderItem(groundType, ItemCameraTransforms.TransformType.FIXED);

            GlStateManager.popMatrix();
        }
        else
        {
            Random rand = new Random(te.getPos().hashCode());
            float rotationY = (float) (360.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL) + rand.nextInt(360);
            float rotationX = (float) (360.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL) + rand.nextInt(360);
            float rotationZ = (float) (360.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL) + rand.nextInt(360);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(rotationY, 0, 1, 0);
            GlStateManager.rotate(rotationX, 1, 0, 0);
            GlStateManager.rotate(rotationZ, 0, 0, 1);
            if (isLocked)
            {
                GlStateManager.scale(0.25,0.25,0.25);
            }
            else
            {
                GlStateManager.scale(0.75, 0.75, 0.75);
            }
            GlStateManager.pushAttrib();
            RenderHelper.enableStandardItemLighting();
            itemRenderer.renderItem(entityItem.getEntityItem(), ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();

        }
        GlStateManager.popMatrix();

    }
}
