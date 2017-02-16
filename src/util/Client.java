package util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import com.ServerReceiver;
import com.ServerSender;

import game.GameData;
import game.Player;
import interfaces.ClientInterface;
import states.ClientState;
import game.states.GameState;

public class Client implements ClientInterface {

	public String id;
	public String username;
	public Player player;
	
	public GameSessionsHandler sessionsHandler;
	public GameSession session;
	
	public ClientState state;
	
	public ObjectInputStream in;
	public ObjectOutputStream out;
	private ServerReceiver receiver;
	private ServerSender sender;

	private ClientsHandler clientsHandler;

	private ArrayList<Transferable> queue;
	
	public Client() { this( "Unknown" ); }
	public Client( String _username ) {
		this.id = UUID.randomUUID().toString();
		this.username = _username;
		this.player = new Player( this.id );
		this.sessionsHandler = null;
		this.clientsHandler = null;
		this.session = new GameSession( this.player );
		this.state = ClientState.DISCONNECTED;
		this.in = null;
		this.out = null;
		this.receiver = null;
		this.sender = null;
		this.queue = new ArrayList<Transferable>();
	}
	
	public void connect( ObjectInputStream _in, ObjectOutputStream _out, ServerSender _sender, ServerReceiver _receiver, GameSessionsHandler _sessionsHandler,  ClientsHandler _clientsHandler ) { _clientsHandler.connectClient( this , _in, _out, _sessionsHandler ); }
	
	public void connectFromHandler( ObjectInputStream _in, ObjectOutputStream _out, ServerSender _sender, ServerReceiver _receiver, ClientsHandler _clientsHandler ) {
		
		this.in = _in;
		this.out = _out;
		this.sender = _sender;
		this.receiver = _receiver;
		
		this.sessionsHandler = this.sender.sessionsHandler;
		this.clientsHandler = _clientsHandler;
		
		new Thread( this.sender ).start();
		new Thread( this.receiver ).start();
		
		this.state = ClientState.CONNECTED;

	}
	
	public void disconnect() { this.clientsHandler.disconnectClient( this ); }

	public void disconnectFromHandler() {
		
		this.sender.stopSending();
		this.receiver.stopReceiving();
		
		try { this.in.close(); this.out.close(); }
		catch (IOException e) { /* Then they are already closed */ }
		
		this.sender = null;
		this.receiver = null;
		try {
		this.in.close();
		this.out.close();
		} catch (Exception e) {}
		this.in = null;
		this.out = null;
		this.session = null;
		this.sessionsHandler = null;
		
		this.state = ClientState.DISCONNECTED;
		
	}

	public synchronized void enqueue( Transferable _data ) { this.queue.add( _data ); }
	
	public synchronized boolean emptyQueue() { return ( this.queue.size() == 0 ); }
	
	public synchronized Transferable shiftQueue() { Transferable data = this.queue.get( 0 ); this.queue.remove( 0 ); return data; }
	
	public GameSession findSession() {
		
		GameSession session = this.sessionsHandler.findSession( this.player );
		
		if( session != null ) { return session; }
		
		GameData gameData = new GameData();
		gameData.mode = this.player.mode;
		gameData.state = GameState.PREGAME;
		
		return this.sessionsHandler.newSession( gameData );
				
	}
	public void joinSession( GameSession _session ) { this.session = _session; this.session.addPlayer( this.player ); }
	public void leaveSession() { this.session.removePlayer( this.player ); this.session = new GameSession( this.player ); }
	
}
