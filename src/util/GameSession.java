package util;

import java.util.UUID;

import game.GameData;
import game.Player;
import interfaces.GameSessionInterface;
import game.states.GameState;

public class GameSession implements GameSessionInterface {

	private enum State { OFFLINE, ONLINE }
	
	public String id;
	public GameData gameData;
	
	public State state;

	public GameSession() {
		
		this.id = UUID.randomUUID().toString();
		this.gameData = new GameData();
		this.state = State.ONLINE;
		
	}
	
	public GameSession( Player _player ) {
		
		this.id = UUID.randomUUID().toString();
		
		this.gameData = new GameData();
		this.gameData.mode = _player.mode;
		this.gameData.state = GameState.PREGAME;
		
		this.state = State.ONLINE;

	}
	
	public GameSession( GameData _gameData ) {
		
		this.id = UUID.randomUUID().toString();
		
		this.gameData = _gameData;
		
		this.state = State.ONLINE;
		
	}

	public synchronized boolean fits( Player _player ) { return this.gameData.fits( _player ); }

	public synchronized void addPlayer( Player _player ) { this.gameData.players.add( _player ); }

	public synchronized void removePlayer( Player _player ) { this.gameData.players.remove( _player ); }

}
