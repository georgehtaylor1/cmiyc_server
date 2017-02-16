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

	/**
	 * If the client is started without a username then set the username as
	 * "Unknown"
	 */
	public Client() {
		this("Unknown");
	}

	/**
	 * Initialise the new client object with a random user ID and create it's
	 * Player and GameSession objects
	 * 
	 * @param _username
	 *            The username for the client
	 */
	public Client(String _username) {
		this.id = UUID.randomUUID().toString();
		this.username = _username;
		this.player = new Player(this.id);
		this.sessionsHandler = null;
		this.clientsHandler = null;
		this.session = new GameSession(this.player);
		this.state = ClientState.DISCONNECTED;
		this.in = null;
		this.out = null;
		this.receiver = null;
		this.sender = null;
		this.queue = new ArrayList<Transferable>();
	}

	// FIXME: Are the sender and receiver parameters needed??
	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientInterface#connect(java.io.ObjectInputStream,
	 * java.io.ObjectOutputStream, com.ServerSender, com.ServerReceiver,
	 * util.GameSessionsHandler, util.ClientsHandler)
	 */
	public void connect(ObjectInputStream _in, ObjectOutputStream _out, ServerSender _sender, ServerReceiver _receiver,
			GameSessionsHandler _sessionsHandler, ClientsHandler _clientsHandler) {
		_clientsHandler.connectClient(this, _in, _out, _sessionsHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * interfaces.ClientInterface#connectFromHandler(java.io.ObjectInputStream,
	 * java.io.ObjectOutputStream, com.ServerSender, com.ServerReceiver,
	 * util.ClientsHandler)
	 */
	public void connectFromHandler(ObjectInputStream _in, ObjectOutputStream _out, ServerSender _sender,
			ServerReceiver _receiver, ClientsHandler _clientsHandler) {

		this.in = _in;
		this.out = _out;
		this.sender = _sender;
		this.receiver = _receiver;

		this.sessionsHandler = this.sender.sessionsHandler;
		this.clientsHandler = _clientsHandler;

		new Thread(this.sender).start();
		new Thread(this.receiver).start();

		this.state = ClientState.CONNECTED;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientInterface#disconnect()
	 */
	public void disconnect() {
		this.clientsHandler.disconnectClient(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientInterface#disconnectFromHandler()
	 */
	public void disconnectFromHandler() {

		this.sender.stopSending();
		this.receiver.stopReceiving();

		try {
			this.in.close();
			this.out.close();
		} catch (IOException e) {
			/* Then they are already closed */ }

		this.sender = null;
		this.receiver = null;
		try {
			this.in.close();
			this.out.close();
		} catch (Exception e) {
		}
		this.in = null;
		this.out = null;
		this.session = null;
		this.sessionsHandler = null;

		this.state = ClientState.DISCONNECTED;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientInterface#enqueue(util.Transferable)
	 */
	public synchronized void enqueue(Transferable _data) {
		this.queue.add(_data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientInterface#emptyQueue()
	 */
	public synchronized boolean emptyQueue() {
		return (this.queue.size() == 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientInterface#shiftQueue()
	 */
	public synchronized Transferable shiftQueue() {
		Transferable data = this.queue.get(0);
		this.queue.remove(0);
		return data;
	}

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

}
