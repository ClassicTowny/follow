package me.zathrasnottheone.follow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import me.zathrasnottheone.follow.Stalker;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FollowRoster {
   private static final FollowRoster instance = new FollowRoster();
   private static HashMap<UUID, Stalker> ROSTER = new HashMap<UUID, Stalker>();

   public static FollowRoster getInstance() {
      return instance;
   }

   public HashSet<Stalker> getStalkersForSuspect(Player suspect) {
      Iterator<UUID> iterator = ROSTER.keySet().iterator();
      HashSet<Stalker> stalkers = new HashSet<Stalker>();

      while(iterator.hasNext()) {
         Stalker s = (Stalker)ROSTER.get(iterator.next());
         if(suspect.getUniqueId().equals(s.getSuspectUUID())) {
            stalkers.add(s);
         }
      }

      return stalkers;
   }

   public Stalker getStalker(UUID stalkerUUID) {
      Iterator<UUID> iterator = ROSTER.keySet().iterator();
      Stalker stalker = null;

      while(iterator.hasNext() && stalker == null) {
         Stalker s = (Stalker)ROSTER.get(iterator.next());
         if(stalkerUUID.equals(s.getUUID())) {
            stalker = s;
         }
      }

      return stalker;
   }

   public void follow(Player stalker, Player suspect, int distance) {
      ROSTER.put(stalker.getUniqueId(), new Stalker(stalker.getUniqueId(), suspect.getUniqueId(), distance));
   }

   public Stalker unfollow(Player stalker) {
      return (Stalker)ROSTER.remove(stalker.getUniqueId());
   }

   public int getSize() {
      return ROSTER.size();
   }

   public String[] toStringArray() {
      String[] result = new String[ROSTER.size() + 2];
      Iterator<UUID> iterator = ROSTER.keySet().iterator();
      byte i = 0;
      int var6 = i + 1;

      Stalker value;
      for(result[i] = ChatColor.GOLD + "===== Follow List ======"; iterator.hasNext(); result[var6++] = ChatColor.GOLD + "• " + ChatColor.RED + Follow.getFetch().getName(value.getUUID()) + ChatColor.GOLD + " is following " + ChatColor.WHITE + Follow.getFetch().getName(value.getSuspectUUID()) + ChatColor.GOLD + " at distance " + ChatColor.AQUA + value.getDistance()) {
         UUID key = (UUID)iterator.next();
         value = (Stalker)ROSTER.get(key);
      }

      result[var6++] = ChatColor.GOLD + "===== End Of List =====";
      return result;
   }

   public void remove(Player player) {
      this.unfollow(player);
      this.removeStalkersForSuspect(player);
   }

   private void removeStalkersForSuspect(Player suspect) {
      Iterator<UUID> iterator = ROSTER.keySet().iterator();

      while(iterator.hasNext()) {
         Stalker s = (Stalker)ROSTER.get(iterator.next());
         if(suspect.getUniqueId().equals(s.getSuspectUUID())) {
            ROSTER.remove(s.getUUID());
         }
      }

   }

   public boolean isSuspect(UUID suspectUUID) {
      Iterator<UUID> iterator = ROSTER.keySet().iterator();

      while(iterator.hasNext()) {
         Stalker s = (Stalker)ROSTER.get(iterator.next());
         if(suspectUUID == s.getSuspectUUID()) {
            return true;
         }
      }

      return false;
   }
}
