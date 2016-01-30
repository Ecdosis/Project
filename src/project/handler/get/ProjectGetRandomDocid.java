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
 *  (c) copyright Desmond Schmidt 2016
 */
package project.handler.get;

import calliope.core.constants.Database;
import calliope.core.constants.JSONKeys;
import calliope.core.constants.Formats;
import calliope.core.database.Connection;
import calliope.core.database.Connector;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import project.exception.ProjectException;

/**
 * Get  list of works and info about them in JSON format
 * @author desmond
 */
public class ProjectGetRandomDocid extends ProjectGetHandler
{
    private static int MAX_RETRIES = 10;
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
        {
            String docid = null;
            String projid = request.getParameter(JSONKeys.PROJID);
            if ( projid != null && projid.length() > 0 )
            {
                Connection conn = Connector.getConnection();
                String[] docs = conn.listDocuments(Database.CORTEX,
                    projid+"/.*",JSONKeys.DOCID);
                int retries = 0;
                Random r = new Random();
                while ( docid == null && retries < MAX_RETRIES )
                {
                    int index = r.nextInt(docs.length);
                    String jDoc = conn.getFromDb(Database.CORTEX,docs[index]);
                    JSONObject jObj = (JSONObject)JSONValue.parse(jDoc);
                    String format = (String)jObj.get(JSONKeys.FORMAT);
                    if ( format.equals(Formats.MVD_TEXT) && !docs[index].endsWith("notes") )
                    {
                        docid = docs[index];
                        break;
                    }
                    else
                        retries++;
                }
                if ( docid==null )
                    docid = "";
            }
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(docid);
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
