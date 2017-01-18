package me.onebone.actaeon;

import cn.nukkit.entity.Entity;
import cn.nukkit.plugin.PluginBase;
import me.onebone.actaeon.entity.MovingEntity;
import me.onebone.actaeon.entity.animal.Chicken;
import me.onebone.actaeon.entity.animal.Cow;
import me.onebone.actaeon.entity.animal.Pig;
import me.onebone.actaeon.entity.animal.Sheep;

public class Actaeon extends PluginBase{
	public void onEnable(){
		this.saveDefaultConfig();

		this.registerEntity("Sheep", Sheep.class);
		this.registerEntity("Cow", Cow.class);
		this.registerEntity("Chicken", Chicken.class);
		this.registerEntity("Pig", Pig.class);
	}

	private void registerEntity(String name, Class<? extends MovingEntity> clazz){
		Entity.registerEntity(name, clazz, true);
	}
}
