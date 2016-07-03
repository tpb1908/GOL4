
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Theo on 06/09/2015.
 */
public class ContinuousVisualiser extends JPanel implements  Runnable, MouseMotionListener {
    private BufferedImage BI;
    private GridManager Manager; //Backwards communication is needed
    private ArrayBlockingQueue<Cell[][]> cellsQueue;
    public int size;
    private int livingCells;
    private int drawSize;
    private int[] point1;
    private int[] point2;
    private Cell[][] cells;
    private  Object[] cellTemplate;
    private Object[] environmentTemplate;
    public boolean isPaintingCells;
    public boolean point1Declared;

    public int drawMethod;
    private AtomicBoolean parentPauseState;

    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        if (isPaintingCells) {
            drawPoint(e.getX(), e.getY(), drawSize);
            if(parentPauseState.get()) {
                System.out.println("Calcing during pause");
                calc();
            }
            repaint();
        }
    }





    //Constructor
    public ContinuousVisualiser(GridManager Manager, ArrayBlockingQueue queue, final int size) {
        this.size = size;
        this.cellsQueue = queue;
        this.addMouseMotionListener(this);
        this.Manager = Manager;
        this.point1Declared = false;
        this.drawMethod = 0;
        this.drawSize = 1;
        this.livingCells = 0;
        point1 = new int[2];
        point2 = new int[2];
        setLayout(null);
        setMaximumSize(new Dimension(size, size));
        BI = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        isPaintingCells = false;
        parentPauseState = new AtomicBoolean(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (SwingUtilities.isRightMouseButton(e)) {
                    isPaintingCells = false;
                    point1Declared = false;
                    if (drawMethod < 4) {
                        drawMethod++;
                    } else {
                        drawMethod = 0;
                    }
                } else {
                    switch (drawMethod) {
                        case 0:
                            point1Declared = false;
                            drawPoint(e.getX(), e.getY(), drawSize);
                            break;
                        case 1:
                            isPaintingCells = false;
                            if (!point1Declared) {
                                point1[0] = e.getX();
                                point1[1] = e.getY();
                                point1Declared = true;
                            } else {
                                point2[0] = e.getX();
                                point2[1] = e.getY();
                                drawLine(point1[0], point1[1], point2[0], point2[1]);
                                point1Declared = false;
                            }
                            break;
                        case 2:
                            isPaintingCells = !isPaintingCells;
                        /*
                        * No idea why I didn't do this before-
                        * There is no need for if(foo) {foo = false} else {foo = true}
                        * Just write foo = !foo
                         */
                            break;
                        case 3:
                            cells[e.getY()][e.getX()].kill();
                            cells[e.getY()][e.getX()].isBlock = false;
                            break;
                        case 4:
                            System.out.println("Processng food draw call. Food level of " + environmentTemplate[7]);
                            drawPoint(e.getY(), e.getX(), size);
                    }

                    if (parentPauseState.get()) {
                        System.out.println("Calcing during pause");
                        calc();
                    }
                    repaint();
                }
            }
        });

    }

    public void setCellTemplate(Object[] cellTemplate) {
        this.cellTemplate = cellTemplate;
        System.out.println("Visualiser cell template set");
        System.out.println("Template radiation value = " + this.cellTemplate[21]);
        drawSize = (Integer) cellTemplate[0];
        System.out.println("drawSize set to " + drawSize);
    }



    public void setEnvironmentTemplate(Object[] environmentTemplate) {
        this.environmentTemplate = environmentTemplate;
        System.out.println("Visualiser environment template has been set");
        System.out.println("Environment radiation value = " + this.environmentTemplate[4]);
    }

    public void drawGrid(int distance) {
        for(int x = distance; x < size; x += distance) {
            drawLine(x, 0, x, size-1);
            System.out.println("Drawing line");
            System.out.println(x);
        }
        for(int y = distance; y < size; y += distance) {
            drawLine(0, y, size-1, y);
            System.out.println("Drawing line");
            System.out.println(y);
        }
        calc();
        repaint();
    }

    //https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
    public void drawLine(int x ,int y ,int x2 , int y2) {
        Manager.setTextArea("Drew a line between (" + x + ", " + y + ") and (" + x2 + ", " + y2 + ")" );
        int w = x2 - x ; //The lengths of the legs of the right triangle formed between the two points
        int h = y2 - y ;
        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ; //Initialising variables
        if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ; //Whether the gradient is positive or negative
        if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
        if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
        int longest = Math.abs(w) ; //Abs to ensure that the lengths aren't negative
        int shortest = Math.abs(h) ;
        if (!(longest>shortest)) { //Switching the values around if they are in the wrong order
            longest = Math.abs(h) ; //Absolute values remove negatives
            shortest = Math.abs(w) ;
            if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
            dx2 = 0 ;
        }
        int numerator = longest >> 1 ; //Bit shifting by 1
        for (int i=0; i <=longest; i++) {
            try {
                cells[y][x].isBlock = true;
                cells[y + 1][x].isBlock = true;
                cells[y + 1][x + 1].isBlock = true;
                cells[y][x + 1].isBlock = true;
            } catch (IndexOutOfBoundsException e) {}
            /*
            * The exception above will happen when the user attempts to draw a large lattice at the edge of the screen
            * It is expected, no error handling is needed
             */
            numerator += shortest ;
            if (!(numerator<longest)) {
                numerator -= longest ;
                x += dx1 ;
                y += dy1 ;
            } else {
                x += dx2 ;
                y += dy2 ;
            }
        }
    }

    public void drawPoint(int X, int Y, int size) {
        if(drawMethod != 2) {
            Manager.setTextArea("Drew a point at (" + X + ", " + Y + ")");
        }
        if(size == 1) {
            cells[Y][X].isAlive = 1;
        } else if( (((X & 1) == 0) && ((Y & 1) == 0)) || drawMethod == 0) {
            long startTime = System.nanoTime();
            int y2 = Y + size / 2;
            if(y2 < 0) {
                y2 = 0;
            }
            try {
                for (int b = 0; b <= size; b++) {
                    int x2 = X - size / 2;
                    if (x2 < 0) {
                        x2 = 0;
                    }
                    for (int a = 0; a <= size; a++) {
                        if (drawMethod == 4) {
                            cells[y2][x2].increaseFood((int) environmentTemplate[7]);
                        } else {
                            if ((y2 & 1) == 0) {
                                if ((a & 1) == 0) {
                                    cells[y2][x2].applyTemplate(cellTemplate);
                                    cells[y2][x2].applyEnvironmentTemplate(environmentTemplate);
                                }
                            } else {
                                if ((a & 2) != 0) {
                                    cells[y2][x2].applyTemplate(cellTemplate);
                                    cells[y2][x2].applyEnvironmentTemplate(environmentTemplate);
                                }
                            }
                        }
                        x2++;
                    }
                    y2--;
                }
            } catch (IndexOutOfBoundsException i) {
                //This could possibly happen if the square is drawn close to the edge of the grid
            }
            long endtime = System.nanoTime();
            System.out.println("Point drawing took " + (endtime - startTime));
        }

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(size, size);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(size, size);
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        g.drawImage(BI, 0, 0, size, size, null); //Drawing the image to the JPanel
        g.setColor(Color.black);
        g.drawString("Living cells: " + livingCells, 5, 10);
        switch (drawMethod){
            case 0:
                g.drawString("Draw method- Point drawing", 150, 10);
                break;
            case 1:
                g.drawString("Draw method- Line drawing", 150, 10);
                break;
            case 2:
                g.drawString("Draw method- Brush drawing", 150, 10);
                break;
            case 3:
                g.drawString("Draw method- Cell remover", 150, 10);
                break;
            case 4:
                g.drawString("Draw method- Increase food level", 150, 10);
        }
    }

    public void run() {
        while(true) {
            try {
                if(cellsQueue.remainingCapacity() != 0) {
                    cells = cellsQueue.take();
                    calc();
                    this.repaint();

                }
                //Iterating through the  array and setting the colours of the Buffered Image accordingly
            } catch (Exception e) {
                //System.out.println(e.getStackTrace());
            }
        }
    }

    public void calc() {
        int tempLivingCells = 0;
        for (int row = 0; row < cells.length; row++) {
            for (int column = 0; column < cells[row].length; column++) {
                BI.setRGB(column, row, cells[row][column].generateColourCode());
                if(cells[row][column].getIsAlive() > 0) {
                    tempLivingCells++;
                }
            }
        }
        livingCells = tempLivingCells;
    }



    public void saveImage() {
        final JFileChooser fc = new JFileChooser(); //Creating the file choose
        int returnVal = fc.showSaveDialog(new JFrame()); //parent component to JFileChooser
        if (returnVal == JFileChooser.APPROVE_OPTION) { //OK button pressed by user
            File file = fc.getSelectedFile(); //get File selected by user
            String path = file.getAbsolutePath();
            if(!path.contains(".png")) {
                path = path + ".png"; //Ensuring that the file path has a file extension
            }
            try {
                ImageIO.write(BI, "PNG", new File(path));
                Manager.setTextArea("Successfully wrote image to " + path);
            } catch (IOException e) {
                Manager.setTextArea("Unfortunately there was an IO exception");
            }
        }
    }



    public void setParentPauseState(AtomicBoolean newBool) {
        parentPauseState = newBool;
    }
}
