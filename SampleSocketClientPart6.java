import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;


public class SampleSocketClientPart6 {
	private Socket server;
	private OnReceiveMessage listener;
	public void registerListener(OnReceiveMessage listener) {
		this.listener = listener;
	}
	private Queue<PayloadPart6> toServer = new LinkedList<PayloadPart6>();
	private Queue<PayloadPart6> fromServer = new LinkedList<PayloadPart6>();
	private boolean init = true;

	final static String[] ipAddr = {null};
	final static int[] portNum = {-1};

	static JFrame connectWindow = new JFrame();
	JPanel ipInput = new JPanel();
	JPanel portInput = new JPanel();
	JLabel ipLabel = new JLabel("IP Address:");
	JTextField ip = new JTextField("127.0.0.1");
	JLabel portLabel = new JLabel("Port");
	JTextField port = new JTextField("3001");
	JButton connectButton = new JButton("Connect!");

	JFrame usernameWindow = new JFrame();
	JPanel usernamePanel = new JPanel();
	JLabel usernameLabel = new JLabel("Please enter a username:");
	JTextField usernameField = new JTextField();

	JFrame chatWindow = new JFrame();
	JLabel chatTitle = new JLabel("Chat", SwingConstants.CENTER);
	JPanel chatFeedPanel = new JPanel();
	JTextArea chatFeed = new JTextArea();
	JPanel userInputPanel = new JPanel();
	JButton sendButton = new JButton("Send");
	JLabel inputLabel = new JLabel("Enter your message:", SwingConstants.LEFT);
	JTextArea userInput = new JTextArea();

	public ArrayList<String> messages = new ArrayList<>();
	String messageFeed = "";

