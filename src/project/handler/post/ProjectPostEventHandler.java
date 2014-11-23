/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    /** all eventIDs for the given prefix */
    static HashMap<String,HashSet<String>> eventIDs;
    static int CACHE_SIZE = 20;
    static HashMap<String,ArrayList<String>> freeIDs;
    private void init() throws EventException
    {
        if ( eventIDs == null )
        {
            eventIDs = new HashMap<String,HashSet<String>>();
            freeIDs = new HashMap<String,ArrayList<String>>();
        }
        try
        {
            if ( !eventIDs.containsKey(this.docid) )
            {
                Connection conn = Connector.getConnection();
                String[] docids = conn.listDocuments(Database.EVENTS,
                    this.docid+".*","docid");
                HashSet map = new HashSet<String>();
                eventIDs.put( this.docid, map );
                for ( int i=0;i<docids.length;i++ )
                {
                    if ( checkDocID(docids[i]) )
                    {
                        String[] parts = docids[i].split("/");
                        map.add( parts[parts.length-1] );
                    }
                }
            }
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
     * @return a string starting with "event" and ending with a number
     */
    String uniqueEventID() throws EventException
    {
        this.init();
        ArrayList free = freeIDs.get(this.docid);
        if ( free.isEmpty() )
        {
            HashSet items = eventIDs.get(this.docid);
            int limit = items.size()+CACHE_SIZE;
            int count = 0;
            for ( int i=1;i<=limit;i++ )
            {
                String key = "event"+i;
                if ( !items.contains(key) )
                {
                    items.add(key);
                    count++;
                    if ( count >= CACHE_SIZE )
                        break;
                }
            }
        }
        int last = free.size()-1;
        String uniqueID = (String)free.get(last);
        free.remove(last);
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
            did = this.docid + uniqueEventID();
            evt.put("docid",did);
        }
        return evt.toJSONString();
    }
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
                System.out.println("Retrieved for deletion: "+res );
                if ( res != null )
                {
                    JSONObject jobj = (JSONObject)JSONValue.parse( res );
                    String did = (String)jobj.get("docid");
                    if ( did != null && checkDocID(did) )
                    {
                        String[] parts = did.split("/");
                        ArrayList<String> list = freeIDs.get(this.docid);
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
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println(resp); 
            }
            else if ( first.equals(Service.UPDATE) )
            {
                String resp = Connector.getConnection().addToDb( 
                    Database.EVENTS, event );                
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
