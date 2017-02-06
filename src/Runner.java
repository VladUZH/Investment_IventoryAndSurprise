/**
 * Created by Vladimir Petrov on 05.09.2016.
 */
public class Runner {
    public float prevDC;
    public float extreme;
    public float deltaUp;
    public float deltaDown;
    public float osL;
    public int type;
    public boolean initalized;
    public float reference;


    public Runner(float deltaUp, float deltaDown, ATick aTick, int type){
        if (type == -1){
            extreme = aTick.ask;
            prevDC = aTick.ask;
        }
        else if (type == 1){
            extreme = aTick.bid;
            prevDC = aTick.bid;
        }

        this.type = type; this.deltaUp = deltaUp; this.deltaDown = deltaDown; osL = 0; initalized = true;
    }


    public int run(ATick aTick) {

        if (type == -1) {
            if (aTick.ask < extreme) {
                extreme = aTick.ask;
                if ((extreme - reference) / reference * 100 <= -deltaDown) {
                    reference = extreme;
                    return -2;
                }
                return 0;

            } else if ((aTick.bid - extreme) / extreme * 100.0 >= deltaUp) {
                prevDC = aTick.bid;
                extreme = aTick.bid;
                reference = aTick.bid;
                type = 1;
                return 1;
            }

        } else if (type == 1) {
            if (aTick.bid > extreme) {
                extreme = aTick.bid;
                if ((extreme - reference) / reference * 100 >= deltaUp) {
                    reference = extreme;
                    return 2;
                }
                return 0;

                } else if ((extreme - aTick.ask) / extreme * 100.0 >= deltaDown) {
                    prevDC = aTick.ask;
                    extreme = aTick.ask;
                    reference = aTick.ask;
                    type = -1;
                    return -1;
                }
            }


        return 0;
    }


    public void changeDeltaUp(float newDeltaUp){
        deltaUp = newDeltaUp;
    }

    public void changeDeltaDown(float newDeltaDown){
        deltaDown = newDeltaDown;
    }



}
