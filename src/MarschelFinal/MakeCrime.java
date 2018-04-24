package MarschelFinal;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.sql.*;
import java.util.*;
import java.io.*;

public class MakeCrime {
    public static ArrayList<String> colorWheel = new ArrayList<String>();

    public MakeCrime(){
        //
    }


    public static void makeMap() throws Exception{
        populateColorWheel();

        try {
            File fileTest = new File("outputMap.svg");
            if(fileTest.exists()){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String originalSvgSource = readFileSource("usCountiesOriginal.svg");
        Document originalSvg = Jsoup.parse(originalSvgSource,"", Parser.xmlParser());

        File fileOut = new File("outputMap.svg");
        PrintWriter printOutput = new PrintWriter(fileOut);

        printOutput.println(getDocHead("usCountiesOriginal.svg"));

        Elements svgAll = originalSvg.select("svg");

        Elements gTag = svgAll.select("g");
        String defaultStyle = gTag.get(0).attr("style");

        Elements paths = svgAll.select("path");
        for(Element path: paths){
            if(!path.attr("id").equalsIgnoreCase("State_Lines") && !path.attr("id").equalsIgnoreCase("separator")){
                path.attr("style",defaultStyle);
            }
        }

        for(Element path: paths){
            if(!path.attr("id").equalsIgnoreCase("State_Lines") && !path.attr("id").equalsIgnoreCase("separator")){
                String fips = path.attr("id");
                CountyDataGeneral countyData = readCountyDataFromFips(fips);
                CountyCrimeData countyCrime = readCountyCrimeData(countyData.getCountyName(),countyData.getStateId());

                if(countyCrime.getCountyName().equalsIgnoreCase("")){
                    path.attr("style",formatStyle("#000000"));
                }else{
                    String colorCode = findColorForPop(countyData,countyCrime,fips);
                    path.attr("style",formatStyle(colorCode));
                }
            }
        }
        printOutput.print(svgAll);
        printOutput.close();
    }


    public static void populateColorWheel(){
        String line;
        try {
            File inputFile = new File("color_wheel.txt");
            Scanner input = new Scanner(inputFile);

            while(input.hasNext()){
                line = input.nextLine();
                colorWheel.add(line);
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static String findColorForPop(CountyDataGeneral countyData,CountyCrimeData countyCrime,String fips){// for total crime
        // determines what color to use based on the ratio of total crime to population.
        String result = "";

        if(countyCrime.getTotalCrime() == 0 || countyData.getPopulation() ==0){
            result = colorWheel.get(0);
        }else{
            double crimeRate = (double)countyCrime.getTotalCrime()/ (double)countyData.getPopulation();
            double colorPicker = (crimeRate*100)*0.10;
            int finalCodeToGet = (int) Math.round(colorPicker);
            if(finalCodeToGet > 10){
                finalCodeToGet = 9;
            }
            result = colorWheel.get(finalCodeToGet);
        }
        return result;
    }

    public static CountyCrimeData getCrimeDataForOneCounty(String zip){
        CountyCrimeData data = new CountyCrimeData();

        String[] cityState = getCityStateFromZip(zip);
        String[] countyStateFull = getCountyStateFullFromCityState(cityState[0],cityState[1]);
        Connection conn = connectToDB("stateCountyCrime.db");
        try {
            Statement stmt = conn.createStatement();
            String queryString = String.format("Select * from stateCounty where county like '%s' and state like '%s' ",countyStateFull[0],cityState[1]);
            ResultSet rs = stmt.executeQuery(queryString);
            while (rs.next()) {
                String violent_crime = rs.getString("violent_crime");
                String property_crime = rs.getString("property_crime");
                String stateAbb = rs.getString("state");

                data.setViolentCrime(parseInt(violent_crime));
                data.setPropertyCrime(parseInt(property_crime));
                data.setStateId(stateAbb);
                data.setStateFull(countyStateFull[1]);
                data.setCountyName(countyStateFull[0]);

            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            data.setCountyName("null");
        }
        closeDB(conn);

        return data;

    }

    public static String[] getCountyStateFullFromCityState(String city,String state){
        String[] result = {"",""};
        Connection conn = connectToDB("cityData.db");

        try {
            Statement stmt = conn.createStatement();
            String queryString = String.format("Select * from cities1 where city like '%s' and state_id like '%s' ",city,state);
            ResultSet rs = stmt.executeQuery(queryString);
            while (rs.next()) {
                result[0] = rs.getString("county");
                result[1] = rs.getString("state_name");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            result[0]="error";
            e.printStackTrace();
        }
        closeDB(conn);
        return result;
    }

    public static String[] getCityStateFromZip(String zip){
        String[] zipInfo = new String[2];
        Connection conn = connectToDB("zipDatabase.db");
        try {
            Statement stmt = conn.createStatement();
            String queryString = String.format("Select * from zips where zipcode like '%s' and locationtype like 'PRIMARY' ",zip);
            ResultSet rs = stmt.executeQuery(queryString);
            while (rs.next()) {
                zipInfo[0] = rs.getString("city");
                zipInfo[1] = rs.getString("state");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            zipInfo[0]="error"; // catches errors and sets the output accordingly so I can handle it in the Controller
            zipInfo[1] = "error";
        }
        closeDB(conn);
        if(zipInfo[0] == null || zipInfo[1] == null){
            zipInfo[0] = "error";
            zipInfo[1] = "error";
        }
        return zipInfo;
    }

    public static String getDocHead(String inputName){
        String result = "";

        try {
            File inputData = new File(inputName);
            Scanner input = new Scanner(inputData);

            result = input.nextLine();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        return result;
    }

    public static String readFileSource(String inputFileName){
        String data = "";
        String line;
        try {
            File inputData = new File(inputFileName);
            Scanner input = new Scanner(inputData);

            while(input.hasNext()){
                line = input.nextLine();
                data += line;
                //System.out.println(line);
            }
            input.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(3);
            e.printStackTrace();
        }
        return data;
    }

    public static String formatStyle(String colorCode){
        String result = String.format("fill:%s;fill-rule:nonzero;stroke:#000000;stroke-width:0.1",colorCode);
        return result;
    }

    public static CountyCrimeData readCountyCrimeData(String countyName,String stateAbb){
        String queryString = "";
        Connection conn = connectToDB("stateCountyCrime.db");
        CountyCrimeData crimeData = new CountyCrimeData();
        try {
            Statement stmt = conn.createStatement();
            queryString = String.format("select * from stateCounty where county like '%s' and state like '%s' ",countyName,stateAbb);
            ResultSet rs = stmt.executeQuery(queryString);
            while (rs.next()) {
                String county = rs.getString("county");
                String violent_crime = rs.getString("violent_crime");
                String murder = rs.getString("murder");
                String rape_revised = rs.getString("rape_revised");
                String rape_legacy = rs.getString("rape_legacy");
                String robbery = rs.getString("robbery");
                String aggravated_assault = rs.getString("aggravated_assault");
                String property_crime = rs.getString("property_crime");
                String burglary = rs.getString("burglary");
                String larceny_theft = rs.getString("larceny_theft");
                String motor_vehicle_theft = rs.getString("motor_vehicle_theft");
                String arson = rs.getString("arson");
                String state = rs.getString("state");

                crimeData.setCountyName(county);
                crimeData.setViolentCrime(parseInt(violent_crime));
                crimeData.setPropertyCrime(parseInt(property_crime));
                crimeData.setStateId(state);

            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            crimeData = new CountyCrimeData();
        }
        closeDB(conn);
        return crimeData;
    }

    //select * from cities1 where county_fips like '02185'
    public static CountyDataGeneral readCountyDataFromFips(String fips){
        Connection conn = connectToDB("cityData.db");
        String queryString = "";
        CountyDataGeneral countyData = new CountyDataGeneral();
        try {
            Statement stmt = conn.createStatement();

            queryString = String.format("select * from cities1 where county_fips like '%s' ",fips);
            ResultSet rs = stmt.executeQuery(queryString);
            while (rs.next()) {
                String stateId = rs.getString("state_id");
                String stateName = rs.getString("state_name");
                String county = rs.getString("county");
                String population = rs.getString("population_proper");
                int popultationInt = parseInt(population);

                countyData.setStateId(stateId);
                countyData.setStateName(stateName);
                countyData.setCountyName(county);
                countyData.addPopulation(popultationInt);

            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        closeDB(conn);
        return countyData;
    }

    public static int parseInt(String input){
        int result;
        try {
            if(input.equalsIgnoreCase("") || input == null){
                result = 0;
            }else{
                result = Integer.parseInt(input);
            }
        } catch (NumberFormatException e) {
            result = 0;
        }
        return result;
    }


    public static void closeDB(Connection conn){
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection connectToDB(String databaseName){
        try {
            String connectString = "jdbc:sqlite:" + databaseName;
            Connection conn = DriverManager.getConnection(connectString);
            if(conn == null) {
                conn = null;
            }
            return conn;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("It did not open");
            return null;
        }
    }

}
