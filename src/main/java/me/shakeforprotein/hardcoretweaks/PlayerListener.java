package me.shakeforprotein.hardcoretweaks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerListener implements Listener {

    private HardcoreTweaks pl;
    private UpdateChecker uc;

    public PlayerListener(HardcoreTweaks main) {
        pl = main;
        this.uc = new UpdateChecker(pl);
    }

    //Declare Variables
    String command;


    @EventHandler
    public void onPlayerLeaveBed(PlayerBedEnterEvent e) {
    e.getPlayer().sendMessage(ChatColor.RED + "SLEEP IS FOR THE WEAK");
    if(e.getPlayer().getFoodLevel() > 5){e.getPlayer().setFoodLevel(5);}
    if(e.getPlayer().getSaturation() > 3){e.getPlayer().setSaturation(3);}
    e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1), true);
    }


    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e){
        if (e.getPlayer().hasPermission(pl.getName() + ".updatechecker")) {
            uc.getCheckDownloadURL(e.getPlayer());
        }
        Player p = e.getPlayer();
        p.setSaturation(0);
        p.setFoodLevel(5);
        if (pl.getConfig().get("players." + p.getName()) == null) {
            pl.getConfig().set("players." + p.getName(), p.getName());
            pl.getConfig().set("players." + p.getName() + ".cooldownLevel", 0);
            pl.getConfig().set("players." + p.getName() + ".deaths", 0);
            pl.saveConfig();
        }
    }



    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e){
        if (e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            int cdLvl = 0;
            int cdDelay = 0;
            int delayInSeconds;
            int deaths = pl.getConfig().getInt("players." + p.getName() +".deaths") +1;
            pl.getConfig().set("players." + p.getName() +".deaths", deaths);
            int cdCd = (int) pl.getConfig().get("cooldownCooldown");
            String rollback = pl.getConfig().get("rollbackHowFar").toString();
            if (p.getWorld().getName().contains("Hardcore")) {


                cdLvl = pl.getConfig().getInt("players." + p.getName() + ".cooldownLevel");
                if (pl.getConfig().get("cooldownTicks." + cdLvl) != null) {
                    cdDelay = pl.getConfig().getInt("cooldownTicks." + cdLvl);
                } else {
                    cdLvl = pl.getConfig().getInt("maxCooldownLevel");
                    pl.getConfig().set("players." + p.getName() +".cooldownLevel", pl.getConfig().getInt("maxCooldownLevel"));
                    cdDelay = pl.getConfig().getInt("cooldownTicks." + cdLvl);
                }
                delayInSeconds = cdDelay / 20;

                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                command = "co rollback u:" + p.getName() + " r:#global t:" + rollback;
                if(pl.getConfig().getString("debug").equalsIgnoreCase("true")){
                    p.sendMessage(command);
                }
                Bukkit.dispatchCommand(console, command);
                command = "title " + p.getName() + " times 20 100 20";
                Bukkit.dispatchCommand(console, command);
                command = "title "+ p.getName() + " subtitle {\"text\":\"Like a bitch\",\"underlined\":true,\"color\":\"red\"}";
                Bukkit.dispatchCommand(console, command);
                command = "title " + p.getName() + " title {\"text\":\"YOU DIED\",\"color\":\"dark_red\"}";
                Bukkit.dispatchCommand(console, command);


                p.sendMessage("You must now wait " + delayInSeconds + " seconds to respawn");


                p.setHealth(20);
                p.setGameMode(GameMode.SPECTATOR);

                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200000, 1), true);
                //Removed initial call to spawn
                // Bukkit.dispatchCommand(p, "spawn");
                cooldownControl(p, 1);

                // REMOVE ONE COOLDOWN LEVEL AFTER X TIME
       /*         Bukkit.getScheduler().scheduleSyncDelayedTask(pl, new Runnable() {
                    @Override
                    public void run() {
                        cooldownControl(p, -1);
                    }
                    }, pl.getConfig().getInt("cooldownCooldown"));
        */

                Bukkit.getScheduler().scheduleSyncDelayedTask(pl, new Runnable() {

                    @Override
                    public void run() {
                        p.setGameMode(GameMode.SURVIVAL);
                        Bukkit.dispatchCommand(p, "spawn");
                        p.sendMessage("You have respawned.");
                        ClearEffects(p);
                        p.setSaturation(3);
                        p.setFoodLevel(3);
                        Bukkit.dispatchCommand(console, "minecraft:give "+ p.getName() + " minecraft:cookie 3");
                    }

                }, cdDelay);
            }
        }
    }


    public void cooldownControl(Player p, int amount){
        int cdLvl = (int) pl.getConfig().get("players." + p.getName() + ".cooldownLevel");
        cdLvl = cdLvl + amount;
        pl.getConfig().set("players." + p.getName() + ".cooldownLevel", cdLvl);
        pl.saveConfig();
    }

    public void ClearEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());
    }
}
