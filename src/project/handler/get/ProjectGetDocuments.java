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
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 *
 * @author desmond
 */
public class ProjectGetDocuments 
{
    /**
     * Get a list of document with a given docid prefix
     * @param request the request
     * @param response the response
     * @param urn the docid prefix
     * @throws ProjectException 
     */
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
        {
            Connection conn = Connector.getConnection();
            // list documents *starting* with that urn
            String[] docids = conn.listDocuments(Database.METADATA, urn+".*",
                JSONKeys.DOCID );
            JSONObject jDoc = new JSONObject();
            JSONArray jArray = new JSONArray();
            for ( String docid : docids )
            {
                String jStr = conn.getFromDb(Database.METADATA,docid);
                if ( jStr != null )
                {
                    JSONObject obj = (JSONObject)JSONValue.parse( jStr );
                    JSONObject doc = new JSONObject();
                    doc.put( JSONKeys.DOCID, docid );
                    if ( obj.containsKey(JSONKeys.TITLE) )
                        doc.put( JSONKeys.TITLE, obj.get(JSONKeys.TITLE) );
                    if ( obj.containsKey(JSONKeys.SECTION) )
                        doc.put( JSONKeys.SECTION, obj.get(JSONKeys.SECTION) );
                    if ( obj.containsKey(JSONKeys.VERSION1) )
                        doc.put( JSONKeys.VERSION1, obj.get(JSONKeys.VERSION1) );
                    if ( obj.containsKey(JSONKeys.AUTHOR) )
                        doc.put( JSONKeys.AUTHOR, obj.get(JSONKeys.AUTHOR) );
                    jArray.add( doc );
                }
            }
            jDoc.put( JSONKeys.DOCUMENTS, jArray );
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(jDoc.toJSONString()); 
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
