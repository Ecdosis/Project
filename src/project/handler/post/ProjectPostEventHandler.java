/* This file is part of MML.
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
 */

package project.handler.post;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import project.exception.EventException;
import project.exception.ProjectException;
import calliope.core.Utils;
import calliope.core.database.Connector;
import calliope.core.database.Connection;
import calliope.core.constants.Database;
import calliope.core.constants.JSONKeys;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import project.constants.Service;

/**
 * Process the post event to update the project's event list
 * @author desmond
 */
public class ProjectPostEventHandler extends ProjectPostHandler
{
    /** all eventIDs for the given prefix (event+number)*/
    static HashMap<String,HashSet<String>> eventIDs;
    static int CACHE_SIZE = 20;
    /** a list of event+number ids that are free to use */
    static HashMap<String,ArrayList<String>> freeIDs;
    /**
     * Initialise the map for a particular docid
     * @param did the docid
     * @throws EventException 
     */
    private void init(String did) throws EventException
    {
        if ( eventIDs == null )
        {
            eventIDs = new HashMap<String,HashSet<String>>();
            freeIDs = new HashMap<String,ArrayList<String>>();
        }
        try
        {
            if ( !eventIDs.containsKey(did) )
            {
                Connection conn = Connector.getConnection();
                String[] docids = conn.listDocuments(Database.EVENTS,
                    did+".*","docid");
                HashSet map = new HashSet<String>();
                eventIDs.put( did, map );
                for ( int i=0;i<docids.length;i++ )
                {
                    if ( checkDocID(docids[i]) )
                    {
                        String[] parts = docids[i].split("/");
                        map.add( parts[parts.length-1] );
                    }
                }
            }
            if ( ( !freeIDs.containsKey(did)) )
                freeIDs.put(did,new ArrayList<String>());
        }
        catch ( Exception e )
        {
            throw new EventException(e);
        }
    }
    /**
     * Check that an event id is complete
     * @param did the id to check
     * @return true if it ends in event+number
     */
    boolean checkDocID( String did )
    {
        String[] parts = did.split("/");
        if ( parts.length > 0 )
        {
            if ( parts[parts.length-1].startsWith("event") )
            {
                String last = parts[parts.length-1].substring(5);
                try
                {
                    int value = Integer.parseInt(last);
                    return true;
                }
                catch ( Exception e )
                {
                }
            }
        }
        return false;
    }
    /**
     * Issue a new unique eventid
     * @param did the docid for this event, minus number at the end
     * @return a string starting with "event" and ending with a number
     */
    String uniqueEventID(String did) throws EventException
    {
        this.init(did);
        ArrayList free = freeIDs.get(did);
        if ( free.isEmpty() )
        {
            HashSet items = eventIDs.get(did);
            int limit = items.size()+CACHE_SIZE;
            int count = 0;
            for ( int i=1;i<=limit;i++ )
            {
                String key = "event"+i;
                if ( !items.contains(key) )
                {
                    free.add(key);
                    count++;
                    if ( count >= CACHE_SIZE )
                        break;
                }
            }
        }
        String uniqueID = (String)free.get(0);
        free.remove(0);
        return uniqueID;
    }
    /**
     * Check that the docID is complete and unique for an event
     * @param event the event we are about to add
     * @return the checked event
     */
    private String checkEvent( String event ) throws EventException
    {
        JSONObject evt = (JSONObject) JSONValue.parse( event );
        String did = (String) evt.get("docid");
        if ( !checkDocID(did) )
        {
            did += "/"+uniqueEventID(did);
            evt.put("docid",did);
        }
        return evt.toJSONString();
    }
    /**
     * Main hander function - start here
     * @param request the incoming http request
     * @param response the interim response object
     * @param urn the urn used for the post
     * @throws ProjectException 
     */
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
        {
            parseImportParams( request );
            String first = Utils.first(urn);
            if ( first.equals(Service.DELETE) && this._id !=null )
            {
                Connection conn = Connector.getConnection();
                String res = conn.getFromDbByField(Database.EVENTS, 
                    this._id, "_id" );
                if ( res != null )
                {
                    JSONObject jobj = (JSONObject)JSONValue.parse( res );
                    String did = (String)jobj.get("docid");
                    if ( did != null && checkDocID(did) )
                    {
                        String[] parts = did.split("/");
                        String sid = Utils.shortDocID(did);
                        this.init(sid);
                        ArrayList<String> list = freeIDs.get(sid);
                        list.add( parts[parts.length-1] );
                    }
                }
                conn.removeFromDbByField(
                    Database.EVENTS, JSONKeys._ID, this._id);
                System.out.println("deleted id="+this._id);
            }
            else if ( first.equals(Service.ADD) && event != null )
            { 
               event = checkEvent( event );
                String resp = Connector.getConnection().addToDb( 
                    Database.EVENTS, event );
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println(resp); 
            }
            else if ( first.equals(Service.UPDATE) )
            {
                event = checkEvent(event);
                JSONObject cObj = (JSONObject)JSONValue.parse(event);
                Connection conn = Connector.getConnection();
                conn.removeFromDb( Database.EVENTS, (String)cObj.get(JSONKeys.DOCID));
                String resp = conn.addToDb( Database.EVENTS, event );
                JSONObject jObj = (JSONObject)JSONValue.parse(resp);
                jObj.put( "docid",(String)cObj.get("docid") );
                resp = jObj.toJSONString();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println(resp); 
            }
            else
                System.out.println("Unknown command "+first+" for urn="+urn);
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
