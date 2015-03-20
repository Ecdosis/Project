/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package project.handler.get;
import org.json.simple.JSONObject;
import calliope.core.date.FuzzyDate;
/**
 *
 * @author desmond
 */
public class DateHolder implements Comparable<DateHolder> {
    FuzzyDate fd;
    JSONObject jobj;
    DateHolder( FuzzyDate fd, JSONObject jobj )
    {
        this.fd = fd;
        this.jobj = jobj;
    }
    public int compareTo( DateHolder other )
    {
        return this.fd.compareTo(other.fd);
    }
}
