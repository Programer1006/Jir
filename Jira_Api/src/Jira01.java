import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import org.json.simple.*;
import org.json.simple.parser.*;

public class Jira01 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String loginResponse = "";
		String jSessionID = "";
		String jsonData = "";
		String csvData = "";
		String writeToFileOutput = "";
		
		String BaseUrl = "https://visoldev.ddns.net:8888/jira/rest/";
		String loginServiceURL = "auth/1/session";
		String biExportURL = "getbusinessintelligenceexport/1.0/message";
		
		String loginUsername = "testuser";
		String loginPassword = "Test123";
		String analysisStartDate = "28-DEC-19";
        String analysisEndDate = "29-DEC-19";
		boolean error = false;
		String exportDirectory = "./downloads/";
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		if(!error){
	       loginResponse = loginToJira(BaseUrl, loginServiceURL, loginUsername, loginPassword);
	       if(loginResponse == "ERROR") { error = true; }
	    }
	    if(!error){
	       jSessionID = parseJSessionID(loginResponse);
	       if(jSessionID == "ERROR") { error = true; }
	    }
	    if(!error){
	       jsonData = getJsonData(BaseUrl, biExportURL, jSessionID, analysisStartDate, analysisEndDate);
	       if(jsonData == "ERROR") { error = true; }
	    }
	    if(!error){
	       csvData = formatAsCSV(jsonData);
	       if(csvData == "ERROR") { error = true; }
	    }
	    if(!error){
	        writeToFileOutput = writeToFile(csvData, exportDirectory);
	        if(writeToFileOutput == "ERROR") { error = true; }
	    }
	    if(!error){
	        System.out.println("USPJEH");
	    } else {
	        System.out.println("ERORR");
	    }
	 }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		public static String loginToJira(String BaseUrl,String loginServiceURL,String loginUsername,String loginPassword){
			String loginResponse = "";
			URL url = null;
			HttpURLConnection conn = null;
			String input = "";
			OutputStream os = null;
			BufferedReader br = null;
			String output = null;
			try {
				url = new URL(BaseUrl + loginServiceURL);
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("OBJAVLJENO");
				conn.setRequestProperty("Content-Type", "application/json");
				input = "{\"username\":\""+ loginUsername +"\",\"password\":\"" + loginPassword + "\"}";
				os = conn.getOutputStream();
				os.write(input.getBytes());
				os.flush();
				
				if(conn.getResponseCode() == 200) {
					br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
					while((output = br.readLine()) != null ) {
						loginResponse += output;
					}
					conn.disconnect();
				}
			} catch(Exception ex) {
				System.out.println("Jira Error: " + ex.getMessage());
				loginResponse = "ERROR";
			}
			System.out.println("\nloginResponse:");
	        System.out.println(loginResponse);
			return loginResponse;
		}
///////////////////////////////////////////////////////////////////////////////////////////////////////////
		public static String parseJSessionID(String input){
			String jSessionID = "";
	        try {
	            JSONParser parser = new JSONParser();
	            Object obj = parser.parse(input);
	            JSONObject jsonObject = (JSONObject) obj;
	            JSONObject sessionJsonObj = (JSONObject) jsonObject.get("session");
	            jSessionID = (String) sessionJsonObj.get("value");
	        } catch (Exception ex) {
	            System.out.println("Error in parseJSessionID: " + ex.getMessage());
	            jSessionID = "ERROR";
	        }
	        System.out.println("\njSessionID:");
	        System.out.println(jSessionID);
	        return jSessionID;
	    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		public static String getJsonData(String baseURL, String biExportURL, String jSessionID, String analysisStartDate, String analysisEndDate){
	        String jsonData = "";
	        try {
	            URL url = new URL(baseURL + biExportURL + "?startDate=" + analysisStartDate + "&endDate=" + analysisEndDate);
	            //URL url = new URL(baseURL + "api/2/issue/picker" + "?currentJQL=assignee%3Dadmin");
	            String cookie = "JSESSIONID=" + jSessionID;
	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	            conn.setRequestMethod("GET");
	            conn.setRequestProperty("Content-Type", "application/json");
	            conn.setRequestProperty("Cookie", cookie);
	            if(conn.getResponseCode() == 200)
	            {
	                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	                String output = "";
	                while((output = br.readLine()) != null){
	                    jsonData += output;
	                }
	                conn.disconnect();
	            }
	        } catch (Exception ex){
	            System.out.println("Error in getJsonData: " + ex.getMessage());
	            jsonData = "ERROR";
	        }
	        
	        System.out.println("\njsonData:");
	        System.out.println(jsonData);
	        return jsonData;
	    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
		public static String formatAsCSV(String jsonData){
	        String csvData = "";
	        try {
	            JSONParser parser = new JSONParser();
	            JSONArray records = null;
	            String headerRow = "";
	            String dataRows = "";
	            
	            String[] arrColNames = {"recordType","project","projectId","projectName","projectLeadUser","issueKey",
	            		"issueId","issueCreated","issueUpdated","issueCreatorUserName","issueDueDate","issueRemainingEstimate",
	            		"issueOriginalEstimate","issuePriority","issueReporter","issueStatus","issueTotalTimeSpent","issueVotes",
	            		"issueWatches","issueResolution","issueResolutionDate","commentId","commentAuthor","commentAuthorKey",
	            		"commentCreated","commentUpdated","commentUpdateAuthor","worklogId","worklogAuthor","worklogAuthorKey",
	            		"worklogCreated","worklogStarted","worklogUpdated","worklogTimeSpent","commentText","worklogText"};
	            List<String>colNames = Arrays.asList(arrColNames);
	            
	            for(int i=0;i<colNames.size();i++)
	            {
	                headerRow += colNames.get(i) + ",";
	            }
	            headerRow = headerRow.replaceAll(",$","\n");
	            
	            Object obj = parser.parse(jsonData);
	            JSONObject jsonValue = (JSONObject) obj;
	            records = (JSONArray) jsonValue.get("records");
	            Iterator iterRecords = records.iterator();
	            
	            while(iterRecords.hasNext())
	            {
	                JSONObject thisRecord = (JSONObject) iterRecords.next();
	                String strRecord = "";
	                for(int i=0;i<colNames.size();i++)
	                {
	                    String thisColName = colNames.get(i);
	                    strRecord += "\"" + (String) thisRecord.get(thisColName) + "\",";
	                }
	                strRecord = strRecord.replaceAll(",$", "\n");
	                dataRows += strRecord;
	            }
	            
	            csvData = headerRow + dataRows;
	            
	        } catch (Exception ex) {
	            System.out.println("Error in formatAsCSV: " + ex.getMessage());
	            csvData = "ERROR";
	        }
	        System.out.println("\ncsvData:");
	        System.out.println(csvData);        
	        return csvData;
	    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		public static String writeToFile(String csvData, String exportDirectory){
	        String writeToFileOutput = "";
	        try {
	            String currentTimeStamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new java.util.Date());
	            BufferedWriter writer = new BufferedWriter(new FileWriter(exportDirectory + "JiraIssues_" + currentTimeStamp + ".csv"));
	            writer.write(csvData);
	            writer.close();
	            
	        } catch (Exception ex) {
	            System.out.println("Error in writeToFile: " + ex.getMessage());
	            writeToFileOutput = "ERROR";
	        }
	        System.out.println("\nwriteToFileOutput:");
	        System.out.println(writeToFileOutput);
	        return writeToFileOutput;
	    }
}
//END/////////////////////////////////////////////////////////////////////////////////////////////////////