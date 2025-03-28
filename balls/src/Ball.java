import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class Ball {
    private List<BallInfo> balls;
    private JPanel ballPanel;
    private Timer physicsTimer;
    private int frameWidth;
    private int frameHeight;
    private final double GRAVITY = 0.5;
    private final double FRICTION = 0.98;
    private final double RESTITUTION = 0.80; // Bounce factor

    // FPS tracking
    private int frameCount = 0;
    private long lastFpsTime = 0;
    private int fps = 0;

    // Performance optimization
    private final int SUBDIVISION_SIZE = 100; // Spatial subdivision size

    // Inner class to hold information about individual balls
    private class BallInfo {
        int x, y;
        int diameter;
        Color color;
        double velocityX, velocityY;
        double mass;

        public BallInfo(int x, int y, int diameter, Color color) {
            this.x = x;
            this.y = y;
            this.diameter = diameter;
            this.color = color;
            this.velocityX = Math.random() * 4 - 2; // Random initial velocity
            this.velocityY = 0;
            this.mass = diameter * 0.1; // Mass based on size
        }
    }

    /**
     * Creates specified number of balls in a grid pattern with physics
     * @param count Number of balls to create
     * @param frameWidth Width of the frame
     * @param frameHeight Height of the frame
     */
    public Ball(int count, int frameWidth, int frameHeight) {
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        initializeBalls(count);
    }

    /**
     * Initialize the balls with the given count
     * @param count Number of balls to create
     */
    private void initializeBalls(int count) {
        balls = new ArrayList<>();

        // Define properties for the balls
        int diameter = 50;
        int margin = 10;
        int cols = (int) Math.ceil(Math.sqrt(count)); // Calculate grid columns

        // Calculate starting position to center the grid
        int startX = margin;
        int startY = margin;

        // Create the specified number of balls
        for (int i = 0; i < count; i++) {
            int row = i / cols;
            int col = i % cols;
            int x = startX + col * (diameter + margin);
            int y = startY + row * (diameter + margin);

            // Ensure balls start within bounds
            x = Math.min(x, frameWidth - diameter - 5);
            y = Math.min(y, frameHeight - diameter - 5);

            // Assign a random color
            Color color = new Color(
                    (int)(Math.random() * 200) + 55,
                    (int)(Math.random() * 200) + 55,
                    (int)(Math.random() * 200) + 55
            );

            // Add the ball to the list
            balls.add(new BallInfo(x, y, diameter, color));
        }

        // Create a panel to draw the balls if it doesn't exist
        if (ballPanel == null) {
            createBallPanel();
        }

        // Reset FPS counter
        lastFpsTime = System.currentTimeMillis();
        frameCount = 0;
        fps = 0;

        // Start the physics simulation
        startPhysics();
    }

    // Create a custom JPanel to draw the balls
    private void createBallPanel() {
        ballPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Enable anti-aliasing for smoother circles
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw each ball
                for (BallInfo ball : balls) {
                    g2d.setColor(ball.color);
                    g2d.fillOval(ball.x, ball.y, ball.diameter, ball.diameter);

                    // Add a highlight to make balls look more 3D
                    g2d.setColor(new Color(255, 255, 255, 100));
                    g2d.fillOval(ball.x + ball.diameter/4, ball.y + ball.diameter/4,
                            ball.diameter/4, ball.diameter/4);
                }

                // Draw FPS counter
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                g2d.drawString("FPS: " + fps + " | Balls: " + balls.size(), 10, 25);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(frameWidth, frameHeight);
            }
        };

        // Set panel properties
        ballPanel.setOpaque(false);  // Make panel background transparent
    }

    /**
     * Start the physics simulation timer
     */
    private void startPhysics() {
        // Stop existing timer if it's running
        if (physicsTimer != null && physicsTimer.isRunning()) {
            physicsTimer.stop();
        }

        physicsTimer = new Timer(16, new ActionListener() { // ~60 FPS target
            private long lastUpdateTime = System.currentTimeMillis();

            @Override
            public void actionPerformed(ActionEvent e) {
                // Calculate delta time for smooth physics regardless of frame rate
                long currentTime = System.currentTimeMillis();
                double deltaTime = (currentTime - lastUpdateTime) / 16.0; // Normalize to our time step
                lastUpdateTime = currentTime;

                // Clamp deltaTime to avoid huge jumps if app was suspended
                deltaTime = Math.min(deltaTime, 3.0);

                updatePhysics(deltaTime);
                updateFPS();
                ballPanel.repaint();
            }
        });
        physicsTimer.start();
    }

    /**
     * Update FPS counter
     */
    private void updateFPS() {
        frameCount++;

        long currentTime = System.currentTimeMillis();
        long delta = currentTime - lastFpsTime;

        if (delta >= 1000) {
            fps = (int)(frameCount * 1000 / delta);
            frameCount = 0;
            lastFpsTime = currentTime;
        }
    }

    /**
     * Update the physics for all balls
     * @param deltaTime Time multiplier to ensure consistent physics regardless of frame rate
     */
    private void updatePhysics(double deltaTime) {
        // Update velocity and position for each ball
        for (BallInfo ball : balls) {
            // Apply gravity
            ball.velocityY += GRAVITY * deltaTime;

            // Apply velocity
            ball.x += ball.velocityX * deltaTime;
            ball.y += ball.velocityY * deltaTime;

            // Boundary collision (Window edges)
            // Right edge
            if (ball.x + ball.diameter > frameWidth) {
                ball.x = frameWidth - ball.diameter;
                ball.velocityX *= -RESTITUTION;
            }
            // Left edge
            if (ball.x < 0) {
                ball.x = 0;
                ball.velocityX *= -RESTITUTION;

                // Random boost to prevent sticking
                if (Math.abs(ball.velocityX) < 0.5) {
                    ball.velocityX = Math.random() * 2 + 0.5;
                }
            }
            // Bottom edge
            if (ball.y + ball.diameter > frameHeight) {
                ball.y = frameHeight - ball.diameter;
                ball.velocityY *= -RESTITUTION;
                ball.velocityX *= FRICTION; // Apply friction when hitting floor
            }
            // Top edge
            if (ball.y < 0) {
                ball.y = 0;
                ball.velocityY *= -RESTITUTION;

                // Random boost to prevent sticking
                if (Math.abs(ball.velocityY) < 0.5) {
                    ball.velocityY = Math.random() * 2 + 0.5;
                }
            }

            // Apply small random force to prevent perfect stacking
            if (Math.random() < 0.01) {
                ball.velocityX += (Math.random() - 0.5) * 0.1;
            }
        }

        // Use spatial partitioning for more efficient collision detection
        checkCollisionsWithPartitioning();
    }

    /**
     * Check for collisions using spatial partitioning
     */
    private void checkCollisionsWithPartitioning() {
        // Skip collision detection if too many balls for performance reasons
        if (balls.size() > 500) {
            return; // Just let them fall for extreme numbers
        }

        // Create grid of cells (spatial partitioning)
        int gridWidth = frameWidth / SUBDIVISION_SIZE + 1;
        int gridHeight = frameHeight / SUBDIVISION_SIZE + 1;

        // Create grid (only allocate when needed)
        @SuppressWarnings("unchecked")
        List<BallInfo>[][] grid = new ArrayList[gridWidth][gridHeight];

        // Assign balls to grid cells
        for (BallInfo ball : balls) {
            int centerX = ball.x + ball.diameter / 2;
            int centerY = ball.y + ball.diameter / 2;

            int cellX = centerX / SUBDIVISION_SIZE;
            int cellY = centerY / SUBDIVISION_SIZE;

            // Ensure within bounds
            cellX = Math.min(Math.max(cellX, 0), gridWidth - 1);
            cellY = Math.min(Math.max(cellY, 0), gridHeight - 1);

            // Initialize cell if needed
            if (grid[cellX][cellY] == null) {
                grid[cellX][cellY] = new ArrayList<>();
            }

            // Add ball to cell
            grid[cellX][cellY].add(ball);
        }

        // Check collisions within each cell and neighboring cells
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                if (grid[x][y] == null) continue;

                // Check collisions within this cell
                checkCollisionsInCell(grid[x][y]);

                // Check with neighboring cells (right, bottom, bottom-right)
                for (int nx = x; nx <= x + 1 && nx < gridWidth; nx++) {
                    for (int ny = y; ny <= y + 1 && ny < gridHeight; ny++) {
                        if (nx == x && ny == y) continue; // Skip self
                        if (grid[nx][ny] == null) continue;

                        // Check collisions between cells
                        checkCollisionsBetweenCells(grid[x][y], grid[nx][ny]);
                    }
                }
            }
        }
    }

    /**
     * Check collisions between balls in the same cell
     */
    private void checkCollisionsInCell(List<BallInfo> cellBalls) {
        for (int i = 0; i < cellBalls.size(); i++) {
            for (int j = i + 1; j < cellBalls.size(); j++) {
                checkAndResolveCollision(cellBalls.get(i), cellBalls.get(j));
            }
        }
    }

    /**
     * Check collisions between balls in different cells
     */
    private void checkCollisionsBetweenCells(List<BallInfo> cell1, List<BallInfo> cell2) {
        for (BallInfo b1 : cell1) {
            for (BallInfo b2 : cell2) {
                checkAndResolveCollision(b1, b2);
            }
        }
    }

    /**
     * Check and resolve collision between two balls
     */
    private void checkAndResolveCollision(BallInfo b1, BallInfo b2) {
        // Calculate center points of each ball
        double b1CenterX = b1.x + b1.diameter / 2.0;
        double b1CenterY = b1.y + b1.diameter / 2.0;
        double b2CenterX = b2.x + b2.diameter / 2.0;
        double b2CenterY = b2.y + b2.diameter / 2.0;

        // Calculate distance between centers
        double dx = b2CenterX - b1CenterX;
        double dy = b2CenterY - b1CenterY;
        double distanceSquared = dx * dx + dy * dy;

        // Quick check using squared distance (optimization)
        double minDistance = (b1.diameter + b2.diameter) / 2.0;
        double minDistanceSquared = minDistance * minDistance;

        if (distanceSquared < minDistanceSquared) {
            // Now calculate actual distance
            double distance = Math.sqrt(distanceSquared);

            // Calculate collision normal
            double nx = dx / distance;
            double ny = dy / distance;

            // Calculate relative velocity
            double dvx = b2.velocityX - b1.velocityX;
            double dvy = b2.velocityY - b1.velocityY;

            // Calculate velocity along normal
            double velocityAlongNormal = dvx * nx + dvy * ny;

            // Do not resolve if velocities are separating
            if (velocityAlongNormal > 0) {
                return;
            }

            // Calculate impulse scalar
            double restitution = RESTITUTION;
            double impulseScalar = -(1 + restitution) * velocityAlongNormal;
            impulseScalar /= (1 / b1.mass) + (1 / b2.mass);

            // Apply impulse
            double impulseX = impulseScalar * nx;
            double impulseY = impulseScalar * ny;

            b1.velocityX -= impulseX / b1.mass;
            b1.velocityY -= impulseY / b1.mass;
            b2.velocityX += impulseX / b2.mass;
            b2.velocityY += impulseY / b2.mass;

            // Move balls apart to avoid sticking
            double overlap = minDistance - distance;
            double moveX = nx * overlap * 0.5;
            double moveY = ny * overlap * 0.5;

            b1.x -= moveX;
            b1.y -= moveY;
            b2.x += moveX;
            b2.y += moveY;

            // Apply a tiny random jitter to prevent perfect stacking
            if (Math.abs(b1.velocityX) < 0.1 && Math.abs(b2.velocityX) < 0.1) {
                b1.velocityX += (Math.random() - 0.5) * 0.2;
                b2.velocityX += (Math.random() - 0.5) * 0.2;
            }
        }
    }

    /**
     * Reset the simulation with a new number of balls
     * @param count Number of balls for the new simulation
     */
    public void reset(int count) {
        initializeBalls(count);
    }

    /**
     * Update the frame dimensions when window is resized
     * @param width New width
     * @param height New height
     */
    public void updateDimensions(int width, int height) {
        this.frameWidth = width;
        this.frameHeight = height;
    }

    /**
     * @return JPanel containing the balls
     */
    public JPanel getPanel() {
        return ballPanel;
    }

    /**
     * Change color of all balls
     * @param color New color for balls
     */
    public void setColor(Color color) {
        for (BallInfo ball : balls) {
            ball.color = color;
        }
        ballPanel.repaint();
    }

    /**
     * Stop the physics simulation
     */
    public void stopPhysics() {
        if (physicsTimer != null && physicsTimer.isRunning()) {
            physicsTimer.stop();
        }
    }
}