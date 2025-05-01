import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.*;

class MusicPlayer {
    private Clip clip;
    private boolean isMuted = false;

    public void playMusic(String filePath, boolean loop) {
        try {
            if (clip != null) {
                clip.stop();
                clip.close();
            }

            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.out.println("Music file not found: " + filePath);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audioInput);

            if (!isMuted) {
                if (loop) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    clip.start();
                }
            }
        } catch (Exception e) {
            System.out.println("Error playing music: " + e.getMessage());
        }
    }

    public void toggleMute() {
        if (clip != null) {
            if (isMuted) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.stop();
            }
            isMuted = !isMuted;
        }
    }
}

class Player {
    int x, y, speed;
    boolean hasFile;

    public Player(int startX, int startY) {
        x = startX;
        y = startY;
        speed = 5;
        hasFile = false;
    }

    public void move(String direction) {
        switch (direction) {
            case "UP": y -= speed; break;
            case "DOWN": y += speed; break;
            case "LEFT": x -= speed; break;
            case "RIGHT": x += speed; break;
        }
    }
}

class Guard {
    int x, y, speed, direction;

    public Guard(int startX, int startY, int speed) {
        x = startX;
        y = startY;
        this.speed = speed;
        direction = 1;
    }

    public void patrol() {
        x += speed * direction;
        if (x > 1100 || x < 0) direction *= -1;
    }

    public boolean detects(Player player) {
        return Math.abs(player.x - x) < 30 && Math.abs(player.y - y) < 30;
    }
}

class SecurityCamera {
    int x, y, visionRange;

    public SecurityCamera(int startX, int startY, int visionRange) {
        x = startX;
        y = startY;
        this.visionRange = visionRange;
    }

    public boolean detects(Player player) {
        return Math.abs(player.x - x) < visionRange && Math.abs(player.y - y) < 50;
    }
}

public class CyberHeistGame extends JPanel implements ActionListener, KeyListener {
    Timer timer;
    Player player;
    ArrayList<Guard> guards;
    ArrayList<SecurityCamera> cameras;
    boolean gameOver = false;
    boolean gameWon = false;
    boolean gameStarted = false;
    boolean showIntro = true;
    boolean showLevelSelect = false;
    boolean showInstructions = false;
    int level = 1;
    int score = 0;
    int highScore = 0;

    Point startLocation = new Point(50, 50);
    Point fileLocation = new Point(1050, 650);

    MusicPlayer musicPlayer = new MusicPlayer();

