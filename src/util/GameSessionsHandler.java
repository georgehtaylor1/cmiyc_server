package util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import game.GameData;
import game.Player;
import interfaces.GameSessionsHandlerInterface;

public class GameSessionsHandler implements GameSessionsHandlerInterface {

	private ConcurrentHashMap<String, GameSession> sessions;
	
	public GameSession findSession( Player _player ) {

		Iterator<Entry<String, GameSession>> i = this.sessions.entrySet().iterator();
		
		while( i.hasNext() ) {
			
			Map.Entry<String, GameSession> pair = ( Map.Entry<String, GameSession> ) i.next();
			
			GameSession session = pair.getValue();
			
			if( session.gameData.fits( _player ) ) { return session; }
			
		}

		return null;
	}

	public GameSession newSession() {
		
		GameSession session = new GameSession();
		this.sessions.put( session.id , session );

		return session;

	}

	public GameSession newSession( GameData _gameData ) {
	
		GameSession session = new GameSession( _gameData );		
		this.sessions.put( session.id , session );

		return session;

	}

	public void endSession( GameSession _session ) { this.sessions.remove( _session.id ); }

	public void endSession( String _sessionID ) { this.sessions.remove( _sessionID ); }

}
