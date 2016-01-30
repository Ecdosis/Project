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

import calliope.core.Utils;
import project.exception.*;
import project.constants.*;
import project.handler.ProjectHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Get a project document from the database
 * @author desmond
 */
public class ProjectGetHandler extends ProjectHandler
{
    public void handle(HttpServletRequest request,
            HttpServletResponse response, String urn) throws ProjectException 
    {
        try 
        {
            String first = Utils.first(urn);
            if ( first.equals(Service.RANDOMDOCID) )
                new ProjectGetRandomDocid().handle(request,response,Utils.pop(urn) );
            else if ( first != null && first.equals(Service.SPONSORS) )
                new ProjectGetSponsors().handle(request,response,Utils.pop(urn) );
            else if ( first != null && first.equals(Service.LIST) )
                new ProjectGetProjects().handle(request,response,Utils.pop(urn) );
            else if ( first != null && first.equals(Service.EVENTS) )
                new ProjectGetEvents().handle(request,response,Utils.pop(urn) );
            else if ( first != null && first.equals(Service.BIOGRAPHY) )
                new ProjectGetBiography().handle(request,response,Utils.pop(urn) );
            else if ( first != null && first.equals(Service.TIMELINE) )
                new ProjectGetTimeline().handle(request,response,Utils.pop(urn) );
            else if ( first.equals(Service.VIEW) )
                new ProjectSiteView().handle(request,response,Utils.pop(urn) );
            else if ( first.equals(Service.DOCUMENTS) )
                new ProjectGetDocuments().handle(request,response,Utils.pop(urn) );
            else if ( first.equals(Service.WORKS) )
                new ProjectGetWorks().handle(request,response,Utils.pop(urn) );
            else if ( first.equals(Service.METADATA) )
                new ProjectGetMetadata().handle(request,response,Utils.pop(urn) );
            else    // user wants a specific project
                new ProjectGetOneProject().handle(request,response,urn);
        } 
        catch (Exception e) 
        {
            try
            {
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println(e.getMessage());
            }
            catch ( Exception ex )
            {
                throw new ProjectException(ex);
            }
        }
    }   
}
