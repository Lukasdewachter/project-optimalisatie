import java.util.ArrayList;
import java.util.LinkedList;

public class Setup {
    LinkedList<Job> jobs;
    int[][] setup;
    Job j1;
    Job j2;
    public Setup(LinkedList<Job> jobs){
        this.jobs = jobs;
        this.setup = new int[jobs.size()][jobs.size()];
    }
    public int setupTime(Job job1, Job job2){
        return setup[job1.getId()][job2.getId()];
    }
}