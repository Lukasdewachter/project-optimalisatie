import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class Solution{
    int numberOfJobs, timeIndex,horizon;
    //LinkedList<Job> solutionJobs = new LinkedList<>();
    LinkedList<Job> notScheduledJobs = new LinkedList<>();
    LinkedList<Job> jobs,scheduled;
    LinkedList<Job> bestSol= new LinkedList<>();
    double bestCost;
    double weightDuration;
    int[][] setups;
    Unavailability unavailability;
    List<SetupChange>setupList;
    JSONObject JSONSolution;
    public Solution(LinkedList<Job> jobs, float weightDuration, int[][] setups, Unavailability unavailability, int horizon){
        super();
        this.jobs=jobs;
        numberOfJobs = jobs.size();
        this.weightDuration=weightDuration;
        this.setups = setups;
        this.unavailability=unavailability;
        setupList = new ArrayList<SetupChange>();
        this.scheduled = new LinkedList<>();
        this.horizon = horizon;
        this.JSONSolution = null;
    }
    //Weighted schedule duration + earliness penalty + penalty of rejected jobs
    public float evaluate(LinkedList<Job> solutionJobs){
        float sum =0;
        //Weighted schedule duration
        sum += (weightDuration * (solutionJobs.getLast().getStop()- solutionJobs.getFirst().getStart()));
        float weightedSum = sum;
        System.out.println("Weighted: "+weightedSum);
        //Earlines penalty
        for(Job j : solutionJobs){
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
    public double shortEvaluate(LinkedList<Job> solutionJobs){
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

    public LinkedList<Job> firstSolution2(){

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
        addJob2(firstJob,null);
        for(int index = jobs.size()-2 ; index> -1;index--){
            Job job = jobs.get(index);
            int id = job.getId();
            if(id==379){
                System.out.println();
            }
            int releaseDate = job.getReleaseDate();
            int dueDate = job.getDueDate();
            int duration = job.getDuration();
            int setupTime = getSetupTime(job,prevJob);
            int timeToUnavailable = unavailability.getAvailableTime2(timeIndex);
            //check unavailabiliy
            if(timeToUnavailable >= duration + setupTime){
                //job feasable
                if(dueDate>= timeIndex && releaseDate<= timeIndex-setupTime-duration){
                    addJob2(job,prevJob);
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
                    addJob2(job,prevJob);
                    prevJob = job;
                }
                else{
                    setTimeIndex(dueDate);
                    addJob2(job,prevJob);
                    prevJob = job;
                }
            }else{
                //TODO: mag nog met else ifs zijn voor setup toch nog te doen etc
                int temp = unavailability.getAvailable(timeIndex);
                setTimeIndex(temp);
                if(dueDate>= timeIndex && releaseDate<= timeIndex-setupTime-duration){
                    addJob2(job,prevJob);
                    prevJob = job;
                }
                else if(releaseDate>timeIndex-setupTime-duration){
                    notScheduledJobs.add(job);
                }
                else if(dueDate < timeIndex){
                    setTimeIndex(dueDate);
                    addJob2(job,prevJob);
                    prevJob = job;
                }else{
                    System.out.println("ttttttttt" + id);
                }
            }
        }
        Collections.sort(scheduled,Comparator.comparing(Job::getStart));
        this.bestCost = shortEvaluate(scheduled);
        setJSONFormat();
        return scheduled;
    }

    public void addJob2(Job fj, Job pj){
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

    public List<Job> firstSolution(){
        LinkedList<Job>solutionJobs = new LinkedList<>();
        Collections.sort(jobs, Comparator.comparing(Job::getDueDate));
        Job lastJob;
        Job firstJob = jobs.get(0);
        int startTime = firstJob.getDueDate()-firstJob.getDuration();
        int timeIndex=startTime;
        solutionJobs = addJob(firstJob, timeIndex,solutionJobs);
        timeIndex += firstJob.getDuration();
        lastJob = firstJob;
        for(Job j : jobs){
            //check if job can finish in time
            if(j.getDueDate() >= timeIndex + j.getDuration() + getSetupTime(j, lastJob) && !solutionJobs.contains(j)){
                //check if job can start & check unavailability
                if(j.getReleaseDate() <= timeIndex && unavailability.checkAvailable(timeIndex,timeIndex+j.getDuration()+getSetupTime(j, lastJob))){
                    //add job and setup
                    addSetup(timeIndex,lastJob,j);
                    timeIndex += getSetupTime(j, lastJob);
                    solutionJobs = addJob(j, timeIndex,solutionJobs);
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
                        solutionJobs = addJob(j, timeIndex,solutionJobs);
                        timeIndex += j.getDuration();
                        lastJob = j;
                        //if job has unavailability we need to skip that first
                    }else{
                        timeIndex = unavailability.skipUnavailable(timeIndex);
                        if(j.getDueDate() >= timeIndex + j.getDuration() + getSetupTime(j, lastJob) && !solutionJobs.contains(j)&&unavailability.checkAvailable(timeIndex,timeIndex+j.getDuration()+getSetupTime(j, lastJob))){
                            addSetup(timeIndex,lastJob,j);
                            timeIndex += getSetupTime(j, lastJob);
                            solutionJobs = addJob(j, timeIndex,solutionJobs);
                            timeIndex += j.getDuration();
                            lastJob = j;
                        }else{
                            if(!solutionJobs.contains(j))notScheduledJobs.add(j);
                        }
                    }
                    
                    
                }
            }else{
                //job can't complete in time anymore
                if(!solutionJobs.contains(j))notScheduledJobs.add(j);
            }
        }
        return solutionJobs;
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
    public void increaseTimeIndex(int timeIndex){
        this.timeIndex += timeIndex;
    }
    public void decreaseTimeIndex(int timeIndex){
        this.timeIndex -= timeIndex;
    }

    public LinkedList<Job> deepestDescend(LinkedList<Job> jobOrder){
        int timeIndex=0;
        LinkedList<Job>jobList = jobOrder;
        LinkedList<Job> bestSolution = new LinkedList<>(List.copyOf(jobOrder));

        List<SetupChange>bestSetup = new LinkedList<>(List.copyOf(setupList));
        double bestCost = Double.MAX_VALUE;
        List<LinkedList<Job>>orders = new ArrayList<>();
        jobOrder.addAll(notScheduledJobs);
        Collections.sort(jobOrder, Comparator.comparing(Job::getDueDate));
        int k=0;
        while (k <50){
            for (int i = 0; i < jobOrder.size() - 1; i++) {
                LinkedList<Job> curJobOrder = new LinkedList<>(List.copyOf(jobOrder));
                Collections.swap(curJobOrder, i, i + 1);
                orders.add(curJobOrder);
            }
            int count = 0;
            for (LinkedList<Job> curJobOrder : orders) {
                notScheduledJobs.clear();
                setupList.clear();
                LinkedList<Job> solution = new LinkedList<>();
                Job firstJob = curJobOrder.get(0);
                timeIndex = firstJob.getDueDate() - firstJob.getDuration();
                solution = addJob(firstJob, timeIndex, solution);
                timeIndex += firstJob.getDuration();
                Job lastJob = firstJob;
                for (int i = 1; i < curJobOrder.size(); i++) {
                    Job job = curJobOrder.get(i);
                    if(count == 141 && job.getId() == 20){

                        System.out.println("");
                    }
                    int setupTime = getSetupTime(job, lastJob);
                    int jobTime = job.getDuration();
                    int dueDate = job.getDueDate();
                /*
                    -checken of job al kan
                    -checken of er unavailability is
                    -checken of job optijd kan eindigen
                */
                    if (job.getReleaseDate() < timeIndex + setupTime) {
                        if (unavailability.checkAvailable(timeIndex, timeIndex + setupTime + jobTime)) {
                            if (timeIndex + jobTime + setupTime < dueDate) {
                                addSetup(dueDate-jobTime-setupTime, lastJob, job);
                                timeIndex = dueDate-jobTime;
                                solution = addJob(job, timeIndex, solution);
                                timeIndex += jobTime;
                                lastJob = job;
                            } else {
                                notScheduledJobs.add(job);
                            }
                        } else {
                            timeIndex = unavailability.skipUnavailable(timeIndex);
                            if (timeIndex + jobTime + setupTime < dueDate) {
                                addSetup(dueDate-jobTime-setupTime, lastJob, job);
                                timeIndex = dueDate-jobTime;
                                solution = addJob(job, timeIndex, solution);
                                timeIndex += jobTime;
                                lastJob = job;
                            } else {
                                notScheduledJobs.add(job);
                            }
                        }
                    } else {
                        int releaseDate =  job.getReleaseDate();
                        if(releaseDate-setupTime>=0){
                            releaseDate = releaseDate - setupTime;
                        }
                        if(unavailability.checkAvailable(releaseDate,releaseDate+setupTime+jobTime)){
                            if (timeIndex + jobTime + setupTime < dueDate) {
                                addSetup(dueDate-jobTime-setupTime, lastJob, job);
                                timeIndex = dueDate-jobTime;
                                solution = addJob(job, timeIndex, solution);
                                timeIndex += jobTime;
                                lastJob = job;
                            } else {
                                notScheduledJobs.add(job);
                            }
                        }else {
                            timeIndex = unavailability.skipUnavailable(timeIndex);
                            if (timeIndex + jobTime + setupTime < dueDate) {
                                addSetup(dueDate-jobTime-setupTime, lastJob, job);
                                timeIndex = dueDate-jobTime;
                                solution = addJob(job, timeIndex, solution);
                                timeIndex += jobTime;
                                lastJob = job;
                            } else {
                                notScheduledJobs.add(job);
                            }
                        }

                    }

                }

                double test = shortEvaluate(bestSolution);
                double test2 = shortEvaluate(solution);
                if(k==1 && count ==141){
                    return solution;
                }
                if (test2 < bestCost) {
                    bestCost = shortEvaluate(solution);
                    bestSetup = List.copyOf(this.setupList);
                    double test3 = evaluate(bestSolution);
                    setBestSolution(solution);
                    System.out.println("BETTER SOLUTION FOUND "+count);
                    jobOrder = solution;
                }
                count++;
            }
            k++;
        }
        this.setupList = List.copyOf(bestSetup);
        return bestSol;
    }


    public void setBestSolution(LinkedList<Job>solution){
        this.bestSol = solution;
    }
    public LinkedList<Job> getJobList(LinkedList<Job>opls){
        opls.addAll(notScheduledJobs);
        return opls;
    }
    public LinkedList<Job> getBestSol() {
        return bestSol;
    }

    public LinkedList<Job> addJob(Job job, int timeIndex, LinkedList<Job>solutionJobs){
        //add job to solution
        job.setStart(timeIndex);
        timeIndex += job.getDuration();
        job.setStop(timeIndex);
        solutionJobs.add(job);
        return solutionJobs;
    }
    public void addSetup(int timeIndex, Job lastJob, Job currJob){
        //save setup change
        SetupChange setupChange = new SetupChange(lastJob,currJob,timeIndex);
        setupList.add(setupChange);
    }
    public void clearProject(LinkedList<Job> jobs){
        setupList.clear();
        notScheduledJobs.clear();
        this.jobs = jobs;
    }
    public List<SetupChange> getSetupList() {
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