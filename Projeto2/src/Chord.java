import java.math.BigInteger;

public class Chord {
    private static final int NUM_FINGERS = 7;

    private PeerInfo predecessor;
    private PeerInfo owner;
    private PeerInfo[] fingerTable;

    public Chord(PeerInfo owner) {
        this.owner = owner;
        this.predecessor = owner;
<<<<<<< Updated upstream
        this.fingerTable = new PeerInfo[NUM_FINGERS];
        for(int i = 0; i < this.fingerTable.length; i++) {
            this.fingerTable[i] = owner;
=======
        this.fingers = new PeerInfo[NUM_FINGERS];
        
        for(int i = 0; i < fingers.length; i++) {
            this.fingers[i] = owner;
>>>>>>> Stashed changes
        }
    }

    /************************** Setters **************************/
   
    public void set_predecessor(PeerInfo predecessor) {
        this.predecessor = predecessor;
    }


    /************************** Others **************************/
    public void update_fingers(PeerInfo new_peer) {
        System.out.println("entered");
        BigInteger new_peer_id = new_peer.get_id();
        
        //a node with new_peer_id joined
        for(int i = 0; i < this.fingerTable.length; i++) {
            int value = (1 + (int)Math.pow(2, i)) % 160;
            System.out.println("i = " + i + " ; value = " + value + " ; "
                 + (BigInteger.valueOf(value).compareTo(new_peer_id)<0) + " ; " + (new_peer_id.compareTo(this.fingerTable[i].get_id()) < 0));
            if((BigInteger.valueOf(value).compareTo(new_peer_id) < 0) && (new_peer_id.compareTo(this.fingerTable[i].get_id()) < 0)) {
                this.fingerTable[i] = new_peer;
            }
        }
    }

    public void print_finger_table() {
        for(int i = 0; i < this.fingerTable.length; i++) {
            System.out.println(" - " + i + " - " + this.fingerTable[i].get_id());
        }
    }
}