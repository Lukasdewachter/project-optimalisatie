import java.util.*;

public class Solution {
    int numberOfJobs;
    LinkedList<Job> solution = new LinkedList<>();
    LinkedList<Job> notScheduledJobs = new LinkedList<>();
    ArrayList<Job> jobs= new ArrayList<>();
    double bestCost;
    double weightDuration;
    int[][] setups;
    public Solution(ArrayList<Job> jobs, double weightDuration, int[][] setups){
        this.jobs=jobs;
        numberOfJobs = jobs.size();
        this.weightDuration=weightDuration;
        this.setups = setups;
    }

    public double getWeightDuration() {
        return weightDuration;
    }

    // Function to schedule the jobs take 2 arguments
    // arraylist and no of jobs to schedule
    public void printJobScheduling(ArrayList<Job> jobs) {

    }
    public boolean isScheduled(Job job){
        if (solution.contains(job)){
            return true;
        } else return false;
    }
    //Weighted schedule duration + earliness penalty + penalty of rejected jobs
    public double evaluate(){
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
        return sum;
    }
    public void firstSolution(){
        Job lastJob;
        int currIndex = 0;
        Job firstJob = jobs.get(currIndex);
        long timeIndex=firstJob.getReleaseDate();
        timeIndex = addJob(firstJob, timeIndex);
        lastJob = firstJob;
        for(Job j : jobs){
            if(j.getId()==14){
                System.out.println("h");
            }
            if(j.getDueDate() >= timeIndex + j.getDuration() + getSetupTime(j, lastJob) && !solution.contains(j)){
                if(j.getReleaseDate() <= timeIndex){
                    timeIndex += getSetupTime(j, lastJob);
                    timeIndex = addJob(j, timeIndex);
                    lastJob = j;
                }
                else{
                    timeIndex = j.getReleaseDate();
                    timeIndex += getSetupTime(j, lastJob);
                    timeIndex = addJob(j, timeIndex);
                    lastJob = j;
                }
            }else{
                if(!solution.contains(j))notScheduledJobs.add(j);
            }
        }
        System.out.println(solution.size()+" solution: ");
        for(Job j : solution){j.print();}
        System.out.println(notScheduledJobs.size()+ "not included: ");
        for(Job j : notScheduledJobs){j.print();}
    }
    public long addJob(Job job, long timeIndex){
        job.setStart(timeIndex);
        timeIndex += job.getDuration();
        job.setStop(timeIndex);
        solution.add(job);
        return timeIndex;
    }
    public long getSetupTime(Job curJob, Job prevJob){
        return setups[curJob.getId()][prevJob.getId()];
    }
}