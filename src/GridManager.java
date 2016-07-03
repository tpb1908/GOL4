
import java.awt.*;
import java.io.Serializable;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Theo on 05/09/2015.
 */
public class GridManager implements Serializable{
    private Cell[][] Cells; //The cell array, Duh
    public int generationCounter; //Generation counter for iterations
    private transient WindowManager windowManager; //The window manager, allowing communication
    private Random rnd = new Random(); //Random, for generating the new grid
    private long startTime; //Start time is set on each iteration
    //As a continuous visualiser is updated, it is shared between methods
    private ArrayBlockingQueue<Cell[][]> visualiserQueue;
    public int gridSize;
    private Object[] environmentTemplate;
    private Object[] cellTemplate;

    private  int environmentTemperature = 0;
    private int environmentHumidity = 0;
    private int environmentPH = 7;
    private int environmentPressure = 0;
    private int environmentRad = 0;
    private int environmentOxygenLevel = 22;
    private int environmentToxicityLevel = 0;

    ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    //The constructor
    public GridManager(int cellWidth, int cellHeight, WindowManager parentWindowManager) {
        this.gridSize = cellHeight;
        Cells = new Cell[cellHeight][cellWidth]; //Initialising the array
        windowManager = parentWindowManager; //Allowing communication
        generationCounter = 0; //Setting up the generationCounter
        visualiserQueue = new ArrayBlockingQueue<Cell[][]>(500); /*
        The array blocking queue is now 500 in size due to an increase in the date being passed through it.
        With a smaller queue, crashes sometimes occurred when passing data from the Gridmanager to the queue
        as the queue had was full
        */
        startTime = System.nanoTime(); //Setting the startTime
    }


    public void setCells(float percent) { //Bringing forth living cells from the primordial soup
        for (Cell[] row : Cells) {
            for (int j = 0; j < row.length; j++) {
                row[j] = new Cell();
                if (rnd.nextDouble() < percent) {
                    row[j].applyTemplate(cellTemplate);
                    //If the random meets the requirement, the cell is brought to life
                }
            }
        }
        startTime = System.nanoTime();
    }



    //The most important part of the code
    public void iterate() {
        try {
            for(int i = 0; i < Cells.length; i++) {
                final Cell[] row = Cells[i];
                final int finalI = i;
                exec.submit(new Runnable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < Cells.length; j++) {
                            doUpdate(finalI, j, row);
                        }
                    }
                });
            }
            Thread.sleep(25);
        } catch (Exception e) {} finally {
            generationCounter++;
        }
        if (generationCounter % 100 == 0) { //TODO- Allow this to be changed?
            float endTime = System.nanoTime(); //Collecting the end time
            windowManager.setTextArea("Generation count " + generationCounter);
            try {
                float seconds = ((endTime - startTime) / 1000000000);
                System.out.println("Seconds = " + seconds);
                System.out.println("Generation counter = " + generationCounter);
                windowManager.setTextArea("Iterating at a speed of " + (100 / seconds) + " iterations per second");
            } catch (ArithmeticException e) {
                e.printStackTrace();
                /*This shouldn't actually happen anymore
                * It used to happen because of a casting problem between int and float, whereby the division
                * of endTime-startTime by 1000000000 resulted in 0, as it was cast from a float to an int
                * This resulted in an ArithmeticException when 100 was divided by 0
                 */
            }
            startTime = System.nanoTime(); //Resetting the start time
        }
        if(visualiserQueue.remainingCapacity() != 0) {
            visualiserQueue.add(Cells); //Adding the cell array to the blocking queue, allowing the visualiser to access it
        } else {
            visualiserQueue.clear();
        }
    }

    public void doUpdate(int i, int j, Cell[] row) {
        List<Cell> surroundingCells = new ArrayList<Cell>() {}; //The list for surrounding cells
        try {
            //Collecting the surrounding cells
            surroundingCells.add(Cells[i + 1][j]);
            surroundingCells.add(Cells[i][j + 1]);
            surroundingCells.add(Cells[i][j - 1]);
            surroundingCells.add(Cells[i - 1][j + 1]);
            surroundingCells.add(Cells[i + 1][j + 1]);
            surroundingCells.add(Cells[i - 1][j - 1]);
            surroundingCells.add(Cells[i + 1][j - 1]);
            surroundingCells.add(Cells[i - 1][j]);
        } catch (IndexOutOfBoundsException outOfBounds) {
                    /*This exception will happen if the cell is at the edge of the array
                    *I need to find out if it is more efficient to catch the exception or to add extra code
                    * to stop the exception from happening.
                    * As there are so many cells, and the exception will happen at least 4000 times per iteration,
                    * it seems like leaving the catch in will be the most efficient
                     */
        } catch (Exception e) {
            //Any other exception, hasn't happened... yet
            System.out.println("An unknown exception has occured while gathering surrounding cells");
            System.out.println("Cell row of " + i + " and cell column of " + j);
            System.out.println(e.getStackTrace());
        }
        row[j].update(surroundingCells);
    }

    public void setEnvironmentTemplate(Object[] o) {
        environmentTemperature = (int) o[0];
        environmentHumidity = (int) o[1];
        environmentPH = (int) o[2];
        environmentPressure = (int) o[3];
        environmentRad = (int) o[4];
        environmentOxygenLevel = (int) o[5];
        environmentToxicityLevel = (int) o[6];
        System.out.println("Manager set environment template");

        for(Cell[] row : Cells) {
            for (Cell c : row) {
                c.applyEnvironmentTemplate(o);
            }
        }
    }
    public void setCellTemplate(Object[] o) {
        cellTemplate = o;
    }

    public void setTextArea(String text) {
        windowManager.setTextArea(text);
    }

    public ArrayBlockingQueue visualise() {
        return visualiserQueue;
    }

    public  void setCells(Cell[][] cells) {
        this.Cells = cells;
    }
    public Cell[][] getCells() { return Cells;}


    public void setWindowManager(WindowManager windowManager1) {
        windowManager = windowManager1;
    }

    public void setSize(int size) {
        Cells = new Cell[size][size];
    }


}
