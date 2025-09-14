import java.awt.*;
import java.util.Random;

public class Dot {
    private static final Random RNG = new Random();
    private final double x, y;
    private final double radius;
    private final Color color;

    public Dot(double x, double y, double radius, Color color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }

    public static Dot randomDot(int width, int height) {
        int margin = 18;
        double r = 4.0 + RNG.nextDouble() * 3.5; // 4..7.5
        double x = margin + RNG.nextDouble() * (width - 2 * margin);
        double y = margin + RNG.nextDouble() * (height - 2 * margin);
        Color c = new Color(60 + RNG.nextInt(160), 60 + RNG.nextInt(160), 60 + RNG.nextInt(160));
        return new Dot(x, y, r, c);
    }

    public void render(Graphics2D g2) {
        g2.setColor(color.darker());
        g2.fillOval((int)Math.round(x - radius), (int)Math.round(y - radius),
                (int)Math.round(radius * 2), (int)Math.round(radius * 2));
        g2.setColor(color);
        g2.fillOval((int)Math.round(x - radius + 1), (int)Math.round(y - radius + 1),
                (int)Math.round(radius * 2 - 2), (int)Math.round(radius * 2 - 2));
    }

    // getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
}