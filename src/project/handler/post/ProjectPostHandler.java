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

package project.handler.post;

import calliope.core.constants.JSONKeys;
import calliope.core.database.*;
import project.exception.*;
import project.constants.Params;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.*;
import calliope.core.constants.Database;
import java.util.Set;

/**
 * Handle posting or saving of project data
 * @author desmond
 */
public class ProjectPostHandler {
    String docid;
    String description;
    String owner;
     /**
     * Handle a POST request
     * @param request the raw request
     * @param response the response we will write to
     * @param urn the rest of the URL after stripping off the context
     * @throws ProjectException 
     */
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
        {
            docid = request.getParameter(Params.DOCID);
            description = request.getParameter(Params.DESCRIPTION);
            owner = request.getParameter(Params.OWNER);
            JSONObject jDoc = new JSONObject();
            jDoc.put(JSONKeys.DESCRIPTION, description);
            Connection conn = Connector.getConnection();
            String oldJDoc = conn.getFromDb( Database.PROJECTS, docid );
            if ( oldJDoc != null )
            {
                JSONObject old = (JSONObject)JSONValue.parse( oldJDoc );
                if ( old.containsKey(JSONKeys.DESCRIPTION) )
                {
                    String oldDesc = (String)old.get(JSONKeys.DESCRIPTION);
                    if ( !oldDesc.equals(description) )
                        jDoc.put( JSONKeys.DESCRIPTION, description );
                }
                // should check the permissions of this
                String oldOwner = (String)old.get(JSONKeys.OWNER);
                if ( oldOwner != null && owner != null 
                    && !oldOwner.equals(owner) )
                    jDoc.put(JSONKeys.OWNER, owner );
                Set<String> keys = old.keySet();
                for ( String key: keys )
                {
                    if ( !key.equals(JSONKeys.OWNER) 
                        && !key.equals(JSONKeys.DESCRIPTION) 
                        && !key.equals(JSONKeys.DOCID) 
                        && !key.equals("_id") )
                    {
                        Object obj = old.get( key );
                        jDoc.put( key, obj );
                    }
                }
            }
            else if ( owner != null )
                jDoc.put(JSONKeys.OWNER, owner );
            conn.putToDb( Database.PROJECTS, docid, 
                jDoc.toJSONString() );
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
