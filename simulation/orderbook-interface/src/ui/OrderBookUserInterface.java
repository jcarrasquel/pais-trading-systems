package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.PriorityQueue;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import main.Main;
import model.Event;
import model.Order;
import model.OrderBook;
import util.Utils;

public class OrderBookUserInterface extends JFrame{

	private static final long serialVersionUID = -1335700646610935165L;
	
	protected JTable table; 
	
	protected JLabel currentEventLabel;
	
	protected MyTableModel orderBookTableModel;
	
	protected MyTableModel arrivingBuyOrdersTableModel;
	
	protected MyTableModel arrivingSellOrdersTableModel;
	
	protected JTable orderBookTable;
	
	protected JTable arrivingSellOrdersTable;
	
	protected JTable arrivingBuyOrdersTable;
	
	public boolean play = false;
	
	static public String currentEvent;
	
	static public String side;
	
	static public int editedOrderPosition;
	
	static public String editedQueue;
	
	static public int x;
	
	class MyTableModel extends DefaultTableModel{
		
		public String id;
		
		public Color color;
		
		public MyTableModel(String id){
			super();
			this.id = id;
		}
		
		public String getId(){
			return id;
		}
		
		public void setRowColour(int row, Color c) {
			color = c;
			System.out.println("hello baby 33");
		    fireTableRowsUpdated(row, row);
		}
		
		public Color getColor() {
	        return color;
	    }
	}
	
