public class RemovedMsg extends Message{
    
    public RemovedMsg(String version,  Integer senderId, String fieldId, Integer chunkNo) {
        super(version, senderId, fieldId, null);

        this.msgType = MessageType.REMOVED;
        this.msgChunkNo = chunkNo;
    }
}