public class GetChunkMsg extends Message{
    
    public GetChunkMsg(String version,  Integer senderId, String fieldId, int chunkNo) {
        super(version, senderId, fieldId, null);

        this.msgType = MessageType.STORED;
        this.msgChunkNo = chunkNo;
    }
}