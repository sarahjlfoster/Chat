import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.swing.JLabel;
import javax.swing.JTextArea;

public class UISample extends JFrame implements OnReceiveMessage{
	static SampleSocketClientPart6 client;
	static JButton toggle;
	static JButton clickit;
	public UISample() {
		super("Callable SocketClient");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// add a window listener
		this.addWindowListener(new WindowAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				// before we stop the JVM stop the example
				//client.isRunning = false;
				super.windowClosing(e);
			}
		});
	}
	public static boolean toggleButton(boolean isOn) {
		String t = UISample.toggle.getText();
		if(isOn) {
			UISample.toggle.setText("ON");
			UISample.toggle.setBackground(Color.BLUE);
			UISample.toggle.setForeground(Color.BLUE);
			clickit.setText("Click to Turn Off");
			return true;
		}
		else {
			UISample.toggle.setText("OFF");
			UISample.toggle.setBackground(Color.RED);
			UISample.toggle.setForeground(Color.RED);
			clickit.setText("Click to Turn On");
			return false;
		}
	}
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException ex) {
		} catch (InstantiationException ex) {
		} catch (IllegalAccessException ex) {
		} catch (UnsupportedLookAndFeelException ex) {
		}
		final UISample window = new UISample();
		window.setLayout(new BorderLayout());
		JPanel connectionDetails = new JPanel();
		final JTextField host = new JTextField();
		host.setText("127.0.0.1");
		final JTextField port = new JTextField();
		port.setText("3001");
		final JButton connect = new JButton();
		
		connect.setText("Connect");
		connectionDetails.add(host);
		connectionDetails.add(port);
		connectionDetails.add(connect);
		window.add(connectionDetails, BorderLayout.NORTH);
		JPanel area = new JPanel();
		area.setLayout(new BorderLayout());
		window.add(area, BorderLayout.CENTER);
		final JButton toggle = new JButton();
		toggle.setText("OFF");
		//Cache it statically (not great but it's a sample)
		UISample.toggle = toggle;
		BoxIcon icon = new BoxIcon(Color.blue,400,200, 2);
		icon.setText("This is a test");
		final JButton click = new JButton("Enter Message", 
				icon);
		icon.setParent(click);
		clickit = click;
		click.setPreferredSize(new Dimension(400,200));
		click.setText("Click to Turn On");
		click.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	String t = toggle.getText();
		    	//boolean isOn = UISample.toggleButton();
		    	boolean turnOn = toggle.getText().contains("OFF");
		    	//TODO send to server
		    	client.doClick(turnOn);
		    }
		});
		click.setEnabled(false);
		
		connect.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	client = new SampleSocketClientPart6();
		    	int _port = -1;
		    	try {
		    		_port = Integer.parseInt(port.getText());
		    	}
		    	catch(Exception num) {
		    		System.out.println("Port not a number");
		    	}
		    	if(_port > -1) {
			    	client = SampleSocketClientPart6.connect(host.getText(), _port);
			    	
			    	//METHOD 1 Using the interface
			    	client.registerListener(window);
			    	//METHOD 2 Lamba Expression (unnamed function to handle callback)
			    	/*client.registerListener(()->{	
			    		if(UISample.toggle != null) {
			    			UISample.toggle.setText("OFF");
			    			UISample.toggle.setBackground(Color.RED);
			    		}
			    	});*/
			    	
			    	
			    	//trigger any one-time data after client connects
			    	client.postConnectionData();
			    	connect.setEnabled(false);
			    	click.setEnabled(true);
		    	}
		    }
		});
		
		area.add(toggle, BorderLayout.CENTER);
		area.add(click, BorderLayout.SOUTH);
		
		window.setPreferredSize(new Dimension(400,600));
		window.pack();
		window.setVisible(true);
	}
	@Override
	public void onReceived(boolean isOn) {
		// TODO Auto-generated method stub
		if(UISample.toggle != null) {
			UISample.toggleButton(isOn);
		}
	}
}
/*
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UserTable {
	private JButton fresh;
    private DefaultTableModel model;
    private JTable table;
    private JScrollPane panel;
    String name;
    JPopupMenu menu;
    JMenuItem item;
    
    UserTable(int x, int y, int z, int w){
    	fresh = new JButton("Fresh");
    	String[] columnNames = {"Users", "PORT", "IP"};
    	model = new DefaultTableModel(null, columnNames);
    	table = new JTable(model);
    	panel = new JScrollPane(table);
    	panel.setBounds(x,y,z,w);
    	
    }
    
    public JScrollPane getTable(){
    	return this.panel;
    }
    
    public JButton getButton(){
    	return fresh;
    }
    
    public void resetTable(){
    	model.setRowCount(0);
    }
    
    public void resetName(final String name){
    	this.name = name;
    	menu = new JPopupMenu();
    	item = new JMenuItem();
    	item.addActionListener(new java.awt.event.ActionListener() {  
            public void actionPerformed(java.awt.event.ActionEvent evt) {  
            	int select = table.getSelectedRow();
            	if(select == -1){
            		return;
            	}else{
            		 String to_whom = (String) table.getValueAt(select, 0);
            		 //System.out.println(to_whom);
            		 //System.out.println(name+"1");
            		 String port = (String) table.getValueAt(select, 1);
            		 //System.out.println(port);
            		 String ip = (String) table.getValueAt(select, 2);
            		 if(to_whom.equals(name))
            			 return;
            		 try {
						Socket socket = new Socket(ip, Integer.parseInt(port));
						System.out.println(port);
						p2p_sender ppp = new p2p_sender(socket,true, name, to_whom);
						Thread t = new Thread(ppp);
						t.start();
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            }  
        });  
    	item.setText("chat!");
    	menu.add(item);
    	table.addMouseListener(new java.awt.event.MouseAdapter(){
    		public void mouseClicked(java.awt.event.MouseEvent evt){
    			if(evt.getButton() == java.awt.event.MouseEvent.BUTTON3){
    				int focusedRowIndex = table.rowAtPoint(evt.getPoint());
    				if(focusedRowIndex == -1){
    					return;
    				}
    				table.setRowSelectionInterval(focusedRowIndex, focusedRowIndex);
    				if(!table.getValueAt(focusedRowIndex, 0).equals(name))
    					menu.show(table, evt.getX(), evt.getY());
    			}
    		}
    	});
    }
    
    public void addRow(String name,String port, String IP){
    	model.addRow(new String[]{name, port, IP});
    }
}*/