public class DeleteMsg extends Message{
    
    public DeleteMsg(String version,  Integer senderId, String fieldId, Integer chunkNo) {
        super(version, senderId, fieldId, null);

        this.msgType = MessageType.DELETE;
    }
}