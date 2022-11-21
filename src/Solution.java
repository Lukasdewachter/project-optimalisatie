import java.util.*;

public class Solution {
    int numberOfJobs;
    LinkedList<Job> solution = new LinkedList<>();
    LinkedList<Job> notScheduledJobs = new LinkedList<>();
    ArrayList<Job> jobs= new ArrayList<>();
    double bestCost;
    double weightDuration;
    int[][] setups;
    Unavailability unavailability;
    List<SetupChange>setupList;
    public Solution(ArrayList<Job> jobs, double weightDuration, int[][] setups, Unavailability unavailability){
        this.jobs=jobs;
        numberOfJobs = jobs.size();
        this.weightDuration=weightDuration;
        this.setups = setups;
        this.unavailability=unavailability;
        setupList = new ArrayList<SetupChange>();
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
        sum += (weightDuration * (solution.getLast().getStop()-solution.getFirst().getStart()));
        //Earlines penalty
        for(Job j : solution){
            sum += j.getEarlinessPenalty();
        }
        //Rejection penalty
        for(int i=0; i< notScheduledJobs.size();i++){
            sum+=notScheduledJobs.get(i).getRejectionPenalty();
        }
        return sum;
    }
    public List<Job> firstSolution(){
        Collections.sort(jobs, Comparator.comparing(Job::getReleaseDate));
        Job lastJob;
        Job firstJob = jobs.get(0);
        int timeIndex=firstJob.getReleaseDate();
        timeIndex = addJob(firstJob, timeIndex);
        lastJob = firstJob;
        for(Job j : jobs){
            if(j.getId()==140){
                System.out.println("h");
            }
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
                    if(j.getReleaseDate()>timeIndex){
                        timeIndex = j.getReleaseDate();
                    }
                    //if job cannot start at this time we skip to its release date
                    if(unavailability.checkAvailable(timeIndex,timeIndex+j.getDuration()+getSetupTime(j, lastJob))){
                        addSetup(timeIndex,lastJob,j);
                        timeIndex += getSetupTime(j, lastJob);
                        timeIndex = addJob(j, timeIndex);
                        lastJob = j;
                        //if job has unavailability we need to skip that first
                    }else{
                        timeIndex = unavailability.skipUnavailable(timeIndex);
                        if(j.getDueDate() >= timeIndex + j.getDuration() + getSetupTime(j, lastJob) && !solution.contains(j)&&unavailability.checkAvailable(timeIndex,timeIndex+j.getDuration()+getSetupTime(j, lastJob))){
                            addSetup(timeIndex,lastJob,j);
                            timeIndex += getSetupTime(j, lastJob);
                            timeIndex = addJob(j, timeIndex);
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
    public int addJob(Job job, int timeIndex){
        //add job to solution
        job.setStart(timeIndex);
        timeIndex += job.getDuration();
        job.setStop(timeIndex);
        solution.add(job);
        return timeIndex;
    }
    public void addSetup(int timeIndex, Job lastJob, Job currJob){
        //save setup change
        SetupChange setupChange = new SetupChange(lastJob,currJob,(int)timeIndex);
        setupList.add(setupChange);
    }

    public List<SetupChange> getSetupList() {
        return setupList;
    }
    public int getSetupTime(Job curJob, Job prevJob){
        return setups[prevJob.getId()][curJob.getId()];
    }
}