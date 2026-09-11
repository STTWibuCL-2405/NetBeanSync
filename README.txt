======================================================================
     Foreign Exchange Arbitrage & Best Conversion Rate Calculator
======================================================================

OVERVIEW
--------
This Java application analyzes foreign exchange rates across multiple 
currencies to solve two primary graph tasks:

1. Task 1 (Arbitrage Detection): Uses the Bellman-Ford algorithm 
   (via negative log-transformed weights) to detect profitable currency 
   arbitrage cycles.
2. Task 2 (Best Conversion Rate): Uses the Floyd-Warshall algorithm 
   to calculate the most optimal conversion rate and path between two 
   specified currencies (executed only if no arbitrage exists).


PREREQUISITES
-------------
* Java Development Kit (JDK): Version 8 or higher.
* An IDE (such as NetBeans, Eclipse, IntelliJ IDEA) or command line/terminal.


FILE STRUCTURE
--------------
* ExchangeMoney.java — Main program containing input parsing, validation logic,
                       Bellman-Ford, and Floyd-Warshall algorithms.
* InvalidInputException — Custom runtime exception handling input validation errors.


HOW TO RUN
----------

Method 1: Command Line / Terminal
1. Open your terminal and navigate to the directory containing the file:
   cd path/to/your/project/src/projacke

2. Compile the class:
   javac ExchangeMoney.java

3. Run the program from the source root directory:
   java projacke.ExchangeMoney


Method 2: NetBeans / Eclipse / IntelliJ IDEA
1. Open the project in your IDE.
2. Locate ExchangeMoney.java inside the projacke package.
3. Right-click ExchangeMoney.java and select "Run File" (or press Shift + F6 in NetBeans).


INPUT FORMAT EXAMPLE
--------------------
When prompted, paste or type the header line followed by the exchange rate matrix.

Header Line Format: N, CURR1, CURR2, ..., CURRN
Matrix Format:      N rows, each containing N space-separated positive rates.

Sample Input:
5, NZD, VND, JPY, CNY, AUD
1.0000 1.6630 1.5050 0.9128 151.232
0.5893 1.0000 0.8957 0.5433 90.0190
0.6644 1.1164 1.0000 0.6065 100.486
1.0955 1.8406 1.6488 1.0000 165.679
0.0066 0.0111 0.0099 0.0060 1.0000


TRACE / DEBUG MODE
------------------
To turn off step-by-step trace messages and delays during execution, 
open ExchangeMoney.java and change:

private static final boolean TRACE = false;
