package me.onebone.actaeon.entity.animal;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.HeartParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import me.onebone.actaeon.entity.Ageable;
import me.onebone.actaeon.entity.MovingEntity;

import java.util.Random;

abstract public class Animal extends Ageable implements EntityAgeable{

	public int inLove = 0;
	public Player playerInLove = null;

	public Animal(FullChunk chunk, CompoundTag nbt){
		super(chunk, nbt);
	}

	@Override
	protected void initEntity() {
		super.initEntity();

		this.inLove = this.namedTag.getInt("InLove");
	}

	@Override
	public void saveNBT() {
		super.saveNBT();

		this.namedTag.putInt("InLove", this.inLove);
	}

	@Override
	public boolean onUpdate(int currentTick) {
		boolean hasUpdate = super.onUpdate(currentTick);

		if(inLove > 0) {
			hasUpdate = true;

			if(inLove % 10 == 0) {
				Random rand = this.level.rand;
				this.level.addParticle(new HeartParticle(new Vector3(this.x + (rand.nextFloat() * this.getWidth() * 2.0F) - this.getWidth(), this.y + 0.5D + (rand.nextFloat() * this.getHeight()), this.z + (rand.nextFloat() * this.getWidth() * 2.0F) - this.getWidth())));
			}

			inLove--;
		}

		return hasUpdate;
	}

	@Override
	public boolean isBaby(){
		return this.getDataFlag(DATA_FLAGS, Entity.DATA_FLAG_BABY);
	}

	@Override
	public void spawnTo(Player player){
		AddEntityPacket pk = new AddEntityPacket();
		pk.type = this.getNetworkId();
		pk.entityUniqueId = this.getId();
		pk.entityRuntimeId = this.getId();
		pk.x = (float) this.x;
		pk.y = (float) this.y;
		pk.z = (float) this.z;
		pk.speedX = (float) this.motionX;
		pk.speedY = (float) this.motionY;
		pk.speedZ = (float) this.motionZ;
		pk.metadata = this.dataProperties;
		player.dataPacket(pk);

		super.spawnTo(player);
	}

	public boolean isBreedingItem(Item item) {
		return item.getId() == Item.WHEAT;
	}

	public void setInLove(Player player)
	{
		this.inLove = 600;
		this.playerInLove = player;
		this.setDataFlag(DATA_FLAGS, DATA_FLAG_INLOVE);
	}

	@Override
	public boolean onInteract(Player player, Item item) {
		if(isBreedingItem(item)) {
			if(this.inLove <= 0) {
				setInLove(player);
			}

			return true;
		}

		return super.onInteract(player, item);
	}
}
