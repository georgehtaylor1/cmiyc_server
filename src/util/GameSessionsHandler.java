package util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import game.GameData;
import game.Player;
import interfaces.GameSessionsHandlerInterface;

public class GameSessionsHandler implements GameSessionsHandlerInterface {

	public ConcurrentHashMap<String, GameSession> sessions;

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.GameSessionsHandlerInterface#findSession(game.Player)
	 */
	public GameSession findSession( Player _player ) {

		for( Map.Entry<String, GameSession> pair : this.sessions.entrySet() ) {

			GameSession session = pair.getValue();
			if( session.gameData.fits( _player ) ) { return session; }

		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.GameSessionsHandlerInterface#newSession()
	 */
	public GameSession newSession() {

		GameSession session = new GameSession();
		this.sessions.put( session.id, session );

		return session;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.GameSessionsHandlerInterface#newSession(game.GameData)
	 */
	public GameSession newSession( GameData _gameData ) {

		GameSession session = new GameSession( _gameData );
		this.sessions.put( session.id, session );

		return session;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.GameSessionsHandlerInterface#endSession(util.GameSession)
	 */
	public void endSession( GameSession _session ) {
		this.sessions.remove( _session.id );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.GameSessionsHandlerInterface#endSession(java.lang.String)
	 */
	public void endSession( String _sessionID ) {
		this.sessions.remove( _sessionID );
	}

}
