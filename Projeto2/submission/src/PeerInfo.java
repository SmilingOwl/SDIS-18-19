public class PeerInfo {
    private int id;
    private String address;
    private int port;
    private int count_files;
    private long time;

    public PeerInfo(int id, int port, String address) {
        this.id = id;
        this.port = port;
        this.address = address;
        this.count_files = 0;
        this.time = System.currentTimeMillis();
    }

    public int get_id() {
        return this.id;
    }

    public int get_port() {
        return this.port;
    }

    public String get_address() {
        return this.address;
    }

    public int get_count_files() {
        return this.count_files;
    }

    public long get_time() {
        return time;
    }

    public void set_time(long time) {
        this.time = time;
    }

    public void increase_count_files() {
        this.count_files++;
    }

    public void set_count_files(int count_files) {
        this.count_files = count_files;
    }
}