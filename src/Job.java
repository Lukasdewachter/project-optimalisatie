public class Job{
    private final int id;
    private final int duration;
    private final int releaseDate;
    private final int dueDate;
    private final float earlinessPenalty;
    private final float rejectionPenalty;

    private int start=0, stop=0;
    public Job(int id, int duration, int releaseDate, int dueDate, float earlinessPenalty, float rejectionPenalty){
        this.id = id;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.dueDate = dueDate;
        this.earlinessPenalty = earlinessPenalty;
        this.rejectionPenalty = rejectionPenalty;
    }

    public int getId() {
        return id;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }

    public int getDuration() {
        return duration;
    }

    public int getReleaseDate() {
        return releaseDate;
    }

    public int getDueDate() {
        return dueDate;
    }

    public float getEarlinessPenalty() {
        //returns the calculated earliness penalty
        float time = getDueDate() - getStop()+1;
        return time * this.earlinessPenalty;
    }

    public float getRejectionPenalty() {
        return rejectionPenalty;
    }

    public void print(){
        System.out.println("-------------------------------");
        System.out.println("id: "+id+ " duration: "+ duration+" releaseD: "+releaseDate+" dueD: "+dueDate+" earlPen: "+earlinessPenalty+" rej: "+rejectionPenalty);
        System.out.println(start+" "+stop);
    }
    public void rPrint(){
        System.out.println(id +" : "+start);
    }
}