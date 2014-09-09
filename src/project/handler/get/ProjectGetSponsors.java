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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import project.exception.ProjectException;
import project.handler.ProjectHandler;
import org.json.simple.*;
/**
 * Handle a request for a list of sponsors in corpix.files/sponsors
 * @author desmond
 */
public class ProjectGetSponsors extends ProjectHandler 
{
    public void handle(HttpServletRequest request,
        HttpServletResponse response, String urn) throws ProjectException 
    {
        try
        {
            Connection conn = Connector.getConnection();
            if ( conn != null )
            {
                String[] jstr = conn.listDocuments( Database.CORPIX, "sponsors.*" );
                JSONArray list = new JSONArray();
                if ( jstr != null )
                {
                    for ( String docid: jstr )
                    {
                        String md = conn.getMetadata( Database.CORPIX, docid );
                        if ( md != null )
                        {
                            JSONObject obj = (JSONObject)JSONValue.parse(md);
                            // assume the MML service is running
                            // maybe a bad idea
                            obj.put(JSONKeys.IMAGE, "/mml/corpix/"+docid);
                            list.add( obj );
                        }
                    }
                }
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println(list.toJSONString());
            }
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
