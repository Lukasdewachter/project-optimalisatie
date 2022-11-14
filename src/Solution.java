import java.util.*;

public class Solution {
    int numberOfJobs;
    LinkedList<Job> solution = new LinkedList<>();
    LinkedList<Job> notScheduledJobs = new LinkedList<>();
    ArrayList<Job> jobs= new ArrayList<>();
    double bestCost;
    double weightDuration;

    public Solution(ArrayList<Job> jobs, double weightDuration){
        this.jobs=jobs;
        numberOfJobs = jobs.size();
        this.weightDuration=weightDuration;
    }

    public void setWeightDuration(double weightDuration) {
        this.weightDuration = weightDuration;
    }

    public double getWeightDuration() {
        return weightDuration;
    }

    // Function to schedule the jobs take 2 arguments
    // arraylist and no of jobs to schedule
    void printJobScheduling(ArrayList<Job> jobs) {

    }
    boolean isScheduled(Job job){
        if (solution.contains(job)){
            return true;
        } else return false;
    }
    //Weighted schedule duration + earliness penalty + penalty of rejected jobs
    double evaluate(){
        double sum =0;

        //Weighted schedule duration
        sum += weightDuration * solution.getLast().getStop();

        //Earlines penalty
        for(int i=0; i<solution.size(); i++){
            sum += solution.get(i).getEarlinessPenalty() * (solution.get(i).getDueDate()- solution.get(i).getStop()) ;
        }

        //Rejection penalty
        for(int i=0; i< notScheduledJobs.size();i++){
            sum+=notScheduledJobs.get(i).getRejectionPenalty();
        }
        System.out.println("werkt");
        return sum;
    }

}