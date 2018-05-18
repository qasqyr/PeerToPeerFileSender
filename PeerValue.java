public class PeerValue {
    private int NumOfRequests;
    private int NumOfUploads;

    public PeerValue() {
        NumOfRequests = 0;
        NumOfUploads = 0;
    }

    void add(int x) {
        NumOfRequests++;
        NumOfUploads += x;
    }

    public String toString() {
        if (NumOfRequests == 0) {
            return "0";
        }

        int x =  (int) (((double) NumOfUploads / NumOfRequests) * 100);
        return String.valueOf(x);
    }
}
