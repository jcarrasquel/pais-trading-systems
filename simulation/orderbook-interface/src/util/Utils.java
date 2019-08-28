package util;
import java.time.LocalDateTime;
import java.util.Comparator;

import model.Order;

public class Utils {

	public static Character MODE_FILE = 'f';
	
	public static Character MODE_SOCKET = 's';
	
	public static int APP_STATE_INITIAL = 1;
	
	public static int APP_STATE_RUNNING = 2;
	
	public static int APP_STATE_PAUSED = 3;
	
	public static int APP_STATE_STOP = 4;
	
	public static int APP_STATE_ONESTEP = 5;
	
	public static String COLOR_REPLACE = "FUCHSIA";
	
	public static String COLOR_TRADE = "LIME";
	
	public static String COLOR_TRADECANCEL = "ORANGE";
	
	public static String COLOR_SUBMIT = "BLUE";
	
	public static String COLOR_NEW = "BLUE";
	
	public static String COLOR_CANCEL = "RED";
	
	public static String COLOR_REJECT = "RED";
	
	public static String COLOR_EXPIRE = "rgb(255,20,147);"; // DARK PINK
	
	public static Comparator<Order> createComparator(String side){
		
		return new Comparator<Order>(){
			@Override
			public int compare(Order o1, Order o2) {
				Double p1 = o1.getPrice();
				Double p2 = o2.getPrice();
				LocalDateTime t1 = o1.getArrivalTime();
				LocalDateTime t2 = o2.getArrivalTime();	
				// price-time priority policy
				return p1.equals(p2) ? (t1.isBefore(t2) ? -1 : 1) : (int) ((double) (side.equalsIgnoreCase("buy") ? p2 - p1 : p1 - p2));
			}
		};
	}

}
