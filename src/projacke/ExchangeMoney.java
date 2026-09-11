/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package projacke;
import java.util.Scanner;
import java.util.List;
/**
 *
 * @author tiend
 */


public class ExchangeMoney {
    // Fields shared by both Task 1 and Task 2 results:
    // arbitrage - true if a negative cycle (arbitrage opportunity) was detected
    // bestPath  - the currency sequence found (an arbitrage cycle for Task 1,
    //             or the optimal conversion path for Task 2)
    // bestRate  - the profit percentage (Task 1) or the best conversion rate (Task 2)
    Boolean arbitrage;
    String bestPath;
    double bestRate;
    
    // Default state: no arbitrage found yet, no path computed, rate at 0
    public ExchangeMoney(){
        arbitrage = false;
        bestPath = " ";
        bestRate = 0.0f;
    }
    
    //-------------------------------------------------------------------------------------------------
    // Task 1: detect arbitrage using the Bellman-Ford algorithm
    public ExchangeMoney exchangeMoney(String[] header, String[] preData){
        // n = number of currencies, taken from the first value in the header line
        int n = Integer.parseInt(header[0]);
        
        // rateMatrix[i][j]  = direct exchange rate from currency i to currency j
        // weight[i][j]      = -log10(rateMatrix[i][j]), used so Bellman-Ford (which adds
        //                     edge weights) can effectively work with multiplied rates
        // currency[i]       = the name of the currency at index i
        double[][] rateMatrix = new double[n][n];
        double[][] weight = new double[n][n];
        String[] currency = new String[n];
        
        String[] temp = new String[n];
        
        // Parse the currency names and fill in the rate matrix from the input rows
        for(int i =0; i<n; i++){
            temp = preData[i].trim().split("\\s+");
            currency[i] = header[i+1];
            for(int j = 0; j<n; j++){
                rateMatrix[i][j] = Double.parseDouble(temp[j]);
            }
        }
        
        // Convert every rate into a negative-log weight (multiplication -> addition)
        for(int i = 0; i<n; i++){
            for(int j =0; j<n; j++){
                weight[i][j] = -Math.log10(rateMatrix[i][j]);
            }
        }
        
        // Bellman-Ford setup:
        // dist[v]   = best known total weight to reach currency v
        // parent[v] = the currency we came from to reach v with that best weight
        double[] dist = new double[n];
        int[] parent = new int[n];
        
        // Initialize dist to 0 for every node (not just one source). This is the
        // standard "virtual source" trick, so a negative cycle can be detected
        // no matter where in the graph it occurs, not just from a single start node.
        for(int v = 0; v < n; v++){
            dist[v] = 0.0f;
            parent[v] = -1;
        }
        
        // Relax all edges (n - 1) times. After this, dist[] holds correct shortest
        // (cheapest) known distances, PROVIDED no negative cycle exists.
        for(int i = 0; i<n-1; i++){
            for(int u = 0; u<n; u++){
                for(int v = 0; v<n; v++){
                    if(dist[u] + weight[u][v] < dist[v]){ // found a cheaper route to v
                        dist[v] = dist[u] + weight[u][v];
                        parent[v] = u;
                    }
                }
            }
        }
        
        // One extra pass: if any edge can still be relaxed after (n - 1) passes,
        // that proves a negative cycle exists somewhere reachable from that edge.
        // Record the node where this happens (cycleNode) and stop searching.
        int cycleNode = -1;
        for(int u = 0; u<n ; u++){
            for(int v = 0; v<n; v++){
                if(dist[u] + weight[u][v] < dist[v]){
                    cycleNode = v;
                    this.arbitrage = true;
                    break;
                }
            }
            if(this.arbitrage){
                break;
            }
        }
        
        // If a negative cycle was found, reconstruct it and compute its profit
        if(this.arbitrage){
            
            // Walking back n times from cycleNode along parent[] guarantees we land
            // on a node that is actually part of the cycle (not just leading into it)
            int start= cycleNode;
            for(int i = 0; i< n; i++){
             start = parent[start];   
            }
            
            
            int curr = start;
            List<Integer> cycle = new java.util.ArrayList<>();
            
            // Follow parent[] backward from start until we loop back to start again,
            // inserting each node at the front so the final list reads in forward order
            do{
                cycle.add(0, curr);
                curr = parent[curr];
            }while(curr != start);
            
            cycle.add(0, start);
            
            // Build the human-readable "A -> B -> C -> A" style string
            StringBuilder pathBuilder = new StringBuilder();
            
            for(int i = 0; i<cycle.size(); i++){
                pathBuilder.append(currency[cycle.get(i)]);
                if(i<cycle.size()-1){
                    pathBuilder.append(" -> ");
                }
            }
            
            bestPath = pathBuilder.toString();
            
            // Calculate the real round-trip profit by multiplying the ACTUAL rates
            // (rateMatrix, not weight) along the cycle we just found
            double product = 1.0;
            for(int i = 0; i< cycle.size()-1; i++){
                int from  = cycle.get(i);
                int to  = cycle.get(i +1);
                product *= rateMatrix[from][to];
            }
            bestRate = (product - 1) *100; // convert to a percentage profit
            
        }
        
        return this;
    }
    //-------------------------------------------------------------------------------------------------    
    // Task 2: find the best conversion rate between two given currencies,
    // using the Floyd-Warshall all-pairs shortest path algorithm.
    // This should only be called when Task 1 found no arbitrage - if a negative
    // cycle exists, "shortest path" is undefined (you could loop forever for profit).
    public ExchangeMoney BestConversionRate (String[] header, String[] preData, String vx, String vy){
        
        // Same setup as Task 1: number of currencies, rate matrix, and log-weights
        int n = Integer.parseInt(header[0]);
        
        double[][] rateMatrix = new double[n][n];
        double[][] weight = new double[n][n];
        String[] currency = new String[n];
        
        String[] temp = new String[n];
        
        for(int i =0; i<n; i++){
            temp = preData[i].trim().split("\\s+");
            currency[i] = header[i+1];
            for(int j = 0; j<n; j++){
                rateMatrix[i][j] = Double.parseDouble(temp[j]);
            }
        }
        
        for(int i = 0; i<n; i++){
            for(int j =0; j<n; j++){
                weight[i][j] = -Math.log10(rateMatrix[i][j]);
            }
        }
        
        
        // dist[i][j] = best known total weight from currency i to currency j
        // next[i][j] = the very NEXT hop to take when travelling from i towards j
        //              (used afterwards to rebuild the actual path, since
        //              Floyd-Warshall itself only tracks distances)
        double[][] dist = new double[n][n];
        int[][] next = new int[n][n];
        
        // Before any optimisation: the best known route from i to j is just the
        // direct edge, so next[i][j] simply points straight at j (or -1 if i == j)
        for(int i =0; i < n; i++){
            for(int j = 0; j < n; j++){
                dist[i][j] = weight[i][j];
                next[i][j] = (j==i)? -1:j;
            }
        }
        
        // Core Floyd-Warshall loop: try every currency k as a possible stopover
        // between every pair (i, j), and keep whichever route is cheaper.
        // k MUST be the outermost loop for the algorithm to be correct.
        for(int k = 0; k<n; k++){
            for(int i = 0; i<n; i++){
                for(int j = 0; j<n; j++){
                    if(dist[i][k] + dist[k][j] < dist[i][j] ){ // routing through k is cheaper
                        dist[i][j] = dist[i][k] + dist[k][j];
                        // Our first hop toward j is now whatever our first hop
                        // toward k already was - not k itself
                        next[i][j] = next[i][k];
                    }
                }
            }
        }
        // Look up the indices of the requested source (vx) and target (vy) currencies
        int source = java.util.Arrays.asList(currency).indexOf(vx);
        int target = java.util.Arrays.asList(currency).indexOf(vy);

        // Guard against a currency name that isn't in the list at all
        // (indexOf returns -1 when not found, which would otherwise crash next[][])
        if (source == -1 || target == -1) {
        throw new InvalidInputException("Error: Source or target currency not found in currency list.");
        }
        
        
        // Reconstruct the path forward, starting at source and repeatedly asking
        // next[][] "what's my next hop towards target?" until we arrive
        List<Integer> cycle = new java.util.ArrayList<>();
        cycle.add(source);
        int curr = source;
        do{
            curr = next[curr][target];
            cycle.add(curr);
        }while(curr != target);
        
        StringBuilder pathBuilder = new StringBuilder();
        for(int i=0 ; i<cycle.size(); i++){
            pathBuilder.append(currency[cycle.get(i)]);
            if(i<cycle.size()-1){
                pathBuilder.append(" -> ");
            }
        }
        bestPath = pathBuilder.toString();
        // Undo the -log10() transform to turn the total weight back into a real rate
        bestRate = Math.pow(10, -dist[source][target]);
        
        return this;
    }
    
    
    // Validates the raw input BEFORE any parsing into numbers happens.
    // Throws InvalidInputException with a specific message the moment any
    // rule from the assignment spec is broken.
    private void validateInput(String[] header, String[] preData){
        int n = Integer.parseInt(header[0].trim());
        
        // Case 1: the number of currency names given doesn't match the declared count n
        if(header.length -1 != n){
            throw new InvalidInputException(
                "Error: Invalid Input. Currency count does not match the number of nodes provided.");
        }
        
        for(int i = 0; i<n; i++){
        String[] values = preData[i].trim().split("\\s+");   
        // Case 2: a row doesn't have exactly n values in it
        if (values.length != n) {
            throw new InvalidInputException(
                "Error: Incomplete row for currency " + header[i + 1].trim() +
                ". Each row must have exactly " + n + " values.");
        }
            
            for(int j = 0; j < n; j++) {
                double rate;
                try {
                    rate = Double.parseDouble(values[j]);
                } catch (NumberFormatException e) {
                    // Case 4: the value at this position isn't a valid number at all
                    throw new InvalidInputException(
                        "Error: Invalid numeric value in exchange matrix (row " + (i + 1) +
                        ", col " + (j + 1) + ").");

            }
             // Case 3: exchange rates must be strictly positive (zero isn't valid either)
            if (rate <= 0) {
                throw new InvalidInputException(
                    "Error: Invalid exchange rate detected. Rates must be positive numbers.");
            }

            // Extra rule: every currency's rate against itself must be exactly 1
            if (i == j && Math.abs(rate - 1.0) > 1e-9) {
                throw new InvalidInputException(
                    "Error: Currency " + header[i + 1].trim() + " must have a self exchange rate of 1.");
            }
        
             
            }
        }            
    }
    
    
    //-------------------------------------------------------------------------------------------------    
    public static void main(String[] args){
        //Call ExchangeMoney Class
        ExchangeMoney result = new ExchangeMoney();
        
        //Call Scanner
        Scanner keyboard = new Scanner(System.in);
        
        //First Input and split it
        System.out.println("Input: ");
        String firstLine = keyboard.nextLine();
        String[] header = firstLine.trim().split(",\\s+");
        
        //Take number Of currencies to have more input
        int n = Integer.parseInt(header[0]);
        String[] data = new String[n];
        for(int i = 0; i < n; i++) {
            data[i] = keyboard.nextLine();
        }
        

        
        // Overall flow:
        // 1) validate the raw input
        // 2) run Task 1 (arbitrage detection)
        // 3) if arbitrage was found, report it and stop
        // 4) otherwise, ask for source/target currencies and run Task 2
        try {
        result.validateInput(header, data);   // <-- check everything first

        result.exchangeMoney(header, data);
        if(result.arbitrage){
            System.out.println("Arbitrage detected!");
            System.out.println("Arbitrage cycle: " + result.bestPath + ".");
            System.out.printf("Profit: %.2f%%.\n", result.bestRate);
        }
        else{
            System.out.println("No arbitrage detected.");
            System.out.println("Enter Source Currency: ");
            String vx = keyboard.nextLine().trim();

            System.out.println("Enter Target Currency: ");
            String vy = keyboard.nextLine().trim();

            result.BestConversionRate(header, data, vx, vy);
            System.out.printf("Best conversion rate from %s to %s: %.4f%n", vx, vy, result.bestRate);
            System.out.println("Best path: " + result.bestPath + ".");
        }
    } catch (InvalidInputException e) {
        System.out.println(e.getMessage());
    } catch (NumberFormatException e) {
        System.out.println("Error: Invalid input format.");
    }

        //Close the keyboard
        keyboard.close();
    }
  
}


// Custom exception type so every validation failure in this program is one
// consistent kind of error, with its own clear, specific message
class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}