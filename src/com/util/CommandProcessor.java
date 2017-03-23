package com.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import constants.Commands.Action;
import constants.Commands.Key;
import game.Camera;
import game.Faction;
import game.Player;
import game.Treasure;
import game.states.GameState;
import game.states.PlayerState;
import game.states.TreasureState;
import game.util.Movement;
import states.ClientState;
import util.Client;
import util.Transferable;

public class CommandProcessor implements Runnable {

	private volatile Client client;
	
	public volatile Object monitor;
	
	public volatile ConcurrentLinkedQueue<Transferable> queue;
	
	public CommandProcessor( Client _client ) {
		this.client = _client;
		this.queue = new ConcurrentLinkedQueue<Transferable>();
		this.monitor = new Object();
	}
	
	public void run() {
		
		while( this.client.connectionState == Client.ConnectionState.CONNECTED ) {
		
			/**if( this.queue.isEmpty() ) {
				synchronized( this.monitor ) {
					try { this.monitor.wait(); } catch( Exception _exception ) { /* God knows.  }
				}
			}*/
			
			if( !this.queue.isEmpty() ){ this.processTransferable( this.queue.poll() ); }

		}
	}
	
	private void processTransferable( Transferable _data ) {
		switch( _data.action ) {
			case UPDATE_USERNAME:
				this.client.id = (String) _data.object.get(Key.CLIENT_USERNAME);
				this.client.player.clientID = (String) _data.object.get(Key.CLIENT_USERNAME);
				client.sessionsHandler.clients.put(client.id, this.client);
				this.client.session = client.sessionsHandler.findSession(client.player) ;
				if (client.session != null) {
					client.session.addPlayer(client.player);
					HashMap<Key, Object> _hash = new HashMap<Key, Object>();
					_hash.put(Key.CLIENT_ID, client.id);
					_hash.put(Key.FACTION, client.player.faction);
					Transferable t = new Transferable(Action.ADD_PLAYER, _hash);
					for (String c : client.session.getClients()) {
						if (!(c.equals(this.client.id))) {
							client.sessionsHandler.clients.get(c).send(t);
							HashMap<Key, Object> _hash2 = new HashMap<Key, Object>();
							_hash2.put(Key.CLIENT_ID, c);
							_hash2.put(Key.FACTION, client.sessionsHandler.clients.get(c).player.faction);
							Transferable t2 = new Transferable(Action.ADD_PLAYER, _hash2);
							client.send(t2);
						}
					}
					client.session.getClients().add(client.id);
				}
				else {
					client.session = client.sessionsHandler.newSession();
					client.session.addPlayer(client.player);
					client.session.getClients().add(client.id);
				}
				break;
			case UPDATE_PLAYER_STATE:
				String _id = (String) _data.object.get(Key.CLIENT_ID);
				int size = this.client.session.gameData.players.size();
				int oog = 0;
				for (Player p : this.client.session.gameData.players.values()) {
					if (p.clientID == _id) {
						p.state = (PlayerState) _data.object.get(Key.PLAYER_STATE);
					}
					if (p.state != PlayerState.NORMAL)
						oog++;
				}
				sendToSession(new Transferable(Action.UPDATE_PLAYER_STATE, _data));
				if ((size == 3 && oog == 1) || (size == 5 && oog == 2))
					this.client.session.gameData.state = GameState.POSTGAME;
				break;
			case UPDATE_CLIENT_STATE:
				this.client.setState((ClientState) _data.object.get(Key.CLIENT_STATE));
				break;
			case UPDATE_MOVEMENT:
				Movement _mov = (Movement) _data.object.get(Key.POSITION);
				this.client.player.position = _mov.position;
				if (this.client.player.faction == Faction.SECURITY) {
					this.client.player.direction = _mov.direction;
					this.client.player.battery = _mov.battery;
				}
				break;
			case UPDATE_TREASURE_STATE:
				for (Treasure t : this.client.session.gameData.treasures) {
					if (t.id == (String) _data.object.get(Key.TREASURE_ID)) {
						t.state = (TreasureState) _data.object.get(Key.TREASURE_STATE);
						break;
					}
				}
				sendToSession(new Transferable(Action.UPDATE_TREASURE_STATE, _data));
				break;
			case DEPLOY_CAMERA:
				this.client.session.gameData.cameras.add((Camera) _data.object.get(Key.CAMERA));
				sendToSession(new Transferable(Action.DEPLOY_CAMERA, _data));
				break;
			default:
				break;
		}
	}
	
	private void sendToSession(Transferable _data) {
		ArrayList<String> clients = this.client.session.getClients();
		for (String c : clients) {
			if (!(c.equals(client.id))) {
				this.client.sessionsHandler.clients.get(c).send(_data);
			}
		}
	}
}
