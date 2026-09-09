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
    //Create ExchangeMoney Class
    Boolean arbitrage;
    String bestPath;
    double bestRate;
    
    public ExchangeMoney(){
        arbitrage = false;
        bestPath = " ";
        bestRate = 0.0f;
    }
    
    //-------------------------------------------------------------------------------------------------
    //exchangeMoney Method task 1
    public ExchangeMoney exchangeMoney(String[] header, String[] preData){
        //number of Currency
        int n = Integer.parseInt(header[0]);
        
        double[][] rateMatrix = new double[n][n];
        double[][] weight = new double[n][n];
        String[] currency = new String[n];
        
        String[] temp = new String[n];
        
        //input currency and put Rates into rateMatrix
        for(int i =0; i<n; i++){
            temp = preData[i].trim().split("\\s+");
            currency[i] = header[i+1];
            for(int j = 0; j<n; j++){
                rateMatrix[i][j] = Double.parseDouble(temp[j]);
            }
        }
        
        //Convert rateMatix into weight
        for(int i = 0; i<n; i++){
            for(int j =0; j<n; j++){
                weight[i][j] = -Math.log10(rateMatrix[i][j]);
            }
        }
        
        //Bellman-Ford method
        double[] dist = new double[n];
        int[] parent = new int[n];
        
        for(int v = 0; v < n; v++){
            dist[v] = 0.0f;
            parent[v] = -1;
        }
        
        for(int i = 0; i<n-1; i++){
            for(int u = 0; u<n; u++){
                for(int v = 0; v<n; v++){
                    if(dist[u] + weight[u][v] < dist[v]){
                        dist[v] = dist[u] + weight[u][v];
                        parent[v] = u;
                    }
                }
            }
        }
        
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
        
        //transform into String
        
        if(this.arbitrage){
            
            int start= cycleNode;
            for(int i = 0; i< n; i++){
             start = parent[start];   
            }
            
            int curr = start;
            List<Integer> cycle = new java.util.ArrayList<>();
            
            do{
                cycle.add(0, curr);
                curr = parent[curr];
            }while(curr != start);
            
            cycle.add(0, start);
            
            StringBuilder pathBuilder = new StringBuilder();
            
            for(int i = 0; i<cycle.size(); i++){
                pathBuilder.append(currency[cycle.get(i)]);
                if(i<cycle.size()-1){
                    pathBuilder.append(" -> ");
                }
            }
            
            bestPath = pathBuilder.toString();
            
            
            double product = 1.0;
            for(int i = 0; i< cycle.size()-1; i++){
                int from  = cycle.get(i);
                int to  = cycle.get(i +1);
                product *= rateMatrix[from][to];
            }
            bestRate = (product - 1) *100;
            
        }
        
        return this;
    }
    //-------------------------------------------------------------------------------------------------    
    //task 2
    public ExchangeMoney BestConversionRate (String[] header, String[] preData, String vx, String vy){
        
        //number of Currency
        int n = Integer.parseInt(header[0]);
        
        double[][] rateMatrix = new double[n][n];
        double[][] weight = new double[n][n];
        String[] currency = new String[n];
        
        String[] temp = new String[n];
        
        //input currency and put Rates into rateMatrix
        for(int i =0; i<n; i++){
            temp = preData[i].trim().split("\\s+");
            currency[i] = header[i+1];
            for(int j = 0; j<n; j++){
                rateMatrix[i][j] = Double.parseDouble(temp[j]);
            }
        }
        
        //Convert rateMatix into weight
        for(int i = 0; i<n; i++){
            for(int j =0; j<n; j++){
                weight[i][j] = -Math.log10(rateMatrix[i][j]);
            }
        }
        
        double[][] dist = new double[n][n];
        int[][] next = new int[n][n];
        
        for(int i =0; i < n; i++){
            for(int j = 0; j < n; j++){
                dist[i][j] = weight[i][j];
                next[i][j] = (j==i)? -1:j;
            }
        }
        
        for(int k = 0; k<n; k++){
            for(int i = 0; i<n; i++){
                for(int j = 0; j<n; j++){
                    if(dist[i][k] + dist[k][j] < dist[i][j] ){
                        dist[i][j] = dist[i][k] + dist[k][j];
                        next[i][j] = next[i][k];
                    }
                }
            }
        }
        
        int source = java.util.Arrays.asList(currency).indexOf(vx);
        int target = java.util.Arrays.asList(currency).indexOf(vy);
        
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
        
        bestRate = Math.pow(10, -dist[source][target]);
        
        return this;
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
        

        
        
        //Call exchangeMoney method
        result.exchangeMoney(header, data);
        if(result.arbitrage){
            System.out.println("Arbitrage detected!");
            System.out.println("Arbitrage cycle: " + result.bestPath + ".");
            System.out.printf("Profit: %.2f%%.\n", result.bestRate);
        }
        else{
            System.out.println("No arbitrage detected !!!");
            System.out.println("Enter Source Currency: ");
            String vx = keyboard.nextLine().trim();
            
            System.out.println("Enter Target Currency: ");
            String vy = keyboard.nextLine().trim();
            
            result.BestConversionRate(header, data, vx, vy);
            System.out.printf("Best conversion rate from %s to %s: %.4f%n", vx, vy, result.bestRate);
            System.out.println("Best path: " + result.bestPath + ".");
        }

        //Close the keyboard
        keyboard.close();
        //test
    }
  
}
