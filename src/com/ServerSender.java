package com;

import java.util.ArrayList;
import java.util.Map.Entry;

import constants.Commands.Action;
import game.Player;
import states.ClientState;
import util.Client;
import util.Debug;
import util.Movement;
import util.Transferable;

public class ServerSender implements Runnable {

	private final int exhaution = 10;
	
	public volatile Object senderMonitor;

	private Client client;

	/**
	 * Create a new server sender for the specified client
	 * 
	 * @param _client
	 *            The client that this sender will communicate with
	 * @param _clientsHandler
	 *            The handler for all of the clients
	 * @param _sessionsHandler
	 *            The handler for the game sessions
	 */
	public ServerSender( Client _client ) {
	
		this.client = _client;
		this.senderMonitor = new Object();
		
	}

	/**
	 * if player is in a state that requires position from the other players
	 * 
	 * @return Whether the player is in a state that requires the position from the other players
	 */
	private boolean needsPosition() {
		switch (this.client.getState()) {
		case FINDING: return true;
		case PREGAME: return true;
		case PLAYING: return true;
		default: return false;
		}
	}

	/**
	 * Sends the specified data to the client, if there is an error sending the data, or it times out, then the connection will close
	 * 
	 * @param _data
	 *            Teh data to be sent
	 */
	private synchronized void sendData(Transferable _data) {

		this.sendData(_data, 0);
	}

	/**
	 * Recursively try to send the data
	 * 
	 * @param _data
	 *            The data to send
	 * @param _tries
	 *            The number of attempts to send
	 * @return Whether sending the data was successful or not
	 */
	private synchronized boolean sendData(Transferable _data, int _tries) {

		if (_tries >= this.exhaution) {
			Debug.say("error on more than 10 tries to send something");
			this.client.disconnect();
			return false;
		}

		Transferable data = _data;

		try {
			this.client.out.reset();
		} catch (Exception _exception) {
			return this.sendData(data, (_tries + 1));
		}

		try {
			this.client.out.writeObject(data);
		} catch (Exception _exception) {
			return this.sendData(data, (_tries + 1));
		}

		try {
			this.client.out.flush();
		} catch (Exception _exception) {
			return this.sendData(data, (_tries + 1));
		}

		return true;

	}

	/**
	 * Get the transferable list of players for the game session
	 * 
	 * @return The transferable list of players
	 */
	private Transferable transferablePosition() {
		
		ArrayList<Movement> object = new ArrayList<Movement>();
		
		for( Entry<String, Player> pair : this.client.session.gameData.players.entrySet() ) {
			
			Movement movement = new Movement( pair.getKey(), pair.getValue().position, pair.getValue().direction );
			
			object.add( movement );
			
		}
		
		return new Transferable( Action.UPDATE_MOVEMENT, object );
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		Debug.say("started running");

		while( this.client.connectionState == Client.ConnectionState.CONNECTED ) {

			if( ( this.client.queue.isEmpty() ) && ( this.client.getState() == ClientState.IDLE ) ) {
				synchronized( this.senderMonitor ) {
					try { this.senderMonitor.wait(); } catch( Exception _exception ) { /* God knows. */ }
				}
			}
			
			if( !this.client.queue.isEmpty()) { this.sendData( this.client.queue.poll() ); }
			if( this.needsPosition()) { this.sendData( this.transferablePosition() ); }

		}

	}

}
