package me.onebone.actaeon.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.timings.Timings;

abstract public class MovingEntity extends EntityCreature{
	private boolean isKnockback = false;

	public MovingEntity(FullChunk chunk, CompoundTag nbt){
		super(chunk, nbt);
	}

	@Override
	public boolean onUpdate(int currentTick){
		if(this.closed){
			return false;
		}

		if(!this.onGround){
			this.motionY -= this.getGravity();
		}else if(this.isKnockback){
			this.motionX = this.motionZ = 0;
			this.isKnockback = false;
		}

		this.motionX *= (1 - this.getDrag());
		this.motionZ *= (1 - this.getDrag());

		this.move(this.motionX, this.motionY, this.motionZ);

		return super.onUpdate(currentTick);
	}

	@Override
	public void initEntity(){
		super.initEntity();

		this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_NO_AI);
	}

	@Override
	public void knockBack(Entity attacker, double damage, double x, double z, double base){
		this.isKnockback = true;
		this.onGround = false;

		super.knockBack(attacker, damage, x, z, base);
	}

	@Override
	public boolean move(double dx, double dy, double dz) {
		if (dx == 0 && dz == 0 && dy == 0) {
			return true;
		}

		if (this.keepMovement) {
			this.boundingBox.offset(dx, dy, dz);
			this.setPosition(this.temporalVector.setComponents((this.boundingBox.minX + this.boundingBox.maxX) / 2, this.boundingBox.minY, (this.boundingBox.minZ + this.boundingBox.maxZ) / 2));
			this.onGround = this.isPlayer;
			return true;
		} else {

			Timings.entityMoveTimer.startTiming();

			this.ySize *= 0.4;

			double movX = dx;
			double movY = dy;
			double movZ = dz;

			AxisAlignedBB axisalignedbb = this.boundingBox.clone();

			AxisAlignedBB[] list = this.level.getCollisionCubes(this, this.level.getTickRate() > 1 ? this.boundingBox.getOffsetBoundingBox(dx, dy, dz) : this.boundingBox.addCoord(dx, dy, dz), false);

			for (AxisAlignedBB bb : list) {
				 dy = bb.calculateYOffset(this.boundingBox, dy);
			}

			this.boundingBox.offset(0, dy, 0);

			boolean fallingFlag = (this.onGround || (dy != movY && movY < 0));

			for (AxisAlignedBB bb : list) {
				dx = bb.calculateXOffset(this.boundingBox, dx);
			}

			this.boundingBox.offset(dx, 0, 0);

			for (AxisAlignedBB bb : list) {
				dz = bb.calculateZOffset(this.boundingBox, dz);
			}

			this.boundingBox.offset(0, 0, dz);

			if (this.getStepHeight() > 0 && fallingFlag && this.ySize < 0.05 && (movX != dx || movZ != dz)) {
				double cx = dx;
				double cy = dy;
				double cz = dz;
				dx = movX;
				dy = this.getStepHeight();
				dz = movZ;

				AxisAlignedBB axisalignedbb1 = this.boundingBox.clone();

				this.boundingBox.setBB(axisalignedbb);

				list = this.level.getCollisionCubes(this, this.boundingBox.addCoord(dx, dy, dz), false);

				for (AxisAlignedBB bb : list) {
					dy = bb.calculateYOffset(this.boundingBox, dy);
				}

				this.boundingBox.offset(0, dy, 0);

				for (AxisAlignedBB bb : list) {
					dx = bb.calculateXOffset(this.boundingBox, dx);
				}

				this.boundingBox.offset(dx, 0, 0);

				for (AxisAlignedBB bb : list) {
					dz = bb.calculateZOffset(this.boundingBox, dz);
				}

				this.boundingBox.offset(0, 0, dz);

				this.boundingBox.offset(0, 0, dz);

				if ((cx * cx + cz * cz) >= (dx * dx + dz * dz)) {
					dx = cx;
					dy = cy;
					dz = cz;
					this.boundingBox.setBB(axisalignedbb1);
				} else {
					this.ySize += 0.5;
				}

			}

			this.x = (this.boundingBox.minX + this.boundingBox.maxX) / 2;
			this.y = this.boundingBox.minY - this.ySize - this.getHeight();
			this.z = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2;

			this.checkChunks();

			this.checkGroundState(movX, movY, movZ, dx, dy, dz);
			this.updateFallState(this.onGround);

			if (movX != dx) {
				this.motionX = 0;
			}

			if (movY != dy) {
				this.motionY = 0;
			}

			if (movZ != dz) {
				this.motionZ = 0;
			}


			//TODO: vehicle collision events (first we need to spawn them!)
			Timings.entityMoveTimer.stopTiming();
			return true;
		}
	}
}
