package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

public class ServerLauncher extends JFrame {

	private JPanel contentPane;
	public JButton mainButton;

	public ServerLauncher() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setPreferredSize( new Dimension( 300, 100 ) );
		contentPane.setMinimumSize( new Dimension( 300, 100 ) );
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setPreferredSize( new Dimension( 300, 100 ) );
		panel.setMinimumSize( new Dimension( 300, 100 ) );
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		mainButton = new JButton("Start");
		mainButton.setPreferredSize( new Dimension( 300, 100 ) );
		mainButton.setMinimumSize( new Dimension( 300, 100 ) );
		panel.add(mainButton, BorderLayout.CENTER);
	}

}
