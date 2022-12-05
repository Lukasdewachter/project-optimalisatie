import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class FirstSolution {
    int numberOfJobs, timeIndex,horizon;
    //LinkedList<Job> solutionJobs = new LinkedList<>();
    LinkedList<Job> notScheduledJobs = new LinkedList<>();
    LinkedList<Job> jobs,scheduled;
    double bestCost;
    double weightDuration;
    int[][] setups;
    Unavailability unavailability;
    LinkedList<SetupChange>setupList;
    JSONObject JSONSolution;
    public FirstSolution(LinkedList<Job> jobs, float weightDuration, int[][] setups, Unavailability unavailability, int horizon){
        super();
        this.jobs=jobs;
        numberOfJobs = jobs.size();
        this.weightDuration=weightDuration;
        this.setups = setups;
        this.unavailability=unavailability;
        setupList = new LinkedList<>();
        this.scheduled = new LinkedList<>();
        this.horizon = horizon;
        this.JSONSolution = null;
    }
    //Weighted schedule duration + earliness penalty + penalty of rejected jobs

    public double evaluate(LinkedList<Job> solutionJobs){
        double sum =0;
        //Weighted schedule duration
        sum += (weightDuration * (solutionJobs.getLast().getStop()- solutionJobs.getFirst().getStart()));
        double weightedSum = sum;
        //Earlines penalty
        for(Job j : solutionJobs){
            sum += j.getEarlinessPenalty();
        }
        double earlinesSum = sum-weightedSum;
        //Rejection penalty
        for(int i=0; i< notScheduledJobs.size();i++){
            sum+=notScheduledJobs.get(i).getRejectionPenalty();
        }
        double rejectionSum = sum-earlinesSum-weightedSum;
        return sum;
    }

    public LinkedList<Job> firstSolution(){

        Collections.sort(jobs,Comparator.comparing(Job::getDueDate));
        Job firstJob = jobs.get(numberOfJobs-1);
        setTimeIndex(firstJob.getDueDate());
        unavailability.getAvailableTime2(timeIndex);
        if(firstJob.getDueDate()>horizon){
            setTimeIndex(horizon);
        }
        else if(unavailability.getAvailableTime2(timeIndex) < 0){
            int time = unavailability.getAvailable(timeIndex);
            setTimeIndex(time);
        }
        Job prevJob = firstJob;
        addJob(firstJob,null);
        for(int index = jobs.size()-2 ; index> -1;index--){
            Job job = jobs.get(index);
            int id = job.getId();
            int releaseDate = job.getReleaseDate();
            int dueDate = job.getDueDate();
            int duration = job.getDuration();
            int setupTime = getSetupTime(job,prevJob);
            int timeToUnavailable = unavailability.getAvailableTime2(timeIndex);
            //check unavailabiliy
            if(timeToUnavailable >= duration + setupTime){
                //job feasable
                if(dueDate>= timeIndex && releaseDate<= timeIndex-setupTime-duration){
                    addJob(job,prevJob);
                    prevJob = job;
                }
                else if(releaseDate>timeIndex-setupTime-duration){
                    notScheduledJobs.add(job);
                }
                else if(dueDate < timeIndex-setupTime){
                    setTimeIndex(dueDate+setupTime);
                    timeToUnavailable = unavailability.getAvailableTime2(timeIndex);
                    if(timeToUnavailable-duration-setupTime < 0){
                        int time = unavailability.getAvailable(timeIndex);
                        setTimeIndex(time);
                    }
                    addJob(job,prevJob);
                    prevJob = job;
                }
                else{
                    setTimeIndex(dueDate);
                    addJob(job,prevJob);
                    prevJob = job;
                }
            }else{
                //TODO: mag nog met else ifs zijn voor setup toch nog te doen etc
                int temp = unavailability.getAvailable(timeIndex);
                setTimeIndex(temp);
                if(dueDate>= timeIndex && releaseDate<= timeIndex-setupTime-duration){
                    addJob(job,prevJob);
                    prevJob = job;
                }
                else if(releaseDate>timeIndex-setupTime-duration){
                    notScheduledJobs.add(job);
                }
                else if(dueDate < timeIndex){
                    setTimeIndex(dueDate);
                    addJob(job,prevJob);
                    prevJob = job;
                }else{
                    System.out.println("ttttttttt" + id);
                }
            }
        }
        Collections.sort(scheduled,Comparator.comparing(Job::getStart));
        this.bestCost = evaluate(scheduled);
        setJSONFormat();
        return scheduled;
    }

    public void addJob(Job fj, Job pj){
        if(pj != null){
            int setupTime = getSetupTime(fj, pj);
            SetupChange sc = new SetupChange(fj,pj,timeIndex-setupTime);
            setupList.add(sc);
            decreaseTimeIndex(setupTime);
        }
        fj.setStop(timeIndex);
        decreaseTimeIndex(fj.getDuration());
        fj.setStart(timeIndex);
        scheduled.add(fj);
    }
    public void setJSONFormat(){
        JSONObject finalSolution = new JSONObject();
        JSONArray jsonSetups = new JSONArray();
        List<SetupChange>setupChanges = this.setupList;
        Collections.sort(setupList,Comparator.comparing(SetupChange::getStart));
        for(SetupChange setupChange : setupChanges){
            JSONObject jsonSetup = new JSONObject();
            jsonSetup.put("from",setupChange.getJ1().getId());
            jsonSetup.put("to",setupChange.getJ2().getId());
            jsonSetup.put("start",setupChange.getStart());
            jsonSetups.put(jsonSetup);
        }
        finalSolution.put("setups",jsonSetups);
        JSONArray array = new JSONArray();
        for(Job job : scheduled){
            JSONObject jsonJob = new JSONObject();
            jsonJob.put("id",job.getId());
            jsonJob.put("start",job.getStart());
            array.put(jsonJob);
        }
        finalSolution.put("jobs",array);
        setJSONSolution(finalSolution);
    }

    public void setJSONSolution(JSONObject JSONSolution) {
        this.JSONSolution = JSONSolution;
    }

    public double getBestCost() {
        return bestCost;
    }

    public JSONObject getJSONSolution() {
        return JSONSolution;
    }

    public void setTimeIndex(int timeIndex) {
        this.timeIndex = timeIndex;
    }
    public void decreaseTimeIndex(int timeIndex){
        this.timeIndex -= timeIndex;
    }

    public LinkedList<SetupChange> getSetupList() {
        return setupList;
    }
    public int getSetupTime(Job curJob, Job prevJob){
        return setups[prevJob.getId()][curJob.getId()];
    }
    // methodes voor SA
    /*public Solution(Solution copy) {
        this.solutionJobs = new LinkedList<Job>(copy.solutionJobs); // deep copy
        this.jobs=copy.jobs;
        this.numberOfJobs = copy.jobs.size();
        this.weightDuration= copy.weightDuration;
        this.setups = copy.setups;
        this.unavailability=copy.unavailability;
        this.setupList = new LinkedList<SetupChange>(copy.getSetupList()); //deep copy
    }*/
}