import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import api.ApiReader;
import gui.PassportGUI;

public class CovidAppController implements ActionListener {

	private final ApiReader apiReader = new ApiReader();
	private JSONObject[] covidData;
	private boolean vaccinated = false;
	private int dosesTaken = 0;
	
	private PassportGUI gui;

	public static void main(String[] args) throws IOException, InterruptedException, ParseException {

		CovidAppController controller = new CovidAppController();
		controller.gui = new PassportGUI(controller);
		controller.updateCovidData();
		controller.gui.setNationalStatisticsLabels((long)controller.covidData[0].get("cumCases"), (long)controller.covidData[0].get("cumDeaths"));
		controller.gui.setVisible(true);

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
					"filters=areaType=overview"
							+ "&structure=" + URLEncoder.encode("{\"date\":\"date\",\"name\":\"areaName\",\"dailyCases\":\"newCasesByPublishDate\",\"cumCases\":\"cumCasesByPublishDate\",\"dailyDeaths\":\"newDeaths28DaysByPublishDate\",\"cumDeaths\":\"cumDeaths28DaysByPublishDate\"}", "UTF-8")
					, true);

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

	private void updateVaccineStatus(long nhsNumber) {
		
		try {
			// read the api
			String api = apiReader.read("https://sandbox.api.service.nhs.uk/immunisation-history/FHIR/R4/Immunization?patient.identifier=https%3A%2F%2Ffhir.nhs.uk%2FId%2F"
					+ "nhs-number%7C"
					+ nhsNumber
					+ "&procedure-code%3Abelow=90640007&_include=Immunization%3Apatient",
					null,
					"application/fhir+json"
					, false);

			// parse the api
			JSONObject vaccineData = (JSONObject) new JSONParser().parse(api);

			// find the highest dose number
			for(int i = 0; i < Math.toIntExact((long) vaccineData.get("total")); i++) {
				// navigates to the part of the api with dose number info
				dosesTaken = Math.max(dosesTaken, Math.toIntExact((long) ((JSONObject) ((JSONArray) ((JSONObject) ((JSONObject) ((JSONArray) vaccineData.get("entry")).get(i)).get("resource")).get("protocolApplied")).get(0)).get("doseNumberPositiveInt")));
			}
			
			if(dosesTaken > 0) {
				vaccinated = true;
			}
			
		} catch(IOException | InterruptedException | ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case "Login":
			
			break;
		}
		
	}
}
