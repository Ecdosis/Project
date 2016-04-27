/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.handler.get.timeline;

/**
 * Type of time-related events
 * @author desmond
 */
public enum EventType 
{
    biography,
    composition,
    letter,
    location,
    other,
    all;
    public static EventType fromInt( int value )
    {
        switch ( value )
        {
            case 0:
                return biography;
            case 1:
                return composition;
            case 2:
                return letter;
            case 3:
                return location;
            case 4:
                return other;
            case 5: default:
                return all;
        }
    }
    public int toInt()
    {
        switch (this)
        {
            case biography:
                return 0;
            case composition:
                return 1;
            case letter:
                return 2;
            case location:
                return 3;
            case other:
                return 4;
            case all: default:
                return 4;
        }
    }
}
