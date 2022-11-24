import java.util.*;

public class SimulatedAnnealing {
    Solution currentSolution;

    public SimulatedAnnealing(Solution currentSolution) {
        this.currentSolution = currentSolution;
    }

    //kans voor accepteren van nieuwe oplossing bij optimizeSA()
    public static double probability(double f1, double f2, double temp) {
        if (f2 < f1) return 1;
        return Math.exp((f1 - f2) / temp);
    }

    public Solution optimizeSA() {
        // Parameters voor SA
        double temperature = 100;
        double coolingFactor = 0.99;

        // t *= coolingFactor => geometric reduction rule
        Solution bestSolution = new Solution(currentSolution);
        for (double t = temperature; t > 1; t *= coolingFactor) {
            Solution neighbor = new Solution(currentSolution);

            // Random twee indexen selecteren tussen index 0 en index lenght of solution,
            Random r = new Random();
            int index1 = r.nextInt(neighbor.solution.size()-1);
            int index2 = index1 + 1;
            if (index1 < 2) {
                continue;
            }

            //All jobs before the swap remain, the jobs after the swap get removed for new schedule
            //Idem for setup
            // FOR EXAMPLE:
            // Solution: J1 J3 J2 | J4 J5 J6
            // Setup: J1-J3, J3-J2, | J2-J4, J4-J5, J5-6
            // to delete every job after J4, you start at index i
            // To delete every setup after setup of j4, you start at index-1

            Job currentJob = neighbor.solution.get(index2);
            Job lastJob = neighbor.solution.get(index1 - 1);


            int timeIndex = lastJob.getStop();
            //delete jobs after index1 including index1 from solution
            neighbor.solution.subList(index1 - 1, neighbor.solution.size()).clear();
            //empty not scheduled jobs
            neighbor.notScheduledJobs.clear();
            //delete setups after index1-1 from setup list
            neighbor.setupList.subList(index1 - 2, neighbor.setupList.size()).clear();


            //----Optimization by swapping neigbours at index i----
            //Jobs at index i and index i+1 get swapped if possible

            addJob(currentJob, timeIndex, neighbor);
            timeIndex += currentJob.getDuration();
            lastJob = currentJob;
            for (Job j : neighbor.jobs) {
                //check if job can finish in time
                if (j.getDueDate() >= timeIndex + j.getDuration() + neighbor.getSetupTime(j, lastJob) && !neighbor.solution.contains(j)) {
                    //check if job can start & check unavailability
                    if (j.getReleaseDate() <= timeIndex && neighbor.unavailability.checkAvailable(timeIndex, timeIndex + j.getDuration() + neighbor.getSetupTime(j, lastJob))) {
                        //add job and setup
                        addSetup(timeIndex, lastJob, j, neighbor);
                        timeIndex += neighbor.getSetupTime(j, lastJob);
                        addJob(j, timeIndex, neighbor);
                        timeIndex+=j.getDuration();
                        lastJob = j;
                    } else {
                        if (j.getReleaseDate() > timeIndex) {
                            timeIndex = j.getReleaseDate();
                        }
                        //if job cannot start at this time we skip to its release date
                        if (neighbor.unavailability.checkAvailable(timeIndex, timeIndex + j.getDuration() + getSetupTime(j, lastJob, neighbor))) {
                            addSetup(timeIndex, lastJob, j, neighbor);
                            timeIndex += neighbor.getSetupTime(j, lastJob);
                            addJob(j, timeIndex, neighbor);
                            timeIndex+=j.getDuration();
                            lastJob = j;
                            //if job has unavailability we need to skip that first
                        } else {
                            timeIndex = neighbor.unavailability.skipUnavailable(timeIndex);
                            if (j.getDueDate() >= timeIndex + j.getDuration() + neighbor.getSetupTime(j, lastJob) && !neighbor.solution.contains(j) && neighbor.unavailability.checkAvailable(timeIndex, timeIndex + j.getDuration() + neighbor.getSetupTime(j, lastJob))) {
                                addSetup(timeIndex, lastJob, j, neighbor);
                                timeIndex += getSetupTime(j, lastJob, neighbor);
                                addJob(j, timeIndex, neighbor);
                                timeIndex+=j.getDuration();
                                lastJob = j;
                            } else {
                                if (!neighbor.solution.contains(j)) neighbor.notScheduledJobs.add(j);
                            }
                        }


                    }
                } else {
                    //job can't complete in time anymore
                    if (!neighbor.solution.contains(j)) neighbor.notScheduledJobs.add(j);
                }
            }
            if (Math.random() < probability(neighbor.evaluate(), currentSolution.evaluate(), t)) {
                currentSolution.solution = neighbor.solution;
            }
            if (bestSolution.evaluate() < currentSolution.evaluate()) {
                bestSolution.solution=currentSolution.solution;
            }
        }
        return bestSolution;
    }
    private void addJob (Job job,int timeIndex, Solution s){
        //add job to solution
        job.setStart(timeIndex);
        timeIndex += job.getDuration();
        job.setStop(timeIndex);
        s.solution.add(job);
    }
    public int getSetupTime (Job curJob, Job prevJob, Solution solution){
        return solution.setups[prevJob.getId()][curJob.getId()];
    }
    public void addSetup( int timeIndex, Job lastJob, Job currJob, Solution s){
        //save setup change
        SetupChange setupChange = new SetupChange(lastJob, currJob, (int) timeIndex);
        s.setupList.add(setupChange);
    }

}

