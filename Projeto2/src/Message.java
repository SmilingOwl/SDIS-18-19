import java.util.Arrays;
import java.util.ArrayList;

class Message {
    private String type;
    private String file_id;
    private byte[] body;
    private int rep_degree;
    private String address;
    private int port;
    private String peer_id;
    private String sender_id;
    private ArrayList<PeerInfo> peers;
    

    Message(String type, String sender_id, String peer_id, String file_id, int rep_degree, byte[] body, String address, int port) {
        this.type = type;
        this.file_id = file_id;
        this.rep_degree = rep_degree;
        this.body = body;
        this.address = address;
        this.port = port;
        this.peer_id = peer_id;
        this.sender_id = sender_id;
    }

    Message(byte[] message) {
        String msg = new String(message);
        msg = msg.trim();
        String[] message_parts = msg.split(" ");
        this.type = message_parts[0];

        if(this.type.equals("JOIN")) {
            this.sender_id = message_parts[1];
            this.peer_id = message_parts[2];
            this.address = message_parts[3];
            this.port = Integer.parseInt(message_parts[4]);

        // Backup Protocol:
        //       --BACKUP <rep_degree> <CRLF><CRLF>
        //       --B_AVAILABLE <address> <port> <address> <port>...
        //       --P2P_BACKUP <body>
        //       --STORED <file_id>
         
        }/*else if(this.type.equals("BACKUP")){
           this.rep_degree = Integer.parseInt(message_parts[1]);

       }else if(this.type.equals("B_AVAILABLE")){  
            PeerInfo peer;
            for (int i= 0; i< message_parts.length; i++){
                peer = new PeerInfo(Integer.parseInt(message_parts[i+1]), message_parts[i]);
                peers.add(peer);
                i++;
            }
                
       }else if(this.type.equals("P2P_BACKUP")){  
           this.separate_body(message);

       }else if(this.type.equals("STORED")){
           this.file_id = message_parts[1];

       // RESTORE Protocol:
            //   --RESTORE <file_id> <CRLF><CRLF>
            //   --R_AVAILABLE <address> <port>
            //   --P2P_RESTORE <file_id>
            //   --FILE <file_id> <body>
         
       }else if(this.type.equals("RESTORE")){
           this.file_id = message_parts[1];

       }else if(this.type.equals("R_AVAILABLE")){
        PeerInfo peer;
        for (int i= 0; i< message_parts.length; i++){
            peer = new PeerInfo(Integer.parseInt(message_parts[i+1]), message_parts[i]);
            peers.add(peer);
            i++;
        }
            

       }else if(this.type.equals("P2P_RESTORE")){
           this.file_id = message_parts[1];

       }else if(this.type.equals("FILE")){
           this.file_id = message_parts[1];
           this.separate_body(message);

        // DELETE Protocol:
        //       --DELETE <file_id> 
        //       --M_DELETE <file_id>
        
       }else if(this.type.equals("DELETE")){
           this.file_id = message_parts[1];

       }else if(this.type.equals("M_DELETE")){
           this.file_id = message_parts[1];
       }*/

   }

    public byte[] build() {
        String message;
        byte[] m_body = null;

        if (this.type.equals("JOIN")) {
            message = this.type + " " + this.sender_id + " " + this.peer_id +
            " " + this.address + " " + this.port + " \r\n\r\n";     
            m_body = message.getBytes();
        
        }/*else if(this.type.equals("BACKUP")){
            message = this.type + " " + this.rep_degree + " \r\n\r\n";
            m_body = message.getBytes();

        }else if(this.type.equals("B_AVAILABLE")){
            String message_ini = this.type + " ";
            String message_end ="\r\n\r\n";
            String message_middle = "";

            for(int i= 0; i< peers.size(); i++){
                message_middle += peers.get(i).get_address() + " " + peers.get(i).get_port()+ " ";
            }

            message= message_ini + message_middle + message_end;
            m_body = message.getBytes();
        
        }else if(this.type.equals("P2P_BACKUP")){
            message= this.type + " " + " \r\n\r\n";
            
            byte[] m = message.getBytes();
            m_body = new byte[m.length + body.length];
            System.arraycopy(m, 0, m_body, 0, m.length);
            System.arraycopy(this.body, 0, m_body, m.length, this.body.length);

        }else if(this.type.equals("STORED")){
            message= this.type + " " + this.file_id + " \r\n\r\n";
            m_body = message.getBytes();

        }else if(this.type.equals("RESTORE")){
            message= this.type + " " + this.file_id + " \r\n\r\n";
            m_body = message.getBytes();

        }else if(this.type.equals("R_AVAILABLE")){
            String message_ini = this.type + " ";
            String message_end ="\r\n\r\n";
            String message_middle = "";

            for(int i= 0; i< peers.size(); i++){
                message_middle += peers.get(i).get_address() + " " + peers.get(i).get_port()+ " ";
            }

            message= message_ini + message_middle + message_end;
            m_body = message.getBytes();

        }else if(this.type.equals("P2P_RESTORE")){
            message= this.type + " " + this.file_id + " \r\n\r\n";
            m_body = message.getBytes();

        }else if(this.type.equals("FILE")){
            message= this.type + " " + this.file_id + " \r\n\r\n";
            byte[] m = message.getBytes();
            m_body = new byte[m.length + body.length];
            System.arraycopy(m, 0, m_body, 0, m.length);
            System.arraycopy(this.body, 0, m_body, m.length, this.body.length);

        }else if(this.type.equals("DELETE")){
            message= this.type + " " + this.file_id + " \r\n\r\n";
            m_body = message.getBytes();

        }else if(this.type.equals("M_DELETE")){
            message= this.type + " " + this.file_id + " \r\n\r\n";
            m_body = message.getBytes();
        }else return null;*/

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

    public String get_peer_id() {
        return this.peer_id;
    }

    public String get_sender_id() {
        return this.sender_id;
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

    public int get_port() {
        return this.port;
    }

    public String get_address() {
        return this.address;
    }
}