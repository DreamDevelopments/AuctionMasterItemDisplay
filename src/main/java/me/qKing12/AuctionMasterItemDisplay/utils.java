package me.qKing12.AuctionMasterItemDisplay;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class utils {
    public static String locationToBase64(Location loc) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);


            dataOutput.writeObject(loc);

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save location.", e);
        }
    }

    public static Location locationFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            // Read the serialized inventory
            Location loc = (Location) dataInput.readObject();

            dataInput.close();
            return loc;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    private static <T> Field getField(Class<?> target, String name, Class<T> fieldType, int index) {
        for (final Field field : target.getDeclaredFields()) {
            if ((name == null || field.getName().equals(name)) && fieldType.isAssignableFrom(field.getType()) && index-- <= 0) {
                field.setAccessible(true);
                return field;
            }
        }

        // Search in parent classes
        if (target.getSuperclass() != null)
            return getField(target.getSuperclass(), name, fieldType, index);
        throw new IllegalArgumentException("Cannot find field with type " + fieldType);
    }

    public static ArmorStand getArmorStandDown(Location loc){
        ArmorStand amF = null;
        for(Entity ent : loc.getWorld().getNearbyEntities(loc, 1, 1, 1)) {
            if (ent.getType().equals(EntityType.ARMOR_STAND)) {
                ArmorStand am = (ArmorStand) ent;
                if(am.getLocation().equals(loc) && AuctionMasterItemDisplay.verifyAngle(am.getLeftArmPose()))
                    if(amF==null)
                        amF=am;
                    else
                        am.remove();
            }
        }
        return amF;
    }

    public static ArmorStand getArmorStandUp(Location loc){
        ArmorStand amF = null;
        for(Entity ent : loc.getWorld().getNearbyEntities(loc, 1, 1, 1)) {
            if (ent.getType().equals(EntityType.ARMOR_STAND)) {
                ArmorStand am = (ArmorStand) ent;
                if(am.getLocation().equals(loc) && AuctionMasterItemDisplay.verifyAngle(am.getRightArmPose()))
                    if(amF==null)
                        amF=am;
                    else
                        am.remove();
            }
        }
        return amF;
    }

    /*public static Item getItem(Location loc){
        for(Entity ent : loc.getWorld().getNearbyEntities(loc, 1, 1, 1)) {
            if (ent.getType().equals(EntityType.DROPPED_ITEM)) {
                Item display = (Item) ent;
                if(!new NBTItem(display.getItemStack()).getString("AuctionMasterItemDisplay").equals("")) {
                    return display;
                }
            }
        }
        return null;
    }*/

    public static ItemStack setSkullOwner(String input) {
        ItemStack i = AuctionMasterItemDisplay.items.skullItem.clone();
        SkullMeta skullMeta = (SkullMeta) i.getItemMeta();
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "amid_customSkull");
        String texture;
        if(input.startsWith("http")) {
            texture = input;
        } else {
            texture = new String(Base64.getDecoder().decode(input)).split("SKIN\":\\{\"url\":\"")[1].split("\"")[0];
        }
        try {
            profile.getTextures().setSkin(new URL(texture));
        } catch(MalformedURLException e) {
            Bukkit.getLogger().warning("[AuctionMasterItemDisplay] Invalid URL for custom skull texture: " + input);
            e.printStackTrace();
        }
        skullMeta.setOwnerProfile(profile);
        i.setItemMeta(skullMeta);
        return i;
    }

    public static boolean isLoadedChunk(Location loc){
        int chunkX = loc.getBlockX() / 16;
        int chunkZ = loc.getBlockZ() / 16;
        return loc.getWorld().isChunkLoaded(chunkX, chunkZ);
    }

    public static int getPosition(ItemStack item){
        try{
            return Integer.parseInt(item.getItemMeta().getDisplayName().split(" ")[0].split("#")[1]);
        }catch (Exception x){
            return 0;
        }
    }

    public static String itemToBase64(ItemStack item) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);


            dataOutput.writeObject(item);

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static ItemStack itemFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            // Read the serialized inventory
            ItemStack item = (ItemStack) dataInput.readObject();

            dataInput.close();
            return item;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }


    public static String chat (String s){
        if(s==null)
            return ChatColor.translateAlternateColorCodes('&', "&cConfig Missing Text");
        byte[] temp = s.getBytes(Charsets.UTF_8);
        return ChatColor.translateAlternateColorCodes('&', new String(temp, Charsets.UTF_8));
    }

    private static final NavigableMap<Double, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000d, AuctionMasterItemDisplay.plugin.getConfig().getString("units.first-unit"));
        suffixes.put(1_000_000d, AuctionMasterItemDisplay.plugin.getConfig().getString("units.second-unit"));
        suffixes.put(1_000_000_000d, AuctionMasterItemDisplay.plugin.getConfig().getString("units.third-unit"));
        suffixes.put(1_000_000_000_000d, AuctionMasterItemDisplay.plugin.getConfig().getString("units.forth-unit"));
    }

    public static String formatSignCoins(Double value) {
        if (value == Double.MIN_VALUE) return formatSignCoins(Double.MIN_VALUE + 1);
        if (value < 0) return "-" + formatSignCoins(-value);
        if (value < 1000) return Double.toString(Math.floor(value)).replace(".0", ""); //deal with easy case

        Map.Entry<Double, String> e = suffixes.floorEntry(value);
        Double divideBy = e.getKey();
        String suffix = e.getValue();

        double truncated = value / (divideBy / 10); //the number part of the output times 10
        //boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        //return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
        return new DecimalFormat("#.0").format(truncated/10).replace(".0", "")+suffix;
    }
}
