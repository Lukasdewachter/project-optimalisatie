public class Job{
    private final int id;
    private final int duration;
    private final int releaseDate;
    private final int dueDate;
    private final double earlinessPenalty;
    private final double rejectionPenalty;

    private int start=0, stop=0;
    public Job(int id, int duration, int releaseDate, int dueDate, double earlinessPenalty, double rejectionPenalty){
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
    public void clean(){
        setStart(0);
        setStop(0);
    }
    public void setStart(Integer start) {
        this.start = start;
    }

    public void setStop(Integer stop) {
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

    public double getEarlinessPenalty() {
        //returns the calculated earliness penalty
        double time = getDueDate() - getStop()+1;
        return time * this.earlinessPenalty;
    }
    public double calculateEarlinessPenalty(int stop){
        return (getDueDate() - stop+1)*earlinessPenalty;
    }
    public double getRejectionPenalty() {
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