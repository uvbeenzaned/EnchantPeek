package net.mended_drum.EnchantPeek;

import org.bukkit.plugin.java.JavaPlugin;

public final class EnchantPeek extends JavaPlugin {

   public void onEnable() {
      this.getLogger().info("EnchantPeek Enabled!");
      EnchantListener listener = new EnchantListener(this);
   }

   public void onDisable() {
      this.getLogger().info("EnchantPeek Disabled!");
      org.bukkit.event.HandlerList.unregisterAll((JavaPlugin)this);
   }


}
