import java.util.ArrayList;

public class Chunk implements Comparable<Chunk> {
    private String file_id;
    private int chunk_number;
    private byte[] body;
    private int current_rep_degree;
    private int rep_degree;
    private ArrayList<Integer> senders_who_stored;

    public static final int MAX_SIZE = 64000;

    Chunk(String file_id, int rep_degree, byte[] body, int chunk_number){
        this.file_id = file_id;
        this.rep_degree = rep_degree;
        this.body = body;
        this.chunk_number = chunk_number;
        this.senders_who_stored = new ArrayList<>();
    }

    Chunk(String file_id, byte[] body, int chunk_number){
        this.file_id = file_id;
        this.body = body;
        this.chunk_number = chunk_number;
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

    public int get_chunk_no() {
        return this.chunk_number;
    }

    @Override
    public int compareTo(Chunk c) {
        return this.get_chunk_no() - c.get_chunk_no();
    }
}