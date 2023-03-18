package de.Ste3et_C0st.FurnitureLib.SchematicLoader;

import de.Ste3et_C0st.FurnitureLib.NBT.CraftItemStack;
import de.Ste3et_C0st.FurnitureLib.NBT.NBTCompressedStreamTools;
import de.Ste3et_C0st.FurnitureLib.NBT.NBTTagCompound;
import de.Ste3et_C0st.FurnitureLib.Utilitis.config;
import de.Ste3et_C0st.FurnitureLib.main.FurnitureLib;
import de.Ste3et_C0st.FurnitureLib.main.ObjectID;
import de.Ste3et_C0st.FurnitureLib.main.entity.fContainerEntity;
import de.Ste3et_C0st.FurnitureLib.main.entity.fEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.migcomponents.migbase64.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectInventory implements Listener {

    private Inventory inv;
    private Player player;
    private ObjectID id;
    private List<fEntity> entityList = new ArrayList<>();

    public ProjectInventory(int slots, ObjectID id) {
        inv = Bukkit.createInventory(null, slots, id.getProjectOBJ().getCraftingFile().getRecipe().getResult().getItemMeta().getDisplayName());
        this.id = id;
        Bukkit.getPluginManager().registerEvents(this, id.getProjectOBJ().getPlugin());
    }

    public Player getPlayer() {
        return player;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (getPlayer() == null) return;
        if (getInv() == null) return;
        if (e.getInventory().equals(inv)) {
            if (e.getPlayer().equals(getPlayer())) {
                this.player = null;
                config c = new config(FurnitureLib.getInstance());
                FileConfiguration file = c.getConfig(this.id.getSerial(), "/plugin/DiceEditor/metadata/");
                file.set("inventory", toString());
                c.saveConfig(this.id.getSerial(), file, "/plugin/DiceEditor/metadata/");

                if (!entityList.isEmpty()) {
                    for (fEntity entity : entityList) {
                        String name = entity.getName();
                        name = name.replace("OnInventoryCloseDisplayItem(", "");
                        name = name.replace(")", "");
                        String[] args = name.split(",");
                        int slot = Integer.parseInt(args[1]);
                        int entitySlot = Integer.parseInt(args[0]);
                        
                        if(entity instanceof fContainerEntity) fContainerEntity.class.cast(entity).getInventory().setSlot(entitySlot, inv.getItem(slot));
                    }
                    this.id.update();
                }
            }
        }
    }

    public void openInventory(Player player) {
        if (this.inv == null) return;
        this.player = player;
        getPlayer().openInventory(inv);
    }

    public Inventory getInv() {
        return inv;
    }

    public List<HumanEntity> getViewers(){
    	List<HumanEntity> humans = new ArrayList<HumanEntity>();
    	if(Objects.nonNull(this.inv)) humans.addAll(this.inv.getViewers());
    	return humans;
    }
    
    public void load() {
        config c = new config(FurnitureLib.getInstance());
        FileConfiguration file = c.getConfig(this.id.getSerial(), "/plugin/DiceEditor/metadata/");
        if (file.isSet("inventory")) {
            setItems(file.getString("inventory"));
        }

        for (fEntity entities : this.id.getPacketList()) {
            if (entities.getName().startsWith("OnInventoryCloseDisplayItem")) {
                this.entityList.add(entities);
            }
        }
    }

    public String toString() {
        if (inv == null) return "";
        NBTTagCompound inventory = new NBTTagCompound();
        NBTTagCompound items = new NBTTagCompound();
        for (int i = 0; i < inv.getContents().length; i++) {
            if (inv.getContents()[i] == null || inv.getContents()[i].getType() == null || inv.getContents()[i].getType().equals(Material.AIR)) {
                items.setString(i + "", "NONE");
                continue;
            }
            try {
                items.set(i + "", new CraftItemStack().getNBTTag(inv.getContents()[i]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        inventory.setInt("size", getInv().getSize());
        inventory.set("inventory", items);
        return Base64.encodeToString(armorStandtoBytes(inventory), false);
    }

    public void setItems(String string) {
        byte[] by = Base64.decodeFast(string);
        ByteArrayInputStream bin = new ByteArrayInputStream(by);
        try {
            NBTTagCompound compound = NBTCompressedStreamTools.read(bin);
            NBTTagCompound inventory = compound.getCompound("inventory");
            int size = compound.getInt("size");
            this.inv = Bukkit.createInventory(null, size, id.getProjectOBJ().getCraftingFile().getRecipe().getResult().getItemMeta().getDisplayName());
            for (int i = 0; i < size; i++) {
                if (!inventory.getString(i + "").equalsIgnoreCase("NONE")) {
                    ItemStack is = new CraftItemStack().getItemStack(inventory.getCompound(i + ""));
                    this.inv.setItem(i, is);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] armorStandtoBytes(NBTTagCompound compound) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            NBTCompressedStreamTools.write(compound, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
        return out.toByteArray();
    }
}
