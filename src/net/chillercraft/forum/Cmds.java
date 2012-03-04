package net.chillercraft.forum;

import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmds implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] split) {
		Player p = null;

		if (sender instanceof Player) {
			p = (Player) sender;

			if (label.equalsIgnoreCase("fban")) {
				if (p.hasPermission("forumbridge.ban")) {
					if (split.length == 1) {
						try {
							if (Bukkit.getPlayer(split[0]) != null) {
								Player pl = Bukkit.getPlayer(split[0]);
								Main.sql.updateQuery("UPDATE xf_user SET is_banned='1' WHERE user_id='" + Main.getUserId(pl.getName()) + "'");
								pl.kickPlayer("You have been banned from the site.");
								Main.log.info("Banning " + pl.getName() + " from the site");
								p.sendMessage(ChatColor.RED + "Banned " + pl.getName() + " from the site");
							} else {
								OfflinePlayer pl = Bukkit.getOfflinePlayer(split[0]);
								Main.sql.updateQuery("UPDATE xf_user SET is_banned='1' WHERE user_id='" + Main.getUserId(pl.getName()) + "'");
								Main.log.info("Banning " + pl.getName() + " from the site");
								p.sendMessage(ChatColor.RED + "Banned " + pl.getName() + " from the site");
							}
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					} else {
						p.sendMessage(ChatColor.RED + "Incorrect usage: /fban <username>");
					}
				} else {
					p.sendMessage(ChatColor.RED + "You do not have access to that command.");
				}
			} else if (label.equalsIgnoreCase("funban")) {
				if (p.hasPermission("forumbridge.unban")) {
					if (split.length == 1) {
						try {
							OfflinePlayer pl = Bukkit.getOfflinePlayer(split[0]);
							Main.sql.updateQuery("UPDATE xf_user SET is_banned='0' WHERE user_id='" + Main.getUserId(pl.getName()) + "'");
							Main.log.info("Unbanning " + pl.getName() + " from the site");
							p.sendMessage(ChatColor.RED + pl.getName() + " has been unbanned");
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					} else {
						p.sendMessage(ChatColor.RED + "Incorrect usage: /funban <username>");
					}
				} else {
					p.sendMessage(ChatColor.RED + "You do not have access to that command.");
				}
			} else if (label.equalsIgnoreCase("frank")) {
				if (p.hasPermission("forumbridge.rank")) {
					if (split.length == 2) {
						try {
							if (Main.getGroup(split[1]) != 0) {
								Player pl = Bukkit.getPlayer(split[0]);
								Main.sql.updateQuery("UPDATE xf_user SET user_group_id='" + Main.getGroup(split[1]) + "' WHERE user_id='" + Main.getUserId(pl.getName()) + "'");
								if (Main.setGroup(Main.getGroup(split[1]), pl)) {
									pl.sendMessage(ChatColor.GREEN + "You have been set to a " + Main.config.getString("groups." + Main.getGroup(split[1])));
									p.sendMessage(ChatColor.GREEN + "Set " + pl.getName() + " to rank " + Main.groups[Main.getGroup(split[1]) - 1]);
								}
							} else {
								p.sendMessage(ChatColor.RED + "Could not find the group " + split[1]);
							}
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					} else {
						p.sendMessage(ChatColor.RED + "Incorrect usage: /frank <username> <group>");
					}
				} else {
					p.sendMessage(ChatColor.RED + "You do not have access to that command.");
				}
			} else if (label.equalsIgnoreCase("fsyncall")) {
				if (p.hasPermission("forumbridge.syncall")) {
					if (split.length == 0) {
						Main.syncAll();
						p.sendMessage(ChatColor.GREEN + "Everyone has been synced");
					} else {
						p.sendMessage(ChatColor.RED + "Incorrect usage: /fsyncall");
					}
				} else {
					p.sendMessage(ChatColor.RED + "You do not have access to that command.");
				}
			} else if (label.equalsIgnoreCase("sync")) {
				if (split.length == 0) {
					try {
						ResultSet res;
						if (Main.multi_tables) {
							res = Main.sql.sqlQuery("SELECT * FROM " + Main.multi_table + " WHERE " + Main.field_id_field + "='" + Main.field_id_name + "' AND " + Main.field_value_field + "='" + p.getName() + "'");
						} else {
							res = Main.sql.sqlQuery("SELECT * FROM " + Main.users_table + " WHERE " + Main.username_field + "='" + p.getName() + "'");
						}

						if (res.next()) {
							int id = res.getInt(Main.user_id_field);

							ResultSet res1 = Main.sql.sqlQuery("SELECT * FROM " + Main.users_table + " WHERE " + Main.user_id_field + "='" + id + "'");

							if (res1.next()) {
								int group = res1.getInt(Main.groups_id_field);

								if (Main.setGroup(group, p)) {
									p.sendMessage(ChatColor.GREEN + "You have been set to a " + Main.config.getString("groups." + group));
								}
							}
						} else {
							p.sendMessage(ChatColor.GREEN + "You are not signed up on the site");
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else {
					p.sendMessage(ChatColor.RED + "Incorrect usage: /sync");
				}
			}
		} else {
			if (label.equalsIgnoreCase("fban")) {
				if (split.length == 1) {
					try {
						if (Bukkit.getPlayer(split[0]) != null) {
							Player pl = Bukkit.getPlayer(split[0]);
							Main.sql.updateQuery("UPDATE xf_user SET is_banned='1' WHERE user_id='" + Main.getUserId(pl.getName()) + "'");
							pl.kickPlayer("You have been banned from the site.");
							Main.log.info("Banning " + pl.getName() + " from the site");
						} else {
							OfflinePlayer pl = Bukkit.getOfflinePlayer(split[0]);
							Main.sql.updateQuery("UPDATE xf_user SET is_banned='1' WHERE user_id='" + Main.getUserId(pl.getName()) + "'");
							Main.log.info("Banning " + pl.getName() + " from the site");
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				} else {
					Main.log.info("Incorrect usage: /fban <username>");
				}
			}
			if (label.equalsIgnoreCase("funban")) {
				if (split.length == 1) {
					try {
						OfflinePlayer pl = Bukkit.getOfflinePlayer(split[0]);
						Main.sql.updateQuery("UPDATE xf_user SET is_banned='0' WHERE user_id='" + Main.getUserId(pl.getName()) + "'");
						Main.log.info("Unbanning " + pl.getName() + " from the site");
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				} else {
					Main.log.info("Incorrect usage: /funban <username>");
				}
			}
			if (label.equalsIgnoreCase("fsyncall")) {
				if (split.length == 0) {
					Main.syncAll();
					Main.log.info("Everyone has been synced");
				} else {
					Main.log.info("Incorrect usage: /fsyncall");
				}
			}
			if (label.equalsIgnoreCase("frank")) {
				if (split.length == 2) {
					try {
						if (Main.getGroup(split[1]) != 0) {
							Player pl = Bukkit.getPlayer(split[0]);
							Main.sql.updateQuery("UPDATE xf_user SET user_group_id='" + Main.getGroup(split[1]) + "' WHERE user_id='" + Main.getUserId(pl.getName()) + "'");
							if (Main.setGroup(Main.getGroup(split[1]), pl)) {
								pl.sendMessage(ChatColor.GREEN + "You have been set to a " + Main.config.getString("groups." + Main.getGroup(split[1])));
							}
						} else {
							Main.log.info("Could not find the group " + split[1]);
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				} else {
					Main.log.info("Incorrect usage: /frank <username> <group>");
				}
			}
		}
		return true;
	}
}