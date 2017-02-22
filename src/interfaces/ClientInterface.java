package interfaces;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.ServerReceiver;
import com.ServerSender;

import util.ClientsHandler;
import util.GameSession;
import util.GameSessionsHandler;
import util.Transferable;

/**
 * 
 * @author A2
 *
 */
public interface ClientInterface {

	// public String id;
	// public String username;
	// public Player player;

	// public ClientState state;

	// private ObjectInputStream in;
	// private ObjectOutputStream out;
	// private ServerReceiver receiver;
	// private ServerSender sender;

	// private ArrayList<Transferable> queue;

	/**
	 * Connect and setup the client
	 * 
	 * @param _in
	 *            The input stream from the server
	 * @param _out
	 *            The output stream to the server
	 * @param _sender
	 *            The sender for transmitting data to the server
	 * @param _receiver
	 *            The receiver for processing received data from the server
	 * @param _sessionsHandler
	 *            The handler for the game sessions
	 * @param _clientsHandler
	 *            The handler for the clients
	 */
	public void connect(ObjectInputStream _in, ObjectOutputStream _out, ServerSender _sender, ServerReceiver _receiver,
			GameSessionsHandler _sessionsHandler, ClientsHandler _clientsHandler);

	/**
	 * Creates the new client from the data provided by the handler
	 * 
	 * @param _in
	 *            The input stream from the server
	 * @param _out
	 *            The output stream for sending data to the server
	 * @param _sender
	 *            The sender provided by the clients handler
	 * @param _receiver
	 *            The receiver provided by the clients handler
	 * @param _clientsHandler
	 *            The clients handler
	 */
	public void connectFromHandler(ObjectInputStream _in, ObjectOutputStream _out, ServerSender _sender,
			ServerReceiver _receiver, ClientsHandler _clientsHandler);

	/**
	 * Tell the clients handler to disconnect the client
	 */
	public void disconnect();

	/**
	 * Destroy the connection to the server
	 */
	public void disconnectFromHandler();

	/**
	 * Add the data to the message queue to be sent to the server
	 * 
	 * @param _data
	 *            The data to be sent to the server
	 */
	public void enqueue(Transferable _data);

	/**
	 * Indicates whether the queue is empty
	 * 
	 * @return Whether or not the queue is empty
	 */
	public boolean emptyQueue();

	/**
	 * Pop the data from the top of the queue
	 * 
	 * @return The data from the top of the queue
	 */
	public Transferable shiftQueue();

	/**
	 * Find a session to join or create a new session if one can't be found
	 * 
	 * @return The game session
	 */
	public GameSession findSession();

	/**
	 * Join the given game session
	 * 
	 * @param _session
	 *            The game session to join
	 */
	public void joinSession(GameSession _session);

	/**
	 * Leave the current game session
	 */
	public void leaveSession();

}
