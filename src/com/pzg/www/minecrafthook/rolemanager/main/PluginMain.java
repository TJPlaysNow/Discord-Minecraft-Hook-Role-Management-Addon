package com.pzg.www.minecrafthook.rolemanager.main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.pzg.www.api.commands.Command;
import com.pzg.www.api.commands.CommandMethod;
import com.pzg.www.api.config.Config;
import com.pzg.www.minecrafthook.main.APILink;
import com.pzg.www.minecrafthook.object.User;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class PluginMain extends JavaPlugin implements Listener {
	
	private Config usersAwaitingRoleConf;
	
	private List<String> usersAwaitingRole;
	
	private List<String> usersAwaitingRemRole;
	
	private APILink mchApi;
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		
		mchApi = new APILink(this);
		
		usersAwaitingRoleConf = new Config("plugins/Minecraft Hook", "Awaiting Roles.yml", this);
		
		if (usersAwaitingRoleConf.getConfig().getStringList("UsersAwaitingRole") != null) {
			usersAwaitingRole = usersAwaitingRoleConf.getConfig().getStringList("UsersAwaitingRole");
		} else {
			usersAwaitingRole = new ArrayList<String>();
		}
		
		if (usersAwaitingRoleConf.getConfig().getStringList("UsersAwaitingRemRole") != null) {
			usersAwaitingRemRole = usersAwaitingRoleConf.getConfig().getStringList("UsersAwaitingRemRole");
		} else {
			usersAwaitingRemRole = new ArrayList<String>();
		}
		
		mchApi.addMinecraftCommand(new Command("addrole", "rolemanager.addrole", "§cUh oh, you don't have permissions to perform this command.", new CommandMethod() {
			@Override
			public boolean run(CommandSender sender, String[] args) {
				if (args.length == 3) {
					boolean w = tryAddRole(args[1], args[2]);
					if (w)
						sender.sendMessage("Added " + args[2] + " role to " + args[1] + ".");
					else
						sender.sendMessage("Queued role " + args[2] + " for " + args[1] + ".");
				} else
					sender.sendMessage("§cUh oh, try /mch addrole <Player> <Role>");
				return true;
			}
		}));
		
		mchApi.addMinecraftCommand(new Command("removerole", "rolemanager.removerole", "§cUh oh, you don't have permissions to perform this command.", new CommandMethod() {
			@Override
			public boolean run(CommandSender sender, String[] args) {
				if (args.length == 3) {
					boolean w = tryRemoveRole(args[1], args[2]);
					if (w)
						sender.sendMessage("Removed " + args[2] + " role from " + args[1] + ".");
					else
						sender.sendMessage("Queued removal of " + args[2] + " role from " + args[1] + ".");
				} else
					sender.sendMessage("§cUh oh, try /mch addrole <Player> <Role>");
				return true;
			}
		}));
	}
	
	@Override
	public void onDisable() {
		usersAwaitingRoleConf.getConfig().set("UsersAwaitingRole", usersAwaitingRole);
		usersAwaitingRoleConf.getConfig().set("UsersAwaitingRemRole", usersAwaitingRemRole);
		usersAwaitingRoleConf.saveConfig();
	}
	
	@EventHandler
	public void playerLogin(PlayerJoinEvent event) {
		List<String> removeAdd = new ArrayList<String>();
		for (String usRole : usersAwaitingRole) {
			String[] part = usRole.split(" : ");
			tryAddRole(part[0], part[1]);
			removeAdd.add(usRole);
		}
		for (String rem : removeAdd) {
			usersAwaitingRole.remove(rem);
		}
		
		List<String> removeRem = new ArrayList<String>();
		for (String usRole : usersAwaitingRemRole) {
			String[] part = usRole.split(" : ");
			tryRemoveRole(part[0], part[1]);
			removeRem.add(usRole);
		}
		for (String rem : removeRem) {
			usersAwaitingRemRole.remove(rem);
		}
	}
	
	public boolean tryAddRole(String playerName, String roleName) {
		boolean worked = false;
		UUID minecraftUUID = null;
		if (Bukkit.getPlayer(playerName) != null) {
			minecraftUUID = Bukkit.getPlayer(playerName).getUniqueId();
			if (mchApi.getBot().getUsers().getUser(minecraftUUID) != null) {
				User user = mchApi.getBot().getUsers().getUser(minecraftUUID);
				for (IGuild guild : mchApi.getBot().getGuilds()) {
					for (IUser userr : guild.getUsers()) {
						if (userr.getLongID() == user.getDiscordID()) {
							for (IRole role : guild.getRoles()) {
								if (role.getName().equalsIgnoreCase(roleName)) {
									userr.addRole(role);
									worked = true;
								}
							}
						}
					}
				}
			}
		}
		if (!worked) {
			if (!usersAwaitingRole.contains(playerName + " : " + roleName)) {
				usersAwaitingRole.add(playerName + " : " + roleName);
			}
		}
		return worked;
	}
	
	public boolean tryRemoveRole(String playerName, String roleName) {
		boolean worked = false;
		UUID minecraftUUID = null;
		if (Bukkit.getPlayer(playerName) != null) {
			minecraftUUID = Bukkit.getPlayer(playerName).getUniqueId();
			if (mchApi.getBot().getUsers().getUser(minecraftUUID) != null) {
				User user = mchApi.getBot().getUsers().getUser(minecraftUUID);
				for (IGuild guild : mchApi.getBot().getGuilds()) {
					for (IUser userr : guild.getUsers()) {
						if (userr.getLongID() == user.getDiscordID()) {
							for (IRole role : guild.getRoles()) {
								if (role.getName().equalsIgnoreCase(roleName)) {
									userr.removeRole(role);
									worked = true;
								}
							}
						}
					}
				}
			}
		}
		if (!worked) {
			if (!usersAwaitingRemRole.contains(playerName + " : " + roleName)) {
				usersAwaitingRemRole.add(playerName + " : " + roleName);
			}
		}
		return worked;
	}
}