import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class LocalSearch {
    private LinkedList<Job>scheduled, bestSolution,notScheduled,jobList;
    private int[][]setups;
    private LinkedList<SetupChange>setupList, bestSetupList;
    private double bestCost, weightDuration;
    private Unavailability un;
    private int timeIndex, totalJobs;
    private JSONObject JSONSolution;

    public LocalSearch(LinkedList<Job>jobs,int[][]setups, Unavailability un, double weightDuration){
        this.notScheduled= new LinkedList<>(List.copyOf(jobs));
        this.jobList = jobs;
        this.setups=setups;
        this.un = un;
        this.weightDuration = weightDuration;
        scheduled = new LinkedList<>();
        setupList = new LinkedList<>();
        bestSetupList = new LinkedList<>();
        bestSolution = new LinkedList<>();
        this.timeIndex=0;
        this.totalJobs=jobs.size();
        this.bestCost = Double.MAX_VALUE;
    }
    public void deepestDescend(){
        LinkedList<LinkedList<Job>>bestSolutions = new LinkedList<>();
        LinkedList<LinkedList<SetupChange>>bestSetups = new LinkedList<>();

        for(int count =0; count<150;count++){
            for (int i = 0; i < jobList.size() - 1; i++) {
                LinkedList<Job> curJobOrder = new LinkedList<>();
                curJobOrder.addAll(jobList);
                Collections.swap(curJobOrder, i, i + 1);
                LinkedList<Job> copyJobOrder = new LinkedList<>(List.copyOf(curJobOrder));
                cleanProject();
                Job fj = curJobOrder.get(0);
                setTimeIndex(fj.getDueDate() - fj.getDuration());
                addJob(fj, null);
                curJobOrder.remove(0);
                Job lastJob = fj;
                while (!curJobOrder.isEmpty()) {
                    Job job = curJobOrder.get(0);
                    if(job.getId() == 28 && count==3 && i==7){
                        System.out.println();
                    }
                    int setupTime = getSetupTime(job, lastJob);
                    int jobTime = job.getDuration();
                    int dueDate = job.getDueDate();
                    int jobId = job.getId();
                    int releaseDate = job.getReleaseDate();
                    if (releaseDate <= timeIndex) {
                        if (un.checkAvailable(timeIndex, timeIndex + setupTime + jobTime)) {
                            if (timeIndex + jobTime + setupTime <= dueDate) {
                                addJob(job, lastJob);
                                curJobOrder.remove(job);
                                lastJob = job;
                            } else {
                                curJobOrder.remove(job);
                            }
                        } else {
                            for (int j = notScheduled.size() - 1; j > -1; j--) {
                                Job extraJob = notScheduled.get(j);
                                if (extraJob.getId() == job.getId()) {
                                    setTimeIndex(un.skipUnavailable(timeIndex));
                                } else {
                                    if (un.checkAvailable(timeIndex, timeIndex + getSetupTime(extraJob, lastJob) + extraJob.getDuration())) {
                                        if (timeIndex + extraJob.getDuration() + getSetupTime(extraJob, lastJob) <= extraJob.getDueDate()) {
                                            addJob(extraJob, lastJob);
                                            curJobOrder.remove(extraJob);
                                            lastJob = extraJob;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        //Job later nog proberen
                        LinkedList<Job> releaseDateList = new LinkedList<>(List.copyOf(curJobOrder));
                        Collections.sort(releaseDateList, Comparator.comparing(Job::getDueDate));
                        LinkedList<Job> dueDateList = new LinkedList<>(List.copyOf(curJobOrder));
                        Collections.sort(dueDateList, Comparator.comparing(Job::getDueDate));
                        Job firstDue = dueDateList.get(0);
                        if (firstDue.getReleaseDate() > timeIndex && releaseDateList.indexOf(firstDue) < 2) {
                            int id = firstDue.getId();
                            int executeTime = firstDue.getDueDate() - firstDue.getDuration() - getSetupTime(firstDue, lastJob);
                            setTimeIndex(executeTime);
                            Collections.swap(curJobOrder, 0, curJobOrder.indexOf(firstDue));
                        } else {
                            Collections.swap(curJobOrder, 0, curJobOrder.indexOf(releaseDateList.get(0)));
                            if(firstDue.getDueDate()<timeIndex){
                                curJobOrder.remove(firstDue);
                            }
                        }
                  }
            }
                double cost = evaluate(scheduled);
                if (cost < bestCost) {
                    setBestCost(cost);
                    System.out.println("BETERE OPLS" + cost+"    "+count+"      "+i);
                    setJobList(copyJobOrder);
                    setJSONFormat();
                }
            }
        }
    }

    public void setBestSetupList(LinkedList<SetupChange> bestSetupList) {
        this.bestSetupList = bestSetupList;
    }

    public void setBestSolution(LinkedList<Job> bestSolution) {
        this.bestSolution = bestSolution;
    }

    public void setJobList(LinkedList<Job> jobList) {
        this.jobList = jobList;
    }

    public LinkedList<SetupChange> getBestSetupList() {
        return bestSetupList;
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
    public JSONObject getJSONFormat(){
        return this.JSONSolution;
    }

    public void setBestCost(double bestCost) {
        this.bestCost = bestCost;
    }

    public double getBestCost() {
        return bestCost;
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
        notScheduled.remove(currentJob);
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
        notScheduled.addAll(jobList);
        scheduled.clear();
        setupList.clear();
        timeIndex=0;
        for(Job j : jobList){
            j.clean();
        }
    }

    public LinkedList<Job> getScheduled() {
        return scheduled;
    }

    public LinkedList<Job> getBestSolution() {
        return bestSolution;
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

    List<SetupChange> getSetupList() {
        return setupList;
    }


}
