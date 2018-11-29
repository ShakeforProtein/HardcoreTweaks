package me.shakeforprotein.hardcoretweaks;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

public final class HardcoreTweaks extends JavaPlugin {

    private PlayerListener pL;
    private UpdateChecker uc;
    public HardcoreTweaks(){}

    @Override
    public void onEnable() {
        // Plugin startup logic
            getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getConfig().options().copyDefaults(true);
        getConfig().set("version", this.getDescription().getVersion());
        saveConfig();
        this.uc = new UpdateChecker(this);


        System.out.println("Hardcore Tweaks Loaded");
    }


    @Override
    public void onDisable() {
        for (String p : getConfig().getConfigurationSection("players").getKeys(false)) {
            getConfig().set("players." + p + ".cooldownLevel", 0);
        }
        System.out.println("Respawn Cooldowns Reset");
        saveConfig();
        // Plugin shutdown logic
    }


    public void ClearEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());
    }
}
