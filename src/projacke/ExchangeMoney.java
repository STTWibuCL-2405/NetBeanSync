/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package projacke;
import java.util.Scanner;
import java.util.List;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * @author tiend
 */


public class ExchangeMoney {
    // Flip this to false to hide trace output AND skip the pauses below
    // (e.g. for a clean, fast run when testing your actual results)
    private static final boolean TRACE = true;

    // Base currency used when fetching live rates from the API
    private static final String BASE_CURRENCY = "EUR"; // exchangeratesapi.io free tier only allows EUR as base
    private static final String API_KEY = "f253109d33f82a66b0f81cd788134c11";

    // Small pause so trace output is readable during a live demo recording.
    // Does nothing if TRACE is false.
    private static void pause(int ms) {
        if (!TRACE) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

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

        if (TRACE) System.out.println("[Task 1] Parsed " + n + " currencies: " + String.join(", ", currency));
        pause(300);
        
        // Convert every rate into a negative-log weight (multiplication -> addition)
        for(int i = 0; i<n; i++){
            for(int j =0; j<n; j++){
                weight[i][j] = -Math.log10(rateMatrix[i][j]);
            }
        }

        if (TRACE) System.out.println("[Task 1] Converted rates to -log10 edge weights.");
        pause(300);
        
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

        if (TRACE) System.out.println("[Task 1] Initialized dist[] to 0 for all nodes (multi-source setup).");
        pause(300);
        
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
            if (TRACE) System.out.println("[Task 1] Completed relaxation pass " + (i + 1) + " of " + (n - 1) + ".");
            pause(200); // one small pause per pass - fine since there are only n-1 of these
        }

        if (TRACE) System.out.println("[Task 1] Distances after relaxation: " + java.util.Arrays.toString(dist));
        pause(300);
        
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

        if (TRACE) {
            if (this.arbitrage) {
                System.out.println("[Task 1] Extra pass found further relaxation at node: " + currency[cycleNode] + " -> negative cycle exists.");
            } else {
                System.out.println("[Task 1] Extra pass found no further relaxation -> no arbitrage.");
            }
        }
        pause(300);
        
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

