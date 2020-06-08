package view.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import authorization.authorization_code.AuthorizationCodeUri;

/**
 * GUI class
 * @author Collin Kersten
 *
 */
public class ArtistRecommenderGUI implements ActionListener {
	private JPanel panel;
	private JLabel label;
	private JTextArea ta;
	private JScrollPane sp;
	private JButton enter;
	private JButton reset;
	private JButton auth;
	private JFrame frame;
	private JButton getRecs;
	private JList<String> recs;
	private JList<String> recentArtists;
	private JPanel panel1, panel2, panel3;
	private JLabel user;
	private JLabel artistHeader, recsHeader;
	
	/**
	 * sets up the first screen, calls the login method
	 */
	public ArtistRecommenderGUI() {
		frame = new JFrame("My First GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,550);
        setUpLogin();
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.CENTER, auth);
        frame.setVisible(true);
	}
	
	/**
	 * sets up the login screen
	 */
	private void setUpLogin() {
		panel = new JPanel();
        label = new JLabel("Enter URL");
        ta = new JTextArea(1,20);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        sp = new JScrollPane(ta);
        enter = new JButton("Enter");
        enter.addActionListener(this);
        reset = new JButton("Reset");
        reset.addActionListener(this);
        auth = new JButton("Get Authorization");
        auth.addActionListener(this);
        panel.add(label);
        panel.add(sp);
        panel.add(enter);
        panel.add(reset);
       
	}
	
	/**
	 * sets up the recommendation screen
	 */
	private void setUpRecs() {
		panel1 = new JPanel();
		panel2 = new JPanel();
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.PAGE_AXIS));
		panel3 = new JPanel();
		panel3.setLayout(new BoxLayout(panel3, BoxLayout.PAGE_AXIS));
		getRecs = new JButton("Get Recommendations");
		getRecs.addActionListener(this);
		user = new JLabel("User: " + AuthorizationCodeUri.getUser());
		artistHeader = new JLabel("Your most lsitened to artists recently");
		recsHeader = new JLabel("Recommendations");
		panel1.add(getRecs);
		panel1.add(user);
		user.setPreferredSize(new Dimension(300,10));
		user.setHorizontalAlignment(SwingConstants.RIGHT);
		frame.add(BorderLayout.PAGE_START, panel1);
		
		artistHeader = new JLabel("Your most listented to artists recently");
		recentArtists = new JList<String>(AuthorizationCodeUri.getArtists());
		recentArtists.setFixedCellHeight(145);
		recentArtists.setFixedCellWidth(200);
		recentArtists.setBackground(frame.getBackground());
		artistHeader.setPreferredSize(new Dimension(250, 50));
		artistHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
		recentArtists.setAlignmentX(Component.CENTER_ALIGNMENT);
		DefaultListCellRenderer renderer = (DefaultListCellRenderer)recentArtists.getCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		panel2.add(artistHeader);
		panel2.add(recentArtists);
		recs = new JList<String>(AuthorizationCodeUri.authorizationCodeUri_Sync());
		recs.setFixedCellHeight(recentArtists.getFixedCellHeight() / 3);
		recs.setBackground(frame.getBackground());
		recsHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
		recs.setAlignmentX(Component.CENTER_ALIGNMENT);
		recsHeader.setPreferredSize(new Dimension(250, 50));
		panel3.add(recsHeader);
		panel3.add(recs);
		frame.getContentPane().add(BorderLayout.LINE_END, panel3);
		frame.getContentPane().add(BorderLayout.LINE_START, panel2);
		panel3.setVisible(false);
		recs.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					AuthorizationCodeUri.getArtistInfo(recs.getSelectedValue());
				}
			}
		});
		frame.validate();
		frame.repaint();
	}

	/**
	 * Listens for an action performed
	 * @param e object that there is an action being performed
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == reset) {
			ta.setText("");
		} else if (e.getSource() == enter) {
			if (AuthorizationCodeUri.login(ta.getText()) == 0) {
				frame.remove(panel);
				frame.remove(auth);
				setUpRecs();
			} else {
				JOptionPane.showMessageDialog(frame, "Incorrect URL");
				ta.setText("");
			}
		} else if (e.getSource() == auth) {
			AuthorizationCodeUri.openSpotify();
		} else if (e.getSource() == getRecs) {
			panel3.setVisible(true);
		}
	}
	
	/**
	 * creates a new GUI
	 * @param args commnand-line arguments
	 */
	public static void main(String[] args) {
		new ArtistRecommenderGUI();
	}
}
