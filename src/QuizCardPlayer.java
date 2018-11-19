

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.*;

public class QuizCardPlayer {
	private JFrame					 frame;
	private JLabel					 cardNo;
	private JTextArea				 display;
	private JButton				 showAnswerButton;
	private JButton				 showQuestionButton;
	private JButton				 nextCardButton;
	private JButton				 playButton;
	private boolean				 isLastCard;
	private String					 trackFileName	= "";
	private ArrayList<QuizCard> cardList; // Kártyák objektumból álló tömblista
	private QuizCard				 currentCard; // Az aktuális kártya
	private int						 currentCardIndex; //Az aktuális kártya indexe a tömblistában

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}
		QuizCardPlayer player = new QuizCardPlayer();
		player.go();
	} // main

	public void go() {
		frame = new JFrame("Quiz Card Player");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension buttonSize = new Dimension(120, 30);
		Font bigFont = new Font("Arial", Font.ITALIC, 24);
		Font smallFont = new Font("Arial", Font.BOLD, 14);
		JPanel mainPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		cardNo = new JLabel(String.format("%03d", 0));
		cardNo.setFont(smallFont);
		mainPanel.add(cardNo);

		display = new JTextArea(10, 20);
		display.setFont(bigFont);
		display.setWrapStyleWord(true);
		display.setLineWrap(true);
		display.setEditable(false);

		JScrollPane qScroller = new JScrollPane(display);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanel.add(qScroller);

		buttonPanel.add(Box.createVerticalGlue());

		showAnswerButton = new JButton("Show Answer");
		showAnswerButton.setPreferredSize(buttonSize);
		showAnswerButton.setMinimumSize(buttonSize);
		showAnswerButton.setMaximumSize(buttonSize);
		showAnswerButton.setEnabled(false);
		showAnswerButton.addActionListener(new showAnswerListener());
		buttonPanel.add(showAnswerButton);

		buttonPanel.add(Box.createVerticalGlue());

		showQuestionButton = new JButton("Show Question");
		showQuestionButton.setPreferredSize(buttonSize);
		showQuestionButton.setMinimumSize(buttonSize);
		showQuestionButton.setMaximumSize(buttonSize);
		showQuestionButton.setEnabled(false);
		showQuestionButton.addActionListener(new showQuestionListener());

		buttonPanel.add(showQuestionButton);

		buttonPanel.add(Box.createVerticalGlue());
		playButton = new JButton("Play");
		playButton.setPreferredSize(buttonSize);
		playButton.setMinimumSize(buttonSize);
		playButton.setMaximumSize(buttonSize);
		playButton.setEnabled(false);
		playButton.addActionListener(new playButtonListener());
		buttonPanel.add(playButton);

		buttonPanel.add(Box.createVerticalGlue());

		nextCardButton = new JButton("Next Card");
		nextCardButton.setPreferredSize(buttonSize);
		nextCardButton.setMinimumSize(buttonSize);
		nextCardButton.setMaximumSize(buttonSize);
		nextCardButton.setEnabled(false);
		nextCardButton.addActionListener(new showNextCardListener());
		buttonPanel.add(nextCardButton);

		buttonPanel.add(Box.createVerticalGlue());

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem loadMenuItem = new JMenuItem("Load card set");
		loadMenuItem.addActionListener(new OpenMenuListener());
		fileMenu.add(loadMenuItem);
		menuBar.add(fileMenu);

		frame.setJMenuBar(menuBar);
		frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		frame.getContentPane().add(BorderLayout.EAST, buttonPanel);
		frame.setSize(700, 450);
		frame.pack();
		frame.setVisible(true);

	} // go

	public class showAnswerListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			display.setText(currentCard.getAnswer());
			showQuestionButton.setEnabled(true);
			showAnswerButton.setEnabled(false);
		}
	} // showAnswerListener

	public class showQuestionListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			display.setText(currentCard.getQuestion());
			showAnswerButton.setEnabled(true);
			showQuestionButton.setEnabled(false);
		}
	} // showQuestionListener

	public class playButtonListener implements ActionListener {
		boolean playCompleted = false;

		public void actionPerformed(ActionEvent ev) {
			JFXPanel p = new javafx.embed.swing.JFXPanel(); // Az FX inicalizálása miatt kell csak
			//Hang fájl neve: csv fájl neve+_+NNN+.mp3
			String uriString = new File(trackFileName + "_" + String.format("%03d", currentCard.getCardID()) + ".mp3")
					.toURI().toString();
			MediaPlayer player = new MediaPlayer(new Media(uriString));
			player.play();
		}
	} // playButtonListener

	public class showNextCardListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			if (isLastCard) { // Ha az utolsó kártyát elértük
				currentCardIndex = 0;
				isLastCard = false;
				nextCardButton.setText("Next Card");
				showAnswerButton.setEnabled(false);
				showQuestionButton.setEnabled(false);
				showNextCard();
			} else if (currentCardIndex < cardList.size()) { //amíg nem értünk az utolsó kártyához
				showNextCard();
			} else { //Az utolsó kártyát is elhagytuk
				display.setText("That was last card!");
				showAnswerButton.setEnabled(false);
				playButton.setEnabled(false);
				isLastCard = true;
				nextCardButton.setText("First Card");
			}
		}
	} // showNextCardListener

	public class OpenMenuListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			JFileChooser fileOpen = new JFileChooser();
			int returnVal = fileOpen.showOpenDialog(frame); // returnVal mutatja mi történt a dialógusablakban
			if (returnVal != JFileChooser.CANCEL_OPTION) {
				loadFile(fileOpen.getSelectedFile());
			}
		}
	} // OpenMenuListener

	private void loadFile(File file) {
		cardList = new ArrayList<QuizCard>();
		Charset charset = StandardCharsets.ISO_8859_1;
		String line;
		try (BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)) {
			while ((line = reader.readLine()) != null) {
				makeCard(line);
			}
			//Előállítjuk a hangfájl nevének elejét
			//A csv fájl nevének és a hangfájlok nevének meg kell egyeznie, 
			// "csvfjálneve_NNN.mp3" formátumúnak kell lennie
			trackFileName = file.getName().substring(0, file.getName().indexOf('.'));
			reader.close();
			showAnswerButton.setEnabled(true);
			currentCardIndex = 0;
			isLastCard = false;
			showNextCard();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(frame, "Hiba történt a fájlművelet közben!", "Hiba", JOptionPane.ERROR_MESSAGE);

		}

	}// loadFile

	private void makeCard(String lineToParse) {
		String[] result = lineToParse.split(";");
		if (result.length == 3) {
			//Kérdés, válasz, kártyaszám
			QuizCard card = new QuizCard(result[1], result[2], Integer.parseInt(result[0]));
			cardList.add(card);
		} else {
			QuizCard card = new QuizCard("NULL", "NULL", 0);
			cardList.add(card);
		}
	} // makeCard

	private void showNextCard() {
		currentCard = cardList.get(currentCardIndex);
		currentCardIndex++;
		cardNo.setText(String.format("%03d", currentCard.getCardID()));
		display.setText(currentCard.getQuestion());
		playButton.setEnabled(true);
		nextCardButton.setEnabled(true);
		showAnswerButton.setEnabled(true);
		showQuestionButton.setEnabled(false);
		showAnswerButton.requestFocus();
	} // showNextCard

}// QuizCardPlayer class
