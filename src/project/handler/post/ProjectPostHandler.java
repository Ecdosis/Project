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
import calliope.core.Utils;
import calliope.core.image.Corpix;
import project.ProjectWebApp;
import project.exception.*;
import project.constants.Service;
import project.constants.Params;
import project.handler.ProjectHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.*;
import calliope.core.constants.Database;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Handle posting or saving of project data
 * @author desmond
 */
public class ProjectPostHandler extends ProjectHandler
{
    String docid;
    String author;
    String work;
    String shortid;
    String description;
    String owner;
    String language;
    String siteUrl;
    String source;
    String _id;
    String event;
    ImageFile icon;
    void processField( String fieldName, String contents )
    {
        if ( fieldName.equals(Params.DOCID) )
        {
            int index = contents.lastIndexOf(".");
            if ( index != -1 )
                contents = contents.substring(0,index);
            docid = contents;
            shortid = Utils.shortDocID( docid );
        }
        // these two needed by subclass
        else if ( fieldName.equals(Params.EVENT) )
            this.event = contents;
        else if ( fieldName.equals(Params._ID) )
            this._id = contents;
        else if ( fieldName.equals(Params.LANGUAGE) )
            this.language = contents;
        else if ( fieldName.equals(Params.OWNER) )
            this.owner = contents;
        else if ( fieldName.equals(Params.DESCRIPTION) )
            this.description = contents; 
        else if ( fieldName.equals(Params.SITE_URL) )
            this.siteUrl = contents;
        else if ( fieldName.equals("source"))
            this.source = contents;
        else if ( fieldName.equals("author"))
            this.author = contents;
        else if ( fieldName.equals("work"))
            this.work = contents;
   }
/**
     * Parse the import params from the request
     * @param request the http request
     */
    void parseImportParams( HttpServletRequest request ) throws ProjectException
    {
        try
        {
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if ( isMultipart )
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
                            processField(fieldName,contents);
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
                                else
                                    System.out.println("skipping file type"+type);
                            }
                        }
                        catch ( Exception e )
                        {
                            throw new ProjectException( e );
                        }
                    }
                }
            }
            else
            {
                Map tbl = request.getParameterMap();
                Set<String> keys = tbl.keySet();
                Iterator<String> iter = keys.iterator();
                while ( iter.hasNext() )
                {
                    String key = iter.next();
                    String[] values = (String[])tbl.get(key);
                    for ( int i=0;i<values.length;i++ )
                        processField( key, values[i]);
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
            String first = Utils.first(urn);
            if ( first.equals(Service.EVENTS) )
                new ProjectPostEventHandler().handle(request,response,
                    Utils.pop(urn));
            else
            {
                parseImportParams( request );
                JSONObject jDoc;
                Connection conn = Connector.getConnection();
                String oldJDoc = conn.getFromDb( Database.PROJECTS, docid );
                if ( oldJDoc != null )
                    jDoc = (JSONObject)JSONValue.parse(oldJDoc);
                else
                    jDoc = new JSONObject();
                if ( description != null && description.length()>0 )
                    jDoc.put( JSONKeys.DESCRIPTION, description );
                // should check the permissions of this
                if ( owner != null && owner.length()>0 )
                    jDoc.put(JSONKeys.OWNER, owner );
                if ( siteUrl != null && siteUrl.length()>0 )
                    jDoc.put(JSONKeys.SITE_URL, siteUrl );
                if ( author != null && author.length()>0 )
                    jDoc.put(JSONKeys.AUTHOR, author );
                if ( work != null && work.length()>0 )
                    jDoc.put(JSONKeys.WORK, work );
                if ( docid != null && docid.length()>0 )
                    jDoc.put(JSONKeys.DOCID, docid );
                if ( language != null && language.length()>0 )
                    jDoc.put(JSONKeys.LANGUAGE, language );
                conn.putToDb( Database.PROJECTS, docid, jDoc.toJSONString() );
                if ( icon != null )
                {
                    String imageId = shortid+"/project/"+JSONKeys.ICON;
                    try
                    {
                        Corpix.addImage( ProjectWebApp.webRoot, imageId, 
                            icon.getName(),"icon",icon.type, icon.getData() );
                    }
                    catch ( Exception e )
                    {
                        // ignore if not there
                    }
                }
            }
            if ( source != null )
                response.sendRedirect(source);
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
