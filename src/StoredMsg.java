public class StoredMsg extends Message{
    
    public StoredMsg(String version,  Integer senderId, String fieldId, int chunkNo) {
        super(version, senderId, fieldId, null);

        this.msgType = MessageType.STORED;
        this.msgChunkNo = chunkNo;
    }
}