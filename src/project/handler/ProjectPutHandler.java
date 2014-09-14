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
import calliope.core.constants.Database;
import calliope.core.constants.JSONKeys;
import calliope.core.database.Connection;
import calliope.core.database.Connector;
import calliope.core.database.MimeType;
import java.io.InputStream;
import java.util.List;
import project.exception.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import project.constants.Params;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Handle a PUT request (used for update)
 * @author desmond
 */
public class ProjectPutHandler extends ProjectHandler
{
    String docid;
    String language;
    String work;
    String author;
    String siteURL;
    String description;
    String owner;
    ImageFile icon;
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
        {
            parseImportParams( request );
            Connection conn = Connector.getConnection();
            String jstr = conn.getFromDb( Database.PROJECTS, urn );
            if ( jstr != null )
            {
                JSONObject jDoc = (JSONObject)JSONValue.parse(jstr);
                if ( docid != null )
                    jDoc.put( JSONKeys.DOCID, docid );
                if ( language != null )
                    jDoc.put( JSONKeys.LANGUAGE, language );
                if ( author != null )
                    jDoc.put( JSONKeys.AUTHOR, author );
                if ( work != null )
                    jDoc.put( JSONKeys.WORK, work );
                if ( siteURL != null )
                    jDoc.put( JSONKeys.SITE_URL, siteURL );
                if ( description != null )
                    jDoc.put( JSONKeys.DESCRIPTION, description );
                if ( owner != null )
                    jDoc.put( JSONKeys.OWNER, owner );
                // now upload image if supplied
                if ( icon != null )
                {
                    String iconid = docid+"/project/icon";
                    byte[] data = conn.getImageFromDb( Database.CORPIX, 
                        iconid, new MimeType(icon.type) );
                    if ( data != null )
                        conn.removeImageFromDb(Database.CORPIX, iconid );
                    conn.putImageToDb( Database.CORPIX, iconid, icon.getData(), 
                        icon.width, icon.height, icon.type );
                }
            }
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
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
                        String contents = item.getString(this.encoding);
                        if ( fieldName.equals(Params.DOCID) )
                        {
                            int index = contents.lastIndexOf(".");
                            if ( index != -1 )
                                contents = contents.substring(0,index);
                            docid = contents;
                        }
                        else if ( fieldName.equals(Params.AUTHOR) )
                            this.author = contents;
                        else if ( fieldName.equals(Params.DESCRIPTION) )
                            this.description = contents; 
                        else if ( fieldName.equals(Params.DOCID) )
                            this.docid = contents;
                        else if ( fieldName.equals(Params.WORK) )
                            this.work = contents;
                        else if ( fieldName.equals(Params.SITE_URL) )
                            this.siteURL = contents;
                        else if ( fieldName.equals(Params.OWNER) )
                            this.owner = contents;
                        else if ( fieldName.equals(Params.LANGUAGE) )
                            language = contents;
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
}
