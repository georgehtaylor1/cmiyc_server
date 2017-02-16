package interfaces;

import game.Player;

public interface GameSessionInterface {

	// public String id;
	// public GameData gameData;
	
	public boolean fits( Player _player );
	
	public void addPlayer( Player _player );
	public void removePlayer( Player _player );	
	
}
