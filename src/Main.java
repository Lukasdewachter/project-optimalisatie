import org.json.*;
import java.io.*;
import java.util.*;
import org.json.JSONTokener;
import org.json.simple.parser.JSONParser;

/*
    - eerst een feasable oplossing vormen dan lokaal zoeken om betere oplossingen te zoeken
    - welke datastructuur voor de oplossing, zien dat oplossing 1x gesaved wordt LinkedList
    - geen unavailability in de linked list oplossing steken
    - setup tijd kan wel in de linked list om de heuristiek makkelijker te maken
*/
public class Main {
    public static void main(String[] args) throws Exception {
        //Als je error hebt moet je gwn alles met args[] in comment zetten
        long t1 = System.currentTimeMillis();
        String inputFile = args[0];
        String solutionFile = args[1];
        int seed = Integer.parseInt(args[2]);
        int timeLimit = Integer.parseInt(args[3]);
        int threads = Integer.parseInt(args[4]);
        Object obj = new JSONParser().parse(new FileReader("./IO/"+inputFile));
        JSONTokener tokener = new JSONTokener(String.valueOf(obj));
        JSONObject object = new JSONObject(tokener);
        String name = object.getString("name");
        float weightDuration = object.getFloat("weight_duration");
        int horizon = object.getInt("horizon");
        JSONArray jobsArray = object.getJSONArray("jobs");
        LinkedList<Job> jobs = new LinkedList<>();
        for (int i = 0; i<jobsArray.length();i++) {
            JSONObject temp = jobsArray.getJSONObject(i);
            int id = temp.getInt("id");
            int duration =  temp.getInt("duration");
            int release_date = temp.getInt("release_date");
            int due_date = temp.getInt("due_date");
            float earliness_penalty = temp.getFloat("earliness_penalty");
            float rejection_penalty = temp.getFloat("rejection_penalty");
            Job job = new Job(id, duration, release_date, due_date, earliness_penalty, rejection_penalty);
            jobs.add(job);
        }
        JSONArray unavailability = object.getJSONArray("unavailability");
        Unavailability un = new Unavailability(horizon);
        for(int i=0; i<unavailability.length();i++){
            JSONObject temp = unavailability.getJSONObject(i);
            int start = temp.getInt("start");
            int end = temp.getInt("end");
            un.addUnavailable(start,end);
        }
        Setup setup = new Setup(jobs);
        int[][] setups = setup.setup;
        JSONArray s = object.getJSONArray("setups");
        for(int i=0; i< jobsArray.length();i++){
            JSONArray s0 = s.getJSONArray(i);
            for (int j=0; j< s0.length();j++){
                setups[i][j] = (int) s.getJSONArray(i).get(j);
            }
        }
        FirstSolution solution = new FirstSolution(jobs, weightDuration, setups, un,horizon);
        LinkedList<Job>test = solution.firstSolution();
        long startTime =  System.currentTimeMillis();
        SteepestDescend sd = new SteepestDescend(jobs,test, solution.getSetupList(),setups,un,weightDuration,horizon,solution.getBestCost(),timeLimit);;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Boolean running = true;

                while(running){
                    if(System.currentTimeMillis() - startTime> timeLimit*1000){
                        running = false;
                    }
                    sd.mixNeighbours();
                }
            }
        });
        thread.run();
        JSONObject finalSolution = sd.getJSONSolution();
        double evaluation=sd.getBestCost();
        finalSolution.put("name",name);
        finalSolution.put("value",evaluation);
        FileWriter fw = new FileWriter("./"+solutionFile);
        fw.write(finalSolution.toString(4));
        fw.flush();
        long t2 = System.currentTimeMillis();
        System.out.println("Executed in: "+(t2-t1)/1000+"s.  Best cost: "+evaluation);
    }
}