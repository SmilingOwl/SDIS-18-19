public class PutCunkMsg extends Message{
    
    public PutChunkMessage(String version,  Integer senderId, String fieldId, int chunkNo, int replicationDegree, byte[] buf) {
        super(version, senderId, fieldId, buf);

        this.msgType = MessageType.PUTCHUNK;
        this.msgReplicationDeg = replicationDegree;
        this.msgChunkNo = chunkNo;
    }
}