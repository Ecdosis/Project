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

import calliope.core.DocType;
import calliope.core.constants.Database;
import calliope.core.constants.JSONKeys;
import calliope.core.database.Connection;
import calliope.core.database.Connector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import project.exception.ProjectException;
import project.constants.Params;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import project.ProjectWebApp;

/**
 * Get a list of documents with a given prefix
 * @author desmond
 */
public class ProjectGetDocuments 
{
    class MyFilter implements FileFilter
    {
        String regex;
        MyFilter( String regex )
        {
            this.regex = regex;
        }
        public boolean accept(File f)
        {
            if ( f.getName().matches(regex) )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
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
            String projid = request.getParameter(Params.PROJID);
            if ( projid == null )
                projid = urn;
            String withImages = request.getParameter("withimages");
            String[] docids = conn.listDocuments(Database.CORTEX, projid+".*",
                JSONKeys.DOCID );
            if ( withImages != null && withImages.equals("true") )
            {
                ArrayList<String> newIds = new ArrayList<String>();
                // prune out these for which there are no images
                String base = ProjectWebApp.webRoot+"/corpix/";
                for ( String docid : docids )
                {
                    String path = base + docid;
                    File f = new File( path );
                    if ( DocType.isLetter(docid) )
                    {
                        File parent = f.getParentFile();
                        //System.out.println("docid="+docid);
                        String wildcard = DocType.getWildcardFor( f.getName() );
                        //System.out.println("wildcard="+wildcard+" fname="+f.getName());
                        MyFilter ff = new MyFilter(wildcard);
                        File[] files = parent.listFiles(ff);
                        if ( files != null && files.length > 0 )
                            newIds.add( docid );
                    }
                    else if ( f.exists() && f.isDirectory() )
                    {
                        newIds.add( docid );
                    }
                }
                docids = new String[newIds.size()];
                newIds.toArray( docids );
            }
            // now we have only docids that have image representations
            JSONObject jDoc = new JSONObject();
            JSONArray jArray = new JSONArray();
            for ( String docId : docids )
            {
                JSONObject doc = new JSONObject();
                doc.put( JSONKeys.DOCID, docId );
                String ccStr = conn.getFromDb(Database.CORCODE,docId+"/default");
                if ( ccStr != null )
                {
                    String jStr = conn.getFromDb(Database.METADATA,docId);
                    if ( jStr != null )
                    {
                        JSONObject obj = (JSONObject)JSONValue.parse( jStr );
                        if ( obj.containsKey(JSONKeys.TITLE) )
                            doc.put( JSONKeys.TITLE, obj.get(JSONKeys.TITLE) );
                        if ( obj.containsKey(JSONKeys.SECTION) )
                            doc.put( JSONKeys.SECTION, obj.get(JSONKeys.SECTION) );
                        if ( obj.containsKey(JSONKeys.VERSION1) )
                            doc.put( JSONKeys.VERSION1, obj.get(JSONKeys.VERSION1) );
                        if ( obj.containsKey(JSONKeys.AUTHOR) )
                            doc.put( JSONKeys.AUTHOR, obj.get(JSONKeys.AUTHOR) );
                    }
                    jArray.add( doc );
                }
            }
            jDoc.put( JSONKeys.DOCUMENTS, jArray );
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(jDoc.toJSONString().replaceAll("\\\\/", "\\/")); 
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
