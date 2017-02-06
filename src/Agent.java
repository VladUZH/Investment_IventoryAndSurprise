import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Vladimir Petrov on 12.11.2016.
 */
public class Agent {


    float inventory;
    float initialVolume;
    float initialDeltaUp;
    float initialDeltaDown;
    float volume;
    float deltaUp;
    float deltaDown;
    float PnL;
    Runner runner;
    boolean initialized;
    int lastRunnerResult;
    float firstLowBound = -15;
    float firstHighBound = 15;
    float secondLowBound = -30;
    float secondHighBound = 30;
    float surprise;
    long nEvents;
    int previousEvent; //
    float probSameDirection;
    float probOpositeDirect;

    ArrayList<Position> longArrayList;
    ArrayList<Position> shortArrayList;

    LinkedList<Float> logSurpList;

    int window;

    public Agent(float initialVolume, float initialDeltaUp, float initialDeltaDown, float firstLowBound, float firstHighBound, float secondLowBound, float secondHighBound, int window){

        this.initialVolume = initialVolume;
        this.initialDeltaUp = initialDeltaUp;
        this.initialDeltaDown = initialDeltaDown;

        deltaUp = initialDeltaUp;
        deltaDown = initialDeltaDown;
        volume = initialVolume;
        PnL = 0;

        this.firstLowBound = firstLowBound;
        this.firstHighBound = firstHighBound;
        this.secondLowBound = secondLowBound;
        this.secondHighBound = secondHighBound;

        nEvents = 0;
        previousEvent = 1;

        longArrayList = new ArrayList<>();
        shortArrayList = new ArrayList<>();
        logSurpList = new LinkedList<>();

        probSameDirection = -(float) Math.log10(Math.exp(-1.0f));
        probOpositeDirect = -(float) Math.log10(1 - Math.exp(-1));

        this.window = window;

    }


    public int run(ATick aTick){

        if (!initialized){
            runner = new Runner(initialDeltaUp, initialDeltaDown, aTick, 1);
            initialized = true;
            return 0;

        } else {

            lastRunnerResult = runner.run(aTick);

            correctVolume(lastRunnerResult);

            if (lastRunnerResult > 0){ // for
                if (longArrayList.size() != 0){
                    closeLongPosition(aTick.bid);
                }
                shortArrayList.add(new Position(aTick.bid, volume));
                nEvents += 1;
                previousEvent = lastRunnerResult;
            } else if (lastRunnerResult < 0){
                if (shortArrayList.size() != 0){
                    closeShortPosition(aTick.ask);
                }
                longArrayList.add(new Position(aTick.ask, volume));
                nEvents += 1;
                previousEvent = lastRunnerResult;
            }

            correctThresholds(inventory);




            return 1;
        }

    }



    public void correctThresholds(float inventory){
        if (inventory < -15){
            runner.changeDeltaUp(1 * initialDeltaUp);
            runner.changeDeltaDown(0.5f * initialDeltaDown);
            if (inventory < -30){
                runner.changeDeltaUp(1 * initialDeltaUp);
                runner.changeDeltaDown(0.25f * initialDeltaDown);
            }
        } else if (inventory > 15){
            runner.changeDeltaUp(0.5f * initialDeltaUp);
            runner.changeDeltaDown(1 * initialDeltaDown);
            if (inventory > 30){
                runner.changeDeltaUp(0.25f * initialDeltaUp);
                runner.changeDeltaDown(1 * initialDeltaDown);
            }
        } else {
            runner.changeDeltaUp(1 * initialDeltaUp);
            runner.changeDeltaDown(1 * initialDeltaDown);
        }

    }


    public float computeSurprise(int currentEvent){
        if ((currentEvent * previousEvent) > 0){
            surprise += probSameDirection;
            logSurpList.add(probSameDirection);

        } else if ((currentEvent * previousEvent) < 0){
            surprise += probOpositeDirect;
            logSurpList.add(probOpositeDirect);
        }

        if (logSurpList.size() >= window){
            surprise -= logSurpList.remove(0);
        }

        return surprise;
    }


    public float probabilityIndicator(int currentEvent){
        float H1 = 0.4604f;
        float H2 = 0.70818f;
        return 1 - CumNorm((computeSurprise(currentEvent) - H1) / ((float) Math.sqrt(nEvents * H2)));
    }


    // another implementation of the CNDF for a standard normal: N(0,1)
    float CumNorm(float x){
        // protect against overflow
        if (x > 6.0)
            return 1.0f;
        if (x < -6.0)
            return 0.0f;

        float b1 = 0.31938153f;
        float b2 = -0.356563782f;
        float b3 = 1.781477937f;
        float b4 = -1.821255978f;
        float b5 = 1.330274429f;
        float p = 0.2316419f;
        float c2 = 0.3989423f;

        float a = Math.abs(x);
        float t = 1.0f / (1.0f + a * p);
        float b = c2 * (float) Math.exp((-x) * (x / 2.0f));
        float n = ((((b5*t+b4)*t+b3)*t+b2)*t+b1)*t;
        n = 1.0f - b * n;

        if ( x < 0.0 )
            n = 1.0f - n;

        return n;
    }


    public void correctVolume(int currentEvent){

        float probIndicator = probabilityIndicator(currentEvent);

        System.out.println("Prob ind: " + probIndicator);
    }


    public void closeLongPosition(float bidPrice){
        Position position = longArrayList.remove(longArrayList.size() - 1);
        PnL += bidPrice - position.price;
    }

    public void closeShortPosition(float askPrice){
        Position position = shortArrayList.remove(shortArrayList.size() - 1);
        PnL += position.price - askPrice;
    }





}
