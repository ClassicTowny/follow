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
	private LanguageFile lang = null;
   
	public CommandHandler(Follow follow) {
		this.plugin = follow;
		this.logger = this.plugin.getLogger();
		this.lang = this.plugin.getLang();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String cmdAlias, String[] args) {
		if(cmd.getName().equalsIgnoreCase("follow")) {
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("list")) {
					if(sender.hasPermission("follow.op.list")) {
   						this.handleFollowList(sender, args);
   					} else {
   						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.noPermsList));
   					}
   				} else if(args[0].equalsIgnoreCase("reload")) {
   					if(sender.hasPermission("follow.op.reload")) {
   						this.handleReload(sender);
   					} else {
   						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.noPermsReload));
   					}
   				} else if(args[0].equalsIgnoreCase("closer")) {
   					if(sender.hasPermission("follow.player")) {
   						this.handleMoveCloser(sender);
   					} else {
   						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.noPermsFollow));
   					}
   				} else if(args[0].equalsIgnoreCase("farther")) {
   					if(sender.hasPermission("follow.player")) {
   						this.handleMoveFarther(sender);
   					} else {
   						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.noPermsFollow));
   					}
   				} else if(args[0].equalsIgnoreCase("stop")) {
   					if(sender.hasPermission("follow.player"))
   						this.handleFollowStop(sender);
   				} else if(args[0].equalsIgnoreCase("help")) {
   					this.handleFollowHelp(sender, cmd, args);
   				} else if(sender.hasPermission("follow.player")) {
   					this.handleFollowPlayer(sender, cmd, args);
   				} else {
   					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.noPermsFollow));
   				}
   			} else if(sender.hasPermission("follow.player")) {
   				this.handleFollowPlayer(sender, cmd, args);
   			} else {
   				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.noPermsFollow));
   			}
		}else if(cmd.getName().equalsIgnoreCase("unfollow")) {
			if(sender.hasPermission("follow.player"))
					this.handleFollowStop(sender);
   		} else {
   			for(String s : lang.help) {
   				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
   			}  			
   		}
   		
   		return true;
	}

   private void handleMoveCloser(CommandSender sender) {
	   if(sender instanceof Player) {
		   Stalker stalker = FollowRoster.getInstance().getStalker(((Player) sender).getUniqueId());
		   if(stalker != null) {
			   stalker.setDistance((stalker.getDistance() - 1) / 2 + 1);
			   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (lang.prefix + lang.follow.replace("%suspect%", Follow.getFetch().getName(stalker.getSuspectUUID())).replace("%distance%", Integer.valueOf(stalker.getDistance()).toString()))));
		   } else {
			   this.logger.severe(ChatColor.stripColor((lang.prefix + lang.notstalkerconsole).replace("%sender%", sender.getName())));
			   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.notstalker));
		   }
	   } else {
		   sender.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.notingame)).replace("%sender%", sender.getName()));
	   }
   }

   private void handleMoveFarther(CommandSender sender) {
	   if(sender instanceof Player) {
		   Stalker stalker = FollowRoster.getInstance().getStalker(((Player) sender).getUniqueId());
		   if(stalker != null) {
			   stalker.setDistance(stalker.getDistance() * 2);
			   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (lang.prefix + lang.follow.replace("%suspect%", Follow.getFetch().getName(stalker.getSuspectUUID())).replace("%distance%", Integer.valueOf(stalker.getDistance()).toString()))));
		   } else {
			   this.logger.severe(ChatColor.stripColor((lang.prefix + lang.notstalkerconsole).replace("%sender%", sender.getName())));
			   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.notstalker));
		   }
	   } else {
		   sender.sendMessage((ChatColor.translateAlternateColorCodes('&',lang.prefix +  lang.notingame)).replace("%sender%", sender.getName()));
	   }
   }

   private void handleReload(CommandSender sender) {
	   FollowConfig.getInstance().reloadConfig();
	   lang.loadLanguageFile();
	   this.lang = this.plugin.getLang();
	   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.reload));
	   if(sender instanceof Player) {
		   this.logger.info(ChatColor.stripColor(lang.prefix + lang.reloadconsole).replace("%sender%", sender.getName()));
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
     			   sender.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.nearestplayer)).replace("%suspect%", Follow.getFetch().getName(suspectUUID)));
     	   } else {       	
     		   suspectUUID = UUIDFetcher.getUUID(args[0]);
     	   }
     	   
     	   if(suspectUUID != null) {
     		   suspect = Bukkit.getServer().getPlayer(suspectUUID);
     		   if(suspect == null) {
     			   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (lang.prefix + lang.notonlineplayer).replace("%suspect%", args[0])));
     			   sender.sendMessage(ChatColor.GOLD + cmd.getUsage());
     		   } else if(suspect.getName().equalsIgnoreCase(sender.getName())) {
     			   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.nofollowself));
     		   } else if(FollowRoster.getInstance().isSuspect(((Player) sender).getUniqueId()) && !FollowConfig.getInstance().isSuspectCanFollow()) {
     			   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.cantfollowifsuspect));
     		   } else {
     			   if(args.length > 1) {
     				   try {
     					   distance = Integer.parseInt(args[1]);
     				   } catch (NumberFormatException var9) {
     					   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.notinteger.replace("%distance%", args[1])));
     				   }
     			   }
               FollowRoster.getInstance().follow(stalker, suspect, distance);            
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (lang.prefix + lang.follow.replace("%suspect%", suspect.getName()).replace("%distance%", distance.toString()))));            
               this.logger.info(ChatColor.stripColor(((lang.prefix + lang.followconsole.replace("%suspect%", suspect.getName())).replace("%stalker%", stalker.getName())).replace("%distance%", distance.toString())));
     		   }
     	   } else {
     		   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.noonetofollow));
     	   }
  	   } else if(args.length == 0) {
  		   this.handleFollowHelp(sender, cmd, args);
  	   } else {
  		   sender.sendMessage((ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.notingame)).replace("%sender%", sender.getName()));
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
	   for(String s : lang.help) {
		   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
	   }
   }

   private void handleFollowStop(CommandSender sender) {
      if(sender instanceof Player) {
         Stalker s = FollowRoster.getInstance().unfollow((Player)sender);
         if(s != null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.prefix + lang.unfollow.replace("%suspect%", Follow.getFetch().getName(s.getSuspectUUID()))));
            this.logger.info(ChatColor.stripColor(lang.prefix + (lang.unfollowconsole.replace("%sender%", sender.getName()).replace("%suspect%", Follow.getFetch().getName(s.getSuspectUUID())))));
         }
      } else {
         sender.sendMessage(ChatColor.stripColor(lang.prefix + lang.notfollowinganyone.replace("%sender%", sender.getName())));
      }

   }

   private void handleFollowList(CommandSender sender, String[] args) {
      String[] listStringArray = FollowRoster.getInstance().toStringArray();
      sender.sendMessage(listStringArray);
      this.logger.info(ChatColor.stripColor(lang.prefix + lang.sentlist.replace("%sender%", sender.getName())));
   }
}
