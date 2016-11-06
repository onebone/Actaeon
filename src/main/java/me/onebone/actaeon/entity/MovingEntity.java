package me.onebone.actaeon.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.timings.Timings;
import me.onebone.actaeon.route.Node;
import me.onebone.actaeon.route.RouteFinter;
import me.onebone.actaeon.route.SimpleRouteFinder;

abstract public class MovingEntity extends EntityCreature{
	private boolean isKnockback = false;
	private RouteFinter route = null;

	protected int lastRouteUpdate = 0;

	public MovingEntity(FullChunk chunk, CompoundTag nbt){
		super(chunk, nbt);

		this.route = new SimpleRouteFinder(); // TODO Improve route finder
	}

	public void jump(){
		if(this.onGround){
			this.motionY = 0.5;
		}
	}

	@Override
	public boolean onUpdate(int currentTick){
		if(this.closed){
			return false;
		}

		AxisAlignedBB[] list = this.level.getCollisionCubes(this, this.level.getTickRate() > 1 ? this.boundingBox.getOffsetBoundingBox(0, -1, 0) : this.boundingBox.addCoord(0, -1, 0), false);

		double maxY = 0;
		for(AxisAlignedBB bb : list){
			if(bb.maxY > maxY){
				maxY = bb.maxY;
			}
		}

		this.onGround = (maxY == this.boundingBox.minY);

		if(!this.onGround){
			this.motionY -= this.getGravity();
		}

		if(this.isKnockback){                   // knockback 이 true 인 경우는 맞은 직후
			this.isKnockback = false;           // 다음으로 땅에 닿을 때 knockback 으로 인한 움직임을 멈춘다.
		}else if(this.onGround){
			this.motionX = this.motionZ = 0;
		}

		this.motionX *= (1 - this.getDrag());
		this.motionZ *= (1 - this.getDrag());

		if(this.onGround){
			if(!this.route.isSearching()){
				Entity[] entities = this.level.getNearbyEntities(new AxisAlignedBB(this.x, this.y, this.z, this.x, this.y, this.z).expand(7, 7, 7));
				Entity near = null;
				for(Entity entity : entities){
					if(near == null || this.distance(near) < this.distance(entity)){
						near = entity;
					}
				}

				if(near != null){
					this.route.setDestination(near);
					if(this.route.search()){
						Node node = this.route.get();

						double speed = this.getMovementSpeed();

						// TODO motionX motionZ
					}else{
						// TODO get path later
					}
				}
			}
		}

		this.move(this.motionX, this.motionY, this.motionZ);

		return super.onUpdate(currentTick);
	}

	@Override
	protected void checkGroundState(double movX, double movY, double movZ, double dx, double dy, double dz) {
		this.isCollidedVertically = movY != dy;
		this.isCollidedHorizontally = (movX != dx || movZ != dz);
		this.isCollided = (this.isCollidedHorizontally || this.isCollidedVertically);

		// this.onGround = (movY != dy && movY < 0);
		// onGround 는 onUpdate 에서 확인
	}

	@Override
	public void initEntity(){
		super.initEntity();

		this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_NO_AI);
	}

	@Override
	public void knockBack(Entity attacker, double damage, double x, double z, double base){
		this.isKnockback = true;

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
