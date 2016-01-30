/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package project.handler.get.timeline;

import org.json.simple.JSONObject;
import calliope.core.date.FuzzyDate;
import java.text.DateFormatSymbols;
import java.util.Locale;
/**
 *
 * @author desmond
 */
public class Event implements Comparable<Event>
{
    FuzzyDate date;
    String title;
    String description;
    int itemType;
    /**
     * Get a locale specific month name
     * @param month the 0-based month value
     * @param locale the locale
     * @return the name of the month
     */
    public String getMonth(int month, Locale locale) 
    { 
        return DateFormatSymbols.getInstance(locale).getMonths()[month]; 
    } 
    public Event( String title, JSONObject dateObj, String description,
       int itemType, Locale locale )
    {
        this.description = description;
        this.title = title;
        this.itemType = itemType;
        StringBuilder spec= new StringBuilder();
        String qualifier = (String)dateObj.get("qualifier");
        Number dayNum = ((Number)dateObj.get("day"));
        int day = (dayNum==null)?0:dayNum.intValue();
        if ( qualifier != null && !qualifier.equals("none") )
        {
            spec.append((String)dateObj.get("qualifier"));
            spec.append(" ");
        }
        if ( dayNum != null && day > 0 && day < 32 )
        {
            spec.append(new Integer(day).toString());
            spec.append(" ");
        }
        if ( dateObj.get("month") != null )
        {
            int m = ((Number)dateObj.get("month")).intValue();
            String mName = "";
            if ( m > -1 && m < 12 )
                mName = getMonth( m, locale );
            if ( mName.length()>0 )
            {
                spec.append(mName);
                spec.append(" ");
            }
        }
        if ( dateObj.get("year") != null )
        {
            Number yearNum = (Number)dateObj.get("year");
            
            spec.append(new Integer(yearNum.intValue()).toString());
        }
        this.date = new FuzzyDate( spec.toString(), locale );
        if ( this.date.getYear() == 0 )
            System.out.println("Year is 0!");
    }
    JSONObject toJSONObject()
    {
        JSONObject obj = new JSONObject();
        obj.put("start_date", date.toJSONObject() );
        obj.put("end_date", date.toJSONObject());
        JSONObject textObj = new JSONObject();
        textObj.put("headline", title);
        if ( description == null || description.length()==0 )
            textObj.put("text","no description");
        else
            textObj.put("text",description);
        obj.put( "text", textObj );
        return obj;
    }
    /**
     * Compare events for sorting by their start dates
     * @param e the other event
     * @return 1 if we are greater than e, -1 is less else 0
     */
    public int compareTo(Event e)
    {
        return this.date.compareTo(e.date);
    }
}
