package me.onebone.actaeon.entity;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import me.onebone.actaeon.route.AdvancedRouteFinder;
import me.onebone.actaeon.route.RouteFinder;

abstract public class MovingEntity extends EntityCreature{
	private boolean isKnockback = false;
	private RouteFinder route = null;
	private Vector3 target = null;

	private double[] expected = new double[3];
	private boolean firstMove = true;

	protected int lastRouteUpdate = 0;

	public MovingEntity(FullChunk chunk, CompoundTag nbt){
		super(chunk, nbt);

		this.route = new AdvancedRouteFinder();
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

		if(this.isCollidedHorizontally){
			this.jump();
		}

		if(!this.route.isSearching() && this.route.isSuccess() && this.route.hasRoute()){
			if(this.route.hasReachedNode(this)){
				if(!this.route.next()){
					this.route.arrived();
					this.firstMove = true;
					return hasUpdate;
				}
			}


			if(!this.firstMove){
				if(this.expected[0] != this.x || this.expected[1] != this.y || this.expected[2] != this.z){ // 장애물을 만났거나 어떤 이유로 다른 곳으로 이동된 경우
					this.route.setStart(this); // 현재의 위치가 바뀌어 있음
					this.firstMove = true;

					return hasUpdate;
				}
			}
			this.firstMove = false;

			Vector3 node = this.route.get().getNode();

			double speed = this.getMovementSpeed();

			double total = Math.abs(node.z - this.z) + Math.abs(node.x - this.x);

			this.motionX = Math.abs(speed * (node.x - this.x) / total) < Math.abs(node.x - this.x) ? speed * (node.x - this.x) / total : node.x - this.x;
			this.motionZ = Math.abs(speed * (node.z - this.z) / total) < Math.abs(node.z - this.z) ? speed * (node.z - this.z) / total : node.z - this.z;

			this.expected = new double[]{this.x + this.motionX, this.y + this.motionY, this.z + this.motionZ};

			double angle = Math.atan2(node.z - this.z, node.x - this.x);
			this.yaw = (float) ((angle * 180) / Math.PI) - 90;

			hasUpdate = true;
		}else if(this.route.isSearching()) this.route.search();

		if(!this.onGround){
			this.motionY -= this.getGravity();
		}

		this.move(this.motionX, this.motionY, this.motionZ);

		this.checkGround();
		if(this.onGround){
			if(!this.route.isSearching()){
				Entity[] entities = this.level.getNearbyEntities(new AxisAlignedBB(this.x, this.y, this.z, this.x, this.y, this.z).expand(7, 7, 7));
				Entity near = null;

				if(this.target == null || this.target.distance(this) > 30){
						for(Entity entity : entities){
						if(entity instanceof Player && (near == null || this.distance(near) < this.distance(entity))){
							near = entity;
						}
					}
				}

				if(!this.hasTarget() && near != null){
					this.target = near;

					this.route.setPositions(this.level, this, near, this.boundingBox.clone());

					this.route.search();

					hasUpdate = true;
				}else if(this.hasTarget()){
					if(this.route.getDestination().distance(this.target) > 1.5){
						this.route.setPositions(this.level, this, this.target, this.boundingBox.clone());

						this.route.search();

						hasUpdate = true;
					}else if(this.distance(this.target) > 30){ // 대상이 너무 멀리 있다면 따라가지 않는다
						this.target = null;
						this.route.arrived();
					}
				}

				this.firstMove = true;
			}
		}

		return hasUpdate;
	}

	private boolean hasTarget(){
		return this.route.getDestination() != null && this.target != null && this.distance(this.target) < 30;
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
	public void addMovement(double x, double y, double z, double yaw, double pitch, double headYaw) {
		this.level.addEntityMovement(this.chunk.getX(), this.chunk.getZ(), this.id, x, y - this.getHeight(), z, yaw, pitch, headYaw);
	}
}
