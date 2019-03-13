public class ChunkMsg extends Message{
    
    public ChunkMsg(String version,  Integer senderId, String fieldId, Integer chunkNo, byte[] buf) {
        super(version, senderId, fieldId, buf);

        this.msgType = MessageType.GETCHUNK;
        this.msgChunkNo = chunkNo;
    }
}