package launcher;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import gui.ServerLauncher;
import states.ServerState;
import util.Client;
import util.Debug;
import util.GameSessionsHandler;
import util.GarbageCollector;

public class Server implements Runnable {

	public static enum State { STARTING, STARTED, STOPPING, STOPPED; }
	
	public volatile State state;
	
	private ServerSocket socket;
	private int port;
	
	public ServerLauncher gui;
	public volatile ConcurrentHashMap<String, Client> clients;
	public volatile GameSessionsHandler sessionsHandler;
	private GarbageCollector gc;
	
	private int exhaution;
	
	public Server() {
		
		this.state = State.STOPPED;
		
		this.socket = null;
		this.port = -1;
		this.gui = null;
		this.clients = null;
		this.sessionsHandler = null;
		this.gc = null;
		
		this.exhaution = 10;
	
		Debug.say("Server Initialized with defaults.");

	}
	
	private ServerSocket openServerSocket() {

		ServerSocket socket = null;

		try { socket = new ServerSocket( this.port ); }
		catch( Exception _exception ) {
			Debug.say( "Error Occured while trying to open Server Socket." );
			return null;
		}

		return socket;

	}

	private void startGarbageCollector() {
		new Thread( this.gc ).start();
		Debug.say("Changed Properties, set as ready for started.");
	}
	
	public boolean init( ServerLauncher _gui, int _port ) {

		this.state = State.STARTING;
		
		this.port = _port;
		if( ( this.socket = this.openServerSocket() ) == null ) { return false; }
		
		this.gui = _gui;
		this.clients = new ConcurrentHashMap<String, Client>();
		this.sessionsHandler = new GameSessionsHandler();
		this.gc = new GarbageCollector( this );
		
		Debug.say("Changed Properties, set as ready for starting.");

		return true;
		
	}
	
	public boolean stop() {
		this.state = State.STOPPING;

		this.sessionsHandler = null;
		this.clients = null;
		this.port = -1;

		if( this.socket != null ) {
			try { this.socket.close(); }
			catch( Exception _exception ) { /* Means the socket was already shitty */ }
		}
		this.socket = null;

		Debug.say("Changed Properties, closed socket, set as ready for stopping.");
		return true;
	}
	
	public void processNewClient( Socket _socket ) {

		new Thread( new Runnable() {
			public void run() {

				Socket socket = _socket;

				ObjectOutputStream out;
				ObjectInputStream in;

				try {
					out = new ObjectOutputStream( socket.getOutputStream() );
					out.flush();
				}
				catch( Exception _exception ) {
					Debug.say("Error Occured while trying to open new Output Stream.");
					return;
				}

				try {
					in = new ObjectInputStream( socket.getInputStream() );
				}
				catch( Exception _exception ) {
					Debug.say("Error Occured while trying to open new Input Stream.");
					return;
				}

				Client newClient = new Client();
				
				newClient.connect( in, out, sessionsHandler );
				newClient.bindGC( gc.gc );
				
				clients.put( newClient.id, newClient );

				Debug.say("New Client Successfully Processed and Stored.");

			}
		} ).start();

	}

	
	public void startListening() { this.startListening( 0 ); }
	
	public boolean startListening( int _triesCount ) {

		if( _triesCount >= this.exhaution ) {
			Debug.say("Error Occured while trying to accept connection.");
			this.stop();
			
			return false;
		}
		
		Socket socket;

		try { socket = this.socket.accept(); }
		catch( Exception _exception ) { return this.startListening( _triesCount + 1 ); }

		this.processNewClient( socket );
		Debug.say("Started new Thread that processes the new client.");

		return true;
		
	}

	public void run() {
		
		this.state = State.STARTED;

		Debug.say( "Server Process Started." );

		while( this.state == State.STARTED ) {

			Debug.say( "Waiting for Client to Connect." );
			this.startListening();

		}

		Debug.say( "Server Process Stopped." );

		this.state = State.STOPPED;

	}
	
}
