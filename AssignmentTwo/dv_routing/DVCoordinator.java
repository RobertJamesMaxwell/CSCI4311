/*
 * CSCI 4311
 * Assignment 2 - Exchanging Distance Vector with Neighbors
 * Robert Maxwell
 * Spring 2018
 */

package dv_routing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class DVCoordinator {

    public static void main(String[] args) {

        //error message to user about correct usage of DVCoordinator
        if (args.length != 1) {
            System.out.println("Usage: java DVCoordinator <AdjacencyListInputTextFile>");
            return;
        }

        //read in file with initial dvNode adjacency data
        File inputAdjacencyList = new File(args[0]);
        System.out.println("Input File is: " + inputAdjacencyList);
        readDataFromFile(inputAdjacencyList);


    }

    private static void readDataFromFile(File inputAdjacencyList) {
        try {
            Scanner inputScanner = new Scanner(inputAdjacencyList);
            while(inputScanner.hasNextLine()){
                String newLine = inputScanner.nextLine();
                System.out.println(newLine);
            }
            inputScanner.close();
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }

}
