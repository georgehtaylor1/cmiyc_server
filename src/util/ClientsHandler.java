package util;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.ServerReceiver;
import com.ServerSender;

import interfaces.ClientsHandlerInterface;

public class ClientsHandler implements ClientsHandlerInterface {

	private ConcurrentHashMap<String, Client> clients;

	/**
	 * Create a new client handler
	 */
	public ClientsHandler() {
		this.clients = new ConcurrentHashMap<String, Client>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientsHandlerInterface#connectClient(util.Client,
	 * java.io.ObjectInputStream, java.io.ObjectOutputStream,
	 * util.GameSessionsHandler)
	 */
	public void connectClient(Client _client, ObjectInputStream _in, ObjectOutputStream _out,
			GameSessionsHandler _sessionsHandler) {

		ServerSender sender = new ServerSender(_client, this, _sessionsHandler);
		ServerReceiver receiver = new ServerReceiver(_client, this, _sessionsHandler);

		_client.connectFromHandler(_in, _out, sender, receiver, this);
		this.clients.put(_client.id, _client);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientsHandlerInterface#disconnectClient(util.Client)
	 */
	public void disconnectClient(Client _client) {
		_client.disconnectFromHandler();
		this.clients.remove(_client.id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * interfaces.ClientsHandlerInterface#disconnectClient(java.lang.String)
	 */
	public void disconnectClient(String _clientID) {

		Client client = this.clients.get(_clientID);

		if (client == null) {
			return;
		}

		client.disconnectFromHandler();
		this.clients.remove(_clientID);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientsHandlerInterface#sendTo(util.Client,
	 * util.Transferable)
	 */
	public void sendTo(Client _client, Transferable _data) {

		if (!this.clients.contains(_client)) {
			Debug.say("no client");
			return;
		}

		_client.enqueue(_data);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientsHandlerInterface#sendTo(java.lang.String,
	 * util.Transferable)
	 */
	public void sendTo(String _clientID, Transferable _data) {

		Client client = this.clients.get(_clientID);

		if (client == null) {
			return;
		}

		client.enqueue(_data);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientsHandlerInterface#sendTo(java.util.ArrayList,
	 * util.Transferable)
	 */
	public void sendTo(ArrayList<String> _clientIDs, Transferable _data) {

		for (String clientID : _clientIDs) {

			Client client = this.clients.get(clientID);

			if (client == null) {
				continue;
			}

			client.enqueue(_data);

		}

	}

	// FIXME: is _isClientList needed??
	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientsHandlerInterface#sendTo(java.util.ArrayList,
	 * util.Transferable, boolean)
	 */
	public void sendTo(ArrayList<Client> _clients, Transferable _data, boolean _isClientList) {

		for (Client client : _clients) {

			if (this.clients.contains(client)) {
				continue;
			}

			client.enqueue(_data);

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.ClientsHandlerInterface#sendToAll(util.Transferable)
	 */
	public void sendToAll(Transferable _data) {

		Iterator<Entry<String, Client>> i = this.clients.entrySet().iterator();

		while (i.hasNext()) {

			Map.Entry<String, Client> pair = (Map.Entry<String, Client>) i.next();

			Client client = pair.getValue();

			client.enqueue(_data);

		}

	}

}
