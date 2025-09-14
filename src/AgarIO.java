import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

public class AgarIO extends JFrame implements Runnable, MouseListener, MouseMotionListener, KeyListener {
    private final int width = 600;
    private final int height = 600;
    private final Thread thread;
    private boolean running;

    // Input state
    private int mouseX = 0, mouseY = 0; // adjusted for title bar in mouse handlers (we subtract 28 there)
    private boolean upPressed = false, downPressed = false, leftPressed = false, rightPressed = false;

    // Game state
    private enum State { MENU, HELP, PLAY, WIN, LOSE }
    private State state = State.MENU;

    // Entities & world
    private Player player;
    private Bot bot;
    private ArrayList<Dot> dots;

    // UI
    private Button playButton, helpButton, backButton, menuButton;

    // Scores
    private int playerScore = 0;
    private int botScore = 0;

    public AgarIO() {
        thread = new Thread(this);

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        setSize(width, height + 28); // keep the titlebar offset like your template did
        setResizable(false);
        setTitle("AgarIO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        initUI();

        start();
    }

    private void initUI() {
        int cx = width / 2;
        int cy = height / 2;
        // circular buttons (centerX, centerY, radius, label)
        playButton = new Button(cx - 120, cy, 60, "PLAY");
        helpButton = new Button(cx + 120, cy, 60, "HELP");
        backButton = new Button(cx, height - 100, 60, "BACK");
        menuButton = new Button(cx, height - 100, 60, "MENU");
    }

    private void startGame() {
        player = new Player(width / 4.0, height / 2.0, 20.0, Color.GREEN);
        bot = new Bot(3.0 * width / 4.0, height / 2.0, 20.0, Color.RED);

        // spawn an initial set of dots
        dots = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            dots.add(Dot.randomDot(width, height));
        }

        playerScore = 0;
        botScore = 0;
    }

    private synchronized void start() {
        running = true;
        thread.start();
    }

