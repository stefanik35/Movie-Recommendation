/********************************************************************
Class:     CSCI 470 (Grad)
Program:   MovieRecommendation.java
Author:    Andrew Stefanik
Z-number:  z1753912
Date Due:  12/02/2016

Purpose:   asks the user for a movie number and display the 20 movies
            in the list that are most similar to it

Execution: java MovieRecommendation

Notes:     measures similarity by using the Pearson r coefficient
            among same reviewers

*********************************************************************/

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovieRecommendation
{

    public static void main(String[] args)
    {
        //declare movie name file variables
        String inFileLine;
        int countMoviesNames = 0;
        ArrayList<String> inMovieNames = new ArrayList();

        try
        {
            //declare movie name file input variables
            FileInputStream inputFile = new FileInputStream("./movie-names2.txt");
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputFile));

            //read the file and save the lines
            while ((inFileLine = inputReader.readLine()) != null)
            {
                inMovieNames.add(inFileLine);

                countMoviesNames++;
            }

            //print summary information
            System.out.printf("Movie Name Count:\t%d%n", countMoviesNames);

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

        //declare movie matrix file variables
        ArrayList<String> inMatrix = null;

        try
        {
            //declare more movie matrix file variables
            int countMatrixRows = 0;
            int countMatrixColumns = 0;

            ////declare movie matrix file input variables
            InputStream inputFile = new FileInputStream("./movie-matrix2.ser");
            InputStream inputBuffer = new BufferedInputStream(inputFile);
            ObjectInput inputReader = new ObjectInputStream(inputBuffer);

            //read the movie matrix and save it
            inMatrix = (ArrayList<String>) inputReader.readObject();

            //close readers
            inputReader.close();
            inputBuffer.close();
            inputFile.close();

            //get summary information about the movie matrix
            for (String matrixRow : inMatrix)
            {
                countMatrixRows++;
                countMatrixColumns = matrixRow.length();

                if (countMatrixColumns / 2 != 943)
                {
                    System.out.println("Matrix column is not the right length!");
                }
            }

            //print summary information
            System.out.printf("Matrix Row Count:\t%d%n", countMatrixRows);
            System.out.printf("Matrix Column Count:\t%d%n", countMatrixColumns / 2);
            System.out.printf("All Matrix Columns Are The Same Length%n");

        } catch (FileNotFoundException e)
        {
            System.out.println("ERROR: File not found!");
        } catch (Exception e)
        {
            System.out.println("ERROR: Something else!");
        }

        try
        {
            //declare standard input variables
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            while (true)
            {
                //get input from the user
                System.out.print("\nPlease enter a movie number: ");
                String inputString = userInput.readLine();
                int inputInt = 0;

                //ask for input until the user enters valid input
                do
                {
                    //exit the program if the user enters q or quit
                    if ("q".equals(inputString) || "quit".equals(inputString))
                    {
                        System.out.println("Exiting the program...");
                        System.exit(0);
                    }

                    //if the user does not enter an integer, print a message to that effect
                    try
                    {
                        inputInt = Integer.parseInt(inputString);
                    } catch (NumberFormatException e)
                    {
                        System.out.println("Invalid input.  Not an integer!");
                    }

                    //if the range of the integer is not valid, print a message and ask for new input
                    if (inputInt < 1 || inputInt > 1682)
                    {
                        System.out.print("Please enter an integer in the range of 1 to 1682: ");
                        inputString = userInput.readLine();
                    }
                } while (inputInt < 1 || inputInt > 1682);

                //decrement the input movie number
                //this is necessary since the movie numbers are stored from 0 to 1681
                //but are labeled from 1 to 1682
                inputInt--;

                //get the input movie name
                String[] movieNameOutput;
                movieNameOutput = inMovieNames.get(inputInt).split("^([0-9][0-9][0-9][0-9])");

                //print the input movie number and name
                System.out.printf("Movie Number: %d%nMovie Name: %s%n", inputInt + 1, movieNameOutput[1]);

                //compare the input movie to the others in the movie matrix
                compareMovies(inMatrix, inMovieNames, inputInt);
            }

        } catch (Exception e)
        {
            System.out.println("ERROR: Something else!");
        }
    }

    //compares a given movie with every movie in the matrix
    //finds the twenty most similar movies and prints out information about them
    public static void compareMovies(ArrayList<String> movieMatrix, ArrayList<String> movieNames, int movieNumber)
    {
        //declare variables
        ArrayList<Integer> movie1Ratings = new ArrayList();
        ArrayList<Integer> movie2Ratings = new ArrayList();
        ArrayList<PrintValues> printArray = new ArrayList();

        int movie1Int;
        int movie2Int;

        String movie1 = movieMatrix.get(movieNumber);
        String movie2;

        //for each movie in the matrix
        for (int i = 0; i < movieMatrix.size(); i++)
        {
            //get the movie ratings
            movie2 = movieMatrix.get(i);

            //for each reviewer
            //NOTE: must increment by two to skip the semicolons that are every other character
            for (int j = 0; j < movie1.length(); j += 2)
            {
                //get the scores of the reviewer for each of the two movies
                movie1Int = Character.getNumericValue(movie1.charAt(j));
                movie2Int = Character.getNumericValue(movie2.charAt(j));

                //if the reviewer has seen both movies, add their ratings to the appropriate lists
                if (movie1Int != 0 && movie2Int != 0)
                {
                    movie1Ratings.add(movie1Int);
                    movie2Ratings.add(movie2Int);
                }
            }

            //if there are not least 10 common reviewers for the two movies, continue to the next movie
            if (movie1Ratings.size() < 10)
            {
                movie1Ratings.clear();
                movie2Ratings.clear();
                continue;
            }

            //calculate r and save it with the current movie number
            PrintValues printValues = new PrintValues();
            printValues.movieNumber = i;
            printValues.r = pearsonCorrelation(movie1Ratings, movie2Ratings);
            printArray.add(printValues);

            //clear the movie rating structures
            movie1Ratings.clear();
            movie2Ratings.clear();
        }

        //if there are less than twenty movies in common, print a message and do not output the similar movies
        if (printArray.size() < 20)
        {
            System.out.println("Insufficient Comparison Movies");
        } //otherwise sort the movies based on correlation coeffient
        else
        {
            Collections.sort(printArray);
            Collections.reverse(printArray);

            //print column headers
            System.out.printf("\tr\t\tMovie Number\tMovie Name%n");

            //print the correlation coefficient and movie number and name for the top twenty most similar movies
            for (int i = 0; i < 20; i++)
            {
                PrintValues printValues = new PrintValues();
                printValues = printArray.get(i);

                String[] printMovieName = movieNames.get(printValues.movieNumber).split("^([0-9][0-9][0-9][0-9])");
                printMovieName[0] = movieNames.get(printValues.movieNumber).substring(0, 4);

                //removes the zeros from the beginning of the movie number
                while (printMovieName[0].charAt(0) == '0')
                {
                    printMovieName[0] = printMovieName[0].substring(1);
                }

                System.out.printf("%d.\t%f\t\t%4s\t%s%n", i + 1, printValues.r, printMovieName[0], printMovieName[1]);
            }
        }
    }

    //calculates the correlation coefficient between the ratings of two movies
    public static double pearsonCorrelation(ArrayList<Integer> movie1Ratings, ArrayList<Integer> movie2Ratings)
    {
        //declare variables
        double movie1Avg = 0, movie2Avg = 0;
        double movie1Std = 0, movie2Std = 0;
        ArrayList<Double> movie1StdCalc = new ArrayList();
        ArrayList<Double> movie2StdCalc = new ArrayList();
        ArrayList<Double> movie1zScore = new ArrayList();
        ArrayList<Double> movie2zScore = new ArrayList();
        double r = 0;

        //calculate the average
        for (int i = 0; i < movie1Ratings.size(); i++)
        {
            movie1Avg += movie1Ratings.get(i);
            movie2Avg += movie2Ratings.get(i);
        }

        movie1Avg /= movie1Ratings.size();
        movie2Avg /= movie1Ratings.size();

        //calcuate the standard deviation
        for (int i = 0; i < movie1Ratings.size(); i++)
        {
            movie1StdCalc.add((double) movie1Ratings.get(i) - movie1Avg);
            movie2StdCalc.add((double) movie2Ratings.get(i) - movie2Avg);
        }

        for (int i = 0; i < movie1Ratings.size(); i++)
        {
            movie1StdCalc.set(i, movie1StdCalc.get(i) * movie1StdCalc.get(i));
            movie2StdCalc.set(i, movie2StdCalc.get(i) * movie2StdCalc.get(i));
        }

        for (int i = 0; i < movie1Ratings.size(); i++)
        {
            movie1Std += movie1StdCalc.get(i);
            movie2Std += movie2StdCalc.get(i);
        }

        movie1Std /= (movie1Ratings.size() - 1);
        movie2Std /= (movie1Ratings.size() - 1);

        movie1Std = sqrt(movie1Std);
        movie2Std = sqrt(movie2Std);

        //calculate the z-scores
        for (int i = 0; i < movie1Ratings.size(); i++)
        {
            movie1zScore.add((movie1Ratings.get(i) - movie1Avg) / movie1Std);
            movie2zScore.add((movie2Ratings.get(i) - movie2Avg) / movie2Std);
        }

        //calculate r
        for (int i = 0; i < movie1Ratings.size(); i++)
        {
            r += movie1zScore.get(i) * movie2zScore.get(i);
        }

        r /= (movie1Ratings.size() - 1);

        return r;
    }

}
