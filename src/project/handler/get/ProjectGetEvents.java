/*
 * This file is part of Project.
 *
 *  Project is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Project.  If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2014
 */

package project.handler.get;

import calliope.core.constants.Database;
import calliope.core.constants.JSONKeys;
import calliope.core.database.Connection;
import calliope.core.database.Connector;
import calliope.core.date.FuzzyDate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import project.exception.ProjectException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Arrays;

/**
 * Get a list of events for this project
 * @author desmond
 */
public class ProjectGetEvents
{
    static final HashMap<String,String> langMap;
    static 
    {
        langMap = new HashMap<>();
        langMap.put("italian","it");
        langMap.put("italiano","it");
        langMap.put("spanish","es");
        langMap.put("español","es");
        langMap.put("english","en");
    }
    /**
     * Get the language code
     * @param urn the docid starting with a language name
     * @return the 2-letter ISO language code
     */
    String getLangCode( String urn )
    {
        String[] parts = urn.split("/");
        if ( parts.length>0 )
        {
            if ( ProjectGetEvents.langMap.containsKey(parts[0]) )
                return ProjectGetEvents.langMap.get(parts[0]);
        }
        return "en";
    }
    private Locale localeFromDocid( String docid )
    {
        String[] parts = docid.split("/");
        if ( parts.length>0 )
            return new Locale(langMap.get(parts[0]));
        else
            return Locale.getDefault();
    }
    /**
     * Get a list of events with a given docid prefix
     * @param request the request
     * @param response the response
     * @param urn the docid prefix
     * @throws ProjectException 
     */
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
        {
            Connection conn = Connector.getConnection();
            // list documents *starting* with that urn
            String[] docids = conn.listDocuments(Database.EVENTS, urn+".*", 
                JSONKeys._ID );
            JSONObject jDoc = new JSONObject();
            JSONArray jArray = new JSONArray();
            ArrayList<DateHolder> dates = new ArrayList<DateHolder>();
            for ( String docid : docids )
            {
                String jStr = conn.getFromDbByField(Database.EVENTS,
                    docid,JSONKeys._ID);
                if ( jStr != null )
                {
                    JSONObject jobj = (JSONObject)JSONValue.parse( jStr );
                    if ( jobj != null && ((JSONObject)jobj).get("date")!= null )
                    {
                        FuzzyDate fd = FuzzyDate.fromJSON(
                            (JSONObject)jobj.get("date"),
                            localeFromDocid((String)jobj.get(JSONKeys.DOCID)));
                        dates.add( new DateHolder(fd,jobj) );
                    }
                }
            }
            // sort the jArray by fuzzydate
            DateHolder[] dhArray = new DateHolder[dates.size()];
            dates.toArray( dhArray );
            Arrays.sort( dhArray );
            for ( int i=0;i<dhArray.length;i++ )
                jArray.add( dhArray[i].jobj );
            jDoc.put( JSONKeys.NEVENTS, jArray );
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().println(jDoc.toJSONString()); 
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
