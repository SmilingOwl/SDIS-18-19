public class SendActiveMessage implements Runnable {
    private Peer owner;
    
    public SendActiveMessage(Peer owner) {
        this.owner = owner;
    }

    public void run() {
        while(true) {
            try {
                Thread.sleep(60000);
            } catch (Exception ex) {
                System.out.println("Error on thread sleep.");
            }
            Message active_message = new Message("ACTIVE", this.owner.get_id(), null, -1, null, null, -1, null);
            SendMessage send = new SendMessage(this.owner.get_manager_address(), this.owner.get_manager_port(), 
                active_message, this.owner.get_context().getSocketFactory());
            send.run();
        }
    }
}