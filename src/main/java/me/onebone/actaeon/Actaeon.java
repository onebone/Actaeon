package me.onebone.actaeon;

import cn.nukkit.entity.Entity;
import cn.nukkit.plugin.PluginBase;
import me.onebone.actaeon.entity.MovingEntity;
import me.onebone.actaeon.entity.animal.Sheep;

public class Actaeon extends PluginBase{
	public void onEnable(){
		this.saveDefaultConfig();

		this.registerEntity(Sheep.class);
	}

	private void registerEntity(Class<? extends MovingEntity> clazz){
		Entity.registerEntity(clazz.getSimpleName(), clazz, true);
	}
}
