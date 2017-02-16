package interfaces;

import game.GameData;
import game.Player;
import util.GameSession;

public interface GameSessionsHandlerInterface {

	// private ConcurentHashMap<String, Gamesession> sessions;
	
	public GameSession findSession( Player _player );
	
	public GameSession newSession();
	public GameSession newSession( GameData _gameData );
	
	public void endSession( GameSession _session );
	public void endSession( String _sessionID );
	
}
