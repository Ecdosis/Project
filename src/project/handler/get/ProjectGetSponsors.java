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
import calliope.core.image.Corpix;
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
            String[] jstr = Corpix.listImages( "sponsors" );
            JSONArray list = new JSONArray();
            if ( jstr != null )
            {
                for ( String docid: jstr )
                {
                    String md = Corpix.getMetaData( docid );
                    if ( md != null )
                    {
                        JSONObject obj = (JSONObject)JSONValue.parse(md);
                        // assume the MML service is running
                        // maybe a bad idea
                        obj.put(JSONKeys.IMAGE, "/corpix/"+docid);
                        list.add( obj );
                    }
                }
            }
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(list.toJSONString());
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
