import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by Vladimir Petrov on 12.11.2016.
 */
public class Investment_InventoryAndSurprise {

    public static String FILE_PATH = "D:/Data/";




    public static void main(String[] args){

        String fileName = "EURUSD_UTC_Ticks_Bid_2015-01-01_2016-01-01.csv";


        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(FILE_PATH + fileName));

            String thisLine = bufferedReader.readLine(); // read head

            ATick aTick;
            Agent agent = new Agent(1, 0.1f, 0.1f, -15, 15, -30, 30, 30);


            while ((thisLine = bufferedReader.readLine()) != null){
                String[] content = thisLine.split(",");

                aTick = new ATick(Float.parseFloat(content[2]), Float.parseFloat(content[1]));

                agent.run(aTick);


            }



        } catch (Exception ex){
            ex.printStackTrace();
        }





    }









}
