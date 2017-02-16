package com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import constants.Commands;
import constants.Commands.Action;
import constants.Commands.Key;
import game.Camera;
import game.Player;
import game.Treasure;
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
	
	public ServerReceiver( Client _client, ClientsHandler _clientsHandler, GameSessionsHandler _sessionsHandler ) {

		this.client = _client;
		this.clientsHandler = _clientsHandler;
		this.sessionsHandler = _sessionsHandler;

		this.running = false;
		
	}

	//public void run() {  }
	public void stopReceiving() { this.running = false; }
	
	// closes stuff around
	public void closeCommunication(){ this.running = false; }

	// These 2 below just reads the data ready on the steam
	private synchronized Transferable readFromClient() { return this.readFromClient( 0 ); }
	private synchronized Transferable readFromClient( int _tries ) {
		
		if( _tries >= this.exhaution ) {
			
			this.clientsHandler.disconnectClient( this.client ); this.running = false; return null;
			
		}
		
		Transferable data = null;
		
		try { data = ( Transferable ) this.client.in.readObject(); }
		catch( Exception _exception ) { this.readFromClient( _tries + 1 ); }
		
		return data;
		
	}
	
	// processes data depending on what it read from the Client
	private void processData( Transferable _data ) {
		
		if( _data == null ) { return; }
		
		Transferable data = _data;
		
		switch( data.action ) {
			case UPDATE_USERNAME : this.updateUsername( data.object ); return;
			case UPDATE_PLAYER_STATE : this.updatePlayerState(data.object); return;
			case UPDATE_CLIENT_STATE : this.updateClientState(data.object); return;
			case UPDATE_MOVEMENT : this.updatePlayerPosition(data.object); return;
			case UPDATE_TREASURE_STATE : this.updateTreasureState(data.object); return;
			case DEPLOY_CAMERA : this.deployCamera(data.object); return;
			case START_DRAG : this.dragPlayer(data.object); return;
			case STOP_DRAG : this.dragPlayer(null); return;
			default: return;
		}
		
	}
		
	private void dragPlayer(HashMap<Key, Object> object) {
		if(object == null) {
			this.client.player.dragging = null;
		}
		else {
			this.client.player.dragging = (String) object.get(Key.DRAGGED_PLAYER);
		}
	}

	private void deployCamera(HashMap<Key, Object> object) {
		this.client.session.gameData.cameras.add((Camera) object.get(Key.CAMERA));
		ArrayList<String> ret = new ArrayList<String>();
		for(int i=0; i < this.client.session.gameData.players.size(); i++) {
			ret.add(this.client.session.gameData.players.get(i).clientID);
		}
		HashMap<Key, Object> trans = new HashMap<Key, Object>();
		trans.put(Key.CAMERA, this.client.session.gameData.cameras);
		clientsHandler.sendTo(ret, new Transferable(Action.DEPLOY_CAMERA, trans));
	}

	private void updateTreasureState(HashMap<Key, Object> object) {
		String treasureID = (String) object.get(Key.TREASURE_ID);
		for(int i=0; i < this.client.session.gameData.treasures.size(); i++) {
			if(this.client.session.gameData.treasures.get(i).id == treasureID) {
				this.client.session.gameData.treasures.get(i).state = (TreasureState) object.get(Key.TREASURE_STATE);
			}
		}
		ArrayList<String> ret = new ArrayList<String>();
		for(int i=0; i < this.client.session.gameData.players.size(); i++) {
			ret.add(this.client.session.gameData.players.get(i).clientID);
		}
		HashMap<Key, Object> trans = new HashMap<Key, Object>();
		trans.put(Key.TREASURE_ID, treasureID);
		trans.put( Key.TREASURE_STATE, (TreasureState) object.get(Key.TREASURE_STATE) );
		clientsHandler.sendTo(ret, new Transferable(Action.UPDATE_TREASURE_STATE, trans));
	}

	private void updatePlayerPosition(HashMap<Key, Object> object) {
		this.client.player.position = (Position) object.get(Key.POSITION);
		// Checks if the player is dragging someone and if so, updates the dragged player's position to be the same as the current player
		if(this.client.player.dragging != null) {
			ArrayList<Player> players = this.client.session.gameData.players;
			for (int i=0; i < players.size(); i++) {
				if (players.get(i).clientID == this.client.player.dragging) this.client.session.gameData.players.get(i).position = (Position) object.get(Key.POSITION);
			}
		}
	}

	private void updateUsername( HashMap<Key, Object> _object ) {

		Debug.say( "Username Update Command Received" );
		
		String username = ( String )_object.get( Key.UNDEFINED );
		
		Debug.say( "Username Update Command Completed [ " + username + " ]" );

		this.client.username = username;
		
		Debug.say( "Sending back confirmation." );
		this.clientsHandler.sendTo( this.client , new Transferable( Action.SUCCESSFULL_CONNECTION ) );

	}
	
	public void updatePlayerState( HashMap<Key, Object> _object ) {
		Debug.say( "Player State Update Command Recieved" );
		
		PlayerState state = (PlayerState) _object.get(Key.PLAYER_STATE);
		
		this.client.player.state = state;
		
		Debug.say( "Player State Command Completed" );
		
		ArrayList<String> ret = new ArrayList<String>();
		for(int i=0; i < this.client.session.gameData.players.size(); i++) {
			ret.add(this.client.session.gameData.players.get(i).clientID);
		}
		HashMap<Key, Object> trans = new HashMap<Key, Object>();
		trans.put(Key.PLAYER_STATE, this.client.player);
		clientsHandler.sendTo(ret, new Transferable(Action.UPDATE_PLAYER_STATE, trans));
	}
	
	public void updateClientState( HashMap<Key, Object> _object ) {
		Debug.say( "Client State Update Command Recieved" );
		
		ClientState state = (ClientState) _object.get(Key.CLIENT_STATE);
		
		this.client.state = state;
		
		Debug.say( "Client State Command Completed" );
	}

	// complicated stuff below (o.o)
		@Override
		public void run() {
			
			this.running = true;
			
			while( this.running ) { this.processData( this.readFromClient() ); }
			
		}
}
