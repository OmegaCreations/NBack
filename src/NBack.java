import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class NBack extends Canvas {

    private final JPanel gridPanel; // grid for square
    private JButton startButton;
    private JButton posButton; // position choice indicator
    private JButton audioButton; // audio choice indicator
    private JLabel timeoutLabel;
    private JLabel nBackLabel;
    private JLabel errorsLabel;

    private int nBackTimout = 3000; // 3000 ms timeout between changes
    private int nBack = 2; // number of n-backs
    private int currentPos = -1; // square's position in grid
    private int currentAudio = -1;
    private int currentGameStep = 1; // how many square changes happened / -1 because we want to check user choices round before after all changes
    private int finalGameSteps = 20;

    // History of n-previous positions and audios
    private List<Integer> posHistory = new ArrayList<>();
    private List<Integer> audioHistory = new ArrayList<>();

    private Timer gameTimer;
    private Random rand = new Random();

    // user data
    private int userErrors = 0; // how many errors did user make
    private boolean userChosePosition = false; // flag - if user chose position
    private boolean userChoseAudio = false; // flag - if user chose audio

    // Setup basic canvas frame
    public NBack() {

        JFrame frame = new JFrame();

        frame.setTitle("N-Back Game");
        frame.setSize(800, 600); // window size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exits app on close button
        frame.setLayout(new BorderLayout()); // app layout
        frame.setBackground(Color.white);

        // grid panel for square 3x3
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(3, 3));

        // line border for grid
        for (int i = 0; i < 9; i++) {
            JPanel cell = new JPanel();
            cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            gridPanel.add(cell);
        }

        // Panel with game information
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS)); // set children in Y-axis

        timeoutLabel = new JLabel("Timeout: " + nBackTimout + "ms");
        nBackLabel = new JLabel("n-back: " + nBack + " back");
        errorsLabel = new JLabel("Errors: " + userErrors);
        timeoutLabel.setBorder(new EmptyBorder(10, 10, 0, 10));
        nBackLabel.setBorder(new EmptyBorder(10, 10, 0, 10));
        errorsLabel.setBorder(new EmptyBorder(0, 10, 10, 10));
        infoPanel.add(timeoutLabel);
        infoPanel.add(nBackLabel);
        infoPanel.add(errorsLabel);

        // Start button
        startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            frame.requestFocusInWindow();
            startGame();
        }); // starts game on click
        startButton.setBorder(new EmptyBorder(10, 10, 10, 10));
        infoPanel.add(startButton);

        // Indicator buttons
        posButton = new JButton("Position -> A");
        posButton.setBackground(Color.white);
        posButton.addActionListener(e -> {
            userChosePosition = true;
            posButton.setBackground(Color.yellow);
        });
        posButton.setBorder(new EmptyBorder(10, 10, 10, 10));
        infoPanel.add(posButton);

        audioButton = new JButton("Audio -> L");
        audioButton.setBackground(Color.white);
        audioButton.addActionListener(e -> {
            userChoseAudio = true;
            audioButton.setBackground(Color.yellow);
        });
        audioButton.setBorder(new EmptyBorder(10, 10, 10, 10));
        infoPanel.add(audioButton);


        // Set panels' position in frame
        frame.add(this);
        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(infoPanel, BorderLayout.EAST);

        // key listener for game
        // L - audio // A - position
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_A) {
                    System.out.println("Hello pos.");
                    userChosePosition = true;
                    posButton.setBackground(Color.yellow);
                }

                if (e.getKeyCode() == KeyEvent.VK_L) {
                    System.out.println("Hello audio.");
                    userChoseAudio = true;
                    audioButton.setBackground(Color.yellow);
                }
            }
        });

        frame.setVisible(true);
        frame.setFocusable(true);
        frame.requestFocusInWindow();

    }


    /**
     * Starts game's timer and logic.
     */
    public void startGame() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        // reset game's data
        currentGameStep = -1;
        currentPos = -1;
        currentAudio = -1;
        userErrors = 0;

        gameTimer = new Timer();
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateGameState(); // upadate grid and audio on nBackTimeout
                updateUserScore(); // update user scores
                currentGameStep++; // update game step
            }
        }, 300, nBackTimout); // refresh every nBackTimeout

    }


    /**
     * Updates grid for squares and current audio
     */
    private void updateGameState() {
        // random position and audio
        currentPos = rand.nextInt(9);
        currentAudio = rand.nextInt(4);

        // clearing grid
        for (Component c : gridPanel.getComponents()) {
            c.setBackground(Color.white);
        }

        // resetting indicators
        posButton.setBackground(Color.white);
        audioButton.setBackground(Color.white);

        // New square in given position
        JPanel cell = (JPanel) gridPanel.getComponent(currentPos);
        cell.setBackground(Color.getHSBColor((float)rand.nextInt(360)/100 , 1, 1)); // random color to know when position does not change

        // play audio sound
        AudioPlayer.playSound("src/audios/letter_"+currentAudio+".wav"); // supports only .wav files!

        // Add position and audio to history
        posHistory.add(currentPos);
        audioHistory.add(currentAudio);
    }

    /**
     * Evaluates user's choices and counts errors
     */
    private void updateUserScore() {

        // Position checking -----
        if ((checkPositionMatch() && !userChosePosition) || (!checkPositionMatch() && userChosePosition)) {
            userErrors++;

            // update button indicator
            posButton.setBackground(Color.red);

            // update error label
            errorsLabel.setText("Errors: " + userErrors);
        } else if (checkPositionMatch() && userChosePosition) {
            posButton.setBackground(Color.green);
        }

        // Audio checking -----
        if ((checkAudioMatch() && !userChoseAudio) || (!checkAudioMatch() && userChoseAudio)) {
            userErrors++;

            // update button indicator
            audioButton.setBackground(Color.red);

            // update error label
            errorsLabel.setText("Errors: " + userErrors);
        } else if (checkAudioMatch() && userChoseAudio) {
            audioButton.setBackground(Color.green);
        }

        // DEBUG
        if(currentGameStep >= nBack)
            System.out.println("pos["+posHistory.get(currentGameStep) + "]: " +"nbackPos["+posHistory.get(currentGameStep - nBack) + "] / userErrors ---> " + userErrors);

        // reset choices after update
        userChosePosition = false;
        userChoseAudio = false;
    }

    /**
     * @return Returns if current square position matches with nBack before.
     */
    private boolean checkPositionMatch() {
        return currentGameStep >= nBack
                && Objects.equals(posHistory.get(currentGameStep), posHistory.get(currentGameStep - nBack));
    }

    /**
     * @return Returns if current audio matches with nBack before.
     */
    private boolean checkAudioMatch() {
        return currentGameStep >= nBack
                && Objects.equals(audioHistory.get(currentGameStep), audioHistory.get(currentGameStep - nBack));
    }

    // main
    public static void main(String[] args) {
        new NBack();
    }
}