	class MyTableCellRenderer extends DefaultTableCellRenderer {
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	MyTableModel model = (MyTableModel) table.getModel();
	
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        if(currentEvent.equalsIgnoreCase("submit") && row == editedOrderPosition && !model.getId().equalsIgnoreCase("1")){
	        	c.setBackground(new Color(0,0,255));
	        	c.setForeground(Color.WHITE);
	        }else if(currentEvent.equalsIgnoreCase("new") && row == editedOrderPosition && model.getId().equalsIgnoreCase("1")){
	        	if(side.equalsIgnoreCase("buy") && column <= 3){
	        		c.setBackground(new Color(0,0,255));
	        		c.setForeground(Color.WHITE);
	        	}else if(side.equalsIgnoreCase("sell") && column > 4) {
	        		c.setBackground(new Color(0,0,255));
	        		c.setForeground(Color.WHITE);
	        	}else{
		        	c.setBackground(new Color(255, 255, 255, 192));
			        c.setForeground(Color.BLACK);
	        	}
	        }else if(currentEvent.equalsIgnoreCase("replace") && row == editedOrderPosition){
	        	if(x == 1){
		        	c.setBackground(new Color(255, 0, 255));
		        	c.setForeground(Color.BLACK);
	        	}else if(x == 2 && column <= 3){
	        		c.setBackground(new Color(255, 0, 255));
		        	c.setForeground(Color.BLACK);
	        	}else if(x == 3 && column > 4){
	        		c.setBackground(new Color(255, 0, 255));
		        	c.setForeground(Color.BLACK);
	        	}else if(x == 4){
	        		c.setBackground(new Color(255, 0, 255));
		        	c.setForeground(Color.BLACK);
	        	}else{
	        		c.setBackground(new Color(255, 255, 255, 192));
		        	c.setForeground(Color.BLACK);
	        	}
	        }else if(currentEvent.equalsIgnoreCase("trade_cancel") && row == editedOrderPosition){
	        	if(x == 5 && model.getId().equalsIgnoreCase("2")){
	        		c.setBackground(new Color(255,165,0));
			        c.setForeground(Color.BLACK);
	        	}else if(x == 5 && column > 4){
	        		c.setBackground(new Color(255,165,0));
		        	c.setForeground(Color.BLACK);
	        	}else if(x == 6 && model.getId().equalsIgnoreCase("3")){
	        		c.setBackground(new Color(255,165,0));
			        c.setForeground(Color.BLACK);
	        	}else if(x == 6 && column <= 3){
	        		c.setBackground(new Color(255,165,0));
		        	c.setForeground(Color.BLACK);
	        	}else if(x == 8){
	        		c.setBackground(new Color(255,165,0));
		        	c.setForeground(Color.BLACK);
	        	}else{
	        		c.setBackground(new Color(255, 255, 255, 192));
		        	c.setForeground(Color.BLACK);
	        	}
	        }else if(currentEvent.equalsIgnoreCase("reject") && row == editedOrderPosition && (x == 1 || x == 4)){
	        	c.setBackground(new Color(255, 0, 0));
	        	c.setForeground(Color.WHITE);
	        }else if((currentEvent.equalsIgnoreCase("cancel") || currentEvent.equalsIgnoreCase("expire")) && row == editedOrderPosition){
	        	if(model.getId().equalsIgnoreCase("2") && x == 1){
	        		c.setBackground(currentEvent.equalsIgnoreCase("expire") ? new Color(255, 20, 147) : new Color(255, 0, 0));
	        		c.setForeground(Color.WHITE);
	        	}else if(model.getId().equalsIgnoreCase("3") && x == 4){
	        		c.setBackground(currentEvent.equalsIgnoreCase("expire") ? new Color(255, 20, 147) : new Color(255, 0, 0));
	        		c.setForeground(Color.WHITE);
	        	}else if(model.getId().equalsIgnoreCase("1") && x == 2 && column <= 3){
	        		c.setBackground(currentEvent.equalsIgnoreCase("expire") ? new Color(255, 20, 147) : new Color(255, 0, 0));
	        		c.setForeground(Color.WHITE);
	        	}else if(model.getId().equalsIgnoreCase("1") && x == 3 && column > 4){
	        		c.setBackground(currentEvent.equalsIgnoreCase("expire") ? new Color(255, 20, 147) : new Color(255, 0, 0));
	        		c.setForeground(Color.WHITE);
	        	}else{
	        		c.setBackground(new Color(255, 255, 255, 192));
		        	c.setForeground(Color.WHITE);
	        	}
	        }else if(currentEvent.equalsIgnoreCase("trade") && row == editedOrderPosition){
	        	if(x == 5 && model.getId().equalsIgnoreCase("2")){
	        		c.setBackground(new Color(0,255,0));
			        c.setForeground(Color.BLACK);
	        	}else if(x == 5 && column > 4){
	        		c.setBackground(new Color(0,255,0));
		        	c.setForeground(Color.BLACK);
	        	}else if(x == 6 && model.getId().equalsIgnoreCase("3")){
	        		c.setBackground(new Color(0,255,0));
			        c.setForeground(Color.BLACK);
	        	}else if(x == 6 && column <= 3){
	        		c.setBackground(new Color(0,255,0));
		        	c.setForeground(Color.BLACK);
	        	}else if(x == 8){
	        		c.setBackground(new Color(0,255,0));
		        	c.setForeground(Color.BLACK);
	        	}else{
	        		c.setBackground(new Color(255, 255, 255, 192));
		        	c.setForeground(Color.BLACK);
	        	}
	        }else{
	        	c.setBackground(new Color(255, 255, 255, 192));
	        	c.setForeground(Color.BLACK);
	        }
	        
