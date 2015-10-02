/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project.handler.get;

import calliope.core.database.Connector;
import calliope.core.database.Connection;
import calliope.core.Utils;
import calliope.core.constants.Database;
import project.constants.Params;
import project.exception.ProjectException;
import project.handler.get.timeline.EventType;
import project.handler.get.timeline.BioEvent;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import calliope.core.constants.JSONKeys;

/**
 * JSON Biography handler
 * @author desmond
 */
public class ProjectGetBiography extends ProjectGetHandler
{
    String docid;
    JSONObject biography;
    String lang;
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
        {
            docid = request.getParameter(Params.DOCID);
            if ( docid==null||docid.length()==0)
                docid ="english/harpur";
            lang = Utils.languageFromDocId(docid);
            Connection conn = Connector.getConnection();
            String[] docs = conn.listDocuments( Database.EVENTS, docid+"/.*", 
                JSONKeys.DOCID );
            biography = new JSONObject();
            Locale locale = new Locale( lang );
            ArrayList<BioEvent> events = new ArrayList<BioEvent>();
            for ( int i=0;i<docs.length;i++ )
            {
                String res = conn.getFromDb( Database.EVENTS, docs[i] );
                if ( res != null && res.length()>0 )
                {
                    JSONObject jobj = (JSONObject)JSONValue.parse(res);
                    int it = ((Long)jobj.get("type")).intValue();
                    EventType et = EventType.fromInt(it);
                    if ( et == EventType.biography )
                    {
                        JSONObject eventDate = (JSONObject)jobj.get("date");
                        String eventDesc = (String)jobj.get("description");
                        String eventRefs = (String)jobj.get("references");
                        BioEvent be = new BioEvent( eventDate, eventDesc, 
                            eventRefs, locale );
                        events.add( be );
                    }
                }
            }
            sort( events );
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            String jstring = biography.toJSONString();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().print( jstring );
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
    /**
     * Sort the events on their natural (date) order
     * @param events 
     */
    public void sort( ArrayList<BioEvent> events )
    {
        BioEvent[] arr = new BioEvent[events.size()];
        events.toArray(arr);
        Arrays.sort( arr );
        JSONArray bio = new JSONArray();
        for ( int i=0;i<arr.length;i++ )
        {
            bio.add( arr[i].toJSONObject() );
        }
        biography.put( "biography", bio );
    }
}