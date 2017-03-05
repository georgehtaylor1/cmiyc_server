package util;

import java.util.UUID;

import game.GameData;
import game.Player;
import interfaces.GameSessionInterface;
import game.states.GameState;

public class GameSession implements GameSessionInterface {

	public enum State {
		OFFLINE, ONLINE
	}

	public String id;
	public GameData gameData;

	public State state;

	/**
	 * Create the game session and initialise it with a random ID
	 */
	public GameSession() {

		this.id = UUID.randomUUID().toString();
		this.gameData = new GameData();
		this.state = State.ONLINE;

	}

	/**
	 * Create the game session and add the given player to the session
	 * 
	 * @param _player
	 *            The player in the game session
	 */
	public GameSession(Player _player) {

		this.id = UUID.randomUUID().toString();

		this.gameData = new GameData();
		this.gameData.mode = _player.mode;
		this.gameData.state = GameState.PREGAME;

		this.state = State.ONLINE;

	}

	/**
	 * Create the game session with the given game data
	 * 
	 * @param _gameData
	 *            The game data with which to create the game
	 */
	public GameSession(GameData _gameData) {

		this.id = UUID.randomUUID().toString();

		this.gameData = _gameData;

		this.state = State.ONLINE;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.GameSessionInterface#fits(game.Player)
	 */
	public synchronized boolean fits(Player _player) {
		return this.gameData.fits(_player);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.GameSessionInterface#addPlayer(game.Player)
	 */
	public synchronized void addPlayer(Player _player) {
		this.gameData.players.put(_player.clientID,_player);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.GameSessionInterface#removePlayer(game.Player)
	 */
	public synchronized void removePlayer(Player _player) {
		this.gameData.players.remove(_player);
	}

}
