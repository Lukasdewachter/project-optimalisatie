import java.util.ArrayList;

public class Setup {
    ArrayList<Job> jobs;
    int[][] setup;
    Job j1;
    Job j2;
    public Setup(ArrayList<Job> jobs){
        this.jobs = jobs;
        this.setup = new int[jobs.size()][jobs.size()];
    }
    public int setupTime(Job job1, Job job2){
        return setup[job1.getId()][job2.getId()];
    }
}