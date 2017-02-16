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
	
	public ClientsHandler() {
		this.clients = new ConcurrentHashMap<String, Client>();
	}
	
	public void connectClient( Client _client, ObjectInputStream _in, ObjectOutputStream _out, GameSessionsHandler _sessionsHandler ) {
		
		ServerSender sender = new ServerSender( _client, this, _sessionsHandler );
		ServerReceiver receiver = new ServerReceiver( _client, this, _sessionsHandler );
		
		
		_client.connectFromHandler( _in, _out, sender, receiver, this );
		this.clients.put( _client.id , _client);
		
	}
	public void disconnectClient( Client _client) { _client.disconnectFromHandler(); this.clients.remove( _client.id ); }
	public void disconnectClient( String _clientID) {
		
		Client client = this.clients.get( _clientID );
		
		if( client == null ) { return; }
		
		client.disconnectFromHandler();
		this.clients.remove( _clientID );
		
	}

	public void sendTo( Client _client, Transferable _data ) {
	
		if( !this.clients.contains( _client ) ) { Debug.say( "no client" ); return; }
		
		_client.enqueue( _data );
		
	}
	
	public void sendTo( String _clientID, Transferable _data ) {
		
		Client client = this.clients.get( _clientID );
		
		if( client == null ) { return; }

		client.enqueue( _data );
		
	}
	
	public void sendTo( ArrayList<String> _clientIDs, Transferable _data ) {
		
		for( String clientID : _clientIDs ) {
			
			Client client = this.clients.get( clientID );
			
			if( client == null ) { continue; }
		
			client.enqueue( _data );
			
		}
		
	}
	
	public void sendTo( ArrayList<Client> _clients, Transferable _data, boolean _isClientList ) {
		
		for( Client client : _clients ) {
			
			if( this.clients.contains( client ) ) { continue; }
			
			client.enqueue( _data );
			
		}
		
	}
	
	public void sendToAll( Transferable _data ) {
		
		Iterator<Entry<String, Client>> i = this.clients.entrySet().iterator();
		
		while( i.hasNext() ) {
			
			Map.Entry<String, Client> pair = ( Map.Entry<String, Client> ) i.next();
			
			Client client = pair.getValue();
			
			client.enqueue( _data );
			
		}
		
	}
	
}
