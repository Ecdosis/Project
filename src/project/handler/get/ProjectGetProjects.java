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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import project.exception.ProjectException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author desmond
 */
public class ProjectGetProjects 
{
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
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
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
