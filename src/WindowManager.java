import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

@SuppressWarnings("serial")
public class WindowManager extends JFrame {
    // Field members
    private AtomicBoolean paused; //Whether or not the iterator is paused
    public JTextArea textArea;    //The text area for console output
    private JButton pauseButton;  //Pause button
    private JButton stepButton;
    private JButton drawGridButton;
    private JButton startButton;  //Start button
    private JButton saveButton;   //Save button
    private JButton loadButton;   //Load button
    private JButton editCellButton;
    private JButton saveImageButton;
    private JButton cellTemplateButton;
    private JButton minimizeButton;
    private JButton environmentVarButton;
    private WindowManager thisWindow;
    private CellTemplateView cellView;
    private boolean cellViewSetUpDone = false;
    private EnvironmentVarView environmentVarView;
    private boolean environmentVarViewSetUpDone = false;
    private Thread threadObject;  //The thread for running the GridManager
    private transient ContinuousVisualiser v;
    private GridManager manager;  //The grid manager
    private JScrollPane sp;       //The scroll pane which allows the text area to scroll
    public MigLayout l;
    public GridBagConstraints c;
    private int textLine = 0;     //An integer which is incremented as the text area fills
    private float percent;
    private int gridSize;
    private Object[] cellTemplate;
    private  Object[] environmentTemplate;

    //Constructor
    public WindowManager() {
        l = new MigLayout("wrap 3");
        setLayout(l); //Setting the layout for the WindowManager
        paused = new AtomicBoolean(false); //Obviously it starts as false
        textArea = new JTextArea(50, 50); //The size of the text area
        pauseButton = new JButton();    //Initialising the buttons
        stepButton = new JButton();
        startButton = new JButton();
        drawGridButton = new JButton();
        saveButton = new JButton();
        loadButton = new JButton();
        editCellButton = new JButton();
        saveImageButton = new JButton();
        cellTemplateButton = new JButton();
        minimizeButton = new JButton();
        environmentVarButton = new JButton();
        setMinimumSize(this.getPreferredSize());

        initComponents();
    }


