import java.util.Arrays;

public class Unavailability {
    private Boolean[] un;
    public Unavailability(long horizon){
        un = new Boolean[(int)horizon];
        Arrays.fill(un, false);
    }
    public void addUnavailable(long start, long end){
        for(long i=start; i<= end;i++){
            un[(int)i] = true;
        }
    }
    public Boolean checkAvailable(long start, long end) {
        boolean available = true;
        for (int i = (int) start; i <= end; i++) {
            if (un[i]) {
                available = false;
                break;
            }
        }
        return available;
    }
    public long skipUnavailable(long timeIndex){
        for(int i=(int)timeIndex; i<un.length;i++){
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