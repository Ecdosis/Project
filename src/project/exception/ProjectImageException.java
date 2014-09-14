/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package project.exception;

/**
 *
 * @author desmond
 */
public class ProjectImageException extends ProjectException 
{
    /**
     * Create a general MMLException from scratch
     * @param message the message it is to bear
     */
    public ProjectImageException( String message )
    {
        super( message );
    }
    /**
     * Wrapper for another exception
     * @param e the other exception
     */
    public ProjectImageException( Exception e )
    {
        super( e );
    }
}
