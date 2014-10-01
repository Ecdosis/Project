/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package project.handler.post;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import project.exception.ProjectException;
import calliope.core.Utils;
import calliope.core.database.Connector;
import calliope.core.constants.Database;
import calliope.core.constants.JSONKeys;
import project.constants.Service;

/**
 * Process the post event to update the project's event list
 * @author desmond
 */
public class ProjectPostEventHandler extends ProjectPostHandler
{
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ProjectException
    {
        try
        {
            parseImportParams( request );
            String first = Utils.first(urn);
            if ( first.equals(Service.DELETE) && this._id !=null )
            {
                System.out.println("deleting id="+this._id);
                Connector.getConnection().removeFromDbByField(
                    Database.EVENTS, JSONKeys._ID, this._id);
                System.out.println("deleted id="+this._id);
            }
            else if ( first.equals(Service.ADD) && event != null )
            {
                String resp = Connector.getConnection().addToDb( Database.EVENTS, event );
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println(resp); 
            }
            else if ( first.equals(Service.UPDATE) )
            {
                String resp = Connector.getConnection().addToDb( Database.EVENTS, event );                
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println(resp); 
            }
            else
                System.out.println("Unknown command "+first+" for urn="+urn);
        }
        catch ( Exception e )
        {
            throw new ProjectException(e);
        }
    }
}
