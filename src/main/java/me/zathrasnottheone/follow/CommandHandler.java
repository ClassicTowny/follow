package me.zathrasnottheone.follow;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;
import me.zathrasnottheone.follow.Follow;
import me.zathrasnottheone.follow.FollowConfig;
import me.zathrasnottheone.follow.FollowRoster;
import me.zathrasnottheone.follow.Stalker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
	private Logger logger = null;
	private Follow plugin = null;
   
	public CommandHandler(Follow follow) {
		this.plugin = follow;
		this.logger = this.plugin.getLogger();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String cmdAlias, String[] args) {
		if(cmd.getName().equalsIgnoreCase("follow")) {
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("list")) {
					if(sender.hasPermission("follow.op.list")) {
   						this.handleFollowList(sender, args);
   					} else {
   						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Follow.getFollowPlugin().getLang().noPermsList));
   					}
   				} else if(args[0].equalsIgnoreCase("reload")) {
   					if(sender.hasPermission("follow.op.reload")) {
   						this.handleReload(sender);
   					} else {
   						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Follow.getFollowPlugin().getLang().noPermsReload));
   					}
   				} else if(args[0].equalsIgnoreCase("closer")) {
   					if(sender.hasPermission("follow.player")) {
   						this.handleMoveCloser(sender);
   					} else {
   						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Follow.getFollowPlugin().getLang().noPermsFollow));
   					}
   				} else if(args[0].equalsIgnoreCase("farther")) {
   					if(sender.hasPermission("follow.player")) {
   						this.handleMoveFarther(sender);
   					} else {
   						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Follow.getFollowPlugin().getLang().noPermsFollow));
   					}
   				} else if(args[0].equalsIgnoreCase("stop")) {
   					if(sender.hasPermission("follow.player"))
   						this.handleFollowStop(sender);
   				} else if(args[0].equalsIgnoreCase("help")) {
   					this.handleFollowHelp(sender, cmd, args);
   				} else if(sender.hasPermission("follow.player")) {
   					this.handleFollowPlayer(sender, cmd, args);
   				} else {
   					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Follow.getFollowPlugin().getLang().noPermsFollow));
   				}
   			} else if(sender.hasPermission("follow.player")) {
   				this.handleFollowPlayer(sender, cmd, args);
   			} else {
   				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Follow.getFollowPlugin().getLang().noPermsFollow));
   			}
		}else if(cmd.getName().equalsIgnoreCase("unfollow")) {
			if(sender.hasPermission("follow.player"))
					this.handleFollowStop(sender);
   		} else {
   			sender.sendMessage(ChatColor.GOLD + cmd.getUsage());
   		}
   		
   		return true;
	}

   private void handleMoveCloser(CommandSender sender) {
      if(sender instanceof Player) {
         Stalker stalker = FollowRoster.getInstance().getStalker(((Player) sender).getUniqueId());
         if(stalker != null) {
            stalker.setDistance((stalker.getDistance() - 1) / 2 + 1);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (Follow.getFollowPlugin().getLang().follow.replace("%suspect%", Follow.getFetch().getName(stalker.getSuspectUUID())).replace("%distance%", Integer.valueOf(stalker.getDistance()).toString()))));
         } else {
            this.logger.severe(ChatColor.stripColor((Follow.getFollowPlugin().getLang().notstalkerconsole).replace("%sender%", sender.getName())));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Follow.getFollowPlugin().getLang().notstalker));
         }
      } else {
         sender.sendMessage((ChatColor.translateAlternateColorCodes('&', Follow.getFollowPlugin().getLang().notingame)).replace("%sender%", sender.getName()));
      }

   }

   private void handleMoveFarther(CommandSender sender) {
      if(sender instanceof Player) {
         Stalker stalker = FollowRoster.getInstance().getStalker(((Player) sender).getUniqueId());
         if(stalker != null) {
            stalker.setDistance(stalker.getDistance() * 2);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (Follow.getFollowPlugin().getLang().follow.replace("%suspect%", Follow.getFetch().getName(stalker.getSuspectUUID())).replace("%distance%", Integer.valueOf(stalker.getDistance()).toString()))));
         } else {
        	 this.logger.severe(ChatColor.stripColor((Follow.getFollowPlugin().getLang().notstalkerconsole).replace("%sender%", sender.getName())));
             sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Follow.getFollowPlugin().getLang().notstalker));
         }
      } else {
    	  sender.sendMessage((ChatColor.translateAlternateColorCodes('&', Follow.getFollowPlugin().getLang().notingame)).replace("%sender%", sender.getName()));
      }

   }

   private void handleReload(CommandSender sender) {
      FollowConfig.getInstance().reloadConfig();
      Follow.getFollowPlugin().getLang().loadLanguageFile();
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Follow.getFollowPlugin().getLang().reload));
      if(sender instanceof Player) {
         this.logger.info(ChatColor.stripColor(Follow.getFollowPlugin().getLang().reloadconsole).replace("%sender%", sender.getName()));
      }

   }

   private void handleFollowPlayer(CommandSender sender, Command cmd, String[] args) {
      UUID suspectUUID = null;
      Player stalker = null;
      Player suspect = null;
      Integer distance = FollowConfig.getInstance().getFollowDistance();
      if(sender instanceof Player) {
         stalker = (Player)sender;
         if(args.length == 0) {
            suspectUUID = this.getNearestPlayer(stalker);
            if(suspectUUID!=null)
                sender.sendMessage((ChatColor.translateAlternateColorCodes('&', Follow.getFollowPlugin().getLang().nearestplayer)).replace("%suspect%", Follow.getFetch().getName(suspectUUID)));
         } else {       	
            suspectUUID = UUIDFetcher.getUUID(args[0]);
         }

         if(suspectUUID != null) {
            suspect = Bukkit.getServer().getPlayer(suspectUUID);
            if(suspect == null) {
               sender.sendMessage(ChatColor.WHITE + args[0] + ChatColor.GOLD + " is not an online player.");
               sender.sendMessage(ChatColor.GOLD + cmd.getUsage());
            } else if(suspect.getName().equalsIgnoreCase(sender.getName())) {
               sender.sendMessage(ChatColor.GOLD + "You can\'t follow yourself.");
            } else if(FollowRoster.getInstance().isSuspect(((Player) sender).getUniqueId()) && !FollowConfig.getInstance().isSuspectCanFollow()) {
               sender.sendMessage(ChatColor.GOLD + "You can\'t follow someone while you\'re being followed.");
            } else {
               if(args.length > 1) {
                  try {
                     distance = Integer.parseInt(args[1]);
                  } catch (NumberFormatException var9) {
                     sender.sendMessage(ChatColor.GOLD + args[1] + " was not a parseable integer - using a default value.");
                  }
               }

               FollowRoster.getInstance().follow(stalker, suspect, distance);
               
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (Follow.getFollowPlugin().getLang().follow.replace("%suspect%", suspect.getName()).replace("%distance%", distance.toString()))));
               this.logger.info(stalker.getName() + " is now following " + suspect.getName() + " at distance " + distance);
            }
         } else {
            sender.sendMessage(ChatColor.GOLD + "There is no one to follow.");
         }
      } else if(args.length == 0) {
         this.handleFollowHelp(sender, cmd, args);
      } else {
         sender.sendMessage(ChatColor.GOLD + sender.getName() + " can not follow another player - must be in-game to follow.");
      }
   }

   private UUID getNearestPlayer(Player fromPlayer) {
      UUID nearestPlayer = null;
      double distance = Double.MAX_VALUE;
      ArrayList<Player> players = (ArrayList<Player>)fromPlayer.getWorld().getPlayers();

      for(int i = 0; i < players.size(); ++i) {
         Player p = (Player)players.get(i);
         if(!p.getName().equalsIgnoreCase(fromPlayer.getName())) {
            Double testDistance = Double.valueOf(fromPlayer.getLocation().distance(p.getLocation()));
            if(testDistance.doubleValue() < distance) {
               nearestPlayer = p.getUniqueId();
               distance = testDistance.doubleValue();
            }
         }
      }

      return nearestPlayer;
   }

   private void handleFollowHelp(CommandSender sender, Command cmd, String[] args) {
      sender.sendMessage(ChatColor.GOLD + cmd.getUsage());
   }

   private void handleFollowStop(CommandSender sender) {
      if(sender instanceof Player) {
         Stalker s = FollowRoster.getInstance().unfollow((Player)sender);
         if(s != null) {
            sender.sendMessage(ChatColor.GOLD + "You are no longer following "  + ChatColor.WHITE +  (Follow.getFetch().getName(s.getSuspectUUID())));
            this.logger.info(sender.getName() + " is no longer following " + (Follow.getFetch().getName(s.getSuspectUUID())));
         }
      } else {
         sender.sendMessage(sender.getName() + " is not following anyone.");
      }

   }

   private void handleFollowList(CommandSender sender, String[] args) {
      String[] listStringArray = FollowRoster.getInstance().toStringArray();
      sender.sendMessage(listStringArray);
      this.logger.info("Sent " + sender.getName() + " the list of followers.");
   }
}
