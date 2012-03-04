package net.chillercraft.forum;

import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();

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
					
					if (Main.use_banned) {
						boolean banned = res1.getBoolean("is_banned");

						if (banned) {
							event.setJoinMessage("");
							p.kickPlayer("You have been banned from the site.");
						} else if (!banned) {
							Main.setGroup(group, p);
						}
					} else {
						boolean banned = res1.getInt(Main.groups_id_field) == Main.banned_users_group ? true : false;

						if (banned) {
							event.setJoinMessage("");
							p.kickPlayer("You have been banned from the site.");
						} else if (!banned) {
							Main.setGroup(group, p);
						}
					}
				}
			} else {
				Main.log.info(p.getName() + "'s name not set or not signed up");
				Main.setGroup(Main.default_group, p);
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