import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Theo on 13/10/2015.
 * Written by Flynn on 27/10/2015
 */
public class EnvironmentVarView extends JFrame {

    private WindowManager parentManager;
    private Object[] environmentTemplate;
    private Object[] lastEnvironmentTemplate;
    private MigLayout g;
    private JButton OK;
    private JButton Cancel;
    private JSlider[] sliders;
    private JCheckBox[] checkboxes;
    private boolean firstRun = true;

    private JScrollPane scrollPane;
    private JPanel scrollPanel;
    private MigLayout scrollLayout;


    private  int environmentTemperature = 0;
    private int environmentHumidity = 0;
    private int environmentPH = 5;
    private int environmentPressure = 0;
    private int environmentRad = 0;
    private int environmentOxygenLevel = 22;
    private int environmentToxicityLevel = 0;
    private int foodAbundance = 50;
    private int solarRadiationLevel = 0;
    private int decayLevel = 0;


    public EnvironmentVarView(WindowManager parentManager) {
        this.parentManager = parentManager;
        environmentTemplate = new Object[100];
        lastEnvironmentTemplate = environmentTemplate;
        sliders = new JSlider[100];
        checkboxes = new JCheckBox[100];
        g = new MigLayout("wrap 3");
        setLayout(g);
        scrollLayout = new MigLayout("wrap 3");
        scrollPanel = new JPanel(scrollLayout);
        scrollPane = new JScrollPane(scrollPanel);
        OK = new JButton();
        OK.setText("Create");
        Cancel = new JButton();
        Cancel.setText("Abort");

        addListeners();
        positionUI();
        this.setMinimumSize(new Dimension(530, 600));
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

    }

    public void addListeners() {
        OK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (firstRun) {
                    generateTemplate();
                    firstRun = false;
                    parentManager.setEnvironmentTemplate(environmentTemplate);
                    parentManager.setEnvironmentVarViewSetUpDone();
                    if(parentManager.getCellViewSetUpDone()) {
                        parentManager.initGrid();
                    }
                    setVisible(false);
                } else {
                    System.out.println("Setting environment template");
                    generateTemplate();
                    parentManager.setEnvironmentTemplate(environmentTemplate);
                    setVisible(false);
                }
            }
        });
        Cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restoreLastTemplate();
                setVisible(false);
            }
        });
    }

    public void restoreLastTemplate(){
        environmentTemplate = lastEnvironmentTemplate;
        for (int i = 0; i < sliders.length; i += 2) {
            if(sliders[i] != null && environmentTemplate[i] != null && i != 2) {
                sliders[i].setValue((Integer) environmentTemplate[i]);
            } else {
                System.out.println("Null slider");
            }
        }
    }

    public void positionUI() {


        scrollPanel.setMinimumSize(new Dimension(500, 450));
        scrollPane.setMinimumSize(new Dimension(500, 450));
        scrollPanel.setMaximumSize(new Dimension(500, 450));
        scrollPane.setMaximumSize(new Dimension(500, 450));
        scrollPanel.setLayout(scrollLayout);
        scrollPanel.setVisible(true);
        scrollPane.setVisible(true);
        scrollPane.add(scrollPanel);
        scrollPane.setViewportView(scrollPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, "span, wrap 15");

        addVariableChanger("Temperature", 10, -50, 50, environmentTemperature, 1);
        addVariableChanger("Humidity", 10, 0, 100, environmentHumidity, 2);
        addVariableChanger("PH", 1, 0, 14, environmentPH, 3);
        addVariableChanger("Atmospheric pressure", 100, 0, 1000, environmentPressure, 4);
        addVariableChanger("Radiation", 5, 0, 30, environmentRad, 5);
        addVariableChanger("Toxicity", 1, 0, 10, environmentToxicityLevel, 7);
        addVariableChanger("Food level", 50, 0, 500, foodAbundance, 8);
        addVariableChanger("Solar radiation", 5, 0, 30, solarRadiationLevel, 9);
        addVariableChanger("Decay", 10, 0, 100, decayLevel, 10);


        add(OK);
        add(Cancel);

        this.setVisible(true);
    }

    public void addVariableChanger(String title, int majorTickSpacing,  int low, int high, int defaultValue, final int valueToChange) {

        JLabel label = new JLabel(title);
        final JSlider slider = new JSlider(low, high, defaultValue);
        slider.setMajorTickSpacing(majorTickSpacing);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setPreferredSize(new Dimension(350, 50));
        sliders[valueToChange] = slider;

        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                switch (valueToChange) {
                    case 1:
                        environmentTemperature = slider.getValue();
                        break;
                    case 2:
                        environmentHumidity = slider.getValue();
                        break;
                    case 3:
                        environmentPH = slider.getValue();
                        break;
                    case 4:
                        environmentPressure = slider.getValue();
                        break;
                    case 5:
                        environmentRad = slider.getValue();
                        break;
                    case 6:
                        environmentOxygenLevel = slider.getValue();
                        break;
                    case 7:
                        environmentToxicityLevel = slider.getValue();
                        break;
                    case 8:
                        foodAbundance = slider.getValue();
                        break;
                    case 9:
                        decayLevel = slider.getValue();
                        break;
                }
            }
        });


        scrollPanel.add(label);
        scrollPanel.add(slider, "wrap 10");
    }

    public void generateTemplate() {
        environmentTemplate[0] = environmentTemperature;
        environmentTemplate[1] = environmentHumidity;
        environmentTemplate[2] = environmentPH;
        environmentTemplate[3] = environmentPressure;
        environmentTemplate[4] = environmentRad;
        environmentTemplate[5] = environmentOxygenLevel;
        environmentTemplate[6] = environmentToxicityLevel;
        environmentTemplate[7] = foodAbundance;
        environmentTemplate[8] = decayLevel;

        lastEnvironmentTemplate = environmentTemplate;
    }

}
