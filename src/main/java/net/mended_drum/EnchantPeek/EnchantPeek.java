package net.mended_drum.EnchantPeek;

import java.util.ArrayList;
import java.util.HashSet;
import net.minecraft.server.v1_5_R2.BiomeMeta;
import net.minecraft.server.v1_5_R2.EnumCreatureType;
import net.minecraft.server.v1_5_R2.IChunkProvider;
import net.minecraft.server.v1_5_R2.MathHelper;
import net.minecraft.server.v1_5_R2.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
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
