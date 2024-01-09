package de.Ste3et_C0st.FurnitureLib.NBT;

import de.Ste3et_C0st.FurnitureLib.NBT.BlockDataReader.BlockDataConverter1_20;
import de.Ste3et_C0st.FurnitureLib.NBT.BlockDataReader.BlockDataReader;
import de.Ste3et_C0st.FurnitureLib.main.FurnitureLib;

import org.bukkit.block.data.BlockData;
import com.comphenix.protocol.utility.MinecraftVersion;

import java.util.Objects;
import java.util.Optional;

public class CraftBlockData {

	private final static BlockDataReader reader;
	
	//test
	
	static {
		if(FurnitureLib.getVersion(new MinecraftVersion("1.20.3"))) {
			reader = new BlockDataConverter1_20();
		}else {
			reader = null;
		}
	}
	
	public Optional<BlockData> read(NBTTagCompound compound){
		if(Objects.isNull(reader)) return Optional.empty();
		return reader.read(compound);
	}
}