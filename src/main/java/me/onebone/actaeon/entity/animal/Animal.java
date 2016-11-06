package me.onebone.actaeon.entity.animal;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import me.onebone.actaeon.entity.MovingEntity;

abstract public class Animal extends MovingEntity implements EntityAgeable{
	public Animal(FullChunk chunk, CompoundTag nbt){
		super(chunk, nbt);
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
}
