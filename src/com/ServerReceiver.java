package com;

import java.util.ArrayList;
import java.util.HashMap;

import constants.Commands.Action;
import constants.Commands.Key;
import game.Camera;
import game.states.PlayerState;
import game.states.TreasureState;
import game.util.Position;
import states.ClientState;
import util.Client;
import util.ClientsHandler;
import util.Debug;
import util.GameSessionsHandler;
import util.Transferable;

public class ServerReceiver implements Runnable {

	private Client client;
	private ClientsHandler clientsHandler;
	private GameSessionsHandler sessionsHandler;

	private boolean running;

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
	public ServerReceiver(Client _client, ClientsHandler _clientsHandler, GameSessionsHandler _sessionsHandler) {

		this.client = _client;
		this.clientsHandler = _clientsHandler;
		this.sessionsHandler = _sessionsHandler;

		this.running = false;

	}

	/**
	 * Stop the receiver from listening for communication
	 */
	public void stopReceiving() {
		this.running = false;
	}

	// FIXME: Same as above?
	/**
	 * Stop the receiver from listening for communication
	 */
	public void closeCommunication() {
		this.running = false;
	}

	/**
	 * Read the data from the stream
	 * 
	 * @return The data read from the stream
	 */
	private synchronized Transferable readFromClient() {
		return this.readFromClient(0);
	}

	/**
	 * Attempt to read from the client provided the number of tries doesn't exceed the exhaustion limit
	 * 
	 * @param _tries
	 *            The number of tries so far
	 * @return The data read from the client
	 */
	private synchronized Transferable readFromClient(int _tries) {

		if (_tries >= this.exhaution) {

			this.clientsHandler.disconnectClient(this.client);
			this.running = false;
			return null;

		}

		Transferable data = null;

		try {
			data = (Transferable) this.client.in.readObject();
		} catch (Exception _exception) {
			this.readFromClient(_tries + 1);
		}

		return data;

	}

	/**
	 * Process the read data based on the type of the received data
	 * 
	 * @param _data
	 *            The data to be processed
	 */
	private void processData(Transferable _data) {

		if (_data == null) {
			return;
		}

		Transferable data = _data;

		switch (data.action) {
		case UPDATE_USERNAME:
			this.updateUsername(data.object);
			return;
		case UPDATE_PLAYER_STATE:
			this.updatePlayerState(data.object);
			return;
		case UPDATE_CLIENT_STATE:
			this.updateClientState(data.object);
			return;
		case UPDATE_MOVEMENT:
			this.updatePlayerPosition(data.object);
			return;
		case UPDATE_TREASURE_STATE:
			this.updateTreasureState(data.object);
			return;
		case DEPLOY_CAMERA:
			this.deployCamera(data.object);
			return;
		case START_DRAG:
			this.dragPlayer(data.object);
			return;
		case STOP_DRAG:
			this.dragPlayer(null);
			return;
		default:
			return;
		}

	}

	/**
	 * Indicate that a player is now being dragged
	 * 
	 * @param object
	 *            The data from the client
	 */
	private void dragPlayer(HashMap<Key, Object> object) {
		if (object == null) {
			this.client.player.dragging = null;
		} else {
			this.client.player.dragging = (String) object.get(Key.DRAGGED_PLAYER);
		}
	}

	/**
	 * Create a new camera for the player and place it
	 * 
	 * @param object
	 *            The data from the client
	 */
	private void deployCamera(HashMap<Key, Object> object) {
		this.client.session.gameData.cameras.add((Camera) object.get(Key.CAMERA));
		ArrayList<String> ret = new ArrayList<String>();
		for (int i = 0; i < this.client.session.gameData.players.size(); i++) {
			ret.add(this.client.session.gameData.players.get(i).clientID);
		}
		HashMap<Key, Object> trans = new HashMap<Key, Object>();
		trans.put(Key.CAMERA, this.client.session.gameData.cameras);
		clientsHandler.sendTo(ret, new Transferable(Action.DEPLOY_CAMERA, trans));
	}

	/**
	 * Update the state of the given treasure
	 * 
	 * @param object
	 *            The data from the client
	 */
	private void updateTreasureState(HashMap<Key, Object> object) {
		String treasureID = (String) object.get(Key.TREASURE_ID);
		for (int i = 0; i < this.client.session.gameData.treasures.size(); i++) {
			if (this.client.session.gameData.treasures.get(i).id == treasureID) {
				this.client.session.gameData.treasures.get(i).state = (TreasureState) object.get(Key.TREASURE_STATE);
			}
		}
		ArrayList<String> ret = new ArrayList<String>();
		for (int i = 0; i < this.client.session.gameData.players.size(); i++) {
			ret.add(this.client.session.gameData.players.get(i).clientID);
		}
		HashMap<Key, Object> trans = new HashMap<Key, Object>();
		trans.put(Key.TREASURE_ID, treasureID);
		trans.put(Key.TREASURE_STATE, (TreasureState) object.get(Key.TREASURE_STATE));
		clientsHandler.sendTo(ret, new Transferable(Action.UPDATE_TREASURE_STATE, trans));
	}

	/**
	 * Update the position of the player and any players that they are dragging
	 * 
	 * @param object
	 *            The data from the client
	 */
	private void updatePlayerPosition(HashMap<Key, Object> object) {
		this.client.player.position = (Position) object.get(Key.POSITION);
	}

	/**
	 * Update the username for the client
	 * 
	 * @param _object
	 *            The data from the client
	 */
	private void updateUsername(HashMap<Key, Object> _object) {

		Debug.say("Username Update Command Received");

		String username = (String) _object.get(Key.UNDEFINED);

		Debug.say("Username Update Command Completed [ " + username + " ]");

		this.client.username = username;

		Debug.say("Sending back confirmation.");
		this.clientsHandler.sendTo(this.client, new Transferable(Action.SUCCESSFULL_CONNECTION));

	}

	/**
	 * Update the current state of the player
	 * 
	 * @param _object
	 *            The data from the client
	 */
	public void updatePlayerState(HashMap<Key, Object> _object) {
		Debug.say("Player State Update Command Recieved");

		PlayerState state = (PlayerState) _object.get(Key.PLAYER_STATE);

		this.client.player.state = state;

		Debug.say("Player State Command Completed");

		ArrayList<String> ret = new ArrayList<String>();
		for (int i = 0; i < this.client.session.gameData.players.size(); i++) {
			ret.add(this.client.session.gameData.players.get(i).clientID);
		}
		HashMap<Key, Object> trans = new HashMap<Key, Object>();
		trans.put(Key.PLAYER_STATE, this.client.player);
		clientsHandler.sendTo(ret, new Transferable(Action.UPDATE_PLAYER_STATE, trans));
	}

	/**
	 * Update the state of the client
	 * 
	 * @param _object
	 *            the data from the client
	 */
	public void updateClientState(HashMap<Key, Object> _object) {
		Debug.say("Client State Update Command Recieved");

		ClientState state = (ClientState) _object.get(Key.CLIENT_STATE);

		this.client.state = state;

		Debug.say("Client State Command Completed");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		this.running = true;

		while (this.running) {
			this.processData(this.readFromClient());
		}

	}
}
