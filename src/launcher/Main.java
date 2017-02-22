package launcher;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import gui.ServerLauncher;
import util.Debug;

// TODO: GUI WITH PORT INPUT AND PLAYERLIST

public class Main {

	private String[] arguments;

	private ServerLauncher gui;
	private ServerHandler serverHandler;

	private int port;

	public Main(String _arguments[]) {

		this.arguments = _arguments;
		Debug.say("Set Arguments for later use.");

		this.gui = new ServerLauncher();
		Debug.say("Initialized Server Launcher GUI.");

		this.serverHandler = null;
		Debug.say("Initialized Server Process Handler to null.");

		this.gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.gui.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent _event) {
				guiExit();
			}
		});
		Debug.say("Set Action Handler for Closing the Launcher GUI.");

		this.gui.mainButton.addActionListener(event -> this.mainButtonPress());
		Debug.say("Set Action Handler for Pressing the Main Button ( Start / Stop ).");

		this.port = 1234;
		Debug.say("Set Default Port ( this should be taken from the input ).");

		this.gui.setVisible(true);
		this.gui.pack();
		Debug.say("Set GUI Visible.");

	}

	/**
	 * End the program when the gui is closed
	 */
	private void guiExit() {

		Debug.say("Server Launcher Exit process has begun.");

		if (this.serverHandler == null) {
			System.exit(0);
			return;
		}

		switch (this.serverHandler.serverState) {
		case STOPPED:
			System.exit(0);
			return;
		case STOPPING:
			System.exit(0);
			return;
		default:
			this.stopServer();
			System.exit(0);
		}

	}

	/**
	 * Start or stop the server when the button is pressed
	 */
	private void mainButtonPress() {

		Debug.say("Main Button Press process has begun.");

		if (this.serverHandler == null) {
			this.startServer();
			return;
		}

		switch (this.serverHandler.serverState) {
		case STOPPED:
			this.startServer();
			break;
		case STARTED:
			this.stopServer();
			break;
		default:
			return;
		}

	}

	/**
	 * Prepare and start the server
	 */
	private void startServer() {

		this.serverHandler = new ServerHandler();
		Debug.say("Initialized Server Process Handler.");

		if (!this.serverHandler.prepareStart(this.gui, this.port)) {
			return;
		}
		Debug.say("Prepared Server Handler for Starting.");

		new Thread(this.serverHandler).start();
		Debug.say("Server Thread Started.");

	}

	/**
	 * Stop the server
	 */
	private void stopServer() {

		this.serverHandler.prepareStop();
		Debug.say("Prepared Server Handler for Stopping.");

	}

	public static void main(String _arguments[]) {
		new Main(_arguments);
	}

}
