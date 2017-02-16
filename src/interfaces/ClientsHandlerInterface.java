package interfaces;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import util.Client;
import util.GameSessionsHandler;
import util.Transferable;

public interface ClientsHandlerInterface {

	// private ConcurrentMap<String, Client> clients;

	/**
	 * Setup the sender and receiver for the client and instruct the client to
	 * connect to the handler
	 * 
	 * @param _client
	 *            The client to connect
	 * @param _in
	 *            The input stream from the server for the client
	 * @param _out
	 *            The output stream to the server for the client
	 * @param _sessionsHandler
	 *            The session handler for the server
	 */
	public void connectClient(Client _client, ObjectInputStream _in, ObjectOutputStream _out,
			GameSessionsHandler _sessionsHandler);

	/**
	 * Disconnect the client from the server
	 * 
	 * @param _client
	 *            The client to disconnect
	 */
	public void disconnectClient(Client _client);

	/**
	 * Disconnect the client with the specified ID from the server
	 * 
	 * @param _clientID
	 *            The clients ID
	 */
	public void disconnectClient(String _clientID);

	/**
	 * Send the specified data to the specified client
	 * 
	 * @param _client
	 *            The client to send the data to
	 * @param _data
	 *            The data to send to the client
	 */
	public void sendTo(Client _client, Transferable _data);

	/**
	 * Send the specified data to the client with the specified ID
	 * 
	 * @param _clientID
	 *            The ID of the client to send the data to
	 * @param _data
	 *            The data to send to the client
	 */
	public void sendTo(String _clientID, Transferable _data);

	/**
	 * Send the specified data to every client specified by the list of their
	 * IDs
	 * 
	 * @param _clientIDs
	 *            The list of client IDs
	 * @param _data
	 *            The dat to send to the clients
	 */
	public void sendTo(ArrayList<String> _clientIDs, Transferable _data);

	/**
	 * Send the specified data to each client in the list of clients
	 * 
	 * @param _clients
	 *            The list of clients to send the data to
	 * @param _data
	 *            The data to send to the clients
	 * @param _isClientList
	 *            FIXME: is this needed
	 */
	public void sendTo(ArrayList<Client> _clients, Transferable _data, boolean _isClientList);

	/**
	 * Send the specified data to every client handled by the client handler
	 * 
	 * @param _data
	 *            The data to be sent
	 */
	public void sendToAll(Transferable _data);

}