	        return c;
	    }
	}
	
	public void play() throws FileNotFoundException, IOException, InterruptedException{}
	
	public void pause() throws FileNotFoundException, IOException, InterruptedException{}
	
	public void stop() throws FileNotFoundException, IOException, InterruptedException{}
	
	public void nextStep() throws FileNotFoundException, IOException, InterruptedException{}
	
	public void refreshOrderBook(OrderBook orderBook, Event event){
		
		PriorityQueue<Order> buySide = orderBook.getBuySide();
		
		int rowCount = orderBookTableModel.getRowCount();
		
		if(rowCount > 0){
			orderBookTableModel.setRowCount(0);
		}
			
		if(orderBook.getBuySide().size() > 0){
			
			side = "buy";
			
			Order[] buyOrders = orderBook.getBuySide().toArray(new Order[orderBook.getBuySide().size()]);
			Arrays.sort(buyOrders, orderBook.getBuySide().comparator());	
			
			for(int i = 0; i < buyOrders.length; i++){
				String price = buyOrders[i].getPrice().equals(Double.MAX_VALUE) ? "market" : buyOrders[i].getPrice().toString();
				if(buyOrders[i].getIdentifier().equalsIgnoreCase(event.getOrder1().getIdentifier())){
					editedOrderPosition = i;
				}
				orderBookTableModel.addRow(new Object[] {i+1, buyOrders[i].getIdentifier(), buyOrders[i].getCurrentSize(), price, "", "", "", "", ""});
			}	
		}
		
		rowCount = orderBookTableModel.getRowCount(); // update the row counter once buy orders have been added to the order book
			
		if(orderBook.getSellSide().size() > 0){
			
			side = "sell";
			
			Order[] sellOrders = orderBook.getSellSide().toArray(new Order[orderBook.getSellSide().size()]);
			Arrays.sort(sellOrders, orderBook.getSellSide().comparator());
			
			if(rowCount == 0){ // if the buy side is empty
				
				for(int i = 0; i < sellOrders.length; i++){
					String price = sellOrders[i].getPrice().equals(Double.MAX_VALUE) ? "market" : sellOrders[i].getPrice().toString();
					if(sellOrders[i].getIdentifier().equalsIgnoreCase(event.getOrder1().getIdentifier())){
						editedOrderPosition = i;
					}
					orderBookTableModel.addRow(new Object[] {"", "", "", "", "", i+1, sellOrders[i].getIdentifier(), sellOrders[i].getCurrentSize(), price});
				}
			}else{ // if the buy side is not empty, then modify the last three columns of each row according to the data on sell orders
				
				for(int i = 0; i < sellOrders.length; i++){
					String price = sellOrders[i].getPrice().equals(Double.MAX_VALUE) ? "market" : sellOrders[i].getPrice().toString();
					if(sellOrders[i].getIdentifier().equalsIgnoreCase(event.getOrder1().getIdentifier())){
						editedOrderPosition = i;
					}
					if(i >= rowCount){
						orderBookTableModel.addRow(new Object[] {"", "", "", "", "", i+1, sellOrders[i].getIdentifier(), sellOrders[i].getCurrentSize(), price});
					}else{
						orderBookTableModel.setValueAt( i + 1, i, 5);
						orderBookTableModel.setValueAt( sellOrders[i].getIdentifier(), i, 6);
						orderBookTableModel.setValueAt( sellOrders[i].getCurrentSize(), i, 7);
						orderBookTableModel.setValueAt( price, i, 8);
					}
				}
			}
		}
	}
	
	public void refreshArrivingQueue(PriorityQueue<Order> arrivingQueue, DefaultTableModel tableModel, Event event){

		int rowCount = tableModel.getRowCount();
		
		if(rowCount > 0){
			tableModel.setRowCount(0);
		}
		
		if(arrivingQueue.size() > 0){	
			Order[] orders = arrivingQueue.toArray(new Order[arrivingQueue.size()]);
			Arrays.sort(orders, arrivingQueue.comparator());	
			for(int i = 0; i < orders.length; i++){
				String price = orders[i].getPrice().equals(Double.MAX_VALUE) ? "market" : orders[i].getPrice().toString();
				if(orders[i].getIdentifier().equalsIgnoreCase(event.getOrder1().getIdentifier())){
					editedOrderPosition = i;
				}
				tableModel.addRow(new Object[] {i+1, orders[i].getIdentifier(), orders[i].getCurrentSize(), price});
			}	
		}
	}
	
	private String styleActivityLabel(String activity){
		String res = "";
		if(activity.equalsIgnoreCase("new")){
			res = "<B BGCOLOR=" + Utils.COLOR_NEW + "><FONT COLOR=WHITE>" + activity + "</FONT></B>";
		}else if(activity.equalsIgnoreCase("submit")){
			res = "<B BGCOLOR=" + Utils.COLOR_SUBMIT + "><FONT COLOR=WHITE>" + activity + "</FONT></B>";
		}else if(activity.equalsIgnoreCase("trade")){
			res = "<B BGCOLOR=" + Utils.COLOR_TRADE + "><FONT COLOR=BLACK>" + activity + "</FONT></B>";
		}else if(activity.equalsIgnoreCase("replace")){
			res = "<B BGCOLOR=" + Utils.COLOR_REPLACE + "><FONT COLOR=BLACK>" + activity + "</FONT></B>";
		}else if(activity.equalsIgnoreCase("trade_cancel")){
			res = "<B BGCOLOR=" + Utils.COLOR_TRADECANCEL + "><FONT COLOR=WHITE>" + activity + "</FONT></B>";
		}else if(activity.equalsIgnoreCase("cancel")){
			res = "<B BGCOLOR=" + Utils.COLOR_CANCEL + "><FONT COLOR=WHITE>" + activity + "</FONT></B>";
		}else if(activity.equalsIgnoreCase("reject")){
			res = "<B BGCOLOR=" + Utils.COLOR_REJECT + "><FONT COLOR=WHITE>" + activity + "</FONT></B>";
		}else if(activity.equalsIgnoreCase("replace")){
			res = "<B BGCOLOR=" + Utils.COLOR_REPLACE + "><FONT COLOR=BLACK>" + activity + "</FONT></B>";
		}else if(activity.equalsIgnoreCase("expire")){
			res = "<B BGCOLOR=" + Utils.COLOR_EXPIRE + "><FONT COLOR=WHITE>" + activity + "</FONT></B>";
		}
		return res;
	}
	
	public void displayCurrentEvent(Event e){
		
		String eventLabel = "";
		
		if(e != null){
			String eventIdentifier = e.getIdentifier();
			String activityLabel = styleActivityLabel(e.getActivity().toUpperCase());
			String timestampLabel = e.getTimestamp().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss.SSSSSS"));
			String orderLabel = e.getOrder2() == null ? "order: " + e.getIdentifier() : "order 1: " + e.getOrder1().getIdentifier() + ", order2: " + e.getOrder2().getIdentifier();
			eventLabel = "<html> <b> Current Event: " + eventIdentifier + "</b> <b> Time: " + timestampLabel + "</b><br>" + "<b> Activity: </b>" + activityLabel + "</html>";
		}
		
		currentEventLabel.setText(eventLabel);
	}
	
	public void refresh(OrderBook orderBook, PriorityQueue<Order> arrivingBuyOrders, PriorityQueue<Order> arrivingSellOrders, Event event, int xx){
		
		x = xx;
		
		if(event != null){
			currentEvent = event.getActivity();
		}
		refreshOrderBook(orderBook, event);
		refreshArrivingQueue(arrivingSellOrders, arrivingSellOrdersTableModel, event);
		refreshArrivingQueue(arrivingBuyOrders, arrivingBuyOrdersTableModel, event);
	}
	
	public OrderBookUserInterface(){
		super("Order Book Interface for Replay and Simulation Support - PAIS Lab.");
		JPanel main = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;
			/*Image bg = new ImageIcon(OrderBookWindow.class.getClassLoader().getResource("images/background.png")).getImage();

			@Override
			public void paintComponent(final Graphics g) {
				final int w = bg.getWidth(null);
				final int h = bg.getHeight(null);
				for (int x = 0; x < getWidth(); x += w) {
					for (int y = 0; y < getHeight(); y += h) {
						g.drawImage(bg, x, y, w, h, this);
					}
				}
			}*/
		};
		this.setContentPane(main);
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setResizable(false);
		addWindowListener();
		addComponentsToPane();
		
	}
	
	public void addComponentsToPane(){	
		Container contentPane = getContentPane();
		
		try{
			JPanel headerPanel = new JPanel();
			headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
			headerPanel.setOpaque(false);
			
			ImageIcon paisIcon = createImageIcon("images/pais.png");
			JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			iconPanel.add(new JLabel(paisIcon));
			iconPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
			iconPanel.setOpaque(true);
			headerPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

			JPanel aux = new JPanel();
			aux.setLayout(new BorderLayout());
			
			JPanel simulationControlPanel = new JPanel();
			simulationControlPanel.setOpaque(false);
			simulationControlPanel.setLayout(new BoxLayout(simulationControlPanel, BoxLayout.X_AXIS));
			simulationControlPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Simulation Control"),
            BorderFactory.createEmptyBorder(0,0,0,0))); 
			ImageIcon stopButtonIcon = createImageIcon("images/stop.png");
			ImageIcon playButtonIcon = createImageIcon("images/play2.png");
			ImageIcon pauseButtonIcon = createImageIcon("images/pause.png");
			ImageIcon nextStepIcon = createImageIcon("images/onestep.png");
			
			if(Main.getApplicationMode() == Utils.MODE_FILE){
			
				JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				JButton playButton = new JButton("", playButtonIcon);
			    playButton.setMnemonic(KeyEvent.VK_SPACE);
			    playButton.setPreferredSize(new Dimension(30, 30));
				playButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int state = Main.getApplicationState();
						if(state == Utils.APP_STATE_INITIAL || state == Utils.APP_STATE_PAUSED){
							playButton.setIcon(pauseButtonIcon);
							try {
								play();
							} catch (Exception e1){
								e1.printStackTrace();
							}
						}else if (state == Utils.APP_STATE_RUNNING){
							try {
								pause();
							} catch (Exception e1) {
								e1.printStackTrace();
							}
							playButton.setIcon(playButtonIcon);
						}
					}
				});
				JButton stopButton = new JButton("", stopButtonIcon);
				stopButton.setPreferredSize(new Dimension(30, 30));
				stopButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int state = Main.getApplicationState();
						if(state != Utils.APP_STATE_INITIAL){
							try{
								stop();
								playButton.setIcon(playButtonIcon);
							}catch(Exception e1){
								e1.printStackTrace();
							}
						}
					}
				});
				JButton nextStepButton = new JButton("", nextStepIcon);
			    nextStepButton.setMnemonic(KeyEvent.VK_SPACE);
			    nextStepButton.setPreferredSize(new Dimension(30, 30));
			    nextStepButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int state = Main.getApplicationState();
							try{
								nextStep();
							}catch(Exception e1){
								e1.printStackTrace();
							}
						}
				});
				buttonsPanel.add(stopButton);
				buttonsPanel.add(playButton);
				buttonsPanel.add(nextStepButton);			
				simulationControlPanel.add(buttonsPanel);
			}
			
			currentEventLabel = new JLabel("<html></html>");
			Font f = currentEventLabel.getFont();
			currentEventLabel.setFont(f.deriveFont(f.getStyle() & ~Font.BOLD));
			simulationControlPanel.add(currentEventLabel);
			
			JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.addTab("Order Book", createBookPanel());
			tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
			
			//tabbedPane.addTab("Cases", createCasesPanel());
			//tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
			
			//tabbedPane.addTab("Trades", createTradesPanel());
			//tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
			
			aux.add(simulationControlPanel, BorderLayout.WEST);
			aux.add(iconPanel, BorderLayout.EAST);
			
			contentPane.add(headerPanel,BorderLayout.NORTH);
			contentPane.add(aux, BorderLayout.CENTER);
				contentPane.add(tabbedPane, BorderLayout.PAGE_END);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private JPanel createBookPanel(){
		
		JPanel container = new JPanel();
		container.setOpaque(false);
		container.setLayout(new BorderLayout(0,0));
		
		JPanel centerPanel = new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Order Book State"), BorderFactory.createEmptyBorder(0,0,0,0))); 
		centerPanel.setPreferredSize(new Dimension(480, 150));
		
		orderBookTableModel = new MyTableModel("1");		
		orderBookTableModel.setColumnIdentifiers(new Object[] { "#", "buy order", "size", "price", "", "#", "sell order", "size", "price"});
		
		orderBookTable = new JTable(orderBookTableModel);
		orderBookTable.setDefaultRenderer(Object.class, new MyTableCellRenderer());
		orderBookTable.getTableHeader().setReorderingAllowed(false);
		orderBookTable.setBackground(new Color(255, 255, 255, 192));
		orderBookTable.setOpaque(false);
		
		final JScrollPane scroller = new JScrollPane(orderBookTable);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setOpaque(false);
		scroller.getViewport().setOpaque(false);
		
		centerPanel.add(scroller);
		container.add(centerPanel, BorderLayout.CENTER);
		
		JPanel centerPanel2 = new JPanel();
		centerPanel2.setOpaque(false);
		centerPanel2.setLayout(new BoxLayout(centerPanel2, BoxLayout.Y_AXIS));
		centerPanel2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Incoming Buy Orders"), BorderFactory.createEmptyBorder(0,0,0,0))); 
		centerPanel2.setPreferredSize(new Dimension(180, 150));
		
		arrivingBuyOrdersTableModel = new MyTableModel("2");
		arrivingBuyOrdersTableModel.setColumnIdentifiers(new Object[] {"#", "order", "size", "price"});
		
		arrivingBuyOrdersTable = new JTable(arrivingBuyOrdersTableModel);
		arrivingBuyOrdersTable.setDefaultRenderer(Object.class, new MyTableCellRenderer());
		arrivingBuyOrdersTable.getTableHeader().setReorderingAllowed(false);
		arrivingBuyOrdersTable.setBackground(new Color(255, 255, 255, 192));
		arrivingBuyOrdersTable.setOpaque(false);
		
		final JScrollPane scroller2 = new JScrollPane(arrivingBuyOrdersTable);
		scroller2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller2.setOpaque(false);
		scroller2.getViewport().setOpaque(false);

		centerPanel2.add(scroller2);

		container.add(centerPanel2, BorderLayout.WEST);
		
		JPanel centerPanel3 = new JPanel();
		centerPanel3.setOpaque(false);
		centerPanel3.setLayout(new BoxLayout(centerPanel3, BoxLayout.Y_AXIS));
		centerPanel3.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Incoming Sell Orders"), BorderFactory.createEmptyBorder(0,0,0,0))); 
		centerPanel3.setPreferredSize(new Dimension(180, 150));
		
		arrivingSellOrdersTableModel = new MyTableModel("3");
		arrivingSellOrdersTableModel.setColumnIdentifiers(new Object[] {"#", "order", "size", "price"});
		
		arrivingSellOrdersTable = new JTable(arrivingSellOrdersTableModel);
		arrivingSellOrdersTable.setDefaultRenderer(Object.class, new MyTableCellRenderer());
		arrivingSellOrdersTable.getTableHeader().setReorderingAllowed(false);
		arrivingSellOrdersTable.setBackground(new Color(255, 255, 255, 192));
		arrivingSellOrdersTable.setOpaque(false);
		
		final JScrollPane scroller3 = new JScrollPane(arrivingSellOrdersTable);
		scroller3.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller3.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller3.setOpaque(false);
		scroller3.getViewport().setOpaque(false);

		centerPanel3.add(scroller3);

		container.add(centerPanel3, BorderLayout.EAST);
		
		return container;
	}
	
	private JPanel createTradesPanel(){
			
			JPanel centerContainerPanel = new JPanel();
			centerContainerPanel.setOpaque(false);
			centerContainerPanel.setLayout(new BorderLayout());
			JPanel headerPanel = new JPanel();
			headerPanel.setOpaque(false);
			headerPanel.add(new JLabel("<html><br></html>"));
			centerContainerPanel.add(headerPanel);
			
			JPanel centerPanel = new JPanel();
			centerPanel.setOpaque(false);
			centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
			centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Trades"), BorderFactory.createEmptyBorder(0,0,0,0))); 
			centerPanel.setPreferredSize(new Dimension(380, 150));
			
			final DefaultTableModel tableModel = new DefaultTableModel(){
				private static final long serialVersionUID = 1L;
				
				@Override
				public Class<?> getColumnClass(final int column){
					return super.getColumnClass(column);
				}
				
				@Override
				public boolean isCellEditable(final int row, final int column) {
					return false;
				}
			};
			tableModel.setColumnIdentifiers(new Object[] { "match", "seller", "buyer", "quantity"});
			
			JTable table = new JTable(tableModel);
			table.getTableHeader().setReorderingAllowed(false);
			table.setBackground(new Color(255, 255, 255, 192));
			table.setOpaque(false);
			
			final JScrollPane scroller = new JScrollPane(table);
			scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scroller.setOpaque(false);
			scroller.getViewport().setOpaque(false);
			
			centerPanel.add(scroller);
			centerContainerPanel.add(centerPanel);
			
			JPanel bottomPanel = new JPanel();
			bottomPanel.setOpaque(false);
			bottomPanel.add(new JLabel("<html><br></html>"));
			centerContainerPanel.add(bottomPanel);
			
			for(int i = 0; i < 9; i++) {
				tableModel.addRow(new Object[] {"","","","",""});
			}
		
			return centerContainerPanel;
		}
	
	private JPanel createCasesPanel(){
		
		JPanel centerContainerPanel = new JPanel();
		centerContainerPanel.setOpaque(false);
		centerContainerPanel.setLayout(new BoxLayout(centerContainerPanel, BoxLayout.Y_AXIS));
		
		JPanel headerPanel = new JPanel();
		headerPanel.setOpaque(false);
		headerPanel.add(new JLabel("<html><br></html>"));
		centerContainerPanel.add(headerPanel);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Cases"), BorderFactory.createEmptyBorder(0,0,0,0))); 
		centerPanel.setPreferredSize(new Dimension(380, 150));
		
		final DefaultTableModel tableModel = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			
			@Override
			public Class<?> getColumnClass(final int column){
				return super.getColumnClass(column);
			}
			
			@Override
			public boolean isCellEditable(final int row, final int column) {
				return false;
			}
		};
		tableModel.setColumnIdentifiers(new Object[] { "Order Number", "Order Id", "State", "Trace", "Attributes"});
		
		JTable table = new JTable(tableModel);
		table.getTableHeader().setReorderingAllowed(false);
		table.setBackground(new Color(255, 255, 255, 192));
		table.setOpaque(false);
		
		final JScrollPane scroller = new JScrollPane(table);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroller.setOpaque(false);
		scroller.getViewport().setOpaque(false);
		
		centerPanel.add(scroller);
		centerContainerPanel.add(centerPanel);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setOpaque(false);
		bottomPanel.add(new JLabel("<html><br></html>"));
		centerContainerPanel.add(bottomPanel);
		
		for(int i = 0; i < 9; i++) {
			tableModel.addRow(new Object[] {"","","","",""});
		}
	
		return centerContainerPanel;
	}
	
	 protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = OrderBookUserInterface.class.getClassLoader().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
	}
	 
	private void addWindowListener(){
		this.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
	}
	
	public void display(){
		
		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {	
				try{
					OrderBookUserInterface.this.pack();
					OrderBookUserInterface.this.setVisible(true);
					OrderBookUserInterface.this.setAutoRequestFocus(true);
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}
}