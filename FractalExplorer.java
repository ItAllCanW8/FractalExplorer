import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;


public class FractalExplorer extends JFrame {

    static final int WIDTH  = 1240;
    static final int HEIGHT = 1024;

    Canvas canvas;
    BufferedImage fractalImage;

    static final int MAX_ITERATION = 1000; //> - higher accuracy but at what cost? :o

    static final double DEFAULT_ZOOM       = 50.0;
    static final double DEFAULT_TOP_LEFT_X = -3.0;
    static final double DEFAULT_TOP_LEFT_Y = +3.0;

    double zoomFactor = DEFAULT_ZOOM;
    double topLeftX   = DEFAULT_TOP_LEFT_X;
    double topLeftY   = DEFAULT_TOP_LEFT_Y;
    
    public FractalExplorer() {
        setInitialGUIProperties();
        addCanvas();
        canvas.addKeyStrokeEvents();
        updateFractal();
    }

    public static void main(String[] args) {
        new FractalExplorer();
    }

    private void addCanvas() {
        canvas = new Canvas();
        fractalImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        canvas.setVisible(true);
        this.add(canvas, BorderLayout.CENTER);
    }

    public void setInitialGUIProperties() {
        this.setTitle("Fractal Explorer");      
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(WIDTH,HEIGHT);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
    
    private double getXPos(double x) {
        return x/zoomFactor + topLeftX;
    }

    private double getYPos(double y) {
        return y/zoomFactor - topLeftY;
    }

    private int makeColor(int iterCount) {

        int color = 0b011011100001100101101000; 
        int mask  = 0b000000000000000101010100; 
        int shiftMagnitude = iterCount / 13;

        if(iterCount == MAX_ITERATION)
            return Color.BLACK.getRGB();

        return color * (mask << shiftMagnitude);
    }

    public void updateFractal() {

        // transform coordinates of all pixels into points on the complex plane
        for (int x = 0;x < WIDTH ;x++ ) {
            for (int y = 0;y < HEIGHT ;y++ ) {
                
                // the real values are on x axis and the imag are on y axis (on complex plane)
                double c_real = getXPos(x);
                double c_imag = getYPos(y);

                int iterCount = computeIterations(c_real, c_imag);

                int pixelColor = makeColor(iterCount);

                fractalImage.setRGB(x, y, pixelColor);  
            }
        }

        canvas.repaint();

    }

    private int computeIterations(double c_real, double c_imag) {

        /*
            Complex point: c = a + bi 
                           i^2 == -1

            Formula: Zn+1 = (Zn)^2 + c
                     Zo = 0 
                     |z| = |x + yi| = √(x^2 + y^2)

                     if |z| <= 2 -> continue
                    
                     if |z∞| <= 2 -> point c will foreverly remain within our set 


            c = c_real + c_imag
            z = z_real + z_imag

            z' = z*z + c
               = (z_real + z_imag)*(z_real + z_imag) + c_real + c_imag
               = z_real^2 + 2*z_real*z_imag - z_imag^2 + c_real + c_imag (i^2 == -1)

            z_real' = z_real^2 - z_imag^2 + c_real
            z_imag' = 2*z_real*z_imag + c_imag

            z' = z_real' + z_imag'
        */

        double z_real = 0.0;
        double z_imag = 0.0;

        int iterCount = 0;

        // √(z_real^2 + z_imag^2) <= 2.0
        // z_real^2 + z_imag^2 <= 4.0

        while (z_real * z_real + z_imag * z_imag <= 4.0 ){

            double z_real_tmp = z_real;

            z_real = z_real * z_real - z_imag * z_imag + c_real;
            z_imag = 2 * z_real_tmp * z_imag + c_imag;

            // Point was inside the Mandelbrot set
            if (iterCount >= MAX_ITERATION)
                return MAX_ITERATION;

            iterCount++;

        }

        // Complex point was outside the Mandelbrot set
        return iterCount;
    }

    private void moveUp() {

        double currHeight = HEIGHT / zoomFactor;
        topLeftY += currHeight / 6;
        updateFractal();

    }

     private void moveDown() {

        double currHeight = HEIGHT / zoomFactor;
        topLeftY -= currHeight / 6;
        updateFractal();
        
    }

     private void moveLeft() {

        double currWidth = WIDTH / zoomFactor;
        topLeftX -= currWidth / 6;
        updateFractal();
        
    }

    private void moveRight() {

        double currWidth = WIDTH / zoomFactor;
        topLeftX += currWidth / 6;
        updateFractal();
        
    }

    private void adjustZoom( double newX, double newY, double newZoomFactor ) {

        // Shifting one way
        topLeftX += newX/zoomFactor;
        topLeftY -= newY/zoomFactor;

        zoomFactor = newZoomFactor;

        // Recenter
        topLeftX -= (WIDTH/2) / zoomFactor;
        topLeftY += (HEIGHT/2) / zoomFactor;

        updateFractal();

    }

    private class Canvas extends JPanel implements MouseListener {

        public Canvas() {
            addMouseListener(this);
        }

        @Override public Dimension getPreferredSize() {
            return new Dimension(WIDTH, HEIGHT);
        }

        @Override public void paintComponent(Graphics drawingObj) {
            drawingObj.drawImage(fractalImage, 0, 0, null);
        }

        @Override public void mousePressed(MouseEvent mouse) {

            double x = (double) mouse.getX();
            double y = (double) mouse.getY();

            switch (mouse.getButton()) {

                // Left
                case MouseEvent.BUTTON1:
                    adjustZoom(x, y, zoomFactor*2);
                    break;

                // Right
                case MouseEvent.BUTTON3:
                    adjustZoom(x, y, zoomFactor/2);
                    break;    
            }
        }

        public void addKeyStrokeEvents() {

            KeyStroke wKey = KeyStroke.getKeyStroke(KeyEvent.VK_W, 0 );
            KeyStroke aKey = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0 );
            KeyStroke sKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0 );
            KeyStroke dKey = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0 );

            Action wPressed = new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    moveUp();
                }
            };

            Action aPressed = new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    moveLeft();
                }
            };    
                
            Action sPressed = new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    moveDown();
                }
            };    
                
            Action dPressed = new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    moveRight();
                }            
            };

             this.getInputMap().put( wKey, "w_key" );
             this.getInputMap().put( aKey, "a_key" );
             this.getInputMap().put( sKey, "s_key" );
             this.getInputMap().put( dKey, "d_key" );

             this.getActionMap().put( "w_key", wPressed );
             this.getActionMap().put( "a_key", aPressed );
             this.getActionMap().put( "s_key", sPressed );
             this.getActionMap().put( "d_key", dPressed );

        }

        @Override public void mouseReleased(MouseEvent mouse){}
        @Override public void mouseClicked(MouseEvent mouse){}
        @Override public void mouseEntered(MouseEvent mouse){}
        @Override public void mouseExited(MouseEvent mouse){}

    }   

} 