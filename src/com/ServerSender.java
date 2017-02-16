package com;

import java.io.IOException;

import constants.Commands.Action;
import util.Client;
import util.ClientsHandler;
import util.Debug;
import util.GameSessionsHandler;
import util.Transferable;

public class ServerSender implements Runnable {

	private boolean running;
	private final int exhaution = 10;
	
	public GameSessionsHandler sessionsHandler;
	private Client client;
	private ClientsHandler clientsHandler;

	public ServerSender(Client _client, ClientsHandler _clientsHandler, GameSessionsHandler _sessionsHandler) {

		this.client = _client;
		this.clientsHandler = _clientsHandler;
		this.sessionsHandler = _sessionsHandler;
		
		this.running = false;

	}
	
	public void stopSending() { this.running = false; }
	
	// if player is in a state that requires position from the other players, this returns true
	private boolean needsPosition() {
		switch( this.client.state ) {
			case FINDING : return true;
			case PREGAME : return true;
			case PLAYING : return true;
			default      : return false;
		}
	}
	
	// this either sends data, or disconnects the client (disconnects if streams are kinda messed up for some reason)
	private synchronized void sendData( Transferable _data ) {
				
		this.sendData( _data, 0 );
	}
	
	// the recursive call of the above method
	private synchronized boolean sendData( Transferable _data, int _tries ) {
		
		if( _tries >= this.exhaution ) {  Debug.say( "error on more than 10 tries to send something" );this.clientsHandler.disconnectClient( this.client ); return false; }
		
		Transferable data = _data;
		
		try {  this.client.out.reset(); }
		catch( Exception _exception ) { return this.sendData( data , ( _tries + 1 ) ); }
		
		try { this.client.out.writeObject( data ); }
		catch( Exception _exception ) { return this.sendData( data , ( _tries + 1 ) ); }
		
		try { this.client.out.flush(); }
		catch( Exception _exception ) { return this.sendData( data , ( _tries + 1 ) ); }

		return true;
		
	}
	
	// basically sends the player list for this session EXDEEEEEEEEEEEEEEEEE
	private Transferable transferablePosition() {
						
		return new Transferable( Action.UPDATE_MOVEMENT, this.client.session.gameData.players );
		
	}
	
	// we should all know what this is..
	@Override
	public void run() {
		
		Debug.say( "started running" );
		
		this.running = true;
		
		while( this.running ) {
						
			if( !this.client.emptyQueue() ) { this.sendData( this.client.shiftQueue() ); }
			if( this.needsPosition() ) { this.sendData( this.transferablePosition() ); }
			
		}
		
	}
	
}
