package com.trent.awesomejumper.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.trent.awesomejumper.engine.entity.Entity;
import com.trent.awesomejumper.game.AwesomeJumperMain;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		Entity.entityCount = 0;
		new LwjglApplication(new AwesomeJumperMain(), config);
	}
}
