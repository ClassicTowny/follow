package me.zathrasnottheone.follow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import me.zathrasnottheone.follow.Follow;
import me.zathrasnottheone.follow.FollowConfig;
import me.zathrasnottheone.follow.FollowRoster;
import me.zathrasnottheone.follow.Stalker;
import me.drkmatr1984.customevents.moveEvents.SignificantPlayerMoveEvent;
import me.zathrasnottheone.follow.BlockTypes.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {
   private Logger _logger = null;
   private Follow _plugin = null;
   private static final List<Material> SAFE_TO_SHARE = new ArrayList<Material>();
   private static final List<Material> DONT_STAND_ON = new ArrayList<Material>();
   private static final List<Material> HALF_HEIGHT = new ArrayList<Material>();
   private static final List<Material> HEIGHT_AND_HALF = new ArrayList<Material>();

   static {
      // Safe to stand on these
	  for(SafetoWalk safe : BlockTypes.SafetoWalk.values())
	      SAFE_TO_SHARE.add(Material.getMaterial(safe.toString().toUpperCase()));
      // Don't stand on these
      for(Hazards hazard : BlockTypes.Hazards.values())
          DONT_STAND_ON.add(Material.getMaterial(hazard.toString().toUpperCase()));
      
      // Half-Height (Slabs and such)
      for(Slabs slab : BlockTypes.Slabs.values())
    	  HALF_HEIGHT.add(Material.getMaterial(slab.toString().toUpperCase()));
      for(Beds bed : BlockTypes.Beds.values())
    	  HALF_HEIGHT.add(Material.getMaterial(bed.toString().toUpperCase()));
      
      //Height-and-a-half
      for(Fences fences : BlockTypes.Fences.values())
          HEIGHT_AND_HALF.add(Material.getMaterial(fences.toString().toUpperCase()));
      for(Gates gates : BlockTypes.Gates.values())  
          HEIGHT_AND_HALF.add(Material.getMaterial(gates.toString().toUpperCase()));
   }

   public PlayerListener(Follow follow) {
      this._plugin = follow;
      this._logger = this._plugin.getLogger();
   }

   @EventHandler(priority = EventPriority.LOW)
   public void onPlayerMove(SignificantPlayerMoveEvent e) {
      this.onMovement(e.getPlayer(), e.getFrom(), e.getTo());
      if(FollowConfig.getInstance().isRotateHead()) {
	      FollowRoster roster = FollowRoster.getInstance();
	      Player player = e.getPlayer();
	      Set<Stalker> stalkers = null;
	      stalkers = roster.getStalkersForSuspect(player);
	      if(!stalkers.isEmpty()) {
		      Player stalkingPlayer = null;
		
		      assert stalkers != null;
		
		      Iterator<Stalker> iterator = stalkers.iterator();
		
		      while(iterator.hasNext()) {
		         Stalker s = (Stalker)iterator.next();
		         stalkingPlayer = Bukkit.getPlayer(s.getUUID());
		         if(stalkingPlayer != null) {
		             Location to = e.getTo();
		             this.rotateStalker(stalkingPlayer, player, to);
		         }
		      }
	      }
      }
   }

   @EventHandler(priority = EventPriority.LOW)
   public void onPlayerTeleport(PlayerTeleportEvent e) {
      this.onMovement(e.getPlayer(), e.getFrom(), e.getTo());
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onPlayerLogout(PlayerQuitEvent e) {
      if(!FollowRoster.getInstance().getStalkersForSuspect(e.getPlayer()).isEmpty())
    	  for(Stalker stalker : FollowRoster.getInstance().getStalkersForSuspect(e.getPlayer()))
    		  Bukkit.getPlayer(stalker.getUUID()).sendMessage(ChatColor.translateAlternateColorCodes('&', _plugin.getLang().prefix + _plugin.getLang().unfollow.replace("%suspect%", e.getPlayer().getName())));
      FollowRoster.getInstance().remove(e.getPlayer());
   }

   private void onMovement(Player player, Location from, Location to) {
	  
      FollowRoster roster = FollowRoster.getInstance();
      Set<Stalker> stalkers = null;
      stalkers = roster.getStalkersForSuspect(player);
      if(!stalkers.isEmpty()) {
         if(!from.getWorld().equals(to.getWorld()) || from.distance(to) > 0.21D) {
            this.updateStalkers(stalkers, player, to);
         }

      }
   }

   private void updateStalkers(Set<Stalker> stalkers, Player suspectPlayer, Location to) {
      Player stalkingPlayer = null;

      assert stalkers != null;

      Iterator<Stalker> iterator = stalkers.iterator();

      while(iterator.hasNext()) {
         Stalker s = (Stalker)iterator.next();
         stalkingPlayer = Bukkit.getPlayer(s.getUUID());
         if(stalkingPlayer != null) {
            if(s.isCooledDown(FollowConfig.getInstance().getCoolDown())) {
               if(this.moveStalker(stalkingPlayer, suspectPlayer, s.getDistance(), to)) {
                  s.heatUp();
                  if(FollowConfig.getInstance().isRotateHead()) {
                      this.rotateStalker(stalkingPlayer, suspectPlayer, to);
                  }
                  if(s.getAge() % 60 == 0) {
                     this._logger.info(ChatColor.stripColor((((_plugin.getLang().followtime).replace("%stalker%", Bukkit.getOfflinePlayer(s.getUUID()).getName())).replace("%suspect%", (Bukkit.getOfflinePlayer(s.getSuspectUUID())).getName())).replace("%minutes%", (Integer.valueOf(s.getAge() / 60)).toString())));
                  }
               }
            }
         }
      }

   }

   private boolean rotateStalker(Player stalkingPlayer, Player suspectPlayer, Location to) {
      Location stalkerLocation = stalkingPlayer.getLocation();
      if(!stalkerLocation.getWorld().getName().equalsIgnoreCase(to.getWorld().getName())) {
         return false;
      } else {
         double deltax = to.getX() - stalkerLocation.getX();
         double deltay = to.getY() - stalkerLocation.getY();
         double deltaz = to.getZ() - stalkerLocation.getZ();
         stalkerLocation.setYaw((float)this.calculateYaw(deltax, deltaz));
         stalkerLocation.setPitch((float)this.calculatePitch(deltax, deltay, deltaz));
         return stalkingPlayer.teleport(stalkerLocation);
      }
   }

   private boolean moveStalker(Player stalkingPlayer, Player suspectPlayer, int preferredDistance, Location newSuspectLocation) {
      Location stalkerLocation = stalkingPlayer.getLocation();
      World w = newSuspectLocation.getWorld();
      if(!stalkerLocation.getWorld().getName().equalsIgnoreCase(w.getName())) {
         stalkerLocation.setWorld(w);
      }
      double deltax = newSuspectLocation.getX() - stalkerLocation.getX();
      double deltaz = newSuspectLocation.getZ() - stalkerLocation.getZ();
      double actualDistance = Math.sqrt(deltax * deltax + deltaz * deltaz);
      double ratio = (double)preferredDistance / actualDistance;
      double x = newSuspectLocation.getX() - deltax * ratio;
      double z = newSuspectLocation.getZ() - deltaz * ratio;
      double y = newSuspectLocation.getY();
      boolean alreadyAllowedFlight = stalkingPlayer.getAllowFlight();
      if(suspectPlayer.isFlying()){
      	 stalkingPlayer.setAllowFlight(true);
       	 stalkingPlayer.setFlying(suspectPlayer.isFlying());
      }else{
    	  if(alreadyAllowedFlight){
    		  stalkingPlayer.setAllowFlight(true);
    		  stalkingPlayer.setFlying(suspectPlayer.isFlying());
    	  }else{
    		  stalkingPlayer.setAllowFlight(false);
    		  stalkingPlayer.setFlying(suspectPlayer.isFlying()); 
     	  }	 
      }
      stalkingPlayer.setGameMode(suspectPlayer.getGameMode());
      y = this.makeSafeY(w, x, y, z, stalkingPlayer.isFlying());
      if(y < 1.0D) {
         y = (double)w.getHighestBlockYAt((int)Math.round(Math.floor(x)), (int)Math.round(Math.floor(z)));
      }
         double deltay = newSuspectLocation.getY() - y;
         this.setLocation(stalkerLocation, x, y, z, (float)this.calculateYaw(deltax, deltaz), (float)this.calculatePitch(deltax, deltay, deltaz));
      return stalkingPlayer.teleport(stalkerLocation);
   }

   private void setLocation(Location stalkerLocation, double x, double y, double z, float yaw, float pitch) {
      stalkerLocation.setX(x);
      stalkerLocation.setY(y);
      stalkerLocation.setZ(z);
      stalkerLocation.setYaw(yaw);
      stalkerLocation.setPitch(pitch);
   }

   private double calculateYaw(double deltax, double deltaz) {
      double viewDirection = 90.0D;
      if(deltaz != 0.0D) {
         viewDirection = Math.toDegrees(Math.atan(-deltax / deltaz));
      }

      if(deltaz < 0.0D) {
         viewDirection += 180.0D;
      } else if(deltax > 0.0D && deltaz > 0.0D) {
         viewDirection += 360.0D;
      }

      return viewDirection;
   }

   private double calculatePitch(double x, double y, double z) {
      double pitch = 0.0D;
      double a = Math.sqrt(x * x + z * z);
      double theta = Math.atan(y / a);
      pitch = -Math.toDegrees(theta);
      return pitch;
   }

   private double makeSafeY(World w, double dx, double dy, double dz, boolean flying) {
      int x = (int)Math.floor(dx);
      int y = (int)Math.floor(dy);
      int z = (int)Math.floor(dz);

      Double newy;
      if(flying){
    	  for(newy = Double.valueOf(0.0D); !this.safe(w, x, y, z) && y <= w.getHighestBlockYAt(x, z); ++y) {
    	      y++;
    	  } 
      }else{
    	  for(newy = Double.valueOf(0.0D); !this.safe(w, x, y, z) && y <= w.getHighestBlockYAt(x, z); ++y) {
    	      ;
    	  }  
      }

      do {
         --y;
      } while(this.safe(w, x, y, z) && y > 1 && !flying);

      if(y < w.getMaxHeight()) {
         if(DONT_STAND_ON.contains(w.getBlockAt(x, y, z).getType())) {
            newy = Double.valueOf(0.0D);
         } else if(HALF_HEIGHT.contains(w.getBlockAt(x, y, z).getType())) {
            newy = Double.valueOf((double)y + 0.5626D);
         } else if(HEIGHT_AND_HALF.contains(w.getBlockAt(x, y, z).getType())) {
            newy = Double.valueOf((double)y + 1.5001D);
         } else {
            newy = Double.valueOf((double)y + 1.0D);
         }
      }

      return newy.doubleValue();
   }

   private boolean safe(World w, int x, int y, int z) {
      Block bottom = w.getBlockAt(x, y, z);
      Block top = w.getBlockAt(x, y + 1, z);
      Material bottomMaterial = bottom.getType();
      Material topMaterial = top.getType();
      boolean safe = (bottom.isEmpty() || SAFE_TO_SHARE.contains(bottomMaterial)) && (top.isEmpty() || SAFE_TO_SHARE.contains(topMaterial));
      return safe;
   }
   
}
