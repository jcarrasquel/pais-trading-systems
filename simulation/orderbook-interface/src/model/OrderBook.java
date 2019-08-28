package model;
import java.util.PriorityQueue;

import util.Utils;

public class OrderBook {

	protected PriorityQueue<Order> buySide;
	
	protected PriorityQueue<Order> sellSide;
	
	public OrderBook(){
		this.buySide = new PriorityQueue<>(Utils.createComparator("buy"));
		this.sellSide = new PriorityQueue<>(Utils.createComparator("sell"));
	}
	
	public PriorityQueue<Order> getBuySide() {
		return buySide;
	}

	public PriorityQueue<Order> getSellSide() {
		return sellSide;
	}
	
}
