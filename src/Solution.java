import java.util.*;

public class Solution {
    int numberOfJobs;
    LinkedList<Job> solution = new LinkedList<>();
    LinkedList<Job> notScheduledJobs = new LinkedList<>();
    ArrayList<Job> jobs= new ArrayList<>();
    double cost;
    double weightDuration;
    int[][] setups;
    Unavailability unavailability;
    List<String>setupList;

    // Na heuristiek
    LinkedList<Job> bestSolution = new LinkedList<>();
    LinkedList<Job> bestNotScheduledJobs = new LinkedList<>();
    double bestCost;

    public Solution(ArrayList<Job> jobs, double weightDuration, int[][] setups, Unavailability unavailability){
        this.jobs=jobs;
        numberOfJobs = jobs.size();
        this.weightDuration=weightDuration;
        this.setups = setups;
        this.unavailability=unavailability;
        setupList = new ArrayList<String>();
    }

    public double getWeightDuration() {
        return weightDuration;
    }

    // Function to schedule the jobs take 2 arguments
    // arraylist and no of jobs to schedule
    public boolean isScheduled(Job job){
        if (solution.contains(job)){
            return true;
        } else return false;
    }
    //Weighted schedule duration + earliness penalty + penalty of rejected jobs
    public double evaluate(){
        //Weighted schedule duration
        cost += (weightDuration * (solution.getLast().getStop()-solution.getFirst().getStart()));

        //Earlines penalty
        for(int i=0; i<solution.size(); i++){
            cost += solution.get(i).getEarlinessPenalty();
        }

        //Rejection penalty
        for(int i=0; i< notScheduledJobs.size();i++){
            cost+=notScheduledJobs.get(i).getRejectionPenalty();
        }
        return cost;
    }
    public void firstSolution(){
        Collections.sort(jobs, Comparator.comparing(Job::getDueDate));
        Job lastJob;
        int currIndex = 0;
        Job firstJob = jobs.get(currIndex);
        long timeIndex=firstJob.getReleaseDate();
        timeIndex = addJob(firstJob, timeIndex);
        lastJob = firstJob;
        for(Job j : jobs){
            //check if job can finish in time
            if(j.getDueDate() >= timeIndex + j.getDuration() + getSetupTime(j, lastJob) && !solution.contains(j)){
                //check if job can start & check unavailability
                if(j.getReleaseDate() <= timeIndex && unavailability.checkAvailable(timeIndex,timeIndex+j.getDuration()+getSetupTime(j, lastJob))){
                    //add job and setup
                    addSetup(timeIndex,lastJob,j);
                    timeIndex += getSetupTime(j, lastJob);
                    timeIndex = addJob(j, timeIndex);
                    lastJob = j;
                }
                else{
                    timeIndex = j.getReleaseDate();
                    //if job cannot start at this time we skip to its release date
                    if(unavailability.checkAvailable(timeIndex,timeIndex+j.getDuration()+getSetupTime(j, lastJob))){
                        addSetup(timeIndex,lastJob,j);
                        timeIndex += getSetupTime(j, lastJob);
                        timeIndex = addJob(j, timeIndex);
                        lastJob = j;
                        //if job has unavailability we need to skip that first
                    }else{
                        timeIndex = unavailability.skipUnavailable(timeIndex);
                        addSetup(timeIndex,lastJob,j);
                        timeIndex += getSetupTime(j, lastJob);
                        timeIndex = addJob(j, timeIndex);
                        lastJob = j;
                    }
                    
                    
                }
            }else{
                //job can't complete in time anymore
                if(!solution.contains(j))notScheduledJobs.add(j);
            }
        }
    }
    public void bestSolution(){

    }
    public long addJob(Job job, long timeIndex){
        //add job to solution
        job.setStart(timeIndex);
        timeIndex += job.getDuration();
        job.setStop(timeIndex);
        solution.add(job);
        return timeIndex;
    }
    public void addSetup(long timeIndex, Job lastJob, Job currJob){
        //save setup change
        setupList.add("    from: "+lastJob.getId()+"\n"+"   to: "+currJob.getId()+"\n"+"    start: "+timeIndex);
    }
    public void printSetups(){
        for(String s : setupList){
            System.out.println(s);
        }
    }
    public long getSetupTime(Job curJob, Job prevJob){
        return setups[curJob.getId()][prevJob.getId()];
    }
    public void print(){
        for(Job j : solution){
            System.out.println("    id: "+j.getId());
            System.out.println("    start: "+j.getStart());
        }
    }
}