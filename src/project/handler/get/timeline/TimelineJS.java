/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package project.handler.get.timeline;

import java.util.ArrayList;
import org.json.simple.JSONObject;
import org.json.simple.*;
import java.util.Arrays;
import java.util.Locale;

/**
 * A revised timeline js converter for the new event-type data
 * @author desmond
 */
public class TimelineJS  extends JSONObject
{
    String[] events;
    String title;
    ArrayList<Event> items;
    JSONObject tlTitle;
    public TimelineJS( String title, String subtitle, EventType et, 
        String[] events, String langCode )
    {
        this.events = events;
        this.title = title;
        tlTitle = new JSONObject();
        this.put( "title", tlTitle );
        this.items = new ArrayList<Event>();
        JSONObject tlText = new JSONObject();
        tlTitle.put("text", tlText );
        // prevent resorting of equal dates 
        tlText.put("headline",title);
        tlText.put("text",subtitle);
        Locale locale = new Locale( langCode );
        //System.out.println("Fetching events of type "+et.toString());
        for ( int i=0;i<events.length;i++ )
        {
            JSONObject item = (JSONObject)JSONValue.parse(events[i]);
            String eventTitle = (String)item.get("title");
            String link = (String)item.get("link");
            if ( link != null )
                eventTitle = "<a href=\""+link+"\">"+eventTitle+"</a>";
            JSONObject eventDate = (JSONObject)item.get("date");
            String eventDescription = (String)item.get("description");
            int intVal = ((Number)item.get("type")).intValue();
            EventType eventType = EventType.fromInt(intVal);
            if ( et == EventType.all || et == eventType )
            {
                Event ne = new Event( eventTitle, eventDate, 
                    eventDescription, eventType.toInt(), locale );
                items.add( ne );
            }
        }
        //System.out.println("Found "+items.size()+" events");
    }
    public String toJSONString()
    {
        sort();
        return super.toJSONString();
    }
    public void sort()
    {
        Event[] arr = new Event[items.size()];
        items.toArray(arr);
        Arrays.sort( arr );
        JSONArray dates = new JSONArray();
        for ( int i=0;i<arr.length;i++ )
        {
            dates.add( arr[i].toJSONObject() );
        }
        //if ( arr.length > 0 )
        //    timeline.put("startDate", arr[0].startDate.toCommaSep());
        this.put( "events", dates );
    }
}
