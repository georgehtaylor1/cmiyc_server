package interfaces;

import game.GameData;
import game.Player;
import util.GameSession;

public interface GameSessionsHandlerInterface {

	// private ConcurentHashMap<String, Gamesession> sessions;

	/**
	 * Try to find a game session that the given player fits in
	 * 
	 * @param _player
	 *            The player to place in a session
	 * @return A game session that the player can be added to, if none can be found then return null
	 */
	public GameSession findSession(Player _player);

	/**
	 * Create a new session and add it to the list of sessions
	 * 
	 * @return The new session
	 */
	public GameSession newSession();

	/**
	 * Create a new session with the given game data
	 * 
	 * @param _gameData
	 *            The game data with which to create the game session
	 * @return The new game session
	 */
	public GameSession newSession(GameData _gameData);

	/**
	 * End the given game session
	 * 
	 * @param _session
	 *            The game session to remove
	 */
	public void endSession(GameSession _session);

	/**
	 * Close the game session with the given ID
	 * 
	 * @param _sessionID
	 *            The ID of the game session to remove
	 */
	public void endSession(String _sessionID);

}
