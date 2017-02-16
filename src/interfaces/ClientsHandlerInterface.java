package interfaces;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import util.Client;
import util.GameSessionsHandler;
import util.Transferable;

public interface ClientsHandlerInterface {

	// private ConcurrentMap<String, Client> clients;
	
	public void connectClient( Client _client, ObjectInputStream _in, ObjectOutputStream _out, GameSessionsHandler _sessionsHandler );
	public void disconnectClient( Client _client );
	public void disconnectClient( String _clientID );
	
	public void sendTo( Client _client, Transferable _data );
	public void sendTo( String _clientID, Transferable _data );
	public void sendTo( ArrayList<String> _clientIDs, Transferable _data );
	public void sendTo( ArrayList<Client> _clients, Transferable _data, boolean _isClientList );
	
	public void sendToAll(  Transferable _data );
	
}
