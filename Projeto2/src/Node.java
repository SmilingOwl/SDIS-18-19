import java.net.InetSocketAddress;
import java.lang.Math; 

public class Node {

	private long localHash; //id
	private InetSocketAddress localAddress;
	private InetSocketAddress predecessor;
	private HashMap<Integer, InetSocketAddress> fingerTable;
	
	public Node (String args[]) {
		
		if(args.length == 2) { //String address, String port (cria um ring novo)
		 // Set node fields
        this.address = address;
        this.port = Integer.valueOf(port);

       
		
       
	}
	else if(args.length == 4) { //String address, String port, String existingNodeAddress, String existingNodePort (junta-se a um ring)
		
		this.address = address;
        this.port = Integer.valueOf(port);

        // Set contact node fields
        this.existingNodeAddress = existingNodeAddress;
        this.existingNodePort = Integer.valueOf(existingNodePort);

        

       
	}
		
	// Hash address
    SHA1Hasher sha1Hasher = new SHA1Hasher(this.address + ":" + this.port);
    this.id = sha1Hasher.getLong();
    this.hex = sha1Hasher.getHex();
	 // Initialize finger table and successors
    this.initFingerTable();
    this.informSuccessor();
    this.precedessor = new Finger(this.address, this.port); //have predecessor point to ourselves until a new node arrives and informs us that we're his successor

}
	
	private void initFingerTable() {
		
		 // If this node is the only node in the ring
        if (this.existingNodeAddress == null) {
            // Initialize all fingers to refer to self
            for (int i = 0; i < 32; i++) {
                this.fingers.put(i, new Finger(this.address, this.port));
            }
        } else {
            // Open connection to contact node
            try {
                
     

                for (int i = 0; i < 32; i++) {
                    double index = pow(2, i-1);
                    // Send query to address received (to check if it exists and inform other nodes), read response, parse address and port from response, add response finger to table   
                }

             
           
    }

 
    private void informSuccessor() {
     
    

        // Notify the successor that we are the new predecessor, if we're not our own successors
        if (!this.address.equals(this.successor.getAddress()) || (this.port != this.successor.getPort())) {
            try {
             
              // Tell successor that this node is its new predecessor
   
              
            } catch (IOException e) {
                this.logError("Could not open connection to first successor");
                e.printStackTrace();
            }
        }
    }

	
}

