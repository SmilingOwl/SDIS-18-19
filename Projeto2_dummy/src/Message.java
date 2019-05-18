import java.util.Arrays;
import java.util.ArrayList;

class Message {
    private String type;
    private String file_id;
    private byte[] body;
    private int rep_degree;
    private String address;
    private int port;
    private int peer_id;
    private ArrayList<PeerInfo> peers;
    

    Message(String type, int peer_id, String file_id, int rep_degree, byte[] body, String address, int port,
        ArrayList<PeerInfo> peers) {
        this.type = type;
        this.file_id = file_id;
        this.rep_degree = rep_degree;
        this.body = body;
        this.address = address;
        this.port = port;
        this.peer_id = peer_id;
        this.peers = peers;
    }

    Message(byte[] message) {
        String msg = new String(message);
        msg = msg.trim();
        String[] message_parts = msg.split(" ");
        this.type = message_parts[0];
        this.peers = new ArrayList<PeerInfo>();

        if(this.type.equals("JOIN")) {
            this.peer_id = Integer.parseInt(message_parts[1]);
            this.address = message_parts[2];
            this.port = Integer.parseInt(message_parts[3]);

       /** Backup Protocol:
              --BACKUP <rep_degree> <CRLF><CRLF>
              --AVAILABLE <address> <port> <address> <port>...
              --P2P_BACKUP <file_id> <body>
              --STORED <peer_id> <file_id>
        */  
       }else if(this.type.equals("BACKUP")){
           this.peer_id = Integer.parseInt(message_parts[1]);
           this.rep_degree = Integer.parseInt(message_parts[2]);

       }else if(this.type.equals("AVAILABLE")){  
            PeerInfo peer;
            for (int i= 1; i< message_parts.length; i++){
                peer = new PeerInfo(-1, Integer.parseInt(message_parts[i+1]), message_parts[i]);
                this.peers.add(peer);
                i++;
            }
                
       }else if(this.type.equals("P2P_BACKUP")){
           this.file_id = message_parts[1];
           this.rep_degree = Integer.parseInt(message_parts[2]);

       }else if(this.type.equals("STORED")){
            this.peer_id = Integer.parseInt(message_parts[1]);
            this.file_id = message_parts[2];

       /** RESTORE Protocol:
              --RESTORE <file_id> <CRLF><CRLF>
              --AVAILABLE <address> <port>
              --P2P_RESTORE <file_id>
              --FILE <file_id> <num_chunks> -> <body> divided in parts
        */     
       }else if(this.type.equals("RESTORE")){
           this.peer_id = Integer.parseInt(message_parts[1]);
           this.file_id = message_parts[2];
       }else if(this.type.equals("P2P_RESTORE")){
           this.file_id = message_parts[1];
       }else if(this.type.equals("FILE")){
           this.file_id = message_parts[1];
           this.rep_degree = Integer.parseInt(message_parts[2]);

       /** DELETE Protocol:
              --DELETE <file_id> 
              --M_DELETE <file_id>
        */
       }else if(this.type.equals("DELETE")){
           this.file_id = message_parts[1];

       }else if(this.type.equals("M_DELETE")){
           this.file_id = message_parts[1];
       }

   }

    public byte[] build() {
        String message;
        byte[] m_body = null;

        if (this.type.equals("JOIN")) {
            message = this.type + " " + this.peer_id +
            " " + this.address + " " + this.port + " \r\n\r\n";        
        } else if(this.type.equals("BACKUP")){
            message = this.type + " " + this.peer_id + " " + this.rep_degree + " \r\n\r\n";
        } else if(this.type.equals("AVAILABLE")){
            String message_ini = this.type + " ";
            String message_end ="\r\n\r\n";
            String message_middle = "";

            for(int i= 0; i< this.peers.size(); i++){
                message_middle += this.peers.get(i).get_address() + " " + this.peers.get(i).get_port()+ " ";
            }

            message = message_ini + message_middle + message_end;
        } else if(this.type.equals("P2P_BACKUP")){
            message= this.type + " " + this.file_id + " " + this.rep_degree + " \r\n\r\n";
        } else if(this.type.equals("STORED")){
            message= this.type +  " " + this.peer_id + " " + this.file_id + " \r\n\r\n";
        } else if(this.type.equals("RESTORE")){
            message= this.type + " " + this.peer_id + " " + this.file_id + " \r\n\r\n";
        } else if(this.type.equals("P2P_RESTORE")){
            message= this.type + " " + this.file_id + " \r\n\r\n";
        } else if(this.type.equals("FILE")){
            message= this.type + " " + this.file_id + " " + this.rep_degree + " \r\n\r\n";
        } else if(this.type.equals("DELETE")){
            message= this.type + " " + this.file_id + " \r\n\r\n";
        } else if(this.type.equals("M_DELETE")){
            message= this.type + " " + this.file_id + " \r\n\r\n";
        } else return null;

        m_body = message.getBytes();
        return m_body;
    }

    public void separate_body(byte[] message) {
        for(int i = 0; i < message.length-4; i++) {
            if(message[i] == 0xD && message[i+1] == 0xA && message[i+2] == 0xD && message[i+3] == 0xA) {
                this.body = Arrays.copyOfRange(message, i + 4, message.length);
                break;
            }
        }
    }

    public String get_type() {
        return this.type;
    }

    public int get_peer_id() {
        return this.peer_id;
    }

    public String get_file_id(){
        return this.file_id;
    }

    public int get_rep_degree() {
        return this.rep_degree;
    }

    public byte[] get_body() {
        return this.body;
    }

    public ArrayList<PeerInfo> get_peers() {
        return this.peers;
    }

    public int get_port() {
        return this.port;
    }

    public String get_address() {
        return this.address;
    }
}