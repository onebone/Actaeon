package me.onebone.actaeon.entity.animal;

import cn.nukkit.Player;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockWool;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHumanType;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemDye;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.DyeColor;

import java.util.Random;

public class Sheep extends Animal {
	public static final int NETWORK_ID = 13;

	public boolean sheared = false;
	public int color = 0;

	private int sheepTimer;

	public Sheep(FullChunk chunk, CompoundTag nbt) {
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
		return 1.3f;
	}

	@Override
	public float getEyeHeight() {
		if (isBaby()) {
			return 0.95f * 0.9f; // No have information
		}
		return 0.95f * getHeight();
	}

	@Override
	public String getName() {
		return this.getNameTag();
	}

	@Override
	public Item[] getDrops() {
		return new Item[]{new ItemBlock(new BlockWool(getColor()))};
	}

	@Override
	public int getNetworkId() {
		return NETWORK_ID;
	}

	@Override
	public boolean entityBaseTick(int tickDiff) {
		if (!this.hasTarget()) {
			Entity[] entities = this.level.getNearbyEntities(new AxisAlignedBB(this.x, this.y, this.z, this.x, this.y, this.z).expand(7, 7, 7));
			Entity near = null;

			for (Entity entity : entities) {
				if (entity instanceof Player && (near == null || this.distance(near) < this.distance(entity))) {
					if (((Player) entity).getInventory().getItemInHand().getId() == Item.WHEAT) {
						near = entity;
					}
				}
			}

			this.setTarget(near, "Sheep");
		}

		return super.entityBaseTick(tickDiff);
	}

	public boolean hasTarget() {
		return super.hasFollowingTarget() && this.getTarget() instanceof Player && ((Player) this.getTarget()).getInventory().getItemInHand().getId() == Item.WHEAT;
	}

	@Override
	protected void initEntity() {
		super.initEntity();
		this.setMaxHealth(8);

		if (!this.namedTag.contains("Color")) {
			this.setColor(getRandomSheepColor().getWoolData());
		} else {
			this.setColor(this.namedTag.getByte("Color"));
		}

		if (!this.namedTag.contains("Sheared")) {
			this.namedTag.putByte("Sheared", 0);
		} else {
			this.sheared = this.namedTag.getBoolean("Sheared");
		}

		this.setDataFlag(DATA_FLAGS, DATA_FLAG_SHEARED, this.sheared);
	}

	@Override
	public boolean onInteract(Player player, Item item) {
		if (item.getId() == Item.DYE) {
			this.setColor(((ItemDye) item).getDyeColor().getWoolData());
			return true;
		}

		if (item.getId() != Item.SHEARS) {
			return super.onInteract(player, item);
		}

		return shear();
	}

	public boolean shear() {
		if (sheared) {
			return false;
		}

		this.setSheared(true);
		this.level.dropItem(this, new ItemBlock(new BlockWool(this.getColor()), 0, this.level.rand.nextInt(2) + 1));
		return true;
	}

	public void setSheared(boolean value) {
		this.sheared = value;
		this.setDataFlag(DATA_FLAGS, DATA_FLAG_SHEARED, value);
	}

	public void setColor(int color) {
		this.color = color;
		this.setDataProperty(new ByteEntityData(DATA_COLOUR, color));
	}

	public int getColor() {
		return this.color;
	}

	public static DyeColor getRandomSheepColor() {
		Random random = new Random();
		int i = random.nextInt(100);
		return i < 5 ? DyeColor.BLACK : (i < 10 ? DyeColor.LIGHT_GRAY : (i < 15 ? DyeColor.GRAY : (i < 18 ? DyeColor.BROWN : (random.nextInt(500) == 0 ? DyeColor.PINK : DyeColor.WHITE))));
	}
}
