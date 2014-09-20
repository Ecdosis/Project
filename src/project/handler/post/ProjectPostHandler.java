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
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Handle posting or saving of project data
 * @author desmond
 */
public class ProjectPostHandler {
    String docid;
    String description;
    String owner;
    String siteUrl;
    ImageFile icon;
/**
     * Parse the import params from the request
     * @param request the http request
     */
    void parseImportParams( HttpServletRequest request ) throws ProjectException
    {
        try
        {
            FileItemFactory factory = new DiskFileItemFactory();
            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);
            // Parse the request
            List items = upload.parseRequest( request );
            for ( int i=0;i<items.size();i++ )
            {
                FileItem item = (FileItem) items.get( i );
                if ( item.isFormField() )
                {
                    String fieldName = item.getFieldName();
                    if ( fieldName != null )
                    {
                        String contents = item.getString("UTF-8");
                        if ( fieldName.equals(Params.DOCID) )
                        {
                            int index = contents.lastIndexOf(".");
                            if ( index != -1 )
                                contents = contents.substring(0,index);
                            docid = contents;
                        }
                        else if ( fieldName.equals(Params.OWNER) )
                            this.owner = contents;
                        else if ( fieldName.equals(Params.DESCRIPTION) )
                            this.description = contents; 
                        else if ( fieldName.equals(Params.SITE_URL) )
                            this.siteUrl = contents;
                    }
                }
                else if ( item.getName().length()>0 )
                {
                    try
                    {
                        // item.getName retrieves the ORIGINAL file name
                        String type = item.getContentType();
                        if ( type != null )
                        {
                            if ( type.startsWith("image/") )
                            {
                                InputStream is = item.getInputStream();
                                ByteHolder bh = new ByteHolder();
                                while ( is.available()>0 )
                                {
                                    byte[] b = new byte[is.available()];
                                    is.read( b );
                                    bh.append( b );
                                }
                                icon = new ImageFile(
                                    item.getName(), 
                                    item.getContentType(), 
                                    bh.getData() );
                            }
                        }
                    }
                    catch ( Exception e )
                    {
                        throw new ProjectException( e );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            throw new ProjectException( e );
        }
    }
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
            parseImportParams( request );
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
            if ( icon != null )
            {
                String imageId =docid+"/project/"+JSONKeys.ICON;
                try
                {
                    conn.removeImageFromDb( Database.CORPIX, imageId );
                }
                catch ( Exception e )
                {
                    // ignore if not there
                }
                conn.putImageToDb( Database.CORPIX, imageId,icon.getData(), 
                    icon.getWidth(), 
                        icon.getHeight(), icon.type );
            }
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
