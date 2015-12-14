package com.trent.awesomejumper.utils;

/**
 * Created by Sinthu on 13.12.2015.
 */
public class Message {
    private String txt;
    private float time;
    public Message(String txt, float time) {
        this.txt = txt;
        this.time = time;
    }


    public String getMessage() {
        return txt;
    }

    public float getTime() {
        return time;
    }


}
