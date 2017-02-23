package util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ServerReceiver;
import com.ServerSender;

import game.GameData;
import game.Player;
import states.ClientState;
import game.states.GameState;

public class Client {

	public static enum ConnectionState { CONNECTED, DISCONNECTED; }
	
	public String id;
	public String username;
	public Player player;

	public GameSessionsHandler sessionsHandler;
	public GameSession session;

	private volatile ClientState state;
	public volatile ConnectionState connectionState;

	public ObjectInputStream in;
	public ObjectOutputStream out;
	private ServerReceiver receiver;
	private ServerSender sender;
	
	private volatile Object gc;

	public volatile ConcurrentLinkedQueue<Transferable> queue;

	/**
	 * If the client is started without a username then set the username as
	 * "Unknown"
	 */
	public Client() { this( "Unknown" ); }

	/**
	 * Initialise the new client object with a random user ID and create it's
	 * Player and GameSession objects
	 * 
	 * @param _username
	 *            The username for the client
	 */
	public Client( String _username ) {
		this.id = UUID.randomUUID().toString();
		this.username = _username;
		this.player = new Player( this.id );
		this.sessionsHandler = null;
		this.session = new GameSession(this.player);
		this.state = ClientState.IDLE; // change to IDLE;
		this.connectionState = ConnectionState.DISCONNECTED;
		this.in = null;
		this.out = null;
		this.receiver = null;
		this.sender = null;
		this.queue = new ConcurrentLinkedQueue<Transferable>();
		
		this.gc = null;
	}

	// FIXME: Are the sender and receiver parameters needed??
	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientInterface#connect(java.io.ObjectInputStream,
	 * java.io.ObjectOutputStream, com.ServerSender, com.ServerReceiver,
	 * util.GameSessionsHandler, util.ClientsHandler)
	 */
	public void connect(ObjectInputStream _in, ObjectOutputStream _out, GameSessionsHandler _sessionsHandler) {
		this.in = _in;
		this.out = _out;

		this.sender = new ServerSender( this );
		this.receiver = new ServerReceiver( this );
		
		this.sessionsHandler = _sessionsHandler;
		this.connectionState = ConnectionState.CONNECTED;
		
		new Thread( this.sender ).start();
		new Thread( this.receiver ).start();
		
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientInterface#disconnect()
	 */
	public void disconnect() {

		this.connectionState = ConnectionState.DISCONNECTED;

		try { this.in.close(); this.out.close(); }
		catch (IOException e) { /* Then they are already closed */ }

		this.in = null;
		this.out = null;
		this.session = null;
		this.sessionsHandler = null;

		this.sender = null;
		this.receiver = null;

		synchronized( this.gc ) { this.gc.notifyAll(); }
		
	}

	public void bindGC( Object _gc ) { this.gc = _gc; }
	
	public void send( Transferable _data ) { this.queue.offer( _data ); }
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientInterface#findSession()
	 */
	public GameSession findSession() {

		GameSession session = this.sessionsHandler.findSession(this.player);

		if (session != null) {
			return session;
		}

		GameData gameData = new GameData();
		gameData.mode = this.player.mode;
		gameData.state = GameState.PREGAME;

		return this.sessionsHandler.newSession(gameData);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientInterface#joinSession(util.GameSession)
	 */
	public void joinSession(GameSession _session) {
		this.session = _session;
		this.session.addPlayer(this.player);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientInterface#leaveSession()
	 */
	public void leaveSession() {
		this.session.removePlayer(this.player);
		this.session = new GameSession(this.player);
	}

	public void setState( ClientState _state ) {
		if( _state != ClientState.IDLE || _state != ClientState.POSTGAME ) { synchronized(this.sender.senderMonitor) { this.sender.senderMonitor.notify(); } }
		this.state = _state;
	}
	
	public ClientState getState() { return this.state; }
	
}
