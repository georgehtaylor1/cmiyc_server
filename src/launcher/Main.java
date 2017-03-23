package launcher;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import gui.ServerLauncher;
import util.Debug;

// TODO: GUI WITH PORT INPUT AND PLAYERLIST

public class Main {

	private String[] arguments;

	private ServerLauncher gui;
	private Server server; //TODO: replace with Server

	private int port;

	public Main(String _arguments[]) {

		this.arguments = _arguments;
		Debug.say("Set Arguments for later use.");

		this.gui = new ServerLauncher();
		Debug.say("Initialized Server Launcher GUI.");

		this.server = null;
		Debug.say("Initialized Server Process Handler to null.");

		this.gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.gui.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent _event) {
				//guiExit();
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

	
	private void guiExit() {

		Debug.say("Server Launcher Exit process has begun.");

		if (this.server == null) {
			System.exit(0);
			return;
		}

		switch (this.server.state) {
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

	private void mainButtonPress() {

		Debug.say("Main Button Press process has begun.");

		if (this.server == null) {
			this.startServer();
			return;
		}

		switch (this.server.state) {
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

	private void startServer() {

		this.server = new Server();
		Debug.say("Initialized Server Process Handler.");

		if (!this.server.init(this.port)) {
			return;
		}
		Debug.say("Prepared Server Handler for Starting.");

		new Thread(this.server).start();
		Debug.say("Server Thread Started.");

	}

	private void stopServer() {

		this.server.stop();
		Debug.say("Prepared Server Handler for Stopping.");

	}
	
	public static void main(String _arguments[]) {
		new Main(_arguments);
	}

}
