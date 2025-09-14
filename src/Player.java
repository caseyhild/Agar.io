import java.awt.*;

public class Player {
    private double x, y;
    private double radius;
    private final Color baseColor;

    public Player(double x, double y, double radius, Color baseColor) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.baseColor = baseColor;
    }

    /** Move according to arrow-key booleans; clamp to world bounds. */
    public void update(boolean up, boolean down, boolean left, boolean right, int width, int height) {
        double speed = Math.max(1.0, 4.0 - (radius - 20.0) / 20.0); // slow down slightly when large
        double dx = 0, dy = 0;
        if (up) dy -= 1;
        if (down) dy += 1;
        if (left) dx -= 1;
        if (right) dx += 1;
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
        // draw darker outline
        Color outline = baseColor.darker();
        g2.setColor(outline);
        g2.fillOval((int)Math.round(x - radius), (int)Math.round(y - radius),
                (int)Math.round(radius * 2), (int)Math.round(radius * 2));
        // inner fill (slightly inset)
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