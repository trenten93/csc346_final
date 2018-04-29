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
    public static CountyCrimeData maxCrime = new CountyCrimeData();
    public static double maxCrimeRate = Double.MIN_VALUE;

    public static CountyCrimeData maxPropertyCrime = new CountyCrimeData();
    public static double maxPropertyCrimeRate = Double.MIN_VALUE;

    public MakeCrime(){
        //
    }


    public static void makeMap() throws Exception{
        populateColorWheel();

//        try {
//            File fileTest = new File("outputMap.svg");
//            if(fileTest.exists()){
//                return;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        String originalSvgSource = readFileSource("usCountiesOriginal.svg");
        Document originalSvg = Jsoup.parse(originalSvgSource,"", Parser.xmlParser());

        File fileOut = new File("outputMap.svg");// for violent crime
        PrintWriter printOutput = new PrintWriter(fileOut);

        File propertyCrimeMap = new File("propertyCrimeMap.svg");// for property crime
        PrintWriter propertyCrimeOut = new PrintWriter(propertyCrimeMap);

        printOutput.println(getDocHead("usCountiesOriginal.svg"));
        propertyCrimeOut.println(getDocHead("usCountiesOriginal.svg"));

        Elements svgAll = originalSvg.select("svg");
        Elements svgAllProperty = originalSvg.select("svg");// for property crime

        Elements gTag = svgAll.select("g");
        String defaultStyle = gTag.get(0).attr("style");

        Elements gTagP = svgAllProperty.select("g");
        String defaultStyleP = gTagP.get(0).attr("style");

        Elements paths = svgAll.select("path");
        for(Element path: paths){// this adds a style attribute to all path elements that are counties
            if(!path.attr("id").equalsIgnoreCase("State_Lines") && !path.attr("id").equalsIgnoreCase("separator")){
                path.attr("style",defaultStyle);
            }
        }
        Elements pathsP = svgAllProperty.select("path");// setting style for property crime map
        for(Element path:pathsP){
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
                    path.attr("style",formatStyle("#FFFFFF"));
                    System.out.println("null county name");
                }else{
                    String colorCode = findColorForPopViolent(countyData,countyCrime,fips);
                    path.attr("style",formatStyle(colorCode));
                }
            }
        }

        for(Element path: pathsP){
            if(!path.attr("id").equalsIgnoreCase("State_Lines") && !path.attr("id").equalsIgnoreCase("separator")){
                String fips = path.attr("id");
                CountyDataGeneral countyData = readCountyDataFromFips(fips);
                CountyCrimeData countyCrime = readCountyCrimeData(countyData.getCountyName(),countyData.getStateId());

                if(countyCrime.getCountyName().equalsIgnoreCase("")){
                    path.attr("style",formatStyle("#FFFFFF"));
                }else{
                    String colorCode = findColorForPopPropertyCrime(countyData,countyCrime,fips);
                    path.attr("style",formatStyle(colorCode));
                }
            }
        }




        printOutput.print(svgAll);
        propertyCrimeOut.print(svgAllProperty);
        printOutput.close();
        propertyCrimeOut.close();


        System.out.println("county with max PropertyCrime rate was County: "+maxPropertyCrime.getCountyName() +
                " state: "+maxPropertyCrime.getStateId()+" crime: "+maxPropertyCrime.getViolentCrime()+" pop: "+maxPropertyCrime.getPopulation()+
        " CrimeRate: "+maxPropertyCrimeRate);


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

    public static String findColorForPopViolent(CountyDataGeneral countyData,CountyCrimeData countyCrime,String fips){// for violent crime
        // determines what color to use based on the ratio of total crime to population. //per 1,000 people
        String result = "";

        if(countyCrime.getViolentCrime() == 0 || countyData.getPopulation() ==0){
            result = colorWheel.get(0);
        }else{
            double crimeRate = ((double)countyCrime.getViolentCrime()/ (double)countyData.getPopulation())*1000; // crime per 1000 people
            double crimeRateRounded = Math.round(crimeRate);

            if(crimeRateRounded >= 36){
                result = colorWheel.get(9);
            }else if(crimeRateRounded >= 32){
                result = colorWheel.get(8);
            }else if(crimeRateRounded >= 28){
                result = colorWheel.get(7);
            }else if(crimeRateRounded >= 24){
                result = colorWheel.get(6);
            }else if(crimeRateRounded >= 20){
                result = colorWheel.get(5);
            }else if(crimeRateRounded >=16){
                result = colorWheel.get(4);
            }else if(crimeRateRounded >= 12){
                result = colorWheel.get(3);
            }else if(crimeRateRounded >= 8){
                result = colorWheel.get(2);
            }else if(crimeRateRounded >= 4){
                result = colorWheel.get(1);
            }else{
                result = colorWheel.get(0);
            }

        }
        return result;
    }

    public static String findColorForPopPropertyCrime(CountyDataGeneral countyData,CountyCrimeData countyCrime,String fips){// for property crime
        String result = "";

        double crimeRateTest = 0;
        if(countyData.getPopulation()!=0){
            double propertyCrimeRateTest = ((double)countyCrime.getPropertyCrime()/(double)countyData.getPopulation())*1000.0;
            crimeRateTest = Math.round(propertyCrimeRateTest);
        }

        if(crimeRateTest>maxPropertyCrimeRate){
            maxPropertyCrimeRate = crimeRateTest;
            maxPropertyCrime = countyCrime;
            maxPropertyCrime.setPopulation(countyData.getPopulation());
        }
        System.out.printf("County: %-30s State: %-4s PropCrime: %-8d pop: %-9d fips: %-6s PropCrimeRate: %-7.0f\n",
                countyCrime.getCountyName(),countyCrime.getStateId(),countyCrime.getPropertyCrime(),countyData.getPopulation(),fips,crimeRateTest);


        if(countyCrime.getPropertyCrime() == 0 || countyData.getPopulation() ==0){
            result = colorWheel.get(0);
        }else{
            double crimeRate = (double)countyCrime.getPropertyCrime()/ (double)countyData.getPopulation();
            double colorPicker = (crimeRate*1000)*0.01;
            int finalCodeToGet = (int) Math.round(colorPicker);
            if(finalCodeToGet > 9){
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
        String countyFixed = countyStateFull[0].replaceAll("'","''");
        Connection conn = connectToDB("mainData.db");
        try {
            Statement stmt = conn.createStatement();
            String queryString = String.format("Select * from totalCountyCrimeData where county like '%s' and state like '%s' ",countyFixed,cityState[1]);
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
            e.printStackTrace();
            data.setCountyName("null");
        }
        closeDB(conn);

        return data;

    }

    public static String[] getCountyStateFullFromCityState(String city,String state){
        String[] result = {"",""};
        Connection conn = connectToDB("mainData.db");

        try {
            Statement stmt = conn.createStatement();
            String cityFixed = city.replaceAll("'","''");
            String queryString = String.format("Select * from cities1 where city like '%s' and state_id like '%s' ",cityFixed,state);
            ResultSet rs = stmt.executeQuery(queryString);
            while (rs.next()) {
                result[0] = rs.getString("county");
                result[1] = rs.getString("state_name");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            result[0]="error";
            result[1]= "error";
            e.printStackTrace();
        }
        closeDB(conn);
        return result;
    }

    public static String[] getCityStateFromZip(String zip){
        String[] zipInfo = new String[2];
        Connection conn = connectToDB("mainData.db");
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
        Connection conn = connectToDB("mainData.db");
        CountyCrimeData crimeData = new CountyCrimeData();
        try {
            Statement stmt = conn.createStatement();
            String countyFixed = countyName.replaceAll("'","''");
            queryString = String.format("select * from totalCountyCrimeData where county like '%s' and state like '%s' ",countyFixed,stateAbb);
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
            e.printStackTrace();
        }
        closeDB(conn);
        return crimeData;
    }

    //select * from cities1 where county_fips like '02185'
    public static CountyDataGeneral readCountyDataFromFips(String fips){
        Connection conn = connectToDB("mainData.db");
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

                String replace = " \\(city\\)";
                String countyFixed = county.replaceAll(replace,"");


                countyData.setStateId(stateId);
                countyData.setStateName(stateName);
                countyData.setCountyName(countyFixed);

                int populationInt = getPopulationFromFips(fips);
                countyData.setPopulation(populationInt);

            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        closeDB(conn);
        return countyData;
    }

    public static int getPopulationFromFips(String fips){
        int population = 0;
        Connection conn = connectToDB("mainData.db");

        try {
            Statement stmt = conn.createStatement();
            String queryString = String.format("select * from population_2016 where fips like '%s' ",fips);
            ResultSet rs = stmt.executeQuery(queryString);

            while(rs.next()){
                String populationString = rs.getString("population");
                population = parseInt(populationString);
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        closeDB(conn);
        return population;
    }


    public static int parseInt(String input){
        int result;
        try {
            if(input.equalsIgnoreCase("") || input == null){
                result = 0;
            }else{
                result = Integer.parseInt(input);
            }
        } catch (Exception e) {
            result = 0;
            e.printStackTrace();
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
