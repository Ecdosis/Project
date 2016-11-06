/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package project.handler.get;
import calliope.core.database.Connector;
import calliope.core.Utils;
import calliope.core.constants.JSONKeys;
import calliope.core.constants.Database;
import project.constants.Params;
import project.handler.get.timeline.TimelineJS;
import project.exception.ProjectException;
import calliope.core.database.Connection;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import project.handler.get.timeline.EventType;
/**
 * Compose Timeline.js JSON document as fast as possible
 * @author desmond
 */
public class ProjectGetTimeline extends ProjectGetHandler
{
    String docid;
    String title;
    String subtitle;
    EventType eventType;
    String lang;
    private EventType readEventType( HttpServletRequest request )
    {
        EventType result = EventType.all;
        String et = request.getParameter(Params.EVENT_TYPE);
        if ( et != null && et.length()>0 )
        {
            try
            {
                result = EventType.valueOf(et.toLowerCase());
            }
            catch ( Exception e )
            {
            }
        }
        return result;
    }
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
        {
            title= request.getParameter(Params.TITLE);
            if ( title==null||title.length()==0)
                title ="Project Timeline";
            subtitle= request.getParameter(Params.SUBTITLE);
            if ( subtitle==null||subtitle.length()==0)
                subtitle ="Biographical events";
            docid = request.getParameter(Params.DOCID);
            if ( docid==null||docid.length()==0)
                docid ="english/harpur";
            eventType = readEventType(request);
            lang = Utils.languageFromDocId(docid);
            Connection conn = Connector.getConnection();
            String[] docs = conn.listDocuments( Database.EVENTS, docid+"/.*",
                JSONKeys.DOCID );
            ArrayList<String> events = new ArrayList<String>();
            for ( int i=0;i<docs.length;i++ )
            {
                String res = conn.getFromDb( Database.EVENTS, docs[i] );
                if ( res != null && res.length()>0 )
                {
                    events.add( res );
                }
            }
            String[]array = new String[events.size()];
            events.toArray(array);
            TimelineJS jobj = new TimelineJS(title,subtitle,eventType,
                array,lang);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            String jstring = jobj.toJSONString();
            //System.out.println(jstring);
            response.getWriter().print( jstring );
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
