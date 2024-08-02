package me.qKing12.AuctionMasterItemDisplay;

import me.qKing12.AuctionMasterItemDisplay.TopElements.TopDisplay;
import me.qKing12.AuctionMasterItemDisplay.TopElements.TopHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class Commands implements CommandExecutor, Listener {
    private static AuctionMasterItemDisplay plugin;

    public Commands(AuctionMasterItemDisplay plugin) {
        this.plugin = plugin;

        plugin.getCommand("auctionhousedisplay").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            String permission=plugin.getConfig().getString("display-use-permission");
            if(!p.hasPermission(permission)) {
                p.sendMessage(utils.chat(plugin.getConfig().getString("no-permission-message")));
                return false;
            }
            if(args.length>=1){
                if(args[0].equals("refresh")){
                    TopHolder.loadElements();
                    p.sendMessage(utils.chat("&aAuctions refreshed in display database!"));
                }
                else if(args[0].equalsIgnoreCase("get")){
                    if(args.length==2){
                        try {
                            int pos = Integer.parseInt(args[1]);
                            if(pos<1){
                                p.sendMessage(utils.chat("&cUsage: /ahdisplay get <position>\n&cExample: /ahdisplay get 1"));
                                return true;
                            }
                            ItemStack toGive = Heads.getHead(pos);
                            ItemMeta meta = toGive.getItemMeta();
                            meta.setDisplayName(Heads.displayName.replace("%position%", args[1]));
                            ArrayList<String> lore = new ArrayList<>();
                            lore.add(utils.chat("&eRight click on a block"));
                            lore.add(utils.chat("&eto place a top display"));
                            lore.add(utils.chat("&eof &6#" + pos + " position"));
                            meta.setLore(lore);
                            toGive.setItemMeta(meta);
                            p.getInventory().addItem(toGive);
                        }catch(Exception x){
                            if(args[1].equalsIgnoreCase("removal"))
                                p.getInventory().addItem(Heads.removalItem.clone());
                            else {
                                x.printStackTrace();
                                p.sendMessage(utils.chat("&cUsage: /ahdisplay get <position>\n&cExample: /ahdisplay get 1"));
                            }
                        }
                    }
                    else
                        p.sendMessage(utils.chat("&cUsage: /ahdisplay get <position>\n&cExample: /ahdisplay get 1"));
                }
                else if(args[0].equalsIgnoreCase("place")){
                    try {
                        Integer position = Integer.parseInt(args[1]);
                        for (Entity et : p.getWorld().getNearbyEntities(p.getLocation().getBlock().getLocation(), 1, 3, 1))
                            if (et.getType().equals(EntityType.ARMOR_STAND)) {
                                p.sendMessage(utils.chat("&cThis location is too close to an armorstand."));
                                return false;
                            }
                        Location loc = p.getLocation().getBlock().getLocation();
                        loc.add(0.5, -1.35, 0.5);
                        TopHolder.top.add(new TopDisplay(position-1, loc));
                        try {
                            Database.saveToFile(plugin);
                        } catch (Exception x) {
                            x.printStackTrace();
                        }
                    }catch(Exception x){
                        p.sendMessage(utils.chat("&cThe display position is invalid (the positions start with 1)"));
                    }
                }
                else if(args[0].equalsIgnoreCase("removeCurrent")){
                    for (Entity et : p.getWorld().getNearbyEntities(p.getLocation().getBlock().getLocation(), 1, 3, 1))
                        if (et.getType().equals(EntityType.ARMOR_STAND)) {
                            TopDisplay display = TopHolder.getTopDisplay(et.getLocation());
                            if(display!=null){
                                display.remove();
                                p.sendMessage(utils.chat("&aDisplay found and removed!"));
                            }
                            else
                                p.sendMessage(utils.chat("&cNo display found."));
                            return false;
                        }
                }
                else if(args[0].equalsIgnoreCase("removeall")){
                    for(TopDisplay element : TopHolder.top) {
                        element.despawn();
                    }
                    TopHolder.top = new ArrayList<>();
                    try {
                        Database.saveToFile(plugin);
                    }catch(Exception x){
                        x.printStackTrace();
                    }
                    p.sendMessage(utils.chat("&aRemoved all displays!"));
                }
            }
            else{
                p.sendMessage(utils.chat("&e/ahdisplay get <position> &8- &fGet a top placer tool"));
                p.sendMessage(utils.chat("&e/ahdisplay place <position> &8 - &fPlaces a display at your feet"));
                p.sendMessage(utils.chat("&e/ahdisplay removeCurrent &8- &fRemoves display at  your feet"));
                p.sendMessage(utils.chat("&e/ahdisplay get removal &8- &fGet a removal tool (Click a display with it and will disappear)"));
                p.sendMessage(utils.chat("&e/ahdisplay removeall &8- &fIf you rage quit using the tools, remove them all"));
                //p.sendMessage(utils.chat("&e/ahdisplay save &8- &fSaves the current data into the file. (Recommended after you finish working with displays."));
                p.sendMessage(utils.chat("&e/ahdisplay refresh &8- &fRefreshes the database from AuctionMaster"));
            }

        }
        else
            sender.sendMessage("You can't execute this command here.");
        return false;
    }

}