            if (TRACE) {
                StringBuilder rawCycle = new StringBuilder();
                for (int idx : cycle) rawCycle.append(currency[idx]).append(" ");
                System.out.println("[Task 1] Reconstructed cycle node sequence: " + rawCycle.toString().trim());
            }
            pause(300);
            
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
                if (TRACE) System.out.println("[Task 1] Multiplying rate " + currency[from] + " -> " + currency[to]
                        + " = " + rateMatrix[from][to] + " (running product: " + product + ")");
                pause(200); // small pause per multiplication step - cycles are short, so this is fine
            }
            bestRate = (product - 1) *100; // convert to a percentage profit

            if (TRACE) System.out.println("[Task 1] Final round-trip product: " + product + " -> profit: " + bestRate + "%");
            pause(300);
            
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

        if (TRACE) System.out.println("[Task 2] Parsed " + n + " currencies: " + String.join(", ", currency));
        pause(300);
        
        for(int i = 0; i<n; i++){
            for(int j =0; j<n; j++){
                weight[i][j] = -Math.log10(rateMatrix[i][j]);
            }
        }

        if (TRACE) System.out.println("[Task 2] Converted rates to -log10 edge weights.");
        pause(300);
        
        
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

        if (TRACE) System.out.println("[Task 2] Initialized dist[][] from direct rates and next[][] pointers.");
        pause(300);
        
        // Core Floyd-Warshall loop: try every currency k as a possible stopover
        // between every pair (i, j), and keep whichever route is cheaper.
        // k MUST be the outermost loop for the algorithm to be correct.
        for(int k = 0; k<n; k++){
            boolean improvedThisRound = false;
            for(int i = 0; i<n; i++){
                for(int j = 0; j<n; j++){
                    if(dist[i][k] + dist[k][j] < dist[i][j] ){ // routing through k is cheaper
                        dist[i][j] = dist[i][k] + dist[k][j];
                        // Our first hop toward j is now whatever our first hop
                        // toward k already was - not k itself
                        next[i][j] = next[i][k];
                        improvedThisRound = true;
                    }
                }
            }
            if (TRACE) System.out.println("[Task 2] Considered " + currency[k] + " as a stopover"
                    + (improvedThisRound ? " -> found a cheaper route through it." : " -> no improvement."));
            pause(250); // one pause per candidate stopover currency - only n of these total
        }
        // Look up the indices of the requested source (vx) and target (vy) currencies
        int source = java.util.Arrays.asList(currency).indexOf(vx);
        int target = java.util.Arrays.asList(currency).indexOf(vy);

        // Guard against a currency name that isn't in the list at all
        // (indexOf returns -1 when not found, which would otherwise crash next[][])
        if (source == -1 || target == -1) {
        throw new InvalidInputException("Error: Source or target currency not found in currency list.");
        }

        if (TRACE) System.out.println("[Task 2] Floyd-Warshall complete. Best log-weight from "
                + vx + " to " + vy + ": " + dist[source][target]);
        pause(300);
        
        
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

        if (TRACE) System.out.println("[Task 2] Reconstructed path: " + bestPath + " | rate: " + bestRate);
        pause(300);
        
        return this;
    }
    
    
    // Validates the raw input BEFORE any parsing into numbers happens.
    // Throws InvalidInputException with a specific message the moment any
    // rule from the assignment spec is broken.
    private void validateInput(String[] header, String[] preData){
        int n = Integer.parseInt(header[0].trim());

        if (TRACE) System.out.println("[Validation] Checking input for " + n + " currencies...");
        pause(200);
        
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

        if (TRACE) System.out.println("[Validation] Input passed all checks.");
        pause(300);
    }

    //-------------------------------------------------------------------------------------------------
    // Live API support: fetches real exchange rates from exchangeratesapi.io
    // (200+ world currencies, including ones ECB-only sources like Frankfurter
    // don't cover, e.g. VND). The free tier only allows EUR as the base
    // currency, so we fetch EUR-based rates and derive the full matrix via
    // cross-rate calculation:
    //
    //      rate[i][j] = (rate from EUR to currency j) / (rate from EUR to currency i)
    //
    // This always produces a mathematically consistent matrix (self-rate
    // exactly 1, rate[i][j] * rate[j][i] == 1 exactly), so Task 1 will
    // almost always report "No arbitrage" - as expected with real data.
    private static double[][] fetchRates(String[] currencies) throws Exception {
        int n = currencies.length;
        double[] toBase = new double[n]; // rate from BASE_CURRENCY to currencies[i]

        String url = "https://api.exchangeratesapi.io/v1/latest?access_key=" + API_KEY;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new InvalidInputException(
                    "Error: Failed to fetch live exchange rates (HTTP " + response.statusCode() + ").");
        }

        String json = response.body();

        // exchangeratesapi.io returns {"success":false, "error": {...}} on failure
        // (e.g. invalid/missing API key) even with an HTTP 200 status.
        if (json.contains("\"success\":false") || json.contains("\"success\": false")) {
            throw new InvalidInputException(
                    "Error: exchangeratesapi.io request failed - check your API key and plan limits.");
        }

        for (int i = 0; i < n; i++) {
            String code = currencies[i].trim().toUpperCase();

            if (code.equals(BASE_CURRENCY)) {
                toBase[i] = 1.0; // base currency against itself
                continue;
            }

            Pattern p = Pattern.compile("\"" + code + "\":\\s*([\\d.]+)");
            Matcher m = p.matcher(json);

            if (!m.find()) {
                throw new InvalidInputException(
                        "Error: Currency not found in live API data: " + code);
            }
            toBase[i] = Double.parseDouble(m.group(1));
        }

        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = toBase[j] / toBase[i];
            }
        }

        return matrix;
    }

    // Converts a numeric rate matrix into the String[] row format that
    // exchangeMoney()/BestConversionRate() expect (space-separated values per row).
    private static String[] matrixToDataRows(double[][] matrix) {
        int n = matrix.length;
        String[] data = new String[n];

        for (int i = 0; i < n; i++) {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < n; j++) {
                row.append(matrix[i][j]);
                if (j < n - 1) {
                    row.append(" ");
                }
            }
            data[i] = row.toString();
        }

        return data;
    }
    
    
    //-------------------------------------------------------------------------------------------------    
    public static void main(String[] args){
        //Call ExchangeMoney Class
        ExchangeMoney result = new ExchangeMoney();
        
        //Call Scanner
        Scanner keyboard = new Scanner(System.in);

        // Ask the user which data source to use
        System.out.println("Choose input mode:");
        System.out.println("1. Use my own data (type the full exchange rate matrix)");
        System.out.println("2. Use real-world API data (only type the currency list)");
        System.out.print("Enter 1 or 2: ");
        String mode = keyboard.nextLine().trim();

        System.out.println("------------------------------------------------------------");
        System.out.println("Enter the number of currencies and their codes, e.g.:");
        System.out.println("5, USD, NZD, AUD, EUR, JPY");
        String firstLine = keyboard.nextLine();
        String[] header = firstLine.trim().split(",\\s+");

        int n;
        try {
            n = Integer.parseInt(header[0].trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid numeric value for currency count.");
            keyboard.close();
            return;
        }

        if (header.length - 1 != n) {
            System.out.println("Error: Invalid Input. Currency count does not match the number of nodes provided.");
            keyboard.close();
            return;
        }

        String[] data;

        switch (mode) {
            case "2":
                // Real-world API mode: only the currency list is needed
                String[] currencies = java.util.Arrays.copyOfRange(header, 1, header.length);
                try {
                    System.out.println("Fetching live exchange rates from exchangeratesapi.io...");
                    double[][] matrix = fetchRates(currencies);
                    data = matrixToDataRows(matrix);
                    System.out.println("Live rates retrieved successfully.");
                } catch (Exception e) {
                    System.out.println("Error: Failed to fetch live exchange rates - " + e.getMessage());
                    keyboard.close();
                    return;
                }
                break;

            case "1":
                // Manual mode: type each row of the matrix as before
                System.out.println("Enter " + n + " rows of exchange rates, one row per currency:");
                data = new String[n];
                for (int i = 0; i < n; i++) {
                    data[i] = keyboard.nextLine();
                }
                break;

            default:
                System.out.println("Error: Invalid mode selected. Please enter 1 or 2.");
                keyboard.close();
                return;
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