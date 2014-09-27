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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import project.exception.ProjectException;
import calliope.core.Utils;
/**
 * 
 * @author desmond
 */
public class ProjectGetOneProject 
{
    public void handle(HttpServletRequest request,
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
        {
            Connection conn = Connector.getConnection();
            String jstr = conn.getFromDb( Database.PROJECTS, urn );
            if ( jstr != null )
            {
                JSONObject jDoc = (JSONObject)JSONValue.parse(jstr);
                jDoc.put( JSONKeys.ICON, "/mml/"+Database.CORPIX+"/"+urn
                    +"/project/"+JSONKeys.ICON );
                String[] docs = conn.listDocuments(Database.CORTEX, urn+".*",
                    JSONKeys.DOCID);
                Long works = new Long(docs.length);
                jDoc.put( JSONKeys.NWORKS, works );
                String[] events = conn.listDocuments(Database.EVENTS, 
                    Utils.shortDocID(urn)+".*", JSONKeys.DOCID );
                Long nEvents = new Long(events.length);
                jDoc.put( JSONKeys.NEVENTS, nEvents );
                String[] images = conn.listDocuments(Database.CORPIX, urn+".*",
                    JSONKeys.DOCID);
                jDoc.put( JSONKeys.NIMAGES, images.length );
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println(jDoc.toJSONString());  
            }
            else 
                throw new Exception("failed to find project "+urn);
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
