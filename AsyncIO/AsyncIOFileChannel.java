import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.concurrent.Future;

public class AsyncIOFileChannel {

	private static byte[] content;
	private static int CAPACITY = 100;
	
	public static void main(String args[]) {
		
		AsyncIOFileChannel channel = new AsyncIOFileChannel();
		if(args.length != 2) {
			System.out.println("Wrong arg count, try again");
			return;
		}
		
		String fileFrom = args[0];
		String fileTo = args[1];
		byte[] content = new byte[100];
		try{
			channel.read(fileFrom);
		}
		catch(Exception ex) {
            System.out.println("Error reading.");
            return;
        }
		
		try{
			channel.write(fileTo);
		}catch(Exception ex) {
            System.out.println("Error writing.");
            ex.printStackTrace();
            return;
        }
		System.out.println("Finished writing");
	}
	
	 private void write(String filepath) throws IOException{
	 
		 
	 
		 
		 Path path = Paths.get(filepath);
		 if(!Files.exists(path)){
		     Files.createFile(path);
		 }
		 AsynchronousFileChannel fileChannel = 
		     AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);

		 System.out.println("To write: " + new String(content));
		
		 long position = 0;

		 ByteBuffer buffer = ByteBuffer.wrap(content);
		 buffer.put(content);
		 buffer.flip();

		 fileChannel.write(buffer, position, buffer, new CompletionHandler<Integer, ByteBuffer>() {

		     @Override
		     public void completed(Integer result, ByteBuffer attachment) {
		         System.out.println("bytes written: " + result);
		     }

		     @Override
		     public void failed(Throwable exc, ByteBuffer attachment) {
		         System.out.println("Write failed");
		         exc.printStackTrace();
		     }
		 });
		
	    }
	 
	 
	private void read(String filepath)throws IOException, InterruptedException, java.util.concurrent.ExecutionException
		{
     
        String filePath = filepath;
       
        Path path = Paths.get(filePath);
        File f = path.toFile();
        if(!f.exists()){
		    System.out.println("File doesn't exist");
		 }
         
        AsynchronousFileChannel channel =
            AsynchronousFileChannel.open(path, StandardOpenOption.READ);
         
        ByteBuffer buffer = ByteBuffer.allocate((int)f.length());
 
        Future<Integer> result = channel.read(buffer, 0);
             
        while (!result.isDone()) {
			
			System.out.println("Reading in progress... ");
		}
		
		System.out.println("Reading done: " + result.isDone());
		System.out.println("Bytes read from file: " + result.get());

		buffer.flip();
			
		content = new byte[buffer.capacity()];
		int i = 0;
		while (buffer.hasRemaining()) {
			
		
				 content[i] = buffer.get();    
				 i++;
				 
		}
		System.out.println(" ");    
		
		System.out.println("Content: " + new String(content));

		buffer.clear();
        channel.close();
    }
     

}
