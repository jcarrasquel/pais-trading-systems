/** ===================================================================
 * Laboratory of Process-Aware Information Systems (PAIS Lab)
 * National Research University Higher School of Economics. Moscow, Russia.
 * Author: Julio Cesar Carrasquel. Research Asssistant | PhD Candidate
 * Contact: jcarrasquel@hse.ru 
 * 
 * Program: Extraction of an Order Book directory
 * Description: Given a set of FIX messages captured during a trading day, it generates a list of securities indicating in each line 
 * the security identifiers, as well as how many orders were involved in the trading of such security. We assume a relationship 1:1 between 
 * an order book. This is why we denote it as an "order book directory". The security identifiers can be used as input for generation of
 * order-based and order-book-based events (available on the project GitHub repository).
 * 
 * For more information please contact the author or visit the repository and research project pages.
 * GitHub Repository: https://github.com/jcarrasquel/pais-trading-systems
 * Research Project Page: https://pais.hse.ru/en/research/projects/tradingsystems
 * ==================================================================== **/

package com.pais.fix;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Main {
	
	public static List<FIXMessage> fixMessages;
	
	public static void getSecurities() throws IOException{
		
		FileWriter writer = new FileWriter("order-books-directory.csv");
		
		Map<String, Integer> instruments = new HashMap<String, Integer>();
		
		Map<String, List<String>> marketOrders = new HashMap<String, List<String>>();
		Map<String, List<String>> limitOrders = new HashMap<String, List<String>>();
		
		String instrument = null, user = null, messageType = null, orderType = null, displayQuantity = null, qty = null, validity = null;
		String orderId = null;
		
		for(FIXMessage m : fixMessages){
			
			instrument = m.getField(48);
			user = m.getField(49);
			messageType = m.getField(35);
			orderType = m.getField(40);
			displayQuantity = m.getField(1138);
			qty = m.getField(38);
			validity = m.getField(59);
			orderId = m.getField(37);
			
			if(instrument != null && user != null && messageType != null && (messageType.equalsIgnoreCase("8") || messageType.equalsIgnoreCase("D"))){			
				// rule for Day limit/market orders entering with display quantity same as the order size
				if( ( orderType.equalsIgnoreCase("1") || orderType.equalsIgnoreCase("2") ) &&
					(validity == null || validity.equalsIgnoreCase("0") ) &&
						(messageType.equalsIgnoreCase("8") || (messageType.equalsIgnoreCase("D") && displayQuantity != null && displayQuantity.equalsIgnoreCase(qty)))){
							instruments.put(instrument, instruments.containsKey(instrument) ? instruments.get(instrument) + 1 : 1);
							// include order in the list
							if(orderType.equalsIgnoreCase("1") && messageType.equalsIgnoreCase("8")){
								if(marketOrders.containsKey(instrument) == false){
									marketOrders.put(instrument, new LinkedList<String>());
								}
								if(marketOrders.get(instrument).contains(orderId) == false){
									marketOrders.get(instrument).add(orderId);
								}
							} else if(orderType.equalsIgnoreCase("2") && messageType.equalsIgnoreCase("8")){
								if(limitOrders.containsKey(instrument) == false){
									limitOrders.put(instrument, new LinkedList<String>());
								}
								if(limitOrders.get(instrument).contains(orderId) == false){
									limitOrders.get(instrument).add(orderId);
								}
							}
				}else{
					if(instruments.containsKey(instrument)){	
						instruments.remove(instrument);
						marketOrders.remove(instrument);
						limitOrders.remove(instrument);
					}
				}	
			}
		}
		
		writer.write("SECURITY_IDENTIFIER,EVENTS,TOTAL_ORDERS,MARKET_ORDERS,LIMIT_ORDERS\n");
		
		int marketOrds = 0, limitOrds = 0, cases = 0;
		
		for (Map.Entry<String, Integer> entry : instruments.entrySet()){
			
			marketOrds = marketOrders.containsKey(entry.getKey()) ? marketOrders.get(entry.getKey()).size() : 0;
			limitOrds = limitOrders.containsKey(entry.getKey()) ? limitOrders.get(entry.getKey()).size() : 0;
			cases = marketOrds + limitOrds;
			
			writer.write(entry.getKey() + "," + entry.getValue() + "," + cases + "," + marketOrds + "," + limitOrds + "\n"); 
		}
		
		System.out.println("directory size = " + instruments.size());
		
		writer.close();
	}
	
	public static void main(String[] args) throws ParseException, IOException{
		
		fixMessages = new LinkedList<FIXMessage>();
		
		System.out.println("Laboratory of Process-Aware Information Systems (PAIS Lab). Moscow, Russia.");		
		System.out.println("Order Books - Cases Directory");
		System.out.println("*******************************");
		System.out.println("Notes:");
		System.out.println("1. Each order book is related to the trading of a specific security.");
		System.out.println("2. Extracting information about order books whose submitted orders are either day limit or market orders.\n");
		
		String fixMessagesFilename = args[0];
		
		fixMessages = new LinkedList<FIXMessage>();
		
		System.out.println("Retrieving FIX messages from TCP segment payloads in capture file = " + fixMessagesFilename);
		
		FIXParser.getFIXMessages(fixMessagesFilename, fixMessages); // extract FIX messages from TCP segments payload
	
		System.out.println("Generating order books directory...");
		
		getSecurities();
		
		System.out.println("Order books directory generated!");
	}
	
}