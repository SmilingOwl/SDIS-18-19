public class Chord {
    private static final int NUM_FINGERS = 160;

    private PeerInfo predecessor;
    private PeerInfo owner;
    private PeerInfo[] fingerTable;

    public FingerTable(PeerInfo owner) {
        this.owner = owner;
        this.predecessor = owner;
        this.fingers = new PeerInfo[NUM_FINGERS];
        for(int i = 0; i < fingers.length; i++) {
            this.fingers[i] = owner;
        }
    }

    /************************** Setters **************************/
    public void set_predecessor(PeerInfo predecessor) {
        this.predecessor = predecessor;
    }

    /************************** Others **************************/
    public void update_fingers(PeerInfo new_peer) {
        BigInteger new_peer_id = new_peer.get_id();
        //a node with new_peer_id joined
        for(int i = 0; i < fingers.length; i++) {
            int value = (1 + Math.pow(2, i-1)) % NUM_FINGERS;
            if((BigInteger.valueof(value).compareTo(new_peer_id) < 0) && (new_peer_id.compareTo(finger[i]) < 0)) {
                finger[i] = new_peer_id;
            }
        }
    }
}