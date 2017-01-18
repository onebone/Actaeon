package me.onebone.actaeon.entity.animal;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.nbt.tag.CompoundTag;

public class Cow extends Animal implements EntityAgeable{
	public static final int NETWORK_ID = 11;

	public Cow(FullChunk chunk, CompoundTag nbt){
		super(chunk, nbt);
	}

	@Override
	public int getNetworkId(){
		return NETWORK_ID;
	}

	@Override
	public float getWidth(){
		return 0.9f;
	}

	@Override
	public float getHeight(){
		if (isBaby()) {
			return 0.65f;
		}
		return 1.3f;
	}

	@Override
	public float getEyeHeight(){
		if (isBaby()){
			return 0.65f;
		}
		return 1.2f;
	}

	@Override
	public boolean isBaby(){
		return false;
	}

	@Override
	public Item[] getDrops(){
		return new Item[]{Item.get(Item.LEATHER), Item.get(Item.RAW_BEEF)};
	}

	@Override
	public boolean entityBaseTick(int tickDiff){
		if(!this.hasTarget()){
			Entity[] entities = this.level.getNearbyEntities(new AxisAlignedBB(this.x, this.y, this.z, this.x, this.y, this.z).expand(7, 7, 7));
			Entity near = null;

			for(Entity entity : entities){
				if(entity instanceof Player && (near == null || this.distance(near) < this.distance(entity))){
					if(((Player) entity).getInventory().getItemInHand().getId() == Item.WHEAT){
						near = entity;
					}
				}
			}

			this.setTarget(near, "Cow");
		}

		return super.entityBaseTick(tickDiff);
	}

	public boolean hasTarget(){
		return super.hasFollowingTarget() && this.getTarget() instanceof Player && ((Player) this.getTarget()).getInventory().getItemInHand().getId() == Item.WHEAT;
	}

	@Override
	protected void initEntity(){
		super.initEntity();
		setMaxHealth(10);
	}
}
