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
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import project.constants.Params;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import project.exception.ProjectException;
/**
 *
 * @author desmond
 */
public class ProjectSiteView 
{
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
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
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}