    public CyberHeistGame() {
        setPreferredSize(new Dimension(1200, 700));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
    }

    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highscores.txt"))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line.trim());
            }
        } catch (IOException e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscores.txt"))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            System.out.println("Failed to save high score.");
        }
    }

    public void startGame() {
        gameOver = false;
        gameWon = false;
        gameStarted = true;
        showInstructions = false;
        showLevelSelect = false;

        player = new Player(startLocation.x, startLocation.y);
        guards = new ArrayList<>();
        cameras = new ArrayList<>();
        score = 0;
        loadHighScore();

        Random rand = new Random();
        for (int i = 0; i < level + 2; i++) {
            guards.add(new Guard(rand.nextInt(1100), rand.nextInt(600), 6 + level));
            cameras.add(new SecurityCamera(rand.nextInt(1100), rand.nextInt(600), 150 + level * 10));
        }

        timer = new Timer(100, this);
        timer.start();
        musicPlayer.playMusic("background.wav", true);

        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (showIntro) drawIntroScreen(g);
        else if (!gameStarted && !showLevelSelect && !showInstructions) drawStartScreen(g);
        else if (showInstructions) drawInstructions(g);
        else if (showLevelSelect) drawLevelSelectScreen(g);
        else if (gameOver) drawGameOverScreen(g);
        else if (gameWon) drawGameWinScreen(g);
        else drawGame(g);
    }

    private void drawGame(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillOval(player.x, player.y, 30, 30);

        g.setColor(Color.RED);
        for (Guard guard : guards) {
            g.fillRect(guard.x, guard.y, 30, 30);
        }

        g.setColor(Color.YELLOW);
        for (SecurityCamera cam : cameras) {
            g.drawRect(cam.x, cam.y, 50, 20);
        }

        g.setColor(Color.GREEN);
        g.fillRect(fileLocation.x, fileLocation.y, 20, 20);

        g.setColor(Color.WHITE);
        g.fillRect(startLocation.x, startLocation.y, 20, 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Score: " + score, 20, 20);
        g.drawString("High Score: " + highScore, 20, 40);
    }

    private void drawIntroScreen(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.drawString("CYBER HEIST", 460, 150);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Welcome to the Cyber Heist game!", 420, 200);
        g.drawString("Avoid all security guards and cameras or you'll be caught!", 320, 230);
        g.drawString("Use W A S D to move.", 480, 260);
        g.drawString("Press ENTER to continue or ESC to exit.", 420, 300);
    }

    private void drawStartScreen(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("Select a level to start the Cyber Heist Mission", 330, 200);
        g.drawString("Press ENTER to view levels", 440, 250);
    }

    private void drawLevelSelectScreen(Graphics g) {
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Choose Level (1-5):", 480, 100);
        for (int i = 1; i <= 5; i++) {
            g.drawString("Level " + i, 550, 100 + i * 40);
        }
        g.drawString("Press a number key (1-5) to select level", 410, 400);
    }

    private void drawInstructions(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Instructions", 520, 100);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Objective: Steal the secret file and return to the starting point.", 320, 150);
        g.drawString("Avoid detection by security guards and cameras.", 370, 180);
        g.drawString("W - Up, A - Left, S - Down, D - Right", 440, 230);
        g.drawString("Press ENTER to start the game", 460, 300);
    }

    private void drawGameOverScreen(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("GAME OVER!", 400, 300);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press 'R' to Restart or 'Esc' to Exit", 420, 340);
    }

    private void drawGameWinScreen(Graphics g) {
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("MISSION SUCCESSFUL!", 300, 300);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press 'R' for Next Level or 'Esc' to Exit", 410, 340);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Guard guard : guards) {
            guard.patrol();
            if (guard.detects(player)) gameOver = true;
        }

        for (SecurityCamera cam : cameras) {
            if (cam.detects(player)) gameOver = true;
        }

        if (!player.hasFile && Math.abs(player.x - fileLocation.x) < 20 && Math.abs(player.y - fileLocation.y) < 20) {
            player.hasFile = true;
            score += 100;
        }

        if (player.hasFile && Math.abs(player.x - startLocation.x) < 20 && Math.abs(player.y - startLocation.y) < 20) {
            gameWon = true;
            score += 200;
            if (score > highScore) {
                highScore = score;
                saveHighScore();
            }
        }

        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (showIntro && e.getKeyCode() == KeyEvent.VK_ENTER) {
            showIntro = false;
            repaint();
            return;
        }

        if (!gameStarted && !showLevelSelect && !showInstructions && e.getKeyCode() == KeyEvent.VK_ENTER) {
            showLevelSelect = true;
            repaint();
            return;
        }

        if (showLevelSelect && e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_5) {
            level = e.getKeyCode() - KeyEvent.VK_0;
            showInstructions = true;
            showLevelSelect = false;
            repaint();
            return;
        }

        if (showInstructions && e.getKeyCode() == KeyEvent.VK_ENTER) {
            startGame();
            return;
        }

        if ((gameOver || gameWon) && e.getKeyCode() == KeyEvent.VK_R) {
            if (gameWon) level = Math.min(level + 1, 5);
            startGame();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
        if (e.getKeyCode() == KeyEvent.VK_M) musicPlayer.toggleMute();

        if (gameStarted) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W: player.move("UP"); break;
                case KeyEvent.VK_S: player.move("DOWN"); break;
                case KeyEvent.VK_A: player.move("LEFT"); break;
                case KeyEvent.VK_D: player.move("RIGHT"); break;
            }
        }

        repaint();
    }

    public static void main(String[] args) {
        File csvFile = new File("users.csv");
        if (!csvFile.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
                writer.println("username,password");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error initializing user data file.");
                System.exit(1);
            }
        }

        String[] options = {"Login", "Register"};
        int choice = JOptionPane.showOptionDialog(null, "Welcome to Cyber Heist!", "Choose an Option",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 1) {
            JTextField newUser = new JTextField();
            JPasswordField newPass = new JPasswordField();
            Object[] regFields = {"New Username:", newUser, "New Password:", newPass};
            int regOption = JOptionPane.showConfirmDialog(null, regFields, "Register", JOptionPane.OK_CANCEL_OPTION);

            if (regOption == JOptionPane.OK_OPTION) {
                String username = newUser.getText();
                String password = new String(newPass.getPassword());
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.csv", true))) {
                    writer.write(username + "," + password);
                    writer.newLine();
                    JOptionPane.showMessageDialog(null, "Registration successful. Please login.");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error saving user data.");
                }
            }
        }

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] loginFields = {"Username:", usernameField, "Password:", passwordField};

        int option = JOptionPane.showConfirmDialog(null, loginFields, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
                String line;
                boolean success = false;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                        success = true;
                        break;
                    }
                }

                if (success) {
                    JFrame frame = new JFrame("Cyber Heist - Stealth Game");
                    CyberHeistGame game = new CyberHeistGame();
                    frame.add(game);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                    game.requestFocusInWindow();
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials. Try again.");
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error reading user data.");
            }
        } else {
            System.exit(0);
        }
    }
}