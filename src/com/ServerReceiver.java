package com;

import java.util.HashMap;

import com.util.CommandProcessor;

import constants.Commands.Action;
import constants.Commands.Key;
import game.util.Position;
import util.Client;
import util.Debug;
import util.Transferable;

public class ServerReceiver implements Runnable {

	private Client client;

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

		while( this.client.connectionState == Client.ConnectionState.CONNECTED ) {
			new Thread( new CommandProcessor( this.client, this.readFromClient() ) ).start();
		}

	}
}
