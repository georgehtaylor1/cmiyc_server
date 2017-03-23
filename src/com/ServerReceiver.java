package com;

import com.util.CommandProcessor;

import util.Client;
import util.Transferable;

public class ServerReceiver implements Runnable {

	private volatile Client client;
	private CommandProcessor commandProcessor;

	private final int exhaution = 10;

	/**
	 * Create a new server receiver to listen for the corresponding client
	 * 
	 * @param _client
	 *            The client that the receiver is listening to
	 * @param _clientsHandler
	 *            The handler for the clients
	 * @param _sessionsHandler
	 *            The handler for the game sessions
	 */
	public ServerReceiver( Client _client ) {

		this.client = _client;
		this.commandProcessor = new CommandProcessor( this.client );

	}

	/**
	 * Read the data from the stream
	 * 
	 * @return The data read from the stream
	 */
	private synchronized Transferable readFromClient() {
		return this.readFromClient( 0 );
	}

	/**
	 * Attempt to read from the client provided the number of tries doesn't
	 * exceed the exhaustion limit
	 * 
	 * @param _tries
	 *            The number of tries so far
	 * @return The data read from the client
	 */
	private synchronized Transferable readFromClient( int _tries ) {

		if( _tries >= this.exhaution ) { this.client.disconnect(); return null; }

		Transferable data = null;

		try { data = ( Transferable )this.client.in.readObject(); }
		catch( Exception _exception ) { this.readFromClient( _tries + 1 ); }

		return data;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		new Thread( this.commandProcessor ).start();
		
		while( this.client.connectionState == Client.ConnectionState.CONNECTED ) {
			this.commandProcessor.queue.offer( this.readFromClient() );
			synchronized( this.commandProcessor.monitor ) {
				this.commandProcessor.monitor.notifyAll();
			}
		}

	}
}
