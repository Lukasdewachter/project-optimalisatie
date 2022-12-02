import java.util.Arrays;

public class Unavailability {
    private Boolean[] un;
    public Unavailability(int horizon){
        un = new Boolean[horizon];
        Arrays.fill(un, false);
    }
    public void addUnavailable(int start, int end){
        for(int i=start; i<= end;i++){
            if(i<un.length){
                un[i] = true;
            }else break;
        }
    }
    public Boolean checkAvailable(int start, int end) {
        //checks if the range of time slots has unavailability
        boolean available = true;
        for (int i = start; i < end; i++) {
            if(i>=un.length){
                break;
            }
            if (un[i]) {
                available = false;
            }
        }
        return available;
    }
    public int skipUnavailable(int timeIndex){
        //skips to next available time
        for(int i=timeIndex; i<un.length;i++){
            if(un[i]){
                for(int j = i; j<un.length;j++){
                    if(!un[j]){
                        timeIndex = j;
                        break;
                    }
                }
                break;
            }
        }
        return timeIndex;
    }

    public void print(){
        int i = 0;
        for(Boolean b : un){
            System.out.println(i+ " "+b);
            i++;
        }
    }

}