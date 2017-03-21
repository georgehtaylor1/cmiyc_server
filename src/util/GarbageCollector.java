package util;

import java.util.Map.Entry;

import launcher.Server;
import util.Client.ConnectionState;

public class GarbageCollector implements Runnable {

	public volatile Object gc;
	
	private Server server;
	
	public GarbageCollector( Server _server ) {
		this.gc = new Object();
		this.server = _server;
	}
	
	public void run() {
		
		// GarbageCollector will collect garbage every 10 seconds, or when it's specifically notified
		new Thread(
			new Runnable() {
				public void run() {
					while( true ) {
						try { Thread.sleep( 10000 ); } catch( Exception _exception ) { /* God Knows */ }
						synchronized( gc ) { gc.notify(); }
					}
				}
			}
		).start();
		
		while( true ) {
			synchronized( this.gc ) {
				try { this.gc.wait(); } catch( Exception _exception ) { _exception.printStackTrace(); }
			}
			
			this.clearClients();
			this.clearSessions();
			
		}
		
	}

	private void clearClients() {
		
		for( Entry<String, Client> pair : this.server.clients.entrySet() ) {
			
			Client client = pair.getValue();
			if( client.connectionState == ConnectionState.DISCONNECTED ) {
				client.session.removePlayer( client.player );
				this.server.clients.remove( client.id );
			}
		}
	}
	
	private void clearSessions() {
		
		for( Entry<String, GameSession> pair : this.server.sessionsHandler.sessions.entrySet() ) {
			
			GameSession session = pair.getValue();
			if( session.gameData.players.isEmpty() ) { this.server.sessionsHandler.endSession( session.id ); }
			
		}
		
	}

}