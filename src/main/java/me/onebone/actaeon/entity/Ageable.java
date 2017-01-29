package me.onebone.actaeon.entity;

import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * Created by CreeperFace on 29. 1. 2017.
 */
public abstract class Ageable extends MovingEntity implements EntityAgeable {

    public int forcedAge = 0;

    public Ageable(FullChunk chunk, CompoundTag nbt){
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        if(!this.namedTag.contains("Age")){
            this.namedTag.putInt("Age", -24000);
        }
        this.age = this.namedTag.getInt("Age");

        this.forcedAge = this.namedTag.getInt("ForcedAge");
    }

    @Override
    public boolean onUpdate(int currentTick) {
        boolean hasUpdate = super.onUpdate(currentTick);

        this.setGrowingAge(this.age);

        return hasUpdate;
    }

    @Override
    public boolean isBaby() {
        return this.age < 0;
    }

    public void setGrowingAge(int age) {
        this.age = age;

        this.setDataFlag(DATA_FLAGS, DATA_FLAG_BABY, age < 0);
        this.setScale(isBaby() ? 0.5f : 1f);
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putInt("Age", this.age);
        this.namedTag.putInt("ForcedAge", this.forcedAge);
    }
}
