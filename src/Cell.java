import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * Created by Theo on 05/09/2015.
 */
public class Cell implements Serializable {

    public float isAlive;
    private boolean killed = false;
    public boolean isBlock;
    private float generationsSurvived;
    private float lowerSurvivalBound;
    private float upperSurvivalBound;
    private float birthLowerBound;
    private float birthUpperBound;
	private float colourCode;
    private float foodAbundance;
    private float foodTolerance;
    private float foodConsumption;
    private Random random;
    private float generationsBetweenSplit;
    private Object[] cellTemplate;
    private Object[] environmentTemplate;

    public Cell() {
        isAlive = 0f;
        generationsSurvived = 0;
        generationsBetweenSplit = 1f;
        lowerSurvivalBound = 2;
        upperSurvivalBound = 3f;
        birthUpperBound = 5;
        birthLowerBound = 1;
        random = new Random();
        colourCode = 0;
        foodTolerance = 20; //If food abundance exceeds '20' then decay begins to increase as a result
        foodConsumption = 0.5f;
    }

    public void update(List<Cell> surroundingCells) {
        if(!isBlock && isAlive > 0) {
            foodAbundance -= 0.5f;
            int sum = 0;
            killed = false;
            //TODO- Implement the evolution here
            //There should be some other variables. No idea what
            for (Cell cell : surroundingCells) {
                //Iterating through the surrounding cells
                sum += cell.getIsAlive();
            }
            if(foodAbundance > (int) cellTemplate[26]) {
                environmentTemplate[8] = (int) environmentTemplate[8] + 1;
            }

            if((int) environmentTemplate[4] > (int) cellTemplate[20] && (boolean) cellTemplate[21]) { //Radiation
                //System.out.println("Killed due to radiation");
                kill();
            } else if((int) environmentTemplate[0] > (int) cellTemplate[10] && (boolean) cellTemplate[11]) { //High temp
                kill();
                //System.out.println("Killed due to temperature");
            } else if((int) environmentTemplate[0] < (int) cellTemplate[8] && (boolean) cellTemplate[9]) { //Low temp
                kill();
            } else if((int) environmentTemplate[1] > (int) cellTemplate[12] && (boolean) cellTemplate[13]) { //Humidity
                //System.out.println("Killed due to humidity");
                kill();
            } else if((int) environmentTemplate[2] > (int) cellTemplate[14] && (boolean) cellTemplate[15]) { //High PH
                kill();
                //System.out.println("Killed due to high PH. PH of " + environmentTemplate[2] + " and PH resistance of " + cellTemplate[14]);
            } else if((int) environmentTemplate[2] < (int) cellTemplate[16] && (boolean) cellTemplate[17]) { //Low PH
                kill();
                //System.out.println("Killed due to low PH");
            } else if((int) environmentTemplate[3] > (int) cellTemplate[18] && (boolean) cellTemplate[19]) { //Pressure
                kill();
                //System.out.println("Killed due to pressure");
            } else if((int) environmentTemplate[6] > (int) cellTemplate[24] && (boolean) cellTemplate[25]) {//Toxicity level
                kill();
                //System.out.println("Killed due to Toxicity. T of " + environmentTemplate[6] + " and resistance of " + cellTemplate[24]);
            }
            if((int) foodAbundance <= 0) {
                //System.out.println("Killed due to food");
                kill();
            }

            else if (sum < lowerSurvivalBound || sum > upperSurvivalBound /*  Placeholder- If sum is between survival upper and lower bounds */) {
                //System.out.println("Killing myself with sum of " + sum);
                kill();
            }

            if (sum < lowerSurvivalBound || sum > upperSurvivalBound /*  Placeholder- If sum is between survival upper and lower bounds */) {
                //System.out.println("Killing myself with sum of " + sum);
                kill();
            } else if (sum > birthLowerBound && sum < birthUpperBound /*  Placeholder- If sum is between the birth upper and lower bounds */) {
                //System.out.println("Creating a new cell");
                if(generationsSurvived%generationsBetweenSplit == 0 && !killed) {
                    int i = 0;
                    int rnd = random.nextInt(surroundingCells.size() - 1);
                    while (surroundingCells.get(rnd).isAlive != 0 && !surroundingCells.get(rnd).isBlock && !killed) {
                        rnd = random.nextInt(surroundingCells.size() - 1);
                        if (i++ > 7) {
                            break;
                        }
                        surroundingCells.get(rnd).split(this);
                    }
                }
            }
        } else {
            kill();
        }
        //The cell does nothing
        if (isAlive != 0) {
            generationsSurvived++;
        }
    }

    public void split(Cell cell) {
        isAlive = 1;
        generationsSurvived = 0;
        applyTemplate(cell.getCellTemplate());
        macroEvolve();
    }

