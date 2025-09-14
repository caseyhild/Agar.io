import java.awt.*;

public class Button {
    private final int cx, cy;
    private final int radius;
    private final String label;

    public Button(int cx, int cy, int radius, String label) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.label = label;
    }

    public boolean contains(int mx, int my) {
        int dx = mx - cx;
        int dy = my - cy;
        return dx * dx + dy * dy <= radius * radius;
    }

    public void render(Graphics2D g2, int mouseX, int mouseY) {
        render(g2, mouseX, mouseY, new Color(0, 200, 0)); // default green
    }

    public void render(Graphics2D g2, int mouseX, int mouseY, Color base) {
        boolean hover = contains(mouseX, mouseY);

        Color outline = base.darker().darker();
        Color fill = hover ? base.darker() : base;

        g2.setColor(outline);
        g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        g2.setColor(fill);
        g2.fillOval(cx - radius + 3, cy - radius + 3, radius * 2 - 6, radius * 2 - 6);

        g2.setColor(Color.BLACK);
        Font font = new Font("Arial", Font.BOLD, Math.max(12, (int)(radius * 0.45)));
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int tx = cx - fm.stringWidth(label) / 2;
        int ty = cy + fm.getAscent() / 2 - 2;
        g2.drawString(label, tx, ty);
    }

}