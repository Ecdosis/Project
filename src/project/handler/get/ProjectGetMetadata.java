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
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import project.exception.ProjectException;

/**
 * Get  list of works and info about them in JSON format
 * @author desmond
 */
public class ProjectGetMetadata extends ProjectGetHandler
{
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
        {
            JSONObject jObj=null;
            String docid = request.getParameter(JSONKeys.DOCID);
            if ( docid != null && docid.length() > 0 )
            {
                Connection conn = Connector.getConnection();
                String metadata = (String)conn.getFromDb(Database.PROJECTS,docid);
                if ( metadata != null && metadata.length() > 0 )
                    jObj = (JSONObject)JSONValue.parse( metadata );
            }
            if ( jObj == null )
                jObj = new JSONObject();
            if ( jObj.containsKey(JSONKeys._ID) )
                jObj.remove(JSONKeys._ID);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(jObj.toJSONString());
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
