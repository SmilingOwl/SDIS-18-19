public class ManagerMessageHandler implements Runnable {
    private Message message;
    private PeerManager peer_manager;
    
    public ManagerMessageHandler(PeerManager peer_manager, byte[] msg) {
        this.peer_manager = peer_manager;
        this.message = new Message(msg);
    }

    public void run() {
        if(this.message.get_type().equals("JOIN")) {
            this.peer_join();
        }
    }

    public void peer_join() {
        int peer_id = 1;//obter da mensagem
        int port = 1111; //obter da mensagem
        String address = "localhost"; //obter da mensagem
        PeerInfo peer_info = new PeerInfo(port, address);
        this.peer_manager.get_peers().put(peer_id, peer_info);
        System.out.println("Peer " + peer_id + " joined the system.");
    }
}