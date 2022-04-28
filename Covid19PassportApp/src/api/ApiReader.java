package api;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.zip.GZIPInputStream;

/**
 * Reads APIs and, where necessary, decompressed them
 * 
 * @author Jack
 */
public class ApiReader {

    private final HttpClient client = HttpClient.newHttpClient();
    
    /**
     * Reads an API from the given URL
     * @param url the url of the api
     * @param compressed whether the api is compressed or not
     * @return a String of data returned by the API
     * @throws IOException
     * @throws InterruptedException 
     */
    public String read(String url, boolean compressed) throws IOException, InterruptedException {
    	return read(url, null, "application/json", compressed);
    }
    
    /**
     * Reads an API from the given URL with filters applied
     * @param url the url of the api
     * @param filters filters to apply to the API call
     * @param compressed whether the api is compressed or not
     * @return a String of data returned by the API
     * @throws IOException
     * @throws InterruptedException 
     */
    public String read(String url, String filters, boolean compressed) throws IOException, InterruptedException {
    	return read(url, filters, "application/json", compressed);
    }
	
    public String read(String url, String filters, String header, boolean compressed) throws IOException, InterruptedException{
	/* The code below was researched at the following resource:
         *
         * mkyong, 2019. How to send HTTP request GET/POST in Java [online].
         * Available at: https://mkyong.com/java/how-to-send-http-request-getpost-in-java/
         * [Accessed 26/04/22].
         */
	HttpRequest request = HttpRequest.newBuilder()
			.GET()
			.header("accept", header)
			.uri(URI.create(url
					+ filters
					))
			.timeout(Duration.ofMinutes(2))
			.build();
		
	String output; // to hold the output of the read
	if(compressed) { // need to decompress	
            HttpResponse<byte[]> response = client.send(request, BodyHandlers.ofByteArray());
            output = decompress(response.body());
	} else { // no decompression needed
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            output = response.body();
	}
	
	return output;
    }
	
    /**
     * Method to decompress a file in GZip format
     * 
     * This code has been taken from the following resource:
     * 
     * yfnick, 2015. Java gzip compress/decompress string [online].
     * Available at: https://gist.github.com/yfnick/227e0c12957a329ad138
     * [Accessed 26/04/22].
     * 
     * @param compressed the compressed byte array
     * @return an uncompressed String of the api response
     * @throws IOException 
     */
    private String decompress(byte[] compressed) throws IOException {
    	ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
    	GZIPInputStream gis = new GZIPInputStream(bis);
    	BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
    	StringBuilder sb = new StringBuilder();
    	String line;
    	while((line = br.readLine()) != null) {
    		sb.append(line);
    	}
    	br.close();
    	gis.close();
    	bis.close();
    	return sb.toString();
    }
}
