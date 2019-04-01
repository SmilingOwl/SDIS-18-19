import java.util.Arrays;

class Message {
    private String type;
    private String version;
    private String file_id;
    private byte[] body;
    private int sender_id;
    private int chunk_no;
    private int rep_degree;


    Message(String type, String version, int sender_id, String file_id, int chunk_no, int rep_degree, byte[] body) {
        this.type = type;
        this.version = version;
        this.sender_id = sender_id;
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.rep_degree = rep_degree;
        this.body = body;
    }

    Message(byte[] message) {
        String msg = new String(message);
        msg = msg.trim();
        String[] message_parts = msg.split(" ");
        this.type = message_parts[0];
        this.version = message_parts[1];
        this.sender_id = Integer.parseInt(message_parts[2]);
        this.file_id = message_parts[3];
        if(!this.type.equals("DELETE"))
            this.chunk_no = Integer.parseInt(message_parts[4]);
        
        if(this.type.equals("PUTCHUNK")) {
            this.rep_degree = Integer.parseInt(message_parts[5]);
            this.separate_body(message);

        } else if (this.type.equals("CHUNK")) {
            this.separate_body(message);
        }
    }

    public byte[] build() {
        String message;
        byte[] m_body;

        if (this.type.equals("PUTCHUNK")) {
            message = this.type + " " + this.version + " " + this.sender_id + " " + this.file_id + " " + this.chunk_no + " " 
                + this.rep_degree + " \r\n\r\n";
            byte[] m = message.getBytes();
            m_body = new byte[m.length + body.length];
            System.arraycopy(m, 0, m_body, 0, m.length);
            System.arraycopy(this.body, 0, m_body, m.length, this.body.length);

        } else if (this.type.equals("STORED")){
            message = this.type + " " + this.version + " " + this.sender_id + " " + this.file_id + " " + this.chunk_no + " \r\n\r\n";
            m_body = message.getBytes();

        }else if(this.type.equals("GETCHUNK")){
            message = this.type + " " + this.version + " " + this.sender_id + " " + this.file_id + " " + this.chunk_no + " \r\n\r\n";
            m_body = message.getBytes();

        } else if(this.type.equals("CHUNK")){
            message = this.type + " " + this.version + " " + this.sender_id + " " + this.file_id + " " + this.chunk_no + " \r\n\r\n";
            byte[] m = message.getBytes();
            m_body = new byte[m.length + body.length];
            System.arraycopy(m, 0, m_body, 0, m.length);
            System.arraycopy(this.body, 0, m_body, m.length, this.body.length);

        }else if(this.type.equals("DELETE")){
            message = this.type + " " + this.version + " " + this.sender_id + " " + this.file_id + " " + " \r\n\r\n";
            m_body = message.getBytes();
            
        }else if(this.type.equals("REMOVED")){
            message = this.type + " " + this.version + " " + this.sender_id + " " + this.file_id + " " + this.chunk_no + " \r\n\r\n";
            m_body = message.getBytes();
        } else return null;
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

    public int get_chunk_no() {
        return this.chunk_no;
    }

    public int get_rep_degree() {
        return this.rep_degree;
    }

    public byte[] get_body() {
        return this.body;
    }
}