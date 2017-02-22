package launcher;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import gui.ServerLauncher;
import states.ServerState;
import util.Client;
import util.ClientsHandler;
import util.Debug;
import util.GameSessionsHandler;

public class ServerHandler implements Runnable {

	private boolean running;
	public ServerState serverState;

	private int port;

	private ServerLauncher gui;

	private ServerSocket socket;

	private GameSessionsHandler sessionsHandler;
	private ClientsHandler clientsHandler;

	/**
	 * The default constructor for the ServerHandler; clears the handler's
	 * properties
	 */
	public ServerHandler() {

		this.serverState = ServerState.STOPPED;
		this.running = false;

		this.port = -1;
		this.gui = null;
		this.socket = null;
		this.sessionsHandler = null;
		this.clientsHandler = null;

		Debug.say("Initialized Server Properties.");

	}

	/**
	 * Open the server socket to start listening for connections
	 * 
	 * @return The socket for the connection
	 */
	private ServerSocket openServerSocket() {

		ServerSocket socket = null;

		try {
			socket = new ServerSocket(this.port);
		} catch (Exception _exception) {
			Debug.say("Error Occured while trying to open Server Socket.");
			return null;
		}

		return socket;

	}

	/**
	 * Prepare the server GUI and open the socket
	 * 
	 * @param _gui
	 *            The server GUI
	 * @param _port
	 *            The port that the socket listens on
	 * @return True if the method executes correctly, False if the socket fails
	 *         to open
	 */
	public boolean prepareStart(ServerLauncher _gui, int _port) {

		this.gui = _gui;
		this.port = _port;

		this.gui.mainButton.setEnabled(false);

		this.running = true;
		this.serverState = ServerState.STARTING;

		if ((this.socket = this.openServerSocket()) == null) {
			return false;
		}
		;

		this.sessionsHandler = new GameSessionsHandler();
		this.clientsHandler = new ClientsHandler();

		Debug.say("Changed Properties, set as ready for starting.");

		return true;

	}

	/**
	 * Prepare the server to stop, close the socket, clear the handlers and
	 * disable the GUI button
	 */
	public void prepareStop() {

		this.gui.mainButton.setEnabled(false);

		this.running = false;
		this.serverState = ServerState.STOPPING;

		this.sessionsHandler = null;
		this.clientsHandler = null;
		this.port = -1;

		if (this.socket != null) {
			try {
				this.socket.close();
			} catch (Exception _exception) {
				/* Means the socket was already shitty */ }
		}
		this.socket = null;

		Debug.say("Changed Properties, closed socket, set as ready for stopping.");

	}

	/**
	 * Thread tto create handlers when clients connect
	 * 
	 * @param _socket
	 *            The socket that the server is listening on
	 */
	public void processNewClient(Socket _socket) {

		new Thread(new Runnable() {
			public void run() {

				Socket socket = _socket;

				ObjectOutputStream out;
				ObjectInputStream in;

				try {
					out = new ObjectOutputStream(socket.getOutputStream());
					out.flush();
				} catch (Exception _exception) {
					Debug.say("Error Occured while trying to open new Output Stream.");
					return;
				}

				try {
					in = new ObjectInputStream(socket.getInputStream());
				} catch (Exception _exception) {
					Debug.say("Error Occured while trying to open new Input Stream.");
					return;
				}

				clientsHandler.connectClient(new Client(), in, out, sessionsHandler);

				Debug.say("New Client Successfully Processed and Stored.");

			}
		}).start();

	}

	/**
	 * Start the server listening for new clients
	 */
	public void startListening() {

		Socket socket;

		// Blocks until a client connects
		try {
			socket = this.socket.accept();
		} catch (IOException _exception) {

			Debug.say("Error Occured while trying to accept connection.");
			this.prepareStop();

			return;

		}

		this.processNewClient(socket);
		Debug.say("Started new Thread that processes the new client.");

	}

	/**
	 * The thread to listen for any new connecting clients
	 */
	public void run() {

		// Indicate the server has started listening
		this.serverState = ServerState.STARTED;
		this.gui.mainButton.setText("Stop");
		this.gui.mainButton.setEnabled(true);

		Debug.say("Server Process Started.");

		// While ever the server is running try to continuously connect clients
		while (this.running) {

			Debug.say("Waiting for Client to Connect.");
			this.startListening();

		}

		Debug.say("Server Process Stopped.");

		// Indicate that the server has stopped
		this.serverState = ServerState.STOPPED;
		this.gui.mainButton.setText("Start");
		this.gui.mainButton.setEnabled(true);

	}

}
