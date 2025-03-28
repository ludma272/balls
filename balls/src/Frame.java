import javax.swing.JFrame;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Frame {
    private JFrame frame;
    private int width = 800;
    private int height = 600;
    private List<FrameKeyListener> keyListeners = new ArrayList<>();

    // Interface for objects that want to receive key events
    public interface FrameKeyListener {
        void keyPressed(KeyEvent e);
    }

    public Frame() {
        // Initialize the frame
        frame = new JFrame("Physics Simulation");

        // Set frame properties
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center the window
        frame.setResizable(true);

        // Set background color - this sets the content pane's background
        frame.getContentPane().setBackground(Color.BLACK); // Dark background for contrast

        // Add a component listener to detect window resizing
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                width = frame.getWidth();
                height = frame.getHeight();
            }
        });

        // Add key listener to the frame
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Notify all registered key listeners
                for (FrameKeyListener listener : keyListeners) {
                    listener.keyPressed(e);
                }
            }
        });

        // Ensure the frame can receive key events
        frame.setFocusable(true);
        frame.requestFocus();

        // Make the frame visible
        frame.setVisible(true);
    }

    // Register a key listener
    public void addKeyListener(FrameKeyListener listener) {
        keyListeners.add(listener);
    }

    // Method to change background color
    public void setBackgroundColor(Color color) {
        frame.getContentPane().setBackground(color);
    }

    // Getter for the frame if needed
    public JFrame getFrame() {
        return frame;
    }

    // Getters for dimensions
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // Request focus to ensure key events are captured
    public void requestFocus() {
        frame.requestFocus();
    }
}