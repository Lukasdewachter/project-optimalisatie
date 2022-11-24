import java.sql.SQLOutput;
import java.util.*;

public class Solution{
    int numberOfJobs;
    LinkedList<Job> solution = new LinkedList<>();
    LinkedList<Job> notScheduledJobs = new LinkedList<>();
    ArrayList<Job> jobs= new ArrayList<>();
    float bestCost;
    float weightDuration;
    int[][] setups;
    Unavailability unavailability;
    List<SetupChange>setupList;
    public Solution(ArrayList<Job> jobs, float weightDuration, int[][] setups, Unavailability unavailability){
        super();
        this.jobs=jobs;
        numberOfJobs = jobs.size();
        this.weightDuration=weightDuration;
        this.setups = setups;
        this.unavailability=unavailability;
        setupList = new ArrayList<SetupChange>();
    }

    public float getWeightDuration() {
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
    public float evaluate(){
        float sum =0;
        //Weighted schedule duration
        sum += (weightDuration * (solution.getLast().getStop()-solution.getFirst().getStart()));
        float weightedSum = sum;
        System.out.println("Weighted: "+weightedSum);
        //Earlines penalty
        for(Job j : solution){
            sum += j.getEarlinessPenalty();
        }
        float earlinesSum = sum-weightedSum;
        System.out.println("Earliness: "+ earlinesSum);
        //Rejection penalty
        for(int i=0; i< notScheduledJobs.size();i++){
            sum+=notScheduledJobs.get(i).getRejectionPenalty();
        }
        float rejectionSum = sum-earlinesSum-weightedSum;
        System.out.println("Rejection: " + rejectionSum);
        System.out.println("Total sum of evaluation function is " + sum);
        return sum;
    }
    public List<Job> firstSolution(){
        Collections.sort(jobs, Comparator.comparing(Job::getDueDate));
        Job lastJob;
        Job firstJob = jobs.get(0);
        int timeIndex=firstJob.getReleaseDate();
        addJob(firstJob, timeIndex);
        timeIndex += firstJob.getDuration();
        lastJob = firstJob;
        for(Job j : jobs){
            //check if job can finish in time
            if(j.getDueDate() >= timeIndex + j.getDuration() + getSetupTime(j, lastJob) && !solution.contains(j)){
                //check if job can start & check unavailability
                if(j.getReleaseDate() <= timeIndex && unavailability.checkAvailable(timeIndex,timeIndex+j.getDuration()+getSetupTime(j, lastJob))){
                    //add job and setup
                    addSetup(timeIndex,lastJob,j);
                    timeIndex += getSetupTime(j, lastJob);
                    addJob(j, timeIndex);
                    timeIndex += j.getDuration();
                    lastJob = j;
                }
                else{
                    if(j.getReleaseDate()>timeIndex){
                        timeIndex = j.getReleaseDate();
                    }
                    //if job cannot start at this time we skip to its release date
                    if(unavailability.checkAvailable(timeIndex,timeIndex+j.getDuration()+getSetupTime(j, lastJob))){
                        addSetup(timeIndex,lastJob,j);
                        timeIndex += getSetupTime(j, lastJob);
                        addJob(j, timeIndex);
                        timeIndex += j.getDuration();
                        lastJob = j;
                        //if job has unavailability we need to skip that first
                    }else{
                        timeIndex = unavailability.skipUnavailable(timeIndex);
                        if(j.getDueDate() >= timeIndex + j.getDuration() + getSetupTime(j, lastJob) && !solution.contains(j)&&unavailability.checkAvailable(timeIndex,timeIndex+j.getDuration()+getSetupTime(j, lastJob))){
                            addSetup(timeIndex,lastJob,j);
                            timeIndex += getSetupTime(j, lastJob);
                            addJob(j, timeIndex);
                            timeIndex += j.getDuration();
                            lastJob = j;
                        }else{
                            if(!solution.contains(j))notScheduledJobs.add(j);
                        }
                    }
                    
                    
                }
            }else{
                //job can't complete in time anymore
                if(!solution.contains(j))notScheduledJobs.add(j);
            }
        }
        return this.solution;
    }
    public void addJob(Job job, int timeIndex){
        //add job to solution
        job.setStart(timeIndex);
        timeIndex += job.getDuration();
        job.setStop(timeIndex);
        solution.add(job);
    }
    public void addSetup(int timeIndex, Job lastJob, Job currJob){
        //save setup change
        SetupChange setupChange = new SetupChange(lastJob,currJob,timeIndex);
        setupList.add(setupChange);
    }

    public LinkedList<Job> getSolution() {
        return solution;
    }


    public List<SetupChange> getSetupList() {
        return setupList;
    }
    public int getSetupTime(Job curJob, Job prevJob){
        return setups[prevJob.getId()][curJob.getId()];
    }
    // methodes voor SA
    public Solution(Solution copy) {
        this.solution = new LinkedList<Job>(copy.solution); // deep copy
        this.jobs=copy.jobs;
        this.numberOfJobs = copy.jobs.size();
        this.weightDuration= copy.weightDuration;
        this.setups = copy.setups;
        this.unavailability=copy.unavailability;
        this.setupList = new LinkedList<SetupChange>(copy.getSetupList()); //deep copy
    }
}