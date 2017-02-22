package util;

public class GarbageCollector implements Runnable {

	public volatile Object gc;
	
	public GarbageCollector() {
		this.gc = new Object();
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
	}
	
	private void clearSessions() {
		// Loop through sessions, if any dead, remove them.
	}

}