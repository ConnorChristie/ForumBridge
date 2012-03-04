package net.chillercraft.forum;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import net.chillercraft.api.Logging;
import net.chillercraft.api.SQL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Main extends JavaPlugin {
	public static Logging log;
	public static PluginDescriptionFile pdf;
	public static SQL sql;
	public static File configFile;
	public static FileConfiguration config;

	public static boolean multi_tables = false;
	public static boolean use_banned = false;

	public static String permissions_system;

	public static String users_table;
	public static String multi_table;

	public static String user_id_field;
	public static String groups_id_field;

	public static String username_field;

	public static String is_banned_field;
	public static int banned_users_group;
	public static int default_group;

	public static String field_id_field;
	public static String field_value_field;
	public static String field_id_name;

	public static String[] groups;

	@Override
	public void onEnable() {
		pdf = this.getDescription();
		log = new Logging(Logger.getLogger("Minecraft"), pdf);

		configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			copy(getResource("config.yml"), configFile);
		}
		config = new YamlConfiguration();
		try {
			config.load(configFile);
			config.options().copyHeader(true);
		} catch (Exception e) { }

		getServer().getPluginManager().registerEvents(new EventListener(), this);
		getCommand("fban").setExecutor(new Cmds());
		getCommand("funban").setExecutor(new Cmds());
		getCommand("frank").setExecutor(new Cmds());
		getCommand("sync").setExecutor(new Cmds());
		getCommand("fsyncall").setExecutor(new Cmds());

		if (config.get("db-username").equals("username") && config.get("db-password").equals("password")) {
			log.info("Using default config file.");
			getServer().getPluginManager().disablePlugin(this);
		} else {
			permissions_system = config.getString("permissions-system");

			multi_tables = config.getBoolean("multi-tables");
			use_banned = config.getBoolean("use-banned-field");

			users_table = config.getString("users-table.table");
			user_id_field = config.getString("users-table.user-id-field");
			groups_id_field = config.getString("users-table.groups-id-field");
			username_field = config.getString("users-table.username-field");

			multi_table = config.getString("multi-table.table");
			field_id_field = config.getString("multi-table.field-id-field");
			field_value_field = config.getString("multi-table.field-value-field");
			field_id_name = config.getString("multi-table.field-id-name");
			default_group = config.getInt("users-table.default-group");

			if (use_banned) {
				is_banned_field = config.getString("users-table.banned-field");
			} else {
				banned_users_group = config.getInt("users-table.banned-users-group");
			}

			int gr = 0;
			for (String s : config.getKeys(true)) {
				if (s.contains("groups.")) {
					gr++;
				}
			}

			groups = new String[gr];

			for (int i = 0; i < gr; i++) {
				groups[i] = config.getString("groups." + (i + 1));
			}

			/*
			 * log.info(multi_tables + "");
			 * log.info(use_banned + "");
			 * log.info(users_table);
			 * log.info(user_id_field);
			 * log.info(groups_id_field);
			 * log.info(username_field);
			 * log.info(field_id_field);
			 * log.info(field_value_field);
			 * log.info(field_id_name);
			 * log.info(default_group + "");
			 * log.info(is_banned_field);
			 * log.info(banned_users_group + "");
			 */

			sql = new SQL(config.get("db-host") + ":" + config.get("db-port"), config.get("db-database") + "", config.get("db-username") + "", config.get("db-password") + "");
			sql.initialize();

			log.info("Enabled!");
			
			syncAll();
			startSyncing();
			saveConfig();
		}
	}

	@Override
	public void onDisable() {
		if (sql != null) {
			sql.close();
		}

		log.info("Disabled...");
	}

	private void startSyncing() {
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			public void run() {
				syncAll();
			}
		}, 200L, 200L);
	}

	protected void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getUserId(String username) {
		try {
			ResultSet res = sql.sqlQuery("SELECT * FROM xf_user_field_value WHERE field_id='minecraft_name' AND field_value='" + username + "'");

			if (res.next()) {
				return res.getInt("user_id");
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
		return 0;
	}

	public static boolean setGroup(int group, Player p) {
		if (permissions_system.equalsIgnoreCase("PEX")) {
			if (!PermissionsEx.getUser(p).getGroupsNames()[0].equalsIgnoreCase(Main.config.getString("groups." + group))) {
				PermissionsEx.getUser(p).setGroups(new String[] { Main.config.getString("groups." + group) });
				Main.log.info("Setting " + p.getName() + " to group " + Main.config.getString("groups." + group));
				return true;
			}
		}
		return false;
	}

	public static int getGroup(String id) {
		for (int i = 0; i < groups.length; i++) {
			if (groups[i].toLowerCase().contains(id.toLowerCase())) {
				return i + 1;
			}
		}
		return 0;
	}

	public static void syncAll() {
		for (Player play : Bukkit.getOnlinePlayers()) {
			try {
				ResultSet res;
				if (multi_tables) {
					res = sql.sqlQuery("SELECT * FROM " + multi_table + " WHERE " + field_id_field + "='" + field_id_name + "' AND " + field_value_field + "='" + play.getName() + "'");
				} else {
					res = sql.sqlQuery("SELECT * FROM " + users_table + " WHERE " + username_field + "='" + play.getName() + "'");
				}

				if (res.next()) {
					int id = res.getInt(user_id_field);

					ResultSet res1 = sql.sqlQuery("SELECT * FROM " + users_table + " WHERE " + user_id_field + "='" + id + "'");

					if (res1.next()) {
						int group = res1.getInt(groups_id_field);

						if (setGroup(group, play)) {
							play.sendMessage(ChatColor.GREEN + "You have been set to a " + config.getString("groups." + group));
						}
					}
				} else {
					if (Main.setGroup(Main.default_group, play)) {
						play.sendMessage(ChatColor.RED + "You are not signed up on the site. Being set to default group.");
						Main.log.info(play.getName() + " has not signed up on the site yet.");
					}
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
		}
	}
}