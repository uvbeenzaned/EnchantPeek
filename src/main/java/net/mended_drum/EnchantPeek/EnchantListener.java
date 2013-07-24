package net.mended_drum.EnchantPeek;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.minecraft.server.v1_6_R2.EnchantmentManager;
import net.minecraft.server.v1_6_R2.ItemStack;

public class EnchantListener implements Listener {

    private Map<Player,Map<Integer,ItemStack>> storage = new HashMap<Player,Map<Integer,ItemStack>>();

    public EnchantListener(EnchantPeek plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPrepareItemEnchantEvent(PrepareItemEnchantEvent evt) {
        Player p = evt.getEnchanter();
        if (!p.hasPermission("enchantpeek.allow")) {return;}
        Map<Integer,ItemStack> enchantments = new HashMap<Integer,ItemStack>();
        List<String> lore = new ArrayList<String>();
        // Make every possible enchantment and put it in storage for when the player chooses which one to get.
        for (int cost : evt.getExpLevelCostsOffered()) {
            enchantments.put(cost, makeEnchantedItemStack(evt.getItem(), cost, p));
            StringBuilder ex = new StringBuilder(cost + " -> ");
            org.bukkit.inventory.ItemStack item = CraftItemStack.asBukkitCopy((ItemStack) enchantments.get(cost));
            if (item.getType() == org.bukkit.Material.ENCHANTED_BOOK) {
                for (Map.Entry<org.bukkit.enchantments.Enchantment,Integer> x : ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants().entrySet()) {
                    ex.append(", " + lookup(x.getKey().getName()) +" " + x.getValue());
                    break;
                }
            } else {
                for (Map.Entry<org.bukkit.enchantments.Enchantment,Integer> x : item.getEnchantments().entrySet()) {
                    ex.append(", " + lookup(x.getKey().getName()) +" " + x.getValue());
                }
            }
            lore.add(ex.toString().replace("> ,","> "));
            ItemMeta meta = evt.getItem().getItemMeta();
            if (meta.hasLore()) {
                List<String> oldlore = cleanLore(meta.getLore());
                oldlore.addAll(lore);
                lore = oldlore;
            }
            meta.setLore(lore);
            evt.getItem().setItemMeta(meta);
            storage.put(evt.getEnchanter(), enchantments);
        }
    }

    @EventHandler
    public void onEnchantItemEvent(EnchantItemEvent evt) {
        Player p = evt.getEnchanter();
        if (!p.hasPermission("enchantpeek.allow")) {return;}
        if (this.storage.containsKey(p)) {
            Map playerEnchants = (Map<Integer,ItemStack>) storage.get(p);
            org.bukkit.inventory.ItemStack enchantedItem = CraftItemStack.asBukkitCopy((ItemStack) playerEnchants.get(evt.getExpLevelCost()));
            Map correctEnchantments = enchantedItem.getEnchantments();
            if (enchantedItem.getType() == org.bukkit.Material.ENCHANTED_BOOK) {
                correctEnchantments = ((EnchantmentStorageMeta) enchantedItem.getItemMeta()).getStoredEnchants();
            }
            Map fixit = evt.getEnchantsToAdd(); fixit.clear();
            fixit.putAll(correctEnchantments);
            storage.remove(p);
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent evt) {
        Player p = (Player) evt.getWhoClicked();
        if (!p.hasPermission("enchantpeek.allow")) {return;}
        if (evt.getInventory().getType().equals(InventoryType.ENCHANTING)) {
            org.bukkit.inventory.ItemStack item = evt.getCurrentItem();
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                List<String> lore = cleanLore(item.getItemMeta().getLore());
                if (lore.size() == 0) { lore = null;}
                ItemMeta m = item.getItemMeta();
                m.setLore(lore);
                item.setItemMeta(m);
                storage.remove(p);
            }
        }
    }

    // There is no way to get between the player choosing a cost
    // and CB enchanting the item, so we generate our own enchantments
    // and override what CB did.
    private ItemStack makeEnchantedItemStack(org.bukkit.inventory.ItemStack itemToEnchant, int cost, Player p) {
        ItemStack temp = CraftItemStack.asNMSCopy(itemToEnchant);
        // cobbled together from craftbukkit & forge...
        // we need to *get* the enchantments, not apply them instantly >:-(
        if (cost > 0 && temp != null && p.getLevel() >= cost || p.getGameMode() == GameMode.CREATIVE) {
            return EnchantmentManager.a(new java.util.Random(), temp, cost);
        }
        return temp;
    }

    private static List cleanLore(List<String> lore) {
        List<String> newlore = new ArrayList();
        for (int i = 0; i < lore.size(); i++) {
            if (!lore.get(i).matches("\\d\\d? -> .*")) {
                newlore.add(lore.get(i));
            }
        }
        return newlore;
    }

    private String lookup(String name) {
        if (ENames.containsKey(name)) {return ENames.get(name);}
        return name;
    }

    private static final Map<String,String> ENames = new HashMap<String,String>(){{
            // Weapons
            put("KNOCKBACK",                "Knockback");
            put("LOOT_BONUS_MOBS",          "Looting");
            put("DAMAGE_ALL",               "Sharpness");
            put("DAMAGE_UNDEAD",            "Smite");
            put("DAMAGE_ARTHROPODS",        "Bane of Anthropods");
            put("FIRE_ASPECT",              "Fire Aspect");
            put("ARROW_INFINITE",           "Infinity");
            put("ARROW_FIRE",               "Flame");
            put("ARROW_KNOCKBACK",          "Punch");
            put("ARROW_DAMAGE",             "Power");
            // Armor
            put("OXYGEN",                   "Respiration");
            put("WATER_WORKER",             "Aqua Affinity");
            put("PROTECTION_FALL",      "Feather Falling");
            put("THORNS",                   "Thorns");
            put("PROTECTION_ENVIRONMENTAL", "Protection");
            put("PROTECTION_FIRE",          "Fire Protection");
            put("PROTECTION_EXPLOSIONS",    "Blast Protection");
            put("PROTECTION_PROJECTILE",    "Projectile Protection");
            // Tools
            put("DIG_SPEED",                "Efficiency");
            put("DURABILITY",               "Unbreaking");
            put("LOOT_BONUS_BLOCKS",        "Fortune");
            put("SILK_TOUCH",               "Silk Touch");
        }};
}
