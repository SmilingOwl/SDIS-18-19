public class Chunk {
    private String file_id;
    private int chunk_number;
    private byte[] body;
    private int current_rep_degree;
    private int rep_degree;

    public static final int MAX_SIZE = 64000;

    Chunk(String file_id, int rep_degree, byte[] body, int chunk_number){
        this.file_id = file_id;
        this.rep_degree = rep_degree;
        this.body = body;
        this.chunk_number = chunk_number;
        this.current_rep_degree = 0;
    }

    public void increase_curr_rep_degree() {
        this.current_rep_degree++;
    }

    public byte[] get_body() {
        return this.body;
    }

    public String get_file_id() {
        return this.file_id;
    }

    public int get_rep_degree() {
        return this.rep_degree;
    }

    public int get_curr_rep_degree() {
        return this.current_rep_degree;
    }

    public int get_chunk_no() {
        return this.chunk_number;
    }
}