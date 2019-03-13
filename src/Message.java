
/** Types of message */
public enum MessageType{
    PUTCHUNK,
    STORED,
    GETCHUNK,
    CHUNK,
    DELETE,
    REMOVED
}

public class Message{

    //msg termination
    public static String CRLF="\r\n";
    
    public MessageType msgType;
    public String msgVersion;
    public Integer msgSenderId;
    public Integer msgFieldId;

    /** not used in all type of messages ->null  */
    public Integer msgChunkNo = null;
    public Integer msgReplicationDeg = null;
    public byte[] msgBody = null;

    public void parseHeader(String msg){

        String[] header = msg.split("\\s+");

        switch(header[0]){
            case:"PUTCHUNK" {
                this.msgType == MessageType.PUTCHUNK;
                break;
            }

           case: "STORED"{
                this.msgType == MessageType.STORED;
                break;
            }
            
            case "GETCHUNK"{
                this.msgTyp == MessageType.GETCHUNK;
                break;
            }

            case "CHUNK"{
                this.msgType == MessageType.CHUNK;
                break;
            }

            case: "DELETE"{
                this.msgType == MessageType.DELETED;
                break;
            }

           case: "REMOVED"{
                this.msgSenderId == MessageType.REMOVED;
                break;
            }
            default:
                break;
            
        }

        this.msgVersion = header[1];
        this.msgSenderId = Integer.parseInt(header[2]);
        this.msgFieldId = Integer.parseInt(header[3]);

        if(header.lenght > 4 )
        {
            this.msgChunkNo = header[4];
        }

    }

    public Message(String version, Integer senderID, String fieldID, byte[] body) {
        this.msgversion = version;
        this.msgSenderId = senderID;
        this.msgFieldId = fileldID;
        this.msgbody = body;
    }

}


