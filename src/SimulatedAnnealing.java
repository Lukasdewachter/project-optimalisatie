import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SimulatedAnnealing {
    int numberOfJobs;
    LinkedList<Job> notScheduledJobs = new LinkedList<>();
    ArrayList<Job> jobs= new ArrayList<>();
    double weightDuration;
    int[][] setups;
    Unavailability unavailability;
    List<SetupChange>setupList;

    Solution currentSolution;
    LinkedList<Job> currentSolutionSchedule = new LinkedList<>();
    LinkedList<Job> bestSolutionSchedule = new LinkedList<>();
    public SimulatedAnnealing(Solution s){
        this.currentSolution=s;
        this.currentSolutionSchedule=s.solution;
        this.jobs=s.jobs;
        numberOfJobs = s.jobs.size();
        this.weightDuration=s.weightDuration;
        this.setups = s.setups;
        this.unavailability= s.unavailability;
        setupList = s.setupList;
        notScheduledJobs = s.notScheduledJobs;
    }
    public static double probability(double f1, double f2, double temp) {
        if (f2 < f1) return 1;
        return Math.exp((f1 - f2) / temp);
    }
    public List<Job> optimizeSA(){
        double temperature = 1000;
        double coolingFactor = 0.995;

        for (double t = temperature; t > 1; t *= coolingFactor) {
            Solution neighbor = new Solution(currentSolution);

            int index1 = (int) (neighbor.solution.size() * Math.random());
            int index2 = (int) (neighbor.solution.size() * Math.random());

            Collections.swap(neighbor.solution, index1, index2);

            if (Math.random() < probability(neighbor.evaluate(), currentSolution.evaluate(), t)) {
                currentSolutionSchedule = neighbor.solution;
            }
            if (neighbor.evaluate() < currentSolution.evaluate()) {
                bestSolutionSchedule=currentSolutionSchedule;
            }
        }
        return bestSolutionSchedule;
    }

    public LinkedList<Job> getBestSolutionSchedule() {
        return bestSolutionSchedule;
    }
}