    public void kill() {
        killed = true;
        isAlive = 0;
        generationsSurvived = 0;
    }

    public void macroEvolve() {
        Random r = new Random();
        if(r.nextFloat() > 0.99) {
            int valueToChange = 8 + r.nextInt(17);
            if((valueToChange & 1) != 0) {
                valueToChange += 1;
            }
            switch (valueToChange) {
                case 8: //Low temp
                    cellTemplate[valueToChange] = (int) ((int) cellTemplate[valueToChange] + Math.round(r.nextGaussian()));
                    if((int) cellTemplate[valueToChange] > -50) {
                        cellTemplate[valueToChange] = -50;
                    }
                    if((int) cellTemplate[valueToChange] <= 0) {
                        cellTemplate[valueToChange] = 0;
                    }
                    break;
                case 10: //High temp
                    cellTemplate[10] = (int) ((int) cellTemplate[valueToChange] + Math.round(r.nextGaussian()));
                    if((int) cellTemplate[10] > 0 ) {
                        cellTemplate[valueToChange] = 0;
                    }
                    if((int) cellTemplate[10] <= 50) {
                        cellTemplate[valueToChange] = 50;
                    }
                    break;
                case 12: //Humidity
                    cellTemplate[valueToChange] = (int) ((int) cellTemplate[valueToChange] + Math.round(r.nextGaussian()));
                    if((int) cellTemplate[valueToChange] < 0 ) {
                        cellTemplate[valueToChange] = 0;
                    }
                    if((int) cellTemplate[valueToChange] > 100) {
                        cellTemplate[valueToChange] = 100;
                    }
                    break;
                case 14://High PH
                    cellTemplate[valueToChange] = (int) ((int) cellTemplate[valueToChange] + Math.round(r.nextGaussian()));
                    if((int) cellTemplate[valueToChange] <= 7 ) {
                        cellTemplate[valueToChange] = 7;
                    }
                    if((int) cellTemplate[valueToChange] > 14)  {
                        cellTemplate[valueToChange] = 14;
                    }
                    break;
                case 16: //Low PH
                    cellTemplate[valueToChange] = (int) ((int) cellTemplate[valueToChange] + Math.round(r.nextGaussian()));
                    if((int) cellTemplate[valueToChange] > 7) {

                        cellTemplate[valueToChange]  = 7;
                    }
                    if((int) cellTemplate[valueToChange] < 0) {
                        cellTemplate[valueToChange] = 0;
                    }
                    break;
                case 18: //Pressure
                    cellTemplate[valueToChange] = (int) ((int) cellTemplate[valueToChange] + Math.round(r.nextGaussian()));
                    if((int) cellTemplate[valueToChange] > 1000) {
                        cellTemplate[valueToChange] = 1000;
                    }
                    if((int) cellTemplate[valueToChange] < 0) {
                        cellTemplate[valueToChange] = 0;
                    }
                    break;
                case 20://Radiation
                    cellTemplate[valueToChange] = (int) ((int) cellTemplate[valueToChange] + Math.round(r.nextGaussian()));
                    if((int) cellTemplate[valueToChange] > 30) {
                        cellTemplate[valueToChange] = 30;
                    }
                    if((int) cellTemplate[valueToChange] < 0) {
                        cellTemplate[valueToChange] = 0;
                    }
                    break;
                case 24: //Toxicity level
                    cellTemplate[valueToChange] = (int) ((int) cellTemplate[valueToChange] + Math.round(r.nextGaussian()));
                    if((int) cellTemplate[valueToChange] > 10 ) {
                        cellTemplate[valueToChange] = 10;
                    }
                    if((int) cellTemplate[valueToChange] < 0) {
                        cellTemplate[valueToChange] = 0;
                    }
                    break;
            }
        }
    }

    public int generateColourCode() {
        if(isBlock) {
            return 689127;
        } else if(isAlive > 0) {
            return 16711680;
        } else {
            return 16777215;
        }
    }

    public void applyTemplate(Object[] newValues) {
        if(!isBlock) {
            cellTemplate = newValues.clone();
            isAlive = (int) newValues[3];
            birthLowerBound = (int) newValues[4];
            birthUpperBound = (int) newValues[5];
            lowerSurvivalBound = (int) newValues[6];
            upperSurvivalBound = (int) newValues[7];

        }
    }

    public void applyEnvironmentTemplate(Object[] o) {
        environmentTemplate = o.clone();
        foodAbundance += (int) o[7];
    }

    //Most likely an un-needed method
	public void setColourCode(int colourCode){
		this.colourCode = colourCode;
	}

    //Getters
    public float getIsAlive() {
        return isAlive;
    }

    public Object[] getCellTemplate() {
        return cellTemplate;
    }

    public void increaseFood(int increase) {
        foodAbundance += increase;
        System.out.println("Food abundance increased to " + foodAbundance);
    }
}
