package me.onebone.actaeon.entity.animal;

import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class Sheep extends Animal{
	public static final int NETWORK_ID = 13;

	public Sheep(FullChunk chunk, CompoundTag nbt){
		super(chunk, nbt);
	}

	@Override
	public float getWidth(){
		return 0.9f;
	}

	@Override
	public float getHeight(){
		if (isBaby()){
			return 0.9f; // No have information
		}
		return 1.3f;
	}

	@Override
	public float getEyeHeight(){
		if (isBaby()){
			return 0.95f * 0.9f; // No have information
		}
		return 0.95f * getHeight();
	}

	@Override
	public String getName(){
		return this.getNameTag();
	}

	@Override
	public Item[] getDrops(){
		return new Item[]{Item.get(Item.WOOL)};
	}

	@Override
	public int getNetworkId(){
		return NETWORK_ID;
	}

	@Override
	public void initEntity(){
		super.initEntity();
		this.setMaxHealth(8);
	}
}
