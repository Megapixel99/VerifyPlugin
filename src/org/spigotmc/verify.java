/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spigotmc;
import java.io.BufferedReader;
import java.io.*;
import java.net.URL;
import java.net.*;
import com.google.gson.*;
import java.util.ArrayList;
import java.util.logging.Level;
import org.bukkit.plugin.java.*;
import org.bukkit.command.*;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachment;


public class verify extends JavaPlugin implements Listener {

    ArrayList<String> muted = new ArrayList<>();
    ArrayList<String> muteExempt = new ArrayList<>();
    Boolean serverMute = false;
    String hostname = ""; //change this
    String helpVerbiage = ""; //change this
    String token = ""; //change this
    double SpawnRadius = 10.0;
    Location spawnLoc = null;

    @Override
    public void onEnable(){
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPVP(EntityDamageEvent e) {
        Location EntLoc = e.getEntity().getLocation();
        if (notNearSpawn(EntLoc)) {
            e.setCancelled(true);
            e.getEntity().sendMessage("You are not allowed to PvP near spawn");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!notNearSpawn(e.getPlayer().getLocation())){
           e.setCancelled(true);
           e.getPlayer().sendMessage("You are not allowed to build near spawn");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("Welcome " + e.getPlayer().getName() + ", to the server");
        Bukkit.getLogger().log(Level.INFO, "{0} logged onto the server", e.getPlayer().getName());
        e.getPlayer().setGameMode(GameMode.SPECTATOR);
        e.getPlayer().setPlayerListName(" §f[GHOST] " + e.getPlayer().getName());
        e.getPlayer().setFlySpeed(0.1f);
        e.getPlayer().setWalkSpeed(0.1f);
        try {
            isVerified(e.getPlayer(), readJsonFromUrl("http://localhost:3000/verify/status/uuid/" + e.getPlayer().getUniqueId() + "?ign=" + e.getPlayer().getName()));
        } catch (Exception ex) {
            // server is down
            Bukkit.getLogger().warning(ex.getLocalizedMessage());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (muteExempt.contains(player.getName()) || (!serverMute && !muted.contains(player.getName()))){
           String msg = player.getPlayerListName() + "§f: " + event.getMessage();
           player.sendMessage(msg);
           Bukkit.getLogger().log(Level.INFO, "{0} ({1}): {2}", new Object[]{player.getName(), player.getUniqueId(), event.getMessage()});
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
       if (sender instanceof Player) {
           Player player = (Player) sender;
           if (label.equalsIgnoreCase("verify")) {
               try {
                  isVerified(player, sendPut("http://"+hostname+"/verify/id/" + args[0] + "/code/" + args[1] + "/ign/" + player.getName() + "/uuid/" + player.getUniqueId()));
               } catch (Exception e) {
                   // server is down
               }
           } else if (label.equalsIgnoreCase("help")) {
               player.sendMessage("Please type /verify netID yourCode to verify " + helpVerbiage);
               player.sendMessage("For example: /verify jdoe52 1234567");
           } else if (label.equalsIgnoreCase("reset")) {
               if (player.hasPermission("reset")){
                    try {
                       sendPut("http://"+hostname+"/verify/reset/code/ign/" + args[0].toLowerCase());
                    } catch (Exception e) {
                        // server is down
                    }
               } else {
                    return false;
               }
           } else if (label.equalsIgnoreCase("mute")) {
               if (player.hasPermission("mute")){
                    if (args[0] != null) {
                        muted.add(args[0]);
                    } else {
                        serverMute = true;
                    }
               } else {
                    return false;
               }
           } else if (label.equalsIgnoreCase("unmute")) {
               if (player.hasPermission("mute")){
                    if (args[0] != null) {
                        muted.remove(args[0]);
                    } else {
                        serverMute = false;
                    }
               } else {
                    return false;
               }
           }
       }
       return true;
   }

   public boolean notNearSpawn(Location loc) {
       if (spawnLoc == null) {
        spawnLoc = Bukkit.getWorld(Bukkit.getWorlds().get(0).getName()).getSpawnLocation();
       }
       return (loc.getWorld().equals(spawnLoc.getWorld()) && loc.distanceSquared​(spawnLoc) <= SpawnRadius);
   }

   private void isVerified(Player player, JsonObject res) throws Exception {
        PermissionAttachment attachment = player.addAttachment(this);
        if (res.get("verified").toString().equals("true")) {
            if (res.get("role").toString().equalsIgnoreCase("\"mod\"")){
                player.setOp(false);
                attachment.setPermission("reset",true);
                attachment.setPermission("mute",true);
                attachment.setPermission("minecraft.command.kick",true);
                attachment.setPermission("minecraft.command.ban",true);
                attachment.setPermission("minecraft.command.gamemode",true);
                attachment.setPermission("minecraft.command.teleport",true);
                player.setPlayerListName(" §f[§bMOD§f] §b" + player.getName());
                if (!muteExempt.contains(player.getName())){
                    muteExempt.add(player.getName());
                }
                player.setDisplayName("§b" + player.getName());
            } else if (res.get("role").toString().equalsIgnoreCase("\"staff\"")) {
                player.setOp(false);
                player.setPlayerListName(" §f[STAFF§f] §1" + player.getName());
                attachment.setPermission("mute",true);
                if (!muteExempt.contains(player.getName())){
                    muteExempt.add(player.getName());
                }
                player.setDisplayName("§1" + player.getName());
            } else if (res.get("role").toString().equalsIgnoreCase("\"owner\"")) {
                player.setOp(true);
                attachment.setPermission("reset",true);
                attachment.setPermission("mute",true);
                player.setPlayerListName(" §f[§cOVERLORD§f] §c" + player.getName());
                if (!muteExempt.contains(player.getName())){
                    muteExempt.add(player.getName());
                }
                player.setDisplayName("§c" + player.getName());
            } else {
                player.setOp(false);
                player.setPlayerListName(" " + player.getName());
                player.setGameMode(GameMode.SURVIVAL);
            }
            ((CraftServer) Bukkit.getServer()).getHandle().getServer().getCommandDispatcher().a((((CraftPlayer) player).getHandle()));
        } else {
            player.sendMessage("You are currently unverified on this server and will remain in spectator mode until you verify " + helpVerbiage);
            player.sendMessage("for help with verifying please use /help");
        }
   }

   private JsonObject sendPut(String _url) throws Exception {

        String url = _url;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("Authorization", token);

        con.setRequestMethod("PUT");

         InputStream response = con.getInputStream();

         try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(response, "UTF-8"));
            String jsonText = readAll(rd);
            JsonObject json = new JsonParser().parse(jsonText).getAsJsonObject();
            return json;
          } finally {
            response.close();
          }
    }

   private static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }

  public JsonObject readJsonFromUrl(String _url) throws IOException {

        String url = _url;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("Authorization", token);

        con.setRequestMethod("GET");

         InputStream response = con.getInputStream();

         try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(response, "UTF-8"));
            String jsonText = readAll(rd);
            JsonObject json = new JsonParser().parse(jsonText).getAsJsonObject();
            return json;
          } finally {
            response.close();
          }
  }
}
