import java.util.Arrays;

public class Unavailability {
    private Boolean[] un;
    public Unavailability(int horizon){
        un = new Boolean[horizon+1];
        Arrays.fill(un, false);
    }
    public void addUnavailable(int start, int end){
        for(int i=start; i<= end;i++){
            if(i<un.length){
                un[i] = true;
            }else break;
        }
    }
    public int getAvailableTime2(int timeIndex){
        int time = 0;
        for(int i=timeIndex;i > -1 ;i--){
            if(un[i]){
                time = i+1;
                break;
            }
        }
        return timeIndex-time;
    }
    public int getAvailable(int timeIndex){
        for(int i=timeIndex; i> -1 ;i--){
            if(un[i]){
                for(int j = i; j > -1 ;j--){
                    if(!un[j]){
                        timeIndex = j;
                        break;
                    }
                    if(j == 0){
                        timeIndex = j-1;
                        break;
                    }
                }
                break;
            }
        }
        return timeIndex;
    }
    public int getAvailableTime(int timeIndex){
        int time = 0;
        for(int i=timeIndex;i<un.length;i++){
            if(un[i]){
                time = i-1;
                break;
            }
        }
        if(time == 0 ){
            time = un.length-1;
        }
        return time-timeIndex;
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
                    if(j == un.length-1){
                        timeIndex = j+1;
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