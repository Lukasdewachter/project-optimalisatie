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
        Object obj = new JSONParser().parse(new FileReader("./IO/TOY-20-10.json"));
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
        SteepestDescend sd = new SteepestDescend(jobs,test, solution.getSetupList(),setups,un,weightDuration,horizon,solution.getBestCost());
        JSONObject finalSolution = sd.getJSONSolution();
        double evaluation=sd.getBestCost();
        finalSolution.put("name",name);
        finalSolution.put("value",evaluation);
        FileWriter fw = new FileWriter("./IO/solution-"+name+".json");
        fw.write(finalSolution.toString(4));
        fw.flush();
    }
}