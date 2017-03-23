package interfaces;

import java.util.ArrayList;

import game.Player;

public interface GameSessionInterface {

	// public String id;
	// public GameData gameData;
	
	/**
	 * Get a list of IDs of clients in the session
	 * 
	 * @return ArrayList of client IDs
	 */
	public ArrayList<String> getClients();

	/**
	 * Determine whether the player will fit into the game session
	 * 
	 * @param _player
	 *            The player to fit into the session
	 * @return Whether the player will fit into the game session
	 */
	public boolean fits(Player _player);

	/**
	 * Add the given player to the list of players in the session
	 * 
	 * @param _player
	 *            The player to add to the list
	 */
	public void addPlayer(Player _player);

	/**
	 * Remove the given player from the list of players in the session
	 * 
	 * @param _player
	 *            The player to remove from the game session
	 */
	public void removePlayer(Player _player);

}
