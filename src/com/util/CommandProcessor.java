package com.util;

import util.Client;
import util.Transferable;

public class CommandProcessor implements Runnable {

	private Transferable data;
	private Client client;
	
	public CommandProcessor( Client _client, Transferable _data ) {
		this.client = _client;
		this.data = _data;
	}
	
	public void run() {
		switch( this.data.action ) {
			
			case UPDATE_USERNAME : break;
			case UPDATE_PLAYER_STATE : break;
			case UPDATE_CLIENT_STATE : break;
			case UPDATE_MOVEMENT : break;
			case UPDATE_TREASURE_STATE : break;
			case DEPLOY_CAMERA : break;
			// Actions to be completed above
			// Actions to be added below
			default : break;
			
		}
	}

}
