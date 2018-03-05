/*
 * CSCI 4311
 * Assignment 2 - Exchanging Distance Vector with Neighbors
 * Robert Maxwell
 * Spring 2018
 */

package dv_routing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class DVCoordinator {

    public static List<List<HashMap<Integer, Integer>>> adjacencyList = new ArrayList<>();

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
        System.out.println("\n\nAdjacencyList is: " + adjacencyList);

        //set the DV nodes
        List<DVNode> dvNodes = new ArrayList<>();
        for (int i = 0; i < adjacencyList.size(); i++)  {
            DVNode node = new DVNode();
            node.nodeNum = i;
            node.dv = new int[adjacencyList.size()];

            //set the distance vectors from the list of neighbors
            for (int j = 0; j < node.dv.length; j++)  {
                for (HashMap<Integer, Integer> neighborDV: adjacencyList.get(i))    {
                    if (i == j) {
                        //set the distance vector to zero for your own nodeNum
                        node.dv[i] = 0;
                    } else if (neighborDV.get(j) == null )  {
                        //if you don't have a neighbor at this index, set it to infinity
                        node.dv[j] = Integer.MAX_VALUE;
                    } else {
                        //set your neighbors distance
                        node.dv[j] = neighborDV.get(j);
                        break;
                    }
                }
            }
            dvNodes.add(node);
        }

        System.out.println("\nYou have successfully created the following DV nodes.\n");
        for (DVNode node : dvNodes) {

            System.out.println("\n\nNode number: " + node.nodeNum);
            System.out.println("With dv's: ");
            for(int i: node.dv) {
                System.out.print(i + " ");
            }

        }


    }

    private static void readDataFromFile(File inputAdjacencyList) {
        try {
            Scanner inputScanner = new Scanner(inputAdjacencyList);
            while(inputScanner.hasNextLine()){
                String newLine = inputScanner.nextLine();
                if (newLine.contains("[") && newLine.contains("]")) {
                    System.out.println("New Line: " + newLine);
                    transformTextLineIntoNodeData(newLine);
                }
            }
            inputScanner.close();
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }

    private static void transformTextLineIntoNodeData(String newLine) {

        List<HashMap<Integer, Integer>> currentIndexAdjacencyList = new ArrayList<>();

        //remove brackets from string
        newLine = newLine.replace("[", "");
        newLine = newLine.replace("]", "");

        //split string on commas to get a string array of each mapping of neighborNode:distanceVector
        String[] neighborNodeAndDistanceVectors = newLine.split(",");

        //loop through each neighborNode:distanceVector, and add the data to the adjacencyList instance variable
        for(String neighborNodeAndDistanceVector: neighborNodeAndDistanceVectors){
            System.out.println("Neighbor Node and Distance Vector : " + neighborNodeAndDistanceVector);

            //separate neighbor node from distance amount
            //neighborAndDistance with have length two with index 0 = neighbor and index 1 = distance
            String[] neighborAndDistance = neighborNodeAndDistanceVector.split(":");

            //assign each to a new HashMap (need to trim whitespace for Integer.parseInt to work)
            HashMap<Integer, Integer> neighborNodeAndDistanceVectorMap = new HashMap();
            neighborNodeAndDistanceVectorMap.put(Integer.parseInt(neighborAndDistance[0].trim()), Integer.parseInt(neighborAndDistance[1].trim()));
            System.out.println(neighborNodeAndDistanceVectorMap);

            //add new HashMap to currentAdjacencyList which is the instanceVariable adjacencyList at index
            currentIndexAdjacencyList.add(neighborNodeAndDistanceVectorMap);
        }

        //add the new adjacencyList for this index to the full adjacencyList instance variable
        adjacencyList.add(currentIndexAdjacencyList);

    }

}
