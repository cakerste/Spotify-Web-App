package view.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
	private BufferedImage img;
	private JLabel picLabel;
	private JLabel title;
	private JPanel picPanel;
	private JPanel arrowPanel;
	
	public static final int PIC_SIZE = 200;
	
	/**
	 * sets up the first screen, calls the login method
	 */
	public ArtistRecommenderGUI() {
		frame = new JFrame("Artist Recommender");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,550);
        setUpLogin();
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.CENTER, picPanel);
        frame.setVisible(true);
	}
	
	/**
	 * sets up the login screen
	 */
	private void setUpLogin() {
		// set up the bottom part of the screen
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
        panel.add(label);
        panel.add(sp);
        panel.add(enter);
        panel.add(reset);
        
        // set up the center of the screen
        title = new JLabel("Artist Recommender");
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setForeground(Color.decode("#1DB954"));
        try {
			img = ImageIO.read(new File("images/spotifyicon2.png"));
		} catch (IOException e) {
			System.out.println("Unable to load file");
		}
        Image scaledLogo = img.getScaledInstance(PIC_SIZE, PIC_SIZE, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaledLogo);
        picLabel = new JLabel(icon);
        picLabel.setPreferredSize(new Dimension(PIC_SIZE, PIC_SIZE));
        picPanel = new JPanel();
        picPanel.setLayout(new BoxLayout(picPanel, BoxLayout.PAGE_AXIS));
        auth = new JButton("Get Authorization");
        auth.addActionListener(this);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        auth.setAlignmentX(Component.CENTER_ALIGNMENT);
        picLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        picPanel.add(Box.createVerticalStrut(30));
        picPanel.add(title);
        picPanel.add(Box.createVerticalStrut(80));
        picPanel.add(picLabel);
        picPanel.add(Box.createVerticalStrut(70));
        picPanel.add(auth);
        picPanel.setBackground(Color.WHITE);
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
		
		artistHeader = new JLabel("<html>Your most listented<br>to artists recently</html>");
		recentArtists = new JList<String>(AuthorizationCodeUri.getArtists());
		recentArtists.setFixedCellHeight(147);
		recentArtists.setFixedCellWidth(150);
		recentArtists.setBackground(frame.getBackground());
		artistHeader.setPreferredSize(new Dimension(150, 150));
		artistHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
		artistHeader.setHorizontalAlignment(JLabel.CENTER);
		recentArtists.setAlignmentX(Component.CENTER_ALIGNMENT);
		DefaultListCellRenderer renderer = (DefaultListCellRenderer)recentArtists.getCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		panel2.add(artistHeader);
		panel2.add(recentArtists);
		recs = new JList<String>(AuthorizationCodeUri.authorizationCodeUri_Sync());
		recs.setFixedCellHeight(recentArtists.getFixedCellHeight() / 3);
		recs.setBackground(frame.getBackground());
		recsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
		recs.setAlignmentX(Component.LEFT_ALIGNMENT);
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
		arrowPanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				for (int i = 0; i < AuthorizationCodeUri.RECS_PER_ARTIST; i++) {
					if (i == 0) {
						g.setColor(Color.decode("#1DB954"));
					} else if (i == 1) {
						g.setColor(Color.decode("#FFFFFF"));
					} else {
						g.setColor(Color.decode("#191414"));
					}
					g.drawLine(0, 112 + 147 * i, 200, 62 + 147 * i);
					g.drawLine(0, 112 + 147 * i, 200, 112 + 147 * i);
					g.drawLine(0, 112 + 147 * i, 200, 165 + 147 * i);
				}
			}
		};
		frame.getContentPane().add(BorderLayout.CENTER, arrowPanel);
		arrowPanel.setVisible(false);
		
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
				frame.remove(picPanel);
				setUpRecs();
			} else {
				JOptionPane.showMessageDialog(frame, "Incorrect URL");
				ta.setText("");
			}
		} else if (e.getSource() == auth) {
			AuthorizationCodeUri.openSpotify();
		} else if (e.getSource() == getRecs) {
			panel3.setVisible(true);
			arrowPanel.setVisible(true);
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
