import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class SteepestDescend {
    /*
    * kijken wat beste switch is
    *
    *
    *
    */
    private LinkedList<Job> scheduled,notScheduled,jobList,bestSolution,bestNotScheduled;
    private int[][]setups;
    private LinkedList<SetupChange>setupList,bestSetupList;
    private double bestCost, weightDuration;
    private Unavailability un;
    private int timeIndex, totalJobs, totalTime,swaps,deletions,inserts;
    private JSONObject JSONSolution;
    public SteepestDescend(LinkedList<Job>jobs,LinkedList<Job>solution,LinkedList<SetupChange>setupList, int[][]setups, Unavailability un, double weightDuration, int totalTime,double bestCost){
        notScheduled = new LinkedList<>();
        for(Job j : jobs){
            if(!solution.contains(j))notScheduled.add(j);
        }
        this.jobList = jobs;
        this.setups=setups;
        this.un = un;
        this.weightDuration = weightDuration;
        this.scheduled = solution;
        this.setupList = setupList;
        this.timeIndex=0;
        this.totalJobs=jobs.size();
        this.bestCost = bestCost;
        this.totalTime = totalTime;
        this.bestSolution = new LinkedList<>(List.copyOf(scheduled));
        this.bestNotScheduled = new LinkedList<>(List.copyOf(notScheduled));
        this.bestSetupList = new LinkedList<>(List.copyOf(setupList));
        this.swaps=0;
        this.deletions=0;
        this.inserts=0;
    }
    public void startLocalSearch() {
        Random random = new Random();
        int[] f = {0,1,2};
        int r = random.nextInt(f.length);
        switch (f[r]) {
            case 0:
                mixNeighbours();
                break;
            case 1:
                removeRandomJob();
                break;
            case 2 :
                addNotScheduledJob();
                break;
            default:
                System.out.println("error in switch");
        }
    }
    //adds random job thats not scheduled
    //if there are no unscheduled jobs it does the neighbour swap
    public void addNotScheduledJob(){
        Random random = new Random();
        LinkedList<Job>order = new LinkedList<>(List.copyOf(bestSolution));
        int r1 = random.nextInt(order.size());
        if(!bestNotScheduled.isEmpty()){
            int r2 = random.nextInt(bestNotScheduled.size());
            order.add(r1,bestNotScheduled.get(r2));
            notScheduled.addAll(bestNotScheduled);
            notScheduled.remove(r2);
            double cost = calculateCost(order);
            if (cost < bestCost){
                this.bestCost = cost;
                setBestSolution(new LinkedList<>(List.copyOf(scheduled)));
                setBestNotScheduled(new LinkedList<>(List.copyOf(notScheduled)));
                setBestSetupList(new LinkedList<>(List.copyOf(setupList)));
                inserts++;
                setJSONFormat();
            }
            notScheduled.clear();
            setupList.clear();
        }else{
            mixNeighbours();
        }
    }
    //Removes a random job from the best solution and swaps it for an unscheduled one provider it has them, if it doesnt it only removes
    public void removeRandomJob(){
        Random random = new Random();
        LinkedList<Job>order = new LinkedList<>(List.copyOf(bestSolution));
        int r1 = random.nextInt(order.size());
        if(!bestNotScheduled.isEmpty()){
            notScheduled.addAll(bestNotScheduled);
            int r2 = random.nextInt(notScheduled.size());
            order.add(notScheduled.get(r2));
            notScheduled.remove(r2);
            Collections.swap(order,r1,order.size()-1);
            notScheduled.add(order.get(order.size()-1));
            order.remove(order.size()-1);
        }else{
            notScheduled.add(order.get(r1));
            order.remove(r1);
        }

        double cost = calculateCost(order);
        if (cost < bestCost){
            this.bestCost = cost;
            setBestSolution(new LinkedList<>(List.copyOf(scheduled)));
            setBestNotScheduled(new LinkedList<>(List.copyOf(notScheduled)));
            setBestSetupList(new LinkedList<>(List.copyOf(setupList)));
            deletions++;
            setJSONFormat();
        }
        notScheduled.clear();
        setupList.clear();
    }
    //swaps two random jobs in the solution and checks if its better
    public void mixNeighbours(){
        Random random = new Random();
        LinkedList<Job>order = new LinkedList<>(List.copyOf(bestSolution));
        int r1 = random.nextInt(order.size());
        int r2 = random.nextInt(order.size());
        if(r1 != r2){
            Collections.swap(order,r1,r2);
        }
        notScheduled.addAll(bestNotScheduled);
        double cost =  calculateCost(order);
        if (cost < bestCost){
            this.bestCost = cost;
            setBestSolution(new LinkedList<>(List.copyOf(scheduled)));
            setBestNotScheduled(new LinkedList<>(List.copyOf(notScheduled)));
            setBestSetupList(new LinkedList<>(List.copyOf(setupList)));
            swaps++;
            setJSONFormat();
        }
        notScheduled.clear();
        setupList.clear();
    }
    public void getActions(){
        System.out.println("swaps: "+Integer.toString(swaps)+" deletes: "+Integer.toString(deletions)+" inserts: "+Integer.toString(inserts));
    }

    public void setBestNotScheduled(LinkedList<Job> bestNotScheduled) {
        this.bestNotScheduled = bestNotScheduled;
    }

    public void setBestSetupList(LinkedList<SetupChange> bestSetupList) {
        this.bestSetupList = bestSetupList;
    }

    public double calculateCost(LinkedList<Job> order){
        scheduled.clear();
        Job firstJob = order.get(order.size()-1);
        setTimeIndex(firstJob.getDueDate());
        un.getAvailableTime2(timeIndex);
        if(firstJob.getDueDate()>totalTime){
            setTimeIndex(totalTime);
        }
        else if(un.getAvailableTime2(timeIndex) < 0){
            int time = un.getAvailable(timeIndex);
            setTimeIndex(time);
        }
        Job prevJob = firstJob;
        addJob(firstJob,null);
        for(int index = order.size()-2 ; index> -1;index--){
            Job job = order.get(index);
            int id = job.getId();
            int releaseDate = job.getReleaseDate();
            int dueDate = job.getDueDate();
            int duration = job.getDuration();
            int setupTime = getSetupTime(job,prevJob);
            int timeToUnavailable = un.getAvailableTime2(timeIndex);
            //check unavailabiliy
            if(timeToUnavailable >= duration + setupTime){
                //job feasable
                if(dueDate>= timeIndex && releaseDate<= timeIndex-setupTime-duration){
                    addJob(job,prevJob);
                    prevJob = job;
                }
                else if(releaseDate>timeIndex-setupTime-duration){
                    notScheduled.add(job);
                }
                else if(dueDate < timeIndex-setupTime){
                    setTimeIndex(dueDate+setupTime);
                    timeToUnavailable = un.getAvailableTime2(timeIndex);
                    if(timeToUnavailable-duration-setupTime < 0){
                        int time = un.getAvailable(timeIndex);
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
                int temp = un.getAvailable(timeIndex);
                setTimeIndex(temp);
                if(dueDate>= timeIndex && releaseDate<= timeIndex-setupTime-duration){
                    addJob(job,prevJob);
                    prevJob = job;
                }
                else if(releaseDate>timeIndex-setupTime-duration){
                    notScheduled.add(job);
                }
                else if(dueDate < timeIndex){
                    setTimeIndex(dueDate);
                    addJob(job,prevJob);
                    prevJob = job;
                }
            }
        }
        Collections.sort(scheduled,Comparator.comparing(Job::getStart));
        return evaluate(scheduled);
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

    public void setBestSolution(LinkedList<Job> bestSolution) {
        this.bestSolution = bestSolution;
    }

    public int getSetupTime(Job currentJob, Job lastJob){
        return setups[lastJob.getId()][currentJob.getId()];
    }
    public void setTimeIndex(int time){
        this.timeIndex = time;
    }
    public void decreaseTimeIndex(int time){
        this.timeIndex-=time;
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
        for(SetupChange setupChange : bestSetupList){
            JSONObject jsonSetup = new JSONObject();
            jsonSetup.put("from",setupChange.getJ1().getId());
            jsonSetup.put("to",setupChange.getJ2().getId());
            jsonSetup.put("start",setupChange.getStart());
            jsonSetups.put(jsonSetup);
        }
        finalSolution.put("setups",jsonSetups);
        JSONArray array = new JSONArray();
        for(Job job : bestSolution){
            JSONObject jsonJob = new JSONObject();
            jsonJob.put("id",job.getId());
            jsonJob.put("start",job.getStart());
            array.put(jsonJob);
        }
        finalSolution.put("jobs",array);
        this.JSONSolution = finalSolution;
    }
}
