import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import api.ApiReader;
import api.Hotspot;
import gui.PassportGUI;

/**
 * Main class of the COVID-19 app. Creates all objects needed
 * and handles user interaction within the app.
 * 
 * @author Jack
 */
public class CovidAppController implements ActionListener {

	private final ApiReader apiReader = new ApiReader(); // to read the apis
	private JSONObject[] covidData; // national covid data
	private EnumMap<Hotspot, JSONObject> hotspotData = new EnumMap(Hotspot.class); // regional covid data
	private boolean vaccinated = false; // user vaccination status
	private int dosesTaken = 0; // user dosage number
	private Date mostRecentVaccination; // date of user most recent vaccination

	private PassportGUI gui; // to control gui elements

	public static void main(String[] args) throws IOException, InterruptedException, ParseException {

            // create the controller and gui
            CovidAppController controller = new CovidAppController();
            controller.gui = new PassportGUI(controller);
            
            // get up-to-date data
            controller.updateCovidData();
            controller.updateHotspotData();

            // show gui
            controller.gui.setVisible(true);

	}

        /**
         * Accesses the NHS COVID-19 data API for up-to-date information
         * @throws IOException
         * @throws InterruptedException
         * @throws ParseException 
         */
	public void updateCovidData() throws IOException, InterruptedException, ParseException {

            // read the api
            String api = apiReader.read("https://api.coronavirus.data.gov.uk/v1/data?",
			"filters=areaType=overview"
                        + "&structure=" + URLEncoder.encode("{\"date\":\"date\",\"name\":\"areaName\",\"dailyCases\":\"newCasesByPublishDate\",\"cumCases\":\"cumCasesByPublishDate\",\"dailyDeaths\":\"newDeaths28DaysByPublishDate\",\"cumDeaths\":\"cumDeaths28DaysByPublishDate\"}", "UTF-8")
			, true);

            /* parse the api
             * 
             * The code below was researched at the following resource:
             *
             * GeeksforGeeks, 2019. How to parse JSON in Java [online].
             * Available at: https://www.geeksforgeeks.org/parse-json-java/
             * [Accessed 26/04/22].
             * 
             * The code was not copied verbatim, it was used to find which
             * library to use to handle JSON data and how to use it
             */
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
                
            if(covidData.length >= 1) { // make sure there is data to show
                // show last updated date, the graphs and the figures
                gui.setCurrentDate((String)covidData[0].get("date"));
                gui.updateGraphs(covidData);
                gui.setNationalStatisticsLabels((long)covidData[0].get("cumCases"), (long)covidData[0].get("cumDeaths"));
            }
	}

        /**
         * Accesses the NHS COVID-19 data API for up-to-date information for each
         * region and displays it in a table
         * 
         * @throws IOException
         * @throws InterruptedException
         * @throws ParseException 
         */
	private void updateHotspotData() throws IOException, InterruptedException, ParseException {

            for(Hotspot hotspot: Hotspot.values()) { // https://www.baeldung.com/java-enum-iteration
		// read the api
		String areaType = hotspot == Hotspot.LONDON ? "region" : "utla";
		String api = apiReader.read("https://api.coronavirus.data.gov.uk/v1/data?",
                        "filters=areaType=" + areaType + ";areaCode=" + hotspot.areaCode()
			+ "&structure=" + URLEncoder.encode("{\"curCases\":\"newCasesByPublishDate\",\"totalCases\":\"cumCasesByPublishDate\",\"totalDeaths\":\"cumDeaths28DaysByPublishDate\",\"totalVaccinations\":\"cumVaccinesGivenByPublishDate\"}", "UTF-8")
			+ "&latestBy=newCasesByPublishDate"
			, true);

		/* parse the api and isolate the data
                 * 
                 * The code below was researched at the following resource:
                 *
                 * GeeksforGeeks, 2019. How to parse JSON in Java [online].
                 * Available at: https://www.geeksforgeeks.org/parse-json-java/
                 * [Accessed 26/04/22].
                 * 
                 * The code was not copied verbatim, it was used to find which
                 * library to use to handle JSON data and how to use it
                 */
		JSONObject covidJson = (JSONObject) ((JSONArray) ((JSONObject) new JSONParser().parse(api)).get("data")).get(0);

		for(Object key: covidJson.keySet()) {
                    if(covidJson.get(key) == null) {
                    	covidJson.replace(key, 0l);
                    }
		}

		// update hotspot data
		hotspotData.put(hotspot, covidJson);

                // add the data to the table
		gui.addHotspotRow(hotspot.toString(), (long) hotspotData.get(hotspot).get("curCases"), (long) hotspotData.get(hotspot).get("totalCases"), (long) hotspotData.get(hotspot).get("totalDeaths"), (long) hotspotData.get(hotspot).get("totalCases") - (long) hotspotData.get(hotspot).get("totalDeaths"));
            }
	}

        /**
         * Updates the vaccination status of a user based on the NHS login
         * info they provide
         * 
         * @param nhsNumber users NHS number to pass to the API
         */
	@SuppressWarnings("deprecation")
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
                    String dateString = (String) ((JSONObject) ((JSONObject) ((JSONArray) vaccineData.get("entry")).get(i)).get("resource")).get("occurrenceDateTime");
                    Date date = new Date(Integer.parseInt(dateString.substring(0, 4)), Integer.parseInt(dateString.substring(5, 7)) - 1, Integer.parseInt(dateString.substring(8,10)));
                    if(mostRecentVaccination == null || date.compareTo(mostRecentVaccination) > 0) {
                    	mostRecentVaccination = date;
                    }
		}

                // if they have been vaccinated, update record
		if(dosesTaken > 0) {
                    vaccinated = true;
                    gui.showVaccineDate(mostRecentVaccination.getDate() + "/" + (mostRecentVaccination.getMonth() + 1) + "/" + mostRecentVaccination.getYear());
                    gui.showDoses(dosesTaken);
		}

		gui.showVaccinated(vaccinated); // update the gui

		} catch(IOException | InterruptedException | ParseException e) {
                    e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

            switch(e.getActionCommand()) {
            case "Login": // when the vaccine login button is pressed
                    
		// reset login error messages
		gui.hideInvalidLoginLabel();
		gui.hideInvalidPasswordLabel();

		if(gui.getEnteredLoginNumber().compareTo("9000000009") != 0) {
                    // invalid login
                    gui.displayInvalidLoginLabel();
		} else if(gui.getEnteredLoginPassword().compareTo("test123!") != 0) {
                    // invalid password
                    gui.displayInvalidPasswordLabel();
		} else {
                    // valid login and password
                    gui.closeLoginDialog();
                    updateVaccineStatus(9000000009l);
		}
		break;
            }
	}
}