    public void initComponents() {
        System.out.println("Number of cores = " + Runtime.getRuntime().availableProcessors());
        // Construct components
        manager = new GridManager(1, 1, this);

        thisWindow = this;
        JMenuBar menuBar = new JMenuBar(); //A menubar to hold the buttons
        //Setting up the text area with the scrollpane
        textArea.setBackground(Color.black);
        textArea.setForeground(Color.white);
        sp = new JScrollPane(textArea);
        add(sp);

        sp.setMinimumSize(new Dimension(500, 900));
        textArea.setMinimumSize(new Dimension(500, 900));
        sp.setMinimumSize(new Dimension(500, 900));

        //Setting the size of the menubar
        menuBar.setSize(400, 50);
        menuBar.setMinimumSize(new Dimension(200, 50));
        //Setting up the text of the buttons
        startButton.setText("Begin");
        pauseButton.setText("Pause");
        stepButton.setText("Step");
        drawGridButton.setText("Draw divisions");
        saveButton.setText("Save");
        loadButton.setText("Load");
        editCellButton.setText("Edit");
        saveImageButton.setText("Save image");
        cellTemplateButton.setText("Cell template");
        minimizeButton.setText("Minimize");
        environmentVarButton.setText("Environment");

        setButtonLook(startButton);
        setButtonLook(pauseButton);
        setButtonLook(stepButton);
        setButtonLook(saveButton);
        setButtonLook(drawGridButton);
        setButtonLook(loadButton);
        setButtonLook(editCellButton);
        setButtonLook(saveImageButton);
        setButtonLook(cellTemplateButton);
        setButtonLook(minimizeButton);
        setButtonLook(environmentVarButton);
        //Adding the buttons to the menubar, and the menubar to the JFrame
        menuBar.add(startButton);
        menuBar.add(pauseButton);
        menuBar.add(minimizeButton);
        menuBar.add(stepButton);
        menuBar.add(drawGridButton);
        menuBar.add(saveButton);
        menuBar.add(loadButton);
        menuBar.add(editCellButton);
        menuBar.add(saveImageButton);
        menuBar.add(cellTemplateButton);
        menuBar.add(environmentVarButton);

        menuBar.setForeground(Color.BLACK);
        menuBar.setBackground(Color.WHITE);
        Border line = new LineBorder(Color.BLACK);
        Border margin = new EmptyBorder(1, 1, 1, 1);
        Border compound = new CompoundBorder(line, margin);
        menuBar.setBorder(compound);
        setJMenuBar(menuBar); //Adding the menubar to the Window manager
        //As the WindowManager extends JFrame and therefore inherits its methods

        //Setting up the listeners
        pauseButton.addActionListener(new ButtonListener());
        addButtonListeners();
        // Runnable that performs the main calculations
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(true) { //The warning 'while statement cannot complete without throwing an exception is not important
                    //The exception only means that the while loop will run infinitely, eventually throwing an exception
                    if(paused.get()) {
                            synchronized(threadObject) {// Pause
                                try {
                                    threadObject.wait();
                                }
                                catch (InterruptedException e) {
                                    System.out.println("Interrupted exception in threadObject");
                                }
                            }
                        }
                        manager.iterate();
                        textArea.setCaretPosition(textArea.getDocument().getLength()); //Scrolling to the bottom
                }
            }
        };
        threadObject = new Thread(runnable);
    }

    public void setButtonLook(JButton button) {
            button.setForeground(Color.BLACK);
            button.setBackground(Color.WHITE);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 15, 5, 15);
            Border compound = new CompoundBorder(line, margin);
            button.setBorder(compound);
    }

    //This method is only called when both of the template views have been used
    public void initGrid() {
        //Calling the methods in the manager to set it up
        manager.setSize(gridSize);
        manager.setCellTemplate(cellTemplate);
        manager.setCells(percent);
        manager.setEnvironmentTemplate(environmentTemplate);

        threadObject.start(); //Starting the thread
        startButton.setVisible(false); //Gets rid of the button
        //Initialising the visualiser
        ArrayBlockingQueue queue = manager.visualise();
        v = new ContinuousVisualiser(manager, queue, gridSize);
        v.setCellTemplate(cellTemplate);
        v.setEnvironmentTemplate(environmentTemplate);
        add(v, "top");

        //Starting the visualiser thread
        Thread thread = new Thread(v);
        thread.start();
    }


    public void addButtonListeners(){
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pcnt = JOptionPane.showInputDialog("Enter the percentage of cells in the array to be populated \n" +
                        "from 1 = 100% to 0 = 0%");
                try {
                    percent = Float.parseFloat(pcnt);
                    if(percent > 1 || percent < 0) {
                        setTextArea("\"" + pcnt + "\" is not a valid value. Please try again");
                    } else {
                        String size = JOptionPane.showInputDialog("Enter a grid size between 100 and 1000");
                        try {
                            gridSize = Integer.parseInt(size);
                            if (gridSize >= 100 && gridSize <= 5000) {
                                setTextArea("Initialising a new grid with dimensions of " + gridSize + "^2");
                                environmentVarView = new EnvironmentVarView(thisWindow);
                                environmentVarView.setLocation(getX(), getY());
                                environmentVarView.setVisible(true);
                                cellView = new CellTemplateView(thisWindow);
                                cellView.setLocation(environmentVarView.getX() + environmentVarView.getWidth(), environmentVarView.getY());
                                cellView.setVisible(true);
                            }
                        } catch (NumberFormatException e1) {
                            setTextArea("\"" + pcnt + "\" is not a valid value");
                        }
                    }
                } catch(NumberFormatException n) {
                    setTextArea("\"" + pcnt + "\" is not a valid value");
                } catch (NullPointerException n ) {
                    //This will be thrown if the operation is cancelled
                } catch(Exception unknown) {
                    setTextArea("An unknown error has occurred");
                    unknown.printStackTrace();
                }
            }
        });

        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(threadObject.isAlive()) {
                    manager.iterate();
                    setTextArea("Performed single iteration");
                    v.calc();
                } else {
                    setTextArea("No simulation is running");
                }
            }
        });
        //The listeners below are fairly self explanatory
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                load();
                startButton.setVisible(false);
            }
        });
        editCellButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //pauseButton.doClick();
                //TODO Finish the cell edit view so that this button has something to do
            }
        });
        saveImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(threadObject.isAlive()) {
                    paused.set(true);
                    v.saveImage();
                    paused.set(false);
                } else {
                    setTextArea("No simulation is running. Therefore there is no image to save");
                }
            }
        });
        cellTemplateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            if (cellView == null && threadObject.isAlive()) {
                cellView = new CellTemplateView(thisWindow);
            } else if(threadObject.isAlive()){
                cellView.setVisible(true);
                cellView.setLastCellTemplate();
            } else {
                setTextArea("No simulation is running");
            }
            }
        });
        drawGridButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(threadObject.isAlive()) {
                    String cellSize = JOptionPane.showInputDialog(
                        thisWindow,
                        "Enter an integer size for each cell. \n" +
                                "it must be less than the size of the grid, but greater than 10 ");
                    try{
                        int size = Integer.parseInt(cellSize);
                        if(size >= 10 && size < v.size ) {
                            v.drawGrid(size);
                        } else {
                            setTextArea("\"" + cellSize + "\"" + " is not a valid");
                        }
                    } catch (NumberFormatException e1) {
                        setTextArea("\"" + cellSize + "\"" + " is not a valid");
                    }
                } else {
                    setTextArea("No simulation is running");
                }
            }
        });

        minimizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(sp.getHeight() > 300) {
                    sp.setMinimumSize(new Dimension(300, 300));
                    textArea.setMinimumSize(new Dimension(300, 300));
                    sp.setSize(300, 300);
                    textArea.setSize(300, 300);
                } else {
                    sp.setMinimumSize(new Dimension(500, 900));
                    textArea.setMinimumSize(new Dimension(500, 500));
                    sp.setSize(500, 900);
                    textArea.setSize(500, 900);
                }
            }
        });
        environmentVarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(environmentVarView != null) {
                    environmentVarView.setVisible(true);
                } else {
                    environmentVarView = new EnvironmentVarView(thisWindow);
                }

            }
        });
    }


    //This method sends the cell template to the visualiser
    public void setCellTemplate(Object[] cellTemplate) {
        if(cellViewSetUpDone) {
            System.out.println("WindowManager setting new cell template");
            if (threadObject.isAlive()) {
                v.setCellTemplate(cellTemplate);
            } else {
                setTextArea("No simulation is running.");
            }
        } else {
            this.cellTemplate = cellTemplate;
        }
    }

    public void setEnvironmentTemplate(Object[] o) {
        if(environmentVarViewSetUpDone) {
            System.out.println("WindowManager recieved new environment template");
            manager.setEnvironmentTemplate(o);
            v.setEnvironmentTemplate(o);
        } else {
            this.environmentTemplate = o;
        }
    }

    //Method to serialise and save the grid manager and its contents
    public void save() {
        Thread t = new Thread(saveRunnable);
        t.start();
    }

    public void load() {
        final WindowManager w = this;
        Thread t = new Thread(loadRunnable);
        t.start();
        manager.setWindowManager(w);
    }

    private Runnable saveRunnable = new Runnable() {
        @Override
        public void run() {
            paused.set(true);
            pauseButton.setText("Start");
            /*
            While pauseButton.doClick() may work in some cases it is unreliable as it can unpause an already paused program
            it therefore makes more sense to set paused manually
             */
            try { //Must be in a try/catch as exceptions can be thrown
                final JFileChooser fc = new JFileChooser(); //Creating the file choose
                int returnVal = fc.showSaveDialog(new JFrame()); //parent component to JFileChooser
                if (returnVal == JFileChooser.APPROVE_OPTION) { //OK button pressed by user
                    File file = fc.getSelectedFile(); //get File selected by user
                    String path = file.getAbsolutePath();
                    if(!path.contains(".ser")) {
                        path += ".ser"; //Making sure the file has the correct file name
                    }
                    FileOutputStream fileOut = new FileOutputStream(path); //Setting up the streams. (Don't cross them)
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(manager.getCells());
                    out.close();
                    fileOut.close();
                    setTextArea("Save complete. Your file has been saved to " + path);
                    setTextArea("To continue with the current simulation, click \"Start\"");
                }
            } catch(Exception f) {
                //This most likely won't happen, unless the program doesn't have access to a directory
                f.printStackTrace();
            }
        }
    };

    private Runnable loadRunnable = new Runnable() {
        @Override
        public void run() {
            paused.set(true);
            pauseButton.setText("Start");
            try { //Try/catch
                //An option pane for safety
                int result = JOptionPane.showConfirmDialog(null,
                    "Loading a new simulation will erase your current simulation \n" +
                            "Do you want to save your simulation before loading a new one?",null, JOptionPane.YES_NO_OPTION);
                if(result == JOptionPane.YES_OPTION) {
                    save(); //Opening a save dialog
                }
                final JFileChooser fc = new JFileChooser(); //Creating the file choose
                int returnVal = fc.showSaveDialog(new JFrame()); //parent component to JFileChooser
                if (returnVal == JFileChooser.APPROVE_OPTION) { //OK button pressed by user
                    File file = fc.getSelectedFile(); //get File selected by user
                    System.out.println("The path has been collected");
                    String path = file.getAbsolutePath();
                    if(!path.contains(".ser")) {
                        throw new FileNotFoundException(); //If the file doesn't have this extension, it can't be opened
                    }
                    FileInputStream fileIn = new FileInputStream(path);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    manager.setCells((Cell[][]) in.readObject());   //Reading the gridmanager
                    System.out.println("Set the cell array");
                    ArrayBlockingQueue queue = manager.visualise();
                    //Initialising the new visualiser
                    v = new ContinuousVisualiser(manager, queue, manager.gridSize);
                    add(v);
                    manager.iterate();
                    manager.setWindowManager(thisWindow);
                    //Adding the visualiser to a thread
                    Thread thread = new Thread(v);
                    thread.start();
                    in.close();
                    fileIn.close();
                    setTextArea("Load complete. Your file has been loaded from " + path);
                    setTextArea("To begin running the new simulation, click \"Start\"");
                }
            } catch (FileNotFoundException e ) {
                setTextArea("The file must use the extension .ser");
            } catch(Exception f) {
                setTextArea("Error: Your selected file is not the correct filetype. Save files should be the \".ser\" filetype" );
                f.printStackTrace();
            }
        }
    };


    public void setTextArea(String text) {
        String current = textArea.getText();
        textArea.setText(current + "\n" + textLine++ + " " + text);
    }

    //The getters and setters below are used when initialising the grid
    public void setCellViewSetUpDone() {
        cellViewSetUpDone = true;
    }

    public void setEnvironmentVarViewSetUpDone() {
        environmentVarViewSetUpDone = true;
    }

    public boolean getEnvironmentVarViewSetUpDone() {
        return  environmentVarViewSetUpDone;
    }

    public boolean getCellViewSetUpDone() {
        return  cellViewSetUpDone;
    }



    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1300, 900); //Forcing the  program to open at this size
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(500, 450);
    }

    class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            if (threadObject.isAlive()) {
                if (!paused.get()) {
                    pauseButton.setText("Start");
                    setTextArea("Paused");
                    paused.set(true);
                    v.setParentPauseState(paused);
                } else {
                    pauseButton.setText("Pause");
                    setTextArea("Unpaused");
                    paused.set(false);
                    v.setParentPauseState(paused);
                    System.out.println("Setting visualiserPaused to " + paused);

                    // Resume
                    synchronized (threadObject) {
                        threadObject.notify();
                    }
                }
            }
        }
    }

}