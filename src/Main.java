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
        Object obj = new JSONParser().parse(new FileReader("./IO/A-100-30.json"));
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
        Solution solution = new Solution(jobs, weightDuration, setups, un);
        LinkedList<Job>firstSolution = (LinkedList<Job>) solution.firstSolution();
        LinkedList<Job>jobList = solution.getJobList(firstSolution);
        LocalSearch ls = new LocalSearch(jobList, setups, un, weightDuration);
        int bestCost=Integer.MAX_VALUE;
        for(int i=0; i<50;i++){
            double cost = ls.deepestDescend(jobList);
            if(cost < bestCost){
                LinkedList<Job>algorithm = ls.getBestSolution();
                double evaluation=ls.evaluate(algorithm);
                JSONObject finalSolution = new JSONObject();
                JSONArray jsonSetups = new JSONArray();
                List<SetupChange>setupChanges = ls.getBestSetupList();
                for(SetupChange setupChange : setupChanges){
                    JSONObject jsonSetup = new JSONObject();
                    jsonSetup.put("from",setupChange.getJ1().getId());
                    jsonSetup.put("to",setupChange.getJ2().getId());
                    jsonSetup.put("start",setupChange.getStart());
                    jsonSetups.put(jsonSetup);
                }
                finalSolution.put("setups",jsonSetups);
                JSONArray array = new JSONArray();
                for(Job job : algorithm){
                    JSONObject jsonJob = new JSONObject();
                    jsonJob.put("id",job.getId());
                    jsonJob.put("start",job.getStart());
                    array.put(jsonJob);
                }
                finalSolution.put("jobs",array);
                finalSolution.put("name",name);
                finalSolution.put("value",evaluation);


                FileWriter fw = new FileWriter("./IO/solution-"+name+".json");
                fw.write(finalSolution.toString(4));
                fw.flush();
            }
        }


        //System.out.println("Jobs: ");
        //solution.print();
    }
}