package com.m4thg33k.tombmanygraves.items;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {

    public static ItemDeathList itemDeathList = new ItemDeathList();

    public static void createItems()
    {
        GameRegistry.register(itemDeathList);
    }
}
