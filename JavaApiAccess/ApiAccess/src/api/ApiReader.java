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

public class ApiReader {

	private final HttpClient client = HttpClient.newHttpClient();
	
	public String read(String url, boolean compressed) throws IOException, InterruptedException {
		return read(url, null, "application/json", compressed);
	}
	
	public String read(String url, String filters, boolean compressed) throws IOException, InterruptedException {
		return read(url, filters, "application/json", compressed);
	}
	
	public String read(String url, String filters, String header, boolean compressed) throws IOException, InterruptedException{
		// https://mkyong.com/java/how-to-send-http-request-getpost-in-java/
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
	
	//https://gist.github.com/yfnick/227e0c12957a329ad138
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