	public SampleSocketClientPart6(){
		connectWindow.setSize(300,200);
		connectWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		usernameWindow.setSize(300,200);
		usernameWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		chatWindow.setSize(450, 650);
		chatWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		ipInput.setLayout(new FlowLayout());
		ipInput.setPreferredSize(new Dimension(connectWindow.getWidth(), connectWindow.getHeight()/2));

		portInput.setLayout(new FlowLayout());

		connectWindow.setLayout(new GridLayout(3, 1));


		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ipAddr[0] = ip.getText();
				portNum[0] = Integer.parseInt(port.getText());

			}
		});

		ipInput.add(ipLabel);
		ipInput.add(ip);
		portInput.add(portLabel);
		portInput.add(port);
		connectWindow.add(ipInput);
		connectWindow.add(portInput);
		connectWindow.add(connectButton);
		connectWindow.setVisible(true);
	}
	
	public static SampleSocketClientPart6 connect(String address, int port) {
		final SampleSocketClientPart6 client = new SampleSocketClientPart6();
		client._connect(address, port);
		Thread clientThread =  new Thread() {
			@Override
			public void run() {
				client.start();
			}
		};
		clientThread.start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return client;
	}
	private void _connect(String address, int port) {
		try {
			server = new Socket(address, port);
			System.out.println("Client connected");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		if(server == null) {
			return;
		}
		final String[] username = {null};
		usernameWindow.setLayout(new GridLayout(1,1));
		usernamePanel.setLayout(new FlowLayout());
		usernameField.setPreferredSize(new Dimension(150, 30));

		usernamePanel.add(usernameLabel);
		usernamePanel.add(usernameField);

		usernameWindow.add(usernamePanel);
		connectWindow.setVisible(false);
		usernameWindow.setVisible(true);
		usernameWindow.setFocusableWindowState(true);

		usernameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				username[0] = usernameField.getText();
			}
		});

		//Set up the chat window but don't activate it yet
		final String[] message = {null};
		chatWindow.setLayout(new BorderLayout(10,10));
		chatFeedPanel.setLayout(new FlowLayout());
		chatFeed.setEditable(false);
		chatFeed.setPreferredSize(new Dimension(chatWindow.getWidth()-20, 500));
		chatFeed.setBorder(BorderFactory.createLineBorder(Color.pink, 2));
		chatFeedPanel.add(chatFeed);
		chatWindow.add(chatTitle, BorderLayout.NORTH);
		chatWindow.add(chatFeedPanel, BorderLayout.CENTER);


		userInputPanel.setLayout(new FlowLayout());
		userInput.setPreferredSize(new Dimension(chatWindow.getWidth()/2, 60));
		userInput.setBorder(BorderFactory.createLineBorder(Color.pink, 1));
		userInputPanel.add(inputLabel);
		userInputPanel.add(userInput);
		userInputPanel.add(sendButton);

		chatWindow.add(userInputPanel, BorderLayout.SOUTH);

		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				message[0] = userInput.getText();
				userInput.setText(null);
			}
		});

		System.out.println("Client Started");
		//listen to console, server in, and write to server out
		try(	ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(server.getInputStream())
		){
			Thread inputThread = new Thread() {
				@Override
				public void run() {
					try {
						while(!server.isClosed()) {
							while(username[0] == null){
								System.out.println("waiting for username");
							}
							if (init) {
								sendUserName(username[0]);
								System.out.println("username " + username[0] + " sent!");
								init = false;
							} else {
								usernameWindow.setVisible(false);
								chatWindow.setVisible(true);
								chatWindow.setFocusableWindowState(true);
								if(message[0] != null){
									sendMessage(message[0]);
								}
								message[0] = null;
							}
							PayloadPart6 p = toServer.poll();
							if(p != null) {
								out.writeObject(p);
							}
							else {
								try {
									Thread.sleep(8);
								}
								catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
					catch(Exception e) {
						System.out.println("Client shutdown");
					}
					finally {
						close();
					}
				}
			};
			inputThread.start();//start the thread
			
			//Thread to listen for responses from server so it doesn't block main thread
			Thread fromServerThread = new Thread() {
				@Override
				public void run() {
					try {
						PayloadPart6 p;
						//while we're connected, listen for payloads from server
						while(!server.isClosed() && (p = (PayloadPart6)in.readObject()) != null) {
							//System.out.println(fromServer);
							//processPayload(fromServer);
							fromServer.add(p);
						}
						System.out.println("Stopping server listen thread");
					}
					catch (Exception e) {
						if(!server.isClosed()) {
							e.printStackTrace();
							System.out.println("Server closed connection");
						}
						else {
							System.out.println("Connection closed");
						}
					}
					finally {
						close();
					}
				}
			};
			fromServerThread.start();//start the thread
			
			
			Thread payloadProcessor = new Thread(){
				@Override
				public void run() {
					while(!server.isClosed()) {
						PayloadPart6 p = fromServer.poll();
						if(p != null) {
							processPayload(p);
						}
						else {
							try {
								Thread.sleep(8);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			};
			payloadProcessor.start();
			//Keep main thread alive until the socket is closed
			//initialize/do everything before this line
			while(!server.isClosed()) {
				Thread.sleep(50);
			}
			System.out.println("Exited loop");
			System.exit(0);//force close
			//TODO implement cleaner closure when server stops
			//without this, it still waits for input before terminating
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			close();
		}
	}

	public void sendUserName(String userName){
		PayloadPart6 payload = new PayloadPart6();
		payload.setPayloadType(PayloadTypePart6.USERNAME);
		payload.setUserName(userName);
		toServer.add(payload);
	}
	public void postConnectionData() {
		PayloadPart6 payload = new PayloadPart6();
		payload.setPayloadType(PayloadTypePart6.CONNECT);
		//payload.IsOn(isOn);
		toServer.add(payload);
	}
	public void doClick(boolean isOn) {
		PayloadPart6 payload = new PayloadPart6();
		payload.setPayloadType(PayloadTypePart6.SWITCH);
		payload.IsOn(isOn);
		toServer.add(payload);
	}
	public void sendMessage(String message) {
		PayloadPart6 payload = new PayloadPart6();
		payload.setPayloadType(PayloadTypePart6.MESSAGE);
		payload.setMessage(message);
		toServer.add(payload);
	}
	private void processPayload(PayloadPart6 payload) {
		System.out.println(payload);
		switch(payload.getPayloadType()) {
		case CONNECT:
			System.out.println(
					String.format("Client \"%s\" connected", payload.getMessage())
			);
			break;
		case DISCONNECT:
			System.out.println(
					String.format("Client \"%s\" disconnected", payload.getMessage())
			);
			break;
		case MESSAGE:
			System.out.println(
					String.format("%s", payload.getMessage())
			);
			messages.add(String.format("%s", payload.getMessage()));

			for(int i = 0; i < messages.size(); i++){
				if(messages.get(i).charAt(messages.get(i).length()-1) == '\n'){
					System.out.println("ending with a newline");
					messages.set(i, messages.get(i).substring(0, messages.get(i).length() - 1));
				}

				if(i == 0){
					messageFeed = messages.get(i) + "\n";
				}
				else if(i == messages.size() - 1 ){
					messageFeed = messageFeed + messages.get(i);
				}
				else{
					messageFeed = messageFeed + messages.get(i) + "\n";
				}
			}

			chatFeed.setText(messageFeed);
			
			break;
		case STATE_SYNC:
			System.out.println("Sync");
			//break; //this state will drop down to next state
		case SWITCH:
			System.out.println("switch");
			if (listener != null) {
				listener.onReceived(payload.IsOn());
			}
			break;
		default:
			System.out.println("Unhandled payload type: " + payload.getPayloadType().toString());
			break;
		}
	}
	private void close() {
		if(server != null) {
			try {
				server.close();
				System.out.println("Closed socket");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {

		SampleSocketClientPart6 client = new SampleSocketClientPart6();

		connectWindow.setVisible(true);

		while(ipAddr[0] == null && portNum[0] == -1){
			System.out.println(ipAddr[0]);
		}

		System.out.println("Got out of loop");
		client.connect(ipAddr[0], portNum[0]);
		try {
			//if start is private, it's valid here since this main is part of the class

			client.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

interface OnReceiveMessage{
	void onReceived(boolean isOn);
}