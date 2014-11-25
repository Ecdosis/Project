/*
 * This file is part of MML.
 *
 *  MML is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  MML is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MML.  If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2014
 */
package project.handler;
import project.exception.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import calliope.core.database.*;
import calliope.core.constants.Database;
import calliope.core.constants.JSONKeys;
import org.json.simple.*;
import calliope.core.Utils;

/**
 * Handle a DELETE request
 * @author desmond
 */
public class ProjectDeleteHandler extends ProjectHandler
{
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
        {
            String referer = request.getHeader("Referer");
            if ( referer != null )
            {
                int qPos = referer.indexOf("?");
                if ( qPos != -1 )
                    referer = referer.substring( 0, qPos );
                int slashPos = referer.lastIndexOf("/");
                if ( slashPos != -1 )
                    referer = referer.substring(0,slashPos);
            }
            Connection conn = Connector.getConnection();
            String jDoc = conn.getFromDb(Database.PROJECTS,urn);
            if ( jDoc != null )
            {
                JSONObject jObj = (JSONObject)JSONValue.parse(jDoc);
                String imgID = Utils.shortDocID(urn);
                imgID += "/project/icon";
                conn.removeImageFromDb(Database.CORPIX, imgID );
                conn.removeFromDb(Database.PROJECTS, urn);
            }
            if ( referer != null )
                response.sendRedirect(referer);
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
