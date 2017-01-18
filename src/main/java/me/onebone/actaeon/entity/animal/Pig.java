package me.onebone.actaeon.entity.animal;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.nbt.tag.CompoundTag;

public class Pig extends Animal implements EntityAgeable{
	public static final int NETWORK_ID = 12;

	public Pig(FullChunk chunk, CompoundTag nbt) {
		super(chunk, nbt);
	}

	@Override
	public float getWidth() {
		return 0.9f;
	}

	@Override
	public float getHeight() {
		if (isBaby()) {
			return 0.9f; // No have information
		}
		return 0.9f;
	}

	@Override
	public float getEyeHeight() {
		if (isBaby()) {
			return 0.9f; // No have information
		}
		return 0.9f;
	}

	@Override
	public boolean entityBaseTick(int tickDiff){
		if(!this.hasTarget()){
			Entity[] entities = this.level.getNearbyEntities(new AxisAlignedBB(this.x, this.y, this.z, this.x, this.y, this.z).expand(7, 7, 7));
			Entity near = null;

			for(Entity entity : entities){
				if(entity instanceof Player && (near == null || this.distance(near) < this.distance(entity))){
					if(((Player) entity).getInventory().getItemInHand().getId() == Item.CARROT){
						near = entity;
					}
				}
			}

			this.setTarget(near, "Pig");
		}

		return super.entityBaseTick(tickDiff);
	}

	public boolean hasTarget(){
		return super.hasFollowingTarget() && this.getTarget() instanceof Player && ((Player) this.getTarget()).getInventory().getItemInHand().getId() == Item.WHEAT;
	}

	@Override
	public String getName() {
		return this.getNameTag();
	}

	@Override
	public Item[] getDrops() {
		return new Item[]{Item.get(Item.RAW_PORKCHOP)};
	}

	@Override
	public int getNetworkId() {
		return NETWORK_ID;
	}

	@Override
	protected void initEntity() {
		super.initEntity();
		setMaxHealth(10);
	}

	@Override
	public boolean isBaby(){
		return false;
	}
}
