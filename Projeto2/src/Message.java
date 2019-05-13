/*
Types of messages:

-- Join new Peer in the PeerManager:
JOIN <peer_id> <address> <port> <CRLF><CRLF>

-- Backup Protocol:
BACKUP <peer_id> <file_id> <rep_degree> <CRLF><CRLF> <body>

-- Restore Protocol:
RESTORE <peer_id> <file_id> <CRLF><CRLF>

-- Delete Protocol:
DELETE <peer_id> <file_id> <CRLF><CRLF>
*/

import java.util.Arrays;

class Message {
    private String type;
    private String file_id;
    private byte[] body;
    private int sender_id;
    private int rep_degree;
    private String address;
    private int port;
    

    Message(String type, int sender_id, String file_id, int rep_degree, byte[] body, String address, int port) {
        this.type = type;
        this.sender_id = sender_id;
        this.file_id = file_id;
        this.rep_degree = rep_degree;
        this.body = body;
        this.address = address;
        this.port = port;
    }

    Message(byte[] message) {
        String msg = new String(message);
        msg = msg.trim();
        String[] message_parts = msg.split(" ");
        this.type = message_parts[0];
        this.sender_id = Integer.parseInt(message_parts[1]);
        if(this.type.equals("JOIN")) {
            //TODO
        }
    }

    public byte[] build() {
        String message;
        byte[] m_body = null;

        if (this.type.equals("JOIN")) {
            message = this.type + " " + " \r\n\r\n"; //TODO
             m_body = message.getBytes();
            
            //FOR messages with body:
            /*byte[] m = message.getBytes();
            m_body = new byte[m.length + body.length];
            System.arraycopy(m, 0, m_body, 0, m.length);
            System.arraycopy(this.body, 0, m_body, m.length, this.body.length);*/

        }
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

    public int get_sender_id() {
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