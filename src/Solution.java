import java.util.*;

public class Solution{
    int numberOfJobs;
    //LinkedList<Job> solutionJobs = new LinkedList<>();
    LinkedList<Job> notScheduledJobs = new LinkedList<>();
    LinkedList<Job> jobs= new LinkedList<>();
    LinkedList<Job> bestSol= new LinkedList<>();
    float bestCost;
    float weightDuration;
    int[][] setups;
    Unavailability unavailability;
    List<SetupChange>setupList;
    public Solution(LinkedList<Job> jobs, float weightDuration, int[][] setups, Unavailability unavailability){
        super();
        this.jobs=jobs;
        numberOfJobs = jobs.size();
        this.weightDuration=weightDuration;
        this.setups = setups;
        this.unavailability=unavailability;
        setupList = new ArrayList<SetupChange>();
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