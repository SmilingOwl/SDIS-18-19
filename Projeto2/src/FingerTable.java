public class FingerTable {
    private static final int MAX_PEERS = 4; //change to bigger number later TODO

    private PeerInfo predecessor;
    private PeerInfo successor;
    private PeerInfo owner;
    private PeerInfo[] fingers;

    public FingerTable(PeerInfo owner) {
        this.owner = owner;
        this.predecessor = owner;
        this.successor = owner;
        int table_size = (int) (Math.log(MAX_PEERS) / Math.log(2));
        this.fingers = new PeerInfo[table_size];
        for(int i = 0; i < fingers.length; i++) {
            this.fingers[i] = owner;
        }
    }

    /************************** Setters **************************/
    public void set_predecessor(PeerInfo predecessor) {
        this.predecessor = predecessor;
    }

    public void set_successor(PeerInfo successor) {
        this.successor = successor;
    }
}