/** ===================================================================
 * Laboratory of Process-Aware Information Systems (PAIS Lab)
 * National Research University Higher School of Economics. Moscow, Russia.
 * Author: Julio Cesar Carrasquel. Research Asssistant | PhD Candidate
 * Contact: jcarrasquel@hse.ru 
 * 
 * Program: PAIS Event Log Generation from FIX Messages for Order Books
 * Description: It generates an event log such that each case represents the trading session in an order book.
 * Each case is an order book associated with the trading of a single financial security.
 * Thus, the log is segmented in cases using the financial security identifier.
 * ==================================================================== **/

package com.pais.fix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class Main {
	
	public static List<FIXMessage> fixMessages;
	
	public static List<String> securityIds;
	
	protected static File tmpFile;
	
	public static void generateOrderEventLog() throws ParseException, IOException{
		
		EventLog eventLog = new EventLog();
		
		String messageType = null, sender = null, orderId = null, activity = null, timestamp = null, instrument = null, orderType = null;
		Integer caseCounter = 0, eventCounter = 0;
		
		String price = null, side = null, qty = null, qty2 = null, receiver = null;
		String state = null;
		String ciOrder = null;
		String tradeId = null;
		
		ArrayList<OrderEvent> newOrders = new ArrayList<OrderEvent>();
		
		for(FIXMessage m : fixMessages){
			
			instrument = m.getField(48);
			messageType = m.getField(35);
			sender = m.getField(49);
			orderId = m.getField(37); // case id
			activity = m.getField(150);
			timestamp = m.getField(60);
			orderType = m.getField(40);
			price = m.getField(44);
			side = m.getField(54);
			qty = m.getField(151);
			qty2 = m.getField(38);
			receiver = m.getField(56);
			state = m.getField(39);
			ciOrder = m.getField(11);	
			tradeId = m.getField(880);
			
			// *** extracting new order messages ***
			if(messageType != null && messageType.equals("D") && sender != null &&
					ciOrder != null && timestamp != null && instrument != null
						&& securityIds.contains(instrument) && (price != null || orderType.equalsIgnoreCase("1")) && side != null && qty2 != null && receiver != null){
				OrderEvent e = new OrderEvent(activity, state, timestamp, price, side, qty2, sender, ciOrder, orderType, instrument);
				newOrders.add(e);
			}
			
			// *** extracting execution report messages ***
			if(messageType != null && messageType.equalsIgnoreCase("8") && sender != null && sender.equalsIgnoreCase("FGW") &&
				orderId != null && activity != null && timestamp != null && instrument != null
					&& securityIds.contains(instrument) && orderType != null 
					&& (price != null || orderType.equalsIgnoreCase("1")) && side != null && qty != null && receiver != null){

				if( ((LinkedHashMap<String, Case>) eventLog.getCases()).containsKey(orderId) == false){
					Case newCase = new Case();
					((LinkedHashMap<String, Case>) eventLog.getCases()).put(orderId, newCase);
					caseCounter++;
				}
				
				OrderEvent e = new OrderEvent(activity, state, timestamp, price, side, qty, receiver, ciOrder, orderType, instrument);
				if(e.getActivity().equalsIgnoreCase("trade") || e.getActivity().equalsIgnoreCase("trade_cancel")){
					e.setTradeId(tradeId);
				}
				((Case) ( (LinkedHashMap<String, Case>) eventLog.getCases()).get(orderId)).addEvent(e); // new event e to the case identified by order id
				eventCounter++;
			}
		}
		
		LinkedHashMap<String,Case> cases = eventLog.getCases();
		
		// *** add new orders messages to the cases ***
		for(int i = 0; i < newOrders.size(); i++){
			for(Entry<String, Case> c : cases.entrySet()){
				ArrayList<Event> events = ( (Case) c.getValue()).getEvents();
				OrderEvent e = (OrderEvent) events.get(0);
				if(e.getCiOrderId().equalsIgnoreCase(newOrders.get(i).getCiOrderId())){
					events.add(0, newOrders.get(i));
					break;
				}
			}
		}
		
		tmpFile = File.createTempFile("tmp-event-log", ".tmp"); 
		FileWriter writer = new FileWriter(tmpFile);
		writer.write("CASE, ORDER, EVENT_ID, ACTIVITY, STATE, TIMESTAMP, CLIENT, SIZE, PRICE, SIDE, SEC_ID, [TRADE_ID]\n");

		int n = 0, k = 1;
		for(Entry<String, Case> c : cases.entrySet()){
			n++;
			ArrayList<Event> events = ( (Case) c.getValue()).getEvents();
			for(int i = 0; i < events.size(); i++){
				OrderEvent e = (OrderEvent) events.get(i);
				writer.write(n + "," + c.getKey() + "," + k + "," + e.getActivity() + "," + e.getState() + "," + e.getTimestamp() + "," + e.getReceiver() + "," + e.getQty() + "," + (e.getOrderType().equalsIgnoreCase("market") ? "market" : e.getPrice()) + "," + e.getSide() + "," + e.getSecurityId() + "," + (e.getTradeId() == null ? "" : e.getTradeId()) + "\n");
				k++;
			}
		}
		writer.close();
	}
	
	public static void transformEventLog() throws FileNotFoundException, IOException, ParseException{
		
		EventLog eventLog = new EventLog();
		
		LinkedList<OrderEvent> events = new LinkedList<OrderEvent>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tmpFile)))) {
			String line = reader.readLine(); // reading header
			while ((line = reader.readLine()) != null) {
				String[] var = line.split(",");
				String order, activity, state, timestamp, instrument, price, side, qty, sender = null, ciOrder = null, trade = null;
				order = var[0];
				activity = var[3];
				state = var[4];
				timestamp = var[5];
				DateTimeFormatter myFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss.SSSSSS");
				LocalDateTime tmp = LocalDateTime.parse(timestamp, myFormatter);
				qty = var[7];
				price = var[8];
				side = var[9];
				instrument = var[10];
				if(var.length > 11){
					trade = var[11];
				}
				OrderEvent e = new OrderEvent(activity, state, tmp.format(DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSSSSS")), price, side, qty, sender, ciOrder, instrument);
				e.setOrderId(order);
				if(e.getActivity().equalsIgnoreCase("trade") || e.getActivity().equalsIgnoreCase ("trade_cancel")){
					e.setTradeId(trade);
				}
				events.add(e);
			}
		}
		
		Collections.sort(events);
		
		// *** merge events whose activity label and trade_id attribute are equal *** 
		String activityLabel1, tradeId1, activityLabel2, tradeId2;
		for(int i = 0; i < events.size(); i++){
			activityLabel1 = events.get(i).getActivity();
			tradeId1 = events.get(i).getTradeId();
			if(tradeId1 != null){
				for(int j = i + 1; j < events.size(); j++){
					activityLabel2 = events.get(j).getActivity();
					tradeId2 = events.get(j).getTradeId();
					if(tradeId2 != null && activityLabel1.equalsIgnoreCase(activityLabel2) && tradeId1.equalsIgnoreCase(tradeId2)){
						events.get(i).setTradeOrder(new Order(events.get(j).getOrderId(), events.get(j).getState(), events.get(j).getQty(), events.get(j).getPrice(), events.get(j).getSide()));
						events.remove(j);
						break;
					}
				}
			}
			if( ((LinkedHashMap<String, Case>) eventLog.getCases()).containsKey(events.get(i).getSecurityId()) == false){
				Case newCase = new Case();
				((LinkedHashMap<String, Case>) eventLog.getCases()).put(events.get(i).getSecurityId(), newCase);
			}
			eventLog.getCases().get(events.get(i).getSecurityId()).addEvent(events.get(i));
		}
		
		PrintWriter writer = new PrintWriter("order-books-event-log.csv", "UTF-8");
		
		writer.println("CASE, ORDER_BOOK, EVENT_NUMBER, ACTIVITY, TIMESTAMP, O1_ID, O1_STATE, O1_SIZE, O1_PRICE, O1_SIDE,  [O2_ID], [O2_STATE], [O2_SIZE], [O2_PRICE], [O2_SIDE], [TRADE_ID]");
		
		int n = 0;
		System.out.println(eventLog.getCases().entrySet().size());
		for(Entry<String, Case> c : eventLog.getCases().entrySet()){
			n++;
			ArrayList<Event> caseEvents = ( (Case) c.getValue()).getEvents();
			for(int i = 0; i < caseEvents.size(); i++){
				OrderEvent e = (OrderEvent) caseEvents.get(i);
				if(e.getTradeOrder() == null){
					writer.println(n + "," + e.getSecurityId() + "," + (i + 1) + "," + e.getActivity() + "," + e.getTimestamp() + "," + e.getOrderId() + "," + e.getState() + "," + e.getQty() + "," + e.getPrice() + "," + e.getSide());
				}else{
					writer.println(n + "," + e.getSecurityId() + "," + (i + 1) + "," + e.getActivity() + "," + e.getTimestamp() + "," + e.getOrderId() + "," + e.getState() + "," + e.getQty() + "," + e.getPrice() + "," + e.getSide()
						+ "," + e.getTradeOrder().getIdentifier() + "," + e.getTradeOrder().getCurrentState() + "," + e.getTradeOrder().getCurrentSize() + "," + (e.getTradeOrder().getPrice() == Double.MAX_VALUE ? "market" : e.getPrice()) + "," + e.getTradeOrder().getSide() + "," + e.getTradeId());
				}
			}
		}
		writer.close();
	}
	
	public static void readSecurityNamesFromFile(String securitiesFilename) throws IOException{
		BufferedReader br = Files.newBufferedReader(Paths.get(securitiesFilename));
		String line;      
		while ((line = br.readLine()) != null) {
			securityIds.add(line);
       }
	}
	
	public static void main(String[] args) throws ParseException, IOException{
		
		System.out.println("Laboratory of Process-Aware Information Systems (PAIS Lab). Moscow, Russia.");		
		System.out.println("Order-Books-based Event Log Generator");
		System.out.println("*************************************");
		
		String fixMessagesFilename = args[0];
		String securitiesFilename = args[1];
		
		System.out.println("Retrieving FIX messages from TCP segment payloads in capture file = " + fixMessagesFilename);
		fixMessages = new LinkedList<FIXMessage>();
		FIXParser.getFIXMessages(fixMessagesFilename, fixMessages);
		System.out.println("FIX messages retrieved!");
		
		System.out.println("Reading financial security identifiers from file = " + securitiesFilename);
		securityIds = new LinkedList<String>();
		readSecurityNamesFromFile(securitiesFilename);
		
		System.out.println("Executing order books event log generation...");
		generateOrderEventLog();
		transformEventLog();
		System.out.println("Event log generation completed!");
	}
	
}