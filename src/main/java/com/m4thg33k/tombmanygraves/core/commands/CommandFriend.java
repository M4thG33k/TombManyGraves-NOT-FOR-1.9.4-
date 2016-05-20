package com.m4thg33k.tombmanygraves.core.commands;

import com.m4thg33k.tombmanygraves.core.handlers.FriendHandler;
import com.m4thg33k.tombmanygraves.core.handlers.Friends;
import com.m4thg33k.tombmanygraves.core.handlers.PlayerDataHandler;
import com.m4thg33k.tombmanygraves.core.util.LogHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.core.helpers.UUIDUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandFriend implements ICommand{

    public final String COMMAND_NAME = "tmg_friend";

    private final List<String> aliases;

    public CommandFriend()
    {
        aliases = new ArrayList<String>();
        aliases.add("tmg_addfriend");
        aliases.add("tmg_befriend");
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return COMMAND_NAME + " [player]";
    }

    @Override
    public List<String> getCommandAliases() {
        return this.aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayer)
        {
            if (!((EntityPlayer) sender).worldObj.isRemote)
            {
                if (args.length == 0)
                {
                    sender.addChatMessage(new TextComponentString("Invalid argument: /" + COMMAND_NAME + " [player]"));
                    return;
                }

                if (((EntityPlayer)sender).getName().toLowerCase().equals(args[0].toLowerCase()))
                {
                    sender.addChatMessage(new TextComponentString("You can't add yourself to your friends."));
                    return;
                }

                GameProfile friendProfile = server.getPlayerProfileCache().getGameProfileForUsername(args[0]);
                UUID friendID = friendProfile.getId();

//                PlayerDataHandler.PlayerData playerData = PlayerDataHandler.getData((EntityPlayer)sender);
                UUID playerID = ((EntityPlayer)sender).getUniqueID();

                //if (playerData.addFriend(args[0]))
                if (FriendHandler.addToFriendList(playerID,friendID))
                {
                    sender.addChatMessage(new TextComponentString("Added " + args[0] + " to your friends."));
                }
                else
                {
                    sender.addChatMessage(new TextComponentString("Unable to add " + args[0] + " to your friends."));
                    sender.addChatMessage(new TextComponentString("Maybe they were already there?"));
                }
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }

}
