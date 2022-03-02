import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CovidAppController {

	private final ApiReader apiReader = new ApiReader();
	private JSONObject[] covidData;
	private boolean vaccinated;
	private int dosesTaken;

	public static void main(String[] args) throws UnsupportedEncodingException {

		CovidAppController controller = new CovidAppController();
		
		System.out.println(controller.covidData[1].get("date"));

	}
	
	public CovidAppController() {
		try {
			updateCovidData();
		} catch(IOException | InterruptedException | ParseException e) {
			e.printStackTrace();
		}
	}

	public void updateCovidData() throws IOException, InterruptedException, ParseException {

			// read the api
			String api = apiReader.read("https://api.coronavirus.data.gov.uk/v1/data?",
					"filters=areaType=nation;areaName=england"
							+ "&structure=" + URLEncoder.encode("{\"date\":\"date\",\"name\":\"areaName\",\"dailyCases\":\"newCasesByPublishDate\",\"cumulativeCases\":\"cumCasesByPublishDate\", \"60DayDeath\":\"cumDeaths60DaysByPublishDate\" ,\"dailyDeaths\":\"newDeaths28DaysByPublishDate\",\"cumulativeDeaths\":\"cumDeaths28DaysByPublishDate\"}", "UTF-8")
							//+ "&latestBy=newCasesByPublishDate"
					);

			// parse the api
			// reference - https://www.geeksforgeeks.org/parse-json-java/
			JSONObject covidJson = (JSONObject) new JSONParser().parse(api);

			// isolating the data
			covidData = new JSONObject[Math.toIntExact((long) covidJson.get("length"))]; // to hold the final data
			JSONArray dataJsonArray = (JSONArray) covidJson.get("data"); // the data in array form

			// iterate data array
			Iterator<?> iterator = dataJsonArray.iterator();
			int counter = 0; // hold the array position

			while (iterator.hasNext()) // for every data entry (each days record)
			{
				// add to the overall data array and increase the counter
				covidData[counter++] = (JSONObject) iterator.next();			
			}
	}
}
