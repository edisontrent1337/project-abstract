package com.trent.awesomejumper.engine.entity;

import com.trent.awesomejumper.engine.modelcomponents.Body;
import com.trent.awesomejumper.engine.modelcomponents.Health;

/**
 * Created by Sinthu on 24.08.2016.
 */
public interface ILivingEntity {

    Body getBody();
    Health getHealth();

    /**
     * TODO: specifiy api for living entities.
     */

}
