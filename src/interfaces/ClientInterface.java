package interfaces;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.ServerReceiver;
import com.ServerSender;

import util.ClientsHandler;
import util.GameSession;
import util.GameSessionsHandler;
import util.Transferable;

public interface ClientInterface {

	// public String id;
	// public String username;
	// public Player player;
	
	// public ClientState state;
	
	// private ObjectInputStream in;
	// private ObjectOutputStream out;
	// private ServerReceiver receiver;
	// private ServerSender sender;
	
	// private ArrayList<Transferable> queue;
	
	public void connect( ObjectInputStream _in, ObjectOutputStream _out, ServerSender _sender, ServerReceiver _receiver, GameSessionsHandler _sessionsHandler,  ClientsHandler _clientsHandler );
	public void connectFromHandler( ObjectInputStream _in, ObjectOutputStream _out, ServerSender _sender, ServerReceiver _receiver, ClientsHandler _clientsHandler );
	
	public void disconnect();
	public void disconnectFromHandler();
	
	public void enqueue( Transferable _data );
	public boolean emptyQueue();
	public Transferable shiftQueue();
	
	public GameSession findSession();
	public void joinSession( GameSession _session );
	public void leaveSession();

}
