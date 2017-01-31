/********************************************************************
Class:     CSCI 470 (Grad)
Program:   ProcessInputData.java
Author:    Andrew Stefanik
Z-number:  z1753912
Date Due:  12/02/2016

Purpose:   imports data from two files and preprocesses it for MovieRecommendation.java

Execution: java ProcessInputData

Notes:     None

*********************************************************************/

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProcessInputData
{

    //main function
    //executes the two functions to read the two files and write the output
    public static void main(String[] args)
    {
        readMovieNames();
        readMovieMatrix();
    }

    //reads the movie name files
    //prints lines with non-ASCII characters
    //replaces any non-ASCII characters with their closest ASCII equivalent
    //removes the vertical bar and left pads the movie number with zeros
    //writes the updated movie names to a file
    public static void readMovieNames()
    {
        try
        {
            //declare variables
            String inFileLine;
            String outFileLine;
            String[] splitMovieName;
            int countMovies = 0;
            int countNonAsciiMovies = 0;
            int countNonAsciiChars = 0;
            Boolean nonAsciiFound = false;
            char stringChar;

            //declare input and output variables
            FileInputStream inputFile = new FileInputStream("./movie-names.txt");
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputFile, "ISO-8859-1"));

            FileOutputStream outputFile = new FileOutputStream("./movie-names2.txt");
            BufferedWriter outputWriter = new BufferedWriter(new OutputStreamWriter(outputFile));

            //reads each line in the file
            while ((inFileLine = inputReader.readLine()) != null)
            {
                //checks each char in the line
                for (int i = 0; i < inFileLine.length(); i++)
                {
                    stringChar = inFileLine.charAt(i);

                    //if a character is non-ASCII, print the line
                    if ((int) stringChar > 127)
                    {
                        System.out.println(inFileLine);
                        countNonAsciiChars++;
                        nonAsciiFound = true;
                    }
                }

                //if there is a non-ASCII character, replace it
                if (nonAsciiFound == true)
                {
                    inFileLine = replaceNonAscii(inFileLine);
                    countNonAsciiMovies++;
                    nonAsciiFound = false;
                }

                //split the movie name into two strings using | as a delimiter
                splitMovieName = inFileLine.split("\\|");

                //left pad the movie name with zeros until it is a total of four characters long
                while (splitMovieName[0].length() < 4)
                {
                    splitMovieName[0] = "0" + splitMovieName[0];
                }

                //concatenate the movie number and name, and write it to the output file
                outFileLine = splitMovieName[0] + splitMovieName[1] + '\n';
                outputWriter.write(outFileLine);

                countMovies++;
            }

            //print summary information
            System.out.printf("%nNon-Ascii Characters Count:\t\t%d%n", countNonAsciiChars);
            System.out.printf("Movies With Non-Ascii Characters Count:\t%d%n", countNonAsciiMovies);
            System.out.printf("Movies Count:\t\t\t\t%d%n", countMovies);

            //close readers
            inputReader.close();
            inputFile.close();
            outputWriter.close();
            outputFile.close();

        } catch (FileNotFoundException e)
        {
            System.out.println("ERROR: File not found!");
        } catch (Exception e)
        {
            System.out.println("ERROR: Something else!");
        }
    }

    //replaces non-ASCII characters with their closest ASCII equivalent
    public static String replaceNonAscii(String nonAscii)
    {
        //declare variables
        StringBuilder builder = new StringBuilder();

        //create the character replacement map
        Map<Character, Character> charReplacementMap = new HashMap<Character, Character>()
        {
            {
                put('é', 'e');
                put('è', 'e');
                put('ö', 'o');
                put('Á', 'A');
            }
        };

        //replace any non-ASCII characters in the input string
        for (char currentChar : nonAscii.toCharArray())
        {
            Character replacementChar = charReplacementMap.get(currentChar);
            builder.append(replacementChar != null ? replacementChar : currentChar);
        }

        //saves the StringBuilder as a String
        String ascii = builder.toString();

        return ascii;
    }

    //reads the movie matrix file
    //expands each row into a non-sparse format (replaces null ratings with zeros)
    //writes the expanded rows into a serialized file
    public static void readMovieMatrix()
    {
        //declare variables
        String inFileLine;
        String outFileLine;
        int countMovies = 0;
        int countReviewers = 0;
        ArrayList<String> outMatrix = new ArrayList();

        try
        {
            //declare input variables
            FileInputStream inputFile = new FileInputStream("./movie-matrix.txt");
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputFile));

            //read each row of the matrix
            while ((inFileLine = inputReader.readLine()) != null)
            {
                //expand the matrix row
                outFileLine = buildMatrixRow(inFileLine);
                outMatrix.add(outFileLine);

                countReviewers = outFileLine.length() / 2;
                countMovies++;

                //check the number of columns in the matrix row
                if (countReviewers != 943)
                {
                    System.out.println("Matrix column is not the right length!");
                }
            }

            //print summary information
            System.out.printf("Reviewers Count:\t%d%n", countReviewers);
            System.out.printf("Movies Count:\t\t%d%n", countMovies);

            //close readers
            inputReader.close();
            inputFile.close();

        } catch (FileNotFoundException e)
        {
            System.out.println("ERROR: File not found!");
        } catch (Exception e)
        {
            System.out.println("ERROR: Something else!");
        }

        try
        {
            //declare output variables
            OutputStream outputFile = new FileOutputStream("./movie-matrix2.ser");
            OutputStream outputBuffer = new BufferedOutputStream(outputFile);
            ObjectOutput outputWriter = new ObjectOutputStream(outputBuffer);

            //write the serialized file
            outputWriter.writeObject(outMatrix);

            //close readers
            outputWriter.close();
            outputBuffer.close();
            outputFile.close();

        } catch (Exception e)
        {
            System.out.println("ERROR: Something else!");
        }

    }

    //expands each sparse row into a dense row by replacing all null characters with zeros
    //the row is semicolon delimited
    public static String buildMatrixRow(String sparseLine)
    {
        //declare variables
        StringBuilder builder = new StringBuilder();
        char currentChar;
        char nextChar = '\0';

        //for each character in the string
        for (int i = 0; i < sparseLine.length() - 1; i++)
        {
            //check the current and next character
            currentChar = sparseLine.charAt(i);
            nextChar = sparseLine.charAt(i + 1);

            //if the first character is a semicolon, append a zero to builder
            if (i == 0 && currentChar == ';')
            {
                builder.append('0');
            }

            //append the current character to builder
            builder.append(currentChar);

            //if the current character is equal to the next character, append a zero to builder
            //this will only happen if both characters are semicolons
            if (currentChar == nextChar)
            {
                builder.append('0');
            }
        }

        //append the last character to builder
        //this is necessary to avoid an out of range exception in the above loop
        builder.append(nextChar);

        //if the final string character is a semicolon
        //append a zero and semicolon to builder
        //this is necessary because the lines are not semicolon terminated, so
        //meaning that the final reviewer does not have a semicolon after their ratings
        //if the final character is a semicolon, it means the final reviewer's score is null
        if (nextChar == ';')
        {
            builder.append('0');
            builder.append(';');
        } //otherwise, append a final semicolon
        else
        {
            builder.append(';');
        }

        //saves the StringBuilder as a String
        String denseLine = builder.toString();

        return denseLine;
    }
}
