package me.onebone.actaeon.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import me.onebone.actaeon.route.AdvancedRouteFinder;
import me.onebone.actaeon.route.Node;
import me.onebone.actaeon.route.RouteFinder;

abstract public class MovingEntity extends EntityCreature{
	private boolean isKnockback = false;
	private RouteFinder route = null;
	private Vector3 target = null;
	private String targetSetter = "";

	public MovingEntity(FullChunk chunk, CompoundTag nbt){
		super(chunk, nbt);

		this.route = new AdvancedRouteFinder(this);
	}

	public void jump(){
		if(this.onGround){
			this.motionY = 0.42;
		}
	}

	@Override
	public boolean entityBaseTick(int tickDiff){
		if(this.closed){
			return false;
		}

		boolean hasUpdate = super.entityBaseTick(tickDiff);

		if(this.isKnockback){                   // knockback 이 true 인 경우는 맞은 직후
			this.isKnockback = false;           // 다음으로 땅에 닿을 때 knockback 으로 인한 움직임을 멈춘다.
		}else if(this.onGround){
			this.motionX = this.motionZ = 0;
		}

		this.motionX *= (1 - this.getDrag());
		this.motionZ *= (1 - this.getDrag());

		if(this.onGround && this.hasSetTarget() && (this.route.getDestination() == null || this.route.getDestination().distance(this.getTarget()) > 2)){ // 대상이 이동함
			this.route.setPositions(this.level, this, this.getTarget(), this.boundingBox);

			if(this.route.isSearching()) this.route.research();
			else this.route.search();

			hasUpdate = true;
		}

		if(!this.route.isSearching() && this.route.isSuccess() && this.route.hasRoute()){ // entity has route to go
			hasUpdate = true;

			Node node = this.route.get();
			if(node != null){
				Vector3 vec = node.getVector3();
				double diffX = Math.pow(vec.x - this.x, 2);
				double diffZ = Math.pow(vec.z - this.z, 2);

				if(diffX + diffZ == 0){
					if(!this.route.next()){
						this.route.arrived();
					}
				}else{
					int negX = vec.x - this.x < 0 ? -1 : 1;
					int negZ = vec.z - this.z < 0 ? -1 : 1;

					this.motionX = Math.min(Math.abs(vec.x - this.x), diffX / (diffX + diffZ) * this.getMovementSpeed()) * negX;
					this.motionZ = Math.min(Math.abs(vec.z - this.z), diffZ / (diffX + diffZ) * this.getMovementSpeed()) * negZ;
					double angle = Math.atan2(vec.z - this.z, vec.x - this.x);
					this.yaw = (float) ((angle * 180) / Math.PI) - 90;
				}
			}
		}

		if((this.motionX != 0 || this.motionZ != 0) && this.isCollidedHorizontally){
			this.jump();
		}
		this.move(this.motionX, this.motionY, this.motionZ);

		this.checkGround();
		if(!this.onGround){
			this.motionY -= this.getGravity();
			hasUpdate = true;
		}

		return hasUpdate;
	}

	public double getRange(){
		return 30.0;
	}

	public void setTarget(Vector3 vec, String identifier){
		this.setTarget(vec, identifier, false);
	}

	public void setTarget(Vector3 vec, String identifier, boolean forceSearch){
		if(identifier == null) return;

		if(forceSearch || !this.hasSetTarget() || identifier.equals(this.targetSetter)){
			this.target = vec;

			this.targetSetter = identifier;
		}

		if(this.hasSetTarget() && (forceSearch || !this.route.hasRoute())){
			this.route.setPositions(this.level, this, this.target, this.boundingBox.clone());
			if(this.route.isSearching()) this.route.research();
			else this.route.search();
		}
	}

	public Vector3 getTarget(){
		return new Vector3(this.target.x, this.target.y, this.target.z);
	}

	/**
	 * Returns whether the entity has following target
	 * Entity will try to move to position where target exists
	 */
	public boolean hasFollowingTarget(){
		return this.route.getDestination() != null && this.target != null && this.distance(this.target) < this.getRange();
	}

	/**
	 * Returns whether the entity has set its target
	 * The entity may not follow the target if there is following target and set target is different
	 * If following distance of target is too far to follow or cannot reach, set target will be the next following target
	 */
	public boolean hasSetTarget(){
		return this.target != null && this.distance(this.target) < this.getRange();
	}

	@Override
	protected void checkGroundState(double movX, double movY, double movZ, double dx, double dy, double dz) {
		this.isCollidedVertically = movY != dy;
		this.isCollidedHorizontally = (movX != dx || movZ != dz);
		this.isCollided = (this.isCollidedHorizontally || this.isCollidedVertically);

		// this.onGround = (movY != dy && movY < 0);
		// onGround 는 onUpdate 에서 확인
	}

	private void checkGround(){
		AxisAlignedBB[] list = this.level.getCollisionCubes(this, this.level.getTickRate() > 1 ? this.boundingBox.getOffsetBoundingBox(0, -1, 0) : this.boundingBox.addCoord(0, -1, 0), false);

		double maxY = 0;
		for(AxisAlignedBB bb : list){
			if(bb.maxY > maxY){
				maxY = bb.maxY;
			}
		}

		this.onGround = (maxY == this.boundingBox.minY);
	}

	@Override
	protected void initEntity(){
		super.initEntity();

		this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_NO_AI);
	}

	@Override
	public void knockBack(Entity attacker, double damage, double x, double z, double base){
		this.isKnockback = true;

		super.knockBack(attacker, damage, x, z, base);
	}

	@Override
	public void addMovement(double x, double y, double z, double yaw, double pitch, double headYaw) {
		this.level.addEntityMovement(this.chunk.getX(), this.chunk.getZ(), this.id, x, y - this.getEyeHeight(), z, yaw, pitch, headYaw);
	}
}
