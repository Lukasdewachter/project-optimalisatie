import org.json.JSONArray;
import org.json.JSONObject;

import javax.sound.sampled.Line;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class DeepestDescend {
    private LinkedList<Job> scheduled,notScheduled,jobList;
    private int[][]setups;
    private LinkedList<SetupChange>setupList;
    private double bestCost, weightDuration;
    private Unavailability un;
    private int timeIndex, totalJobs, totalTime;
    private JSONObject JSONSolution;
    public DeepestDescend(LinkedList<Job>jobs,int[][]setups, Unavailability un, double weightDuration, int totalTime){
        this.notScheduled= new LinkedList<>(List.copyOf(jobs));
        this.jobList = jobs;
        this.setups=setups;
        this.un = un;
        this.weightDuration = weightDuration;
        scheduled = new LinkedList<>();
        setupList = new LinkedList<>();
        this.timeIndex=0;
        this.totalJobs=jobs.size();
        this.bestCost = Double.MAX_VALUE;
        this.totalTime = totalTime;
        mixNeighbours();
    }
    public void mixNeighbours(){
        for(int i=0; i<totalJobs-1;i++){
            LinkedList<Job>order = new LinkedList<>(List.copyOf(jobList));
            Collections.swap(order,i,i+1);
            LinkedList<Job>copyOrder = new LinkedList<>(List.copyOf(order));
            double cost =  calculateCost(order);
            if (cost <= bestCost){
                this.bestCost = cost;
                //setJobList(copyOrder);
                setJSONFormat();
            }
        }
    }
    public double calculateCost(LinkedList<Job> order){
        cleanProject();
        Job fj = order.get(0);
        setTimeIndex(fj.getDuration());
        addJob(fj, null);
        order.remove(0);
        Job lastJob = fj;
        while(!order.isEmpty()){
            int timeAvailable = un.getAvailableTime(timeIndex);
            if(timeIndex >= totalTime){
                notScheduled.addAll(order);
                order.clear();
                break;
            }
            if(timeAvailable <= 0 ){
                timeIndex = un.skipUnavailable(timeIndex);
            }
            LinkedList<Job>feasableJobs = new LinkedList<>();
            for(int i=0 ; i<order.size(); i++){
                Job job = order.get(i);
                int setupTime = getSetupTime(job,lastJob);
                int duration = job.getDuration();
                int dueDate = job.getDueDate();
                int releaseDate = job.getReleaseDate();
                if(releaseDate<=timeIndex && dueDate >= timeIndex+setupTime+duration && timeAvailable>=setupTime+duration){
                    feasableJobs.add(job);
                }
                else if(job.getDueDate()< timeIndex){
                    notScheduled.add(job);
                    order.remove(job);
                }
            }
            if(feasableJobs.isEmpty()){
                LinkedList<Job>orderCopy = new LinkedList<>(List.copyOf(order));
                Collections.sort(orderCopy,Comparator.comparing(Job::getReleaseDate));
                Job job;
                if(orderCopy.size()>5) {
                    job = orderCopy.get(4);
                }else{
                    job = orderCopy.get(0);
                }
                //TODO: zorgen dat de job zowel dicht bij timeindex zit met duedate ook voor lagere penalty
                if(timeIndex< job.getReleaseDate()){
                    setTimeIndex(job.getReleaseDate());
                }else{
                    timeIndex = un.skipUnavailable(timeIndex);
                }
            }else {
                double bestJobCost = Double.MAX_VALUE;
                Job bestJob = null;
                for (Job job : feasableJobs) {
                    double jobCost = calculateJobCost(job, lastJob, feasableJobs);
                    if (jobCost <= bestJobCost) {
                        bestJobCost = jobCost;
                        bestJob = job;
                    }
                }
                if (bestJob != null) {
                    addJob(bestJob, lastJob);
                    order.remove(bestJob);
                    lastJob = bestJob;
                }
            }
        }

        return evaluate(scheduled);
    }

    public double calculateJobCost(Job job,Job lastJob, LinkedList<Job>jobs){
        int jobTime = job.getDuration();
        int setupChange = getSetupTime(job,lastJob);
        double cal = weightDuration*(jobTime+setupChange);
        double cal2 = job.calculateEarlinessPenalty(timeIndex+jobTime+setupChange);
        double cost = weightDuration*(jobTime+setupChange) + job.calculateEarlinessPenalty(timeIndex+jobTime+setupChange);
        for(Job j : jobs){
            if(j.getId() != job.getId()){
                if(j.getDueDate()<=timeIndex+jobTime+setupChange){
                    cost += j.getRejectionPenalty();
                }
            }
        }
        return cost;
    }

    public void addJob(Job currentJob, Job lastJob){
        if(lastJob!=null){
            SetupChange setupChange = new SetupChange(lastJob,currentJob,timeIndex);
            setupList.add(setupChange);
            increaseTime(getSetupTime(currentJob,lastJob));
        }
        currentJob.setStart(timeIndex);
        increaseTime(currentJob.getDuration());
        currentJob.setStop(timeIndex);
        scheduled.add(currentJob);
    }
    public int getSetupTime(Job currentJob, Job lastJob){
        return setups[lastJob.getId()][currentJob.getId()];
    }
    public void setTimeIndex(int time){
        this.timeIndex = time;
    }
    public void increaseTime(int time){
        this.timeIndex+=time;
    }
    public void cleanProject(){
        notScheduled.clear();
        scheduled.clear();
        setupList.clear();
    }

    public void setJobList(LinkedList<Job> order) {
        jobList.clear();
        jobList.addAll(order);
    }

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
        for(int i=0; i< notScheduled.size();i++){
            sum+=notScheduled.get(i).getRejectionPenalty();
        }
        double rejectionSum = sum-earlinesSum-weightedSum;
        return sum;
    }

    public double getBestCost() {
        return bestCost;
    }

    public JSONObject getJSONSolution() {
        return JSONSolution;
    }
    public void setJSONFormat(){
        JSONObject finalSolution = new JSONObject();
        JSONArray jsonSetups = new JSONArray();
        List<SetupChange>setupChanges = this.setupList;
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
        this.JSONSolution = finalSolution;
    }
}
