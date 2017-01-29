package me.onebone.actaeon.entity;

import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import me.onebone.actaeon.entity.animal.Animal;

/**
 * Created by CreeperFace on 29. 1. 2017.
 */
public abstract class Tameable extends Animal {

    public Tameable(FullChunk chunk, CompoundTag nbt){
        super(chunk, nbt);
    }
}
