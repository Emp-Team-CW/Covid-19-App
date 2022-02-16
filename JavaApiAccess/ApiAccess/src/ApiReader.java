import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.zip.GZIPInputStream;

public class ApiReader {

	private static final HttpClient client = HttpClient.newHttpClient();
	
	public static void main(String[] args) {
		// https://mkyong.com/java/how-to-send-http-request-getpost-in-java/
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.header("accept", "application/json")
		        .uri(URI.create("https://api.coronavirus.data.gov.uk/v1/data"))
		        .timeout(Duration.ofMinutes(2))
		        .build();
		
		HttpResponse<byte[]> response;
		try {
			response = client.send(request, BodyHandlers.ofByteArray());
			//System.out.println(response.statusCode());
			System.out.println(decompress(response.body())); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	//https://gist.github.com/yfnick/227e0c12957a329ad138
	public static String decompress(byte[] compressed) throws IOException {
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
