import java.awt.*;
import java.util.ArrayList;

public class Bot {
    private double x, y;
    private double radius;
    private final Color baseColor;

    public Bot(double x, double y, double radius, Color baseColor) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.baseColor = baseColor;
    }

    /** Move toward the nearest dot each update. */
    public void update(ArrayList<Dot> dots, int width, int height) {
        if (dots == null || dots.isEmpty()) return;

        Dot nearest = null;
        double bestD2 = Double.MAX_VALUE;
        for (Dot d : dots) {
            double dx = d.getX() - x;
            double dy = d.getY() - y;
            double d2 = dx*dx + dy*dy;
            if (d2 < bestD2) {
                bestD2 = d2;
                nearest = d;
            }
        }
        if (nearest == null) return;

        // about 80% of player’s max speed, scaled with size
        double speed = Math.max(0.7, 0.8 * (3.5 - (radius - 20.0) / 22.0));

        double dx = nearest.getX() - x;
        double dy = nearest.getY() - y;
        double len = Math.hypot(dx, dy);
        if (len > 0.0001) {
            dx = dx / len * speed;
            dy = dy / len * speed;
        }
        x += dx;
        y += dy;

        x = Math.max(radius, Math.min(width - radius, x));
        y = Math.max(radius, Math.min(height - radius, y));
    }

    public void render(Graphics2D g2) {
        Color outline = baseColor.darker();
        g2.setColor(outline);
        g2.fillOval((int)Math.round(x - radius), (int)Math.round(y - radius),
                (int)Math.round(radius * 2), (int)Math.round(radius * 2));
        g2.setColor(baseColor);
        g2.fillOval((int)Math.round(x - radius + 3), (int)Math.round(y - radius + 3),
                (int)Math.round(radius * 2 - 6), (int)Math.round(radius * 2 - 6));
    }

    public boolean eats(Dot d) {
        return Math.hypot(x - d.getX(), y - d.getY()) <= (radius + d.getRadius());
    }

    public void grow(double amount) { radius += amount; }

    // getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
}