    private void update() {
        if (state == State.PLAY) {
            // update player using arrow-key booleans and world bounds
            player.update(upPressed, downPressed, leftPressed, rightPressed, width, height);

            // update bot: pass the dots and world size
            bot.update(dots, width, height);

            // check eating of dots (player & bot) and respawn eaten dots
            for (int i = 0; i < dots.size(); i++) {
                Dot d = dots.get(i);
                // player eats dot
                if (player.eats(d)) {
                    player.grow(0.75);
                    playerScore++;
                    dots.set(i, Dot.randomDot(width, height));
                    continue; // skip bot eating the same dot this tick
                }
                // bot eats dot
                if (bot.eats(d)) {
                    bot.grow(0.75);
                    botScore++;
                    dots.set(i, Dot.randomDot(width, height));
                }
            }

            // check player <-> bot collision: half-overlap required
            double dx = player.getX() - bot.getX();
            double dy = player.getY() - bot.getY();
            double dist = Math.hypot(dx, dy);

            double pr = player.getRadius();
            double br = bot.getRadius();
            double bigger = Math.max(pr, br);
            double smaller = Math.min(pr, br);

            // require smaller to be at least half inside bigger
            if (dist <= bigger - (smaller / 2.0)) {
                if (pr > br) {
                    state = State.WIN;
                } else if (br > pr) {
                    state = State.LOSE;
                }
            }
        }
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics2D g2 = (Graphics2D) bs.getDrawGraphics();
        try {
            // match your template's translation for title bar
            g2.translate(0, 28);

            // clear
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, width, height);

            // render by state (use Graphics2D)
            switch (state) {
                case MENU -> drawMenu(g2);
                case HELP -> drawHelp(g2);
                case PLAY -> drawPlay(g2);
                case WIN  -> drawWin(g2);
                case LOSE -> drawLose(g2);
            }
        } finally {
            g2.dispose();
            bs.show();
        }
    }

    private void drawMenu(Graphics2D g2) {
        // background already black
        // Title
        g2.setColor(new Color(0, 220, 0));
        g2.setFont(new Font("Arial", Font.BOLD, 72));
        centerString(g2, "AGAR.IO", width / 2, 120);

        // buttons
        playButton.render(g2, mouseX, mouseY);
        helpButton.render(g2, mouseX, mouseY);
    }

    private void drawHelp(Graphics2D g2) {
        g2.setColor(new Color(0, 200, 0));
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        centerString(g2, "HOW TO PLAY", width / 2, 90);

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        centerString(g2, "Use arrow keys to move. Eat dots to grow.", width / 2, 160);
        centerString(g2, "The bot will go toward and eat the nearest dot.", width / 2, 190);
        centerString(g2, "The bigger player eats the smaller one when they come together.", width / 2, 220);

        backButton.render(g2, mouseX, mouseY);
    }

    private void drawPlay(Graphics2D g2) {
        // draw dots
        for (Dot d : dots) d.render(g2);

        // draw bot & player (outline + fill inside their renderers)
        // check player <-> bot collision: bigger eats smaller -> end game
        if (player.getRadius() > bot.getRadius()) {
            bot.render(g2);
            player.render(g2);
        } else {
            player.render(g2);
            bot.render(g2);
        }

        // scores
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("Player: " + playerScore, 10, 20);
        String botLabel = "Bot: " + botScore;
        int w = g2.getFontMetrics().stringWidth(botLabel);
        g2.drawString(botLabel, width - w - 10, 20);
    }

    private void drawWin(Graphics2D g2) {
        g2.setColor(new Color(0, 200, 0));
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        centerString(g2, "YOU WIN!", width / 2, height / 2 - 40);

        // green button for win
        menuButton.render(g2, mouseX, mouseY, new Color(0, 200, 0));
    }

    private void drawLose(Graphics2D g2) {
        g2.setColor(new Color(220, 0, 0));
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        centerString(g2, "YOU LOSE!", width / 2, height / 2 - 40);

        // red button for lose
        menuButton.render(g2, mouseX, mouseY, new Color(220, 0, 0));
    }


    private void centerString(Graphics2D g2, String text, int cx, int cy) {
        FontMetrics fm = g2.getFontMetrics();
        int tx = cx - fm.stringWidth(text) / 2;
        int ty = cy - fm.getHeight() / 2 + fm.getAscent();
        g2.setFont(g2.getFont());
        g2.drawString(text, tx, ty);
    }

    // === Input handlers ===

    public void mousePressed(MouseEvent e) {
        // adjust for title bar translation
        int mx = e.getX();
        int my = e.getY() - 28;

        if (state == State.MENU) {
            if (playButton.contains(mx, my)) {
                startGame();
                state = State.PLAY;
            } else if (helpButton.contains(mx, my)) {
                state = State.HELP;
            }
        } else if (state == State.HELP) {
            if (backButton.contains(mx, my)) {
                state = State.MENU;
            }
        } else if (state == State.WIN || state == State.LOSE) {
            if (menuButton.contains(mx, my)) {
                state = State.MENU;
            }
        }
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}

    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY() - 28;
    }

    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY() - 28;
    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    // arrow-key handling
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP    -> upPressed = true;
            case KeyEvent.VK_DOWN  -> downPressed = true;
            case KeyEvent.VK_LEFT  -> leftPressed = true;
            case KeyEvent.VK_RIGHT -> rightPressed = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP    -> upPressed = false;
            case KeyEvent.VK_DOWN  -> downPressed = false;
            case KeyEvent.VK_LEFT  -> leftPressed = false;
            case KeyEvent.VK_RIGHT -> rightPressed = false;
        }
    }

    public void keyTyped(KeyEvent e) {}

    public void run() {
        //main program loop
        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60.0; //60 times per second
        double delta = 0;
        requestFocus();
        while (running) {
            //updates time
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) //Make sure update is only happening 60 times a second
            {
                //update
                update();
                delta--;
            }
            //display to the screen
            render();
        }
    }

    public static void main(String[] args) {
        new AgarIO();
    }
}