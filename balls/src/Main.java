import javax.swing.JOptionPane;
import java.awt.event.KeyEvent;

public class Main {
    private static Frame frame;
    private static Ball balls;

    public static void main(String[] args) {
        // Create the frame using Java's Event Dispatch Thread for thread safety
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Create the frame
                frame = new Frame();

                // Create and add balls
                createBalls();

                // Add key listener for restart
                frame.addKeyListener(new Frame.FrameKeyListener() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        // Check if 'R' key was pressed
                        if (e.getKeyCode() == KeyEvent.VK_R) {
                            // Restart the simulation
                            createBalls();
                        }
                    }
                });

                // Ensure the frame has focus for key events
                frame.requestFocus();
            }
        });
    }

    /**
     * Helper method to create balls with user input
     */
    private static void createBalls() {
        // Ask user for number of balls (default 5)
        String input = JOptionPane.showInputDialog(
                frame.getFrame(),
                "How many balls do you want to simulate?",
                "5"
        );

        int ballCount = 5; // Default
        try {
            if (input != null && !input.isEmpty()) {
                ballCount = Integer.parseInt(input);
                // No longer limiting the number of balls
            }
        } catch (NumberFormatException e) {
            // Just use default if parsing fails
        }

        if (balls == null) {
            // First time - create the Ball object
            balls = new Ball(
                    ballCount,
                    frame.getWidth(),
                    frame.getHeight()
            );

            // Add the ball panel to the frame
            frame.getFrame().add(balls.getPanel());

            // Refresh the frame to show the balls
            frame.getFrame().revalidate();
            frame.getFrame().repaint();
        } else {
            // Reset existing Ball object with new count
            balls.updateDimensions(frame.getWidth(), frame.getHeight());
            balls.reset(ballCount);
        }

        // Ensure frame has focus after dialog closes
        frame.requestFocus();
    }
}