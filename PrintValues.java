/********************************************************************
Class:     CSCI 470 (Grad)
Program:   PrintValues.java
Author:    Andrew Stefanik
Z-number:  z1753912
Date Due:  12/02/2016

Purpose:   a container class that holds the values needed for printing

Execution: None

Notes:     implements Comparable for sorting purposes

*********************************************************************/

public class PrintValues implements Comparable<PrintValues>
{

    //values needed for printing
    public int movieNumber;
    public double r;

    @Override
    //overriden compareTo method for the Comparable interface
    public int compareTo(PrintValues other)
    {
        return Double.compare(this.r, other.r);
    }
}
