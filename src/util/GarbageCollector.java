package util;

import java.util.Collection;

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
		// Loop through clients, if any dead, remove them.
		for(String key : this.server.clients.keySet()) {
			if (this.server.clients.get(key).connectionState == ConnectionState.DISCONNECTED) this.server.clients.remove(key);
		}
	}
	
	private void clearSessions() {
		// Loop through sessions, if any dead, remove them.
		for(String key : this.server.sessionsHandler.sessions.keySet()) {
			if (this.server.sessionsHandler.sessions.get(key).state == GameSession.State.OFFLINE) this.server.sessionsHandler.sessions.remove(key);
		}
	}

}