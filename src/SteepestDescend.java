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
    private int timeIndex, totalJobs, totalTime, upgrades,stopTime;
    private JSONObject JSONSolution;
    public SteepestDescend(LinkedList<Job>jobs,LinkedList<Job>solution,LinkedList<SetupChange>setupList, int[][]setups, Unavailability un, double weightDuration, int totalTime,double bestCost,int stopTime){
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
        this.upgrades =0;
        this.stopTime = stopTime;
    }
    public void startLocalSearch(){
        long t1 = System.currentTimeMillis();
        int iterations = 1000000;
        for(int count = 0; count <iterations;count++) {
            Random random = new Random();
            int r = random.nextInt(3);
            switch (1) {
                case 1:
                    mixNeighbours();
                    break;
            /*case 2 :
                removeRandomJob();
                break;
            case 0 :
                addNotScheduledJob();
                break;*/
                default:
                    System.out.println("error in switch");
            }
            if(count%10000 == 0){
                System.out.println("[----"+count/10000+"%----]");
            }
        }
        long t2 = System.currentTimeMillis();
        System.out.println(((t2-t1)/60000)+"m "+((t2-t1)%60000)/1000 +"s verstreken");
        System.out.println("Een gemiddelde van "+(iterations/100)/1000 +" duizend iteraties / seconde");

    }
    public void mixNeighbours(){
        Random random = new Random();
        LinkedList<Job>order = new LinkedList<>(List.copyOf(bestSolution));
        int r1 = random.nextInt(order.size());
        int r2 = random.nextInt(order.size());
        if(r1 != r2){
            Collections.swap(order,r1,r2);
        }
        LinkedList<Job>copyOrder = new LinkedList<>(List.copyOf(order));
        double cost =  calculateCost(order);
        if (cost < bestCost){
            this.bestCost = cost;
            setBestSolution(new LinkedList<>(List.copyOf(scheduled)));
            setBestNotScheduled(new LinkedList<>(List.copyOf(notScheduled)));
            setBestSetupList(new LinkedList<>(List.copyOf(setupList)));
            //setJobList(copyOrder);
            setJSONFormat();
            upgrades++;
        }
        notScheduled.clear();
        setupList.clear();
    }

    public void setBestNotScheduled(LinkedList<Job> bestNotScheduled) {
        this.bestNotScheduled = bestNotScheduled;
    }

    public void setBestSetupList(LinkedList<SetupChange> bestSetupList) {
        this.bestSetupList = bestSetupList;
    }

    public double calculateCost(LinkedList<Job> order){
        scheduled.clear();
        notScheduled.addAll(bestNotScheduled);
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
