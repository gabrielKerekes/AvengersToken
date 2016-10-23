package com.avengers.avengerstoken;

import java.io.Serializable;

/**
 * Created by Robert on 5/4/2016.
 */
public class Item implements Serializable{
    public String transText;
    public String dateTime;
    public boolean state;
    public String from;

    Item(String trans) {
        String[] splitted = trans.split("#");

        transText = splitted[0];
        dateTime = splitted[1];
        from = splitted[2];

        if(splitted[3].equals("1")) state = true;
        else state = false;
    }
}
