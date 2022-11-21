public class SetupChange {
    private Job j1,j2;
    private int start;
    public SetupChange(Job j1, Job j2, int start){
        this.j1=j1;
        this.j2=j2;
        this.start=start;
    }

    public int getStart() {
        return start;
    }

    public Job getJ1() {
        return j1;
    }

    public Job getJ2() {
        return j2;
    }
}
