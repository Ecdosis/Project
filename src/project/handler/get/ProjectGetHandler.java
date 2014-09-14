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

import calliope.core.database.*;
import calliope.core.Utils;
import calliope.core.constants.JSONKeys;
import project.exception.*;
import project.constants.*;
import org.json.simple.*;
import calliope.core.constants.Database;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Get a project document from the database
 * @author desmond
 */
public class ProjectGetHandler 
{
    public void handle(HttpServletRequest request,
            HttpServletResponse response, String urn) throws ProjectException 
    {
        try 
        {
            String first = Utils.first(urn);
            if ( first != null && first.equals(Service.SPONSORS) )
                new ProjectGetSponsors().handle(request,response,Utils.pop(urn) );
            else if ( first != null && first.equals(Service.LIST) )
            {
                Connection conn = Connector.getConnection();
                String[] projects = conn.listCollection(Database.PROJECTS );
                JSONArray jProjs = new JSONArray();
                for ( String project: projects )
                {
                    String jstr = conn.getFromDb( Database.PROJECTS, project );
                    if ( jstr != null )
                    {
                        JSONObject jDoc = (JSONObject)JSONValue.parse(jstr);
                        jDoc.put( JSONKeys.ICON, Database.CORPIX+"/"+urn
                            +"/project/"+JSONKeys.ICON );
                        jProjs.add( jDoc );
                    }
                }
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println(jProjs.toJSONString());
            }
            else if ( first.equals(Service.VIEW) )
            {
                 String docid = request.getParameter(Params.DOCID);
                 Connection conn = Connector.getConnection();
                 String jstr = conn.getFromDb( Database.PROJECTS, docid );
                 if ( jstr != null )
                 {
                     JSONObject jDoc = (JSONObject)JSONValue.parse(jstr);
                     String url = (String)jDoc.get(JSONKeys.URL);
                     if ( url == null )
                     {
                         System.out.println("Missing site url for "+docid);
                         url = request.getRequestURI();
                     }
                     response.sendRedirect(url);
                 }
            }
            else    // user wants a specific project
            {
                Connection conn = Connector.getConnection();
                String jstr = conn.getFromDb( Database.PROJECTS, urn );
                if ( jstr != null )
                {
                    JSONObject jDoc = (JSONObject)JSONValue.parse(jstr);
                    jDoc.put( JSONKeys.ICON, "/mml/"+Database.CORPIX+"/"+urn
                        +"/project/"+JSONKeys.ICON );
                    String[] docs = conn.listDocuments(Database.CORTEX, urn+".*");
                    Long works = new Long(docs.length);
                    jDoc.put( JSONKeys.NWORKS, works );
                    String[] images = conn.listDocuments(Database.CORPIX, urn+".*");
                    jDoc.put( JSONKeys.NIMAGES, images.length );
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().println(jDoc.toJSONString());  
                }
                else 
                    throw new Exception("failed to find project "+urn);
            }
        } 
        catch (Exception e) 
        {
            try
            {
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println(e.getMessage());
            }
            catch ( Exception ex )
            {
                throw new ProjectException(ex);
            }
        }
    }   
}
