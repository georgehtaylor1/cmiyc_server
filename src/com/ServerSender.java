package com;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import constants.Commands.Action;
import game.Player;
import game.util.Position;
import util.Client;
import util.ClientsHandler;
import util.Debug;
import util.GameSessionsHandler;
import util.Transferable;

public class ServerSender implements Runnable {

	private boolean running;
	private final int exhaution = 10;

	public GameSessionsHandler sessionsHandler;
	private Client client;
	private ClientsHandler clientsHandler;

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
	public ServerSender(Client _client, ClientsHandler _clientsHandler, GameSessionsHandler _sessionsHandler) {

		this.client = _client;
		this.clientsHandler = _clientsHandler;
		this.sessionsHandler = _sessionsHandler;

		this.running = false;

	}

	/**
	 * End the server sender
	 */
	public void stopSending() {
		this.running = false;
	}

	/**
	 * if player is in a state that requires position from the other players
	 * 
	 * @return Whether the player is in a state that requires the position from the other players
	 */
	private boolean needsPosition() {
		switch (this.client.state) {
		case FINDING:
			return true;
		case PREGAME:
			return true;
		case PLAYING:
			return true;
		default:
			return false;
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
			this.clientsHandler.disconnectClient(this.client);
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
		HashMap<String, Position> ret = new HashMap<String, Position>();
		Iterator<Entry<String, Player>> i = this.client.session.gameData.players.entrySet().iterator();

		while (i.hasNext()) {

			Map.Entry<String, Player> pair = (Map.Entry<String, Player>) i.next();

			Player tplayer = pair.getValue();
			
			ret.put(tplayer.clientID, tplayer.position);
		}
		

		return new Transferable(Action.UPDATE_MOVEMENT, ret);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		Debug.say("started running");

		this.running = true;

		while (this.running) {

			if (!this.client.emptyQueue()) {
				this.sendData(this.client.shiftQueue());
			}
			if (this.needsPosition()) {
				this.sendData(this.transferablePosition());
			}

		}

	}

}
