public class PeerInfo {
    private int id;
    private String address;
    private int port;
    private int count_files;
    private long time;
    private int free_space;

    public PeerInfo(int id, int port, String address) {
        this.id = id;
        this.port = port;
        this.address = address;
        this.count_files = 0;
        this.time = System.currentTimeMillis();
        this.free_space = 100000000;
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
        return this.time;
    }

    public int get_free_space() {
        return this.free_space;
    }

    public void set_free_space(int free_space) {
        this.free_space = free_space;
    }

    public void set_time(long time) {
        this.time = time;
    }

    public void increase_count_files() {
        this.count_files++;
    }

    public void decrease_count_files() {
        this.count_files--;
    }

    public void set_count_files(int count_files) {
        this.count_files = count_files;
    }
}