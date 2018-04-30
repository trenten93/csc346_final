package MarschelFinal;


import javafx.scene.control.ProgressBar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import sun.security.provider.MD5;

import java.io.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

@SuppressWarnings("Duplicates")
public class MakeCrime {
    public static ArrayList<String> colorWheel = new ArrayList<String>();
    public MakeCrime(){
        //
    }


    public static void makeMap() throws Exception{
        populateColorWheel();
        File test = new File("violentCrimeMap.svg");
        File test2 = new File("propertyCrimeMap.svg");

        if(test.exists() && test2.exists()){ // to check that the maps exist and are the correct hash value. If not make them.
            String violentMd5 = "59162d9aecbb02e69269e98134dd3216";
            String propertyMd5 = "5bfbdadb8bb603247799e355bc605bfc";
            String violentMd5Generated = getMD5Checksum("violentCrimeMap.svg");
            String propertyMd5Generated = getMD5Checksum("propertyCrimeMap.svg");

            if(!violentMd5Generated.equals(violentMd5)){
                System.out.println("The hash value for the violent crime map was incorrect regenerating map! \n");
                makeViolentMap();
            }
            if(!propertyMd5Generated.equals(propertyMd5)){
                System.out.println("The hash value for the property crime map was incorrect regenerating map! \n");
                makePropertyMap();
            }
            if(violentMd5Generated.equals(violentMd5) && propertyMd5Generated.equals(propertyMd5)){
                return;
            }
        }else{
            if(!test.exists()){
                System.out.println("violent crime map was not in root source making map now!\n");
                makeViolentMap();
            }
            if(!test2.exists()){
                System.out.println("property crime map was not in root source making map now! \n");
                makePropertyMap();
            }
        }
    }


    public static void makeViolentMap() throws Exception{
        String originalSvgSource = readFileSource("usCountiesOriginal.svg");
        Document originalSvg = Jsoup.parse(originalSvgSource,"", Parser.xmlParser());

        File fileOut = new File("violentCrimeMap.svg");// for violent crime
        PrintWriter printOutput = new PrintWriter(fileOut);

        printOutput.println(getDocHead("usCountiesOriginal.svg"));

        Elements svgAll = originalSvg.select("svg");

        Elements gTag = svgAll.select("g");
        String defaultStyle = gTag.get(0).attr("style");


        Elements paths = svgAll.select("path");
        for(Element path: paths){// this adds a style attribute to all path elements that are counties
            if(!path.attr("id").equalsIgnoreCase("State_Lines") && !path.attr("id").equalsIgnoreCase("separator")){
                path.attr("style",defaultStyle);
            }
        }

        int i = 0;
        int progress = 0;
        for(Element path: paths){
            if(!path.attr("id").equalsIgnoreCase("State_Lines") && !path.attr("id").equalsIgnoreCase("separator")){
                String fips = path.attr("id");
                CountyDataGeneral countyData = readCountyDataFromFips(fips);
                CountyCrimeData countyCrime = readCountyCrimeData(countyData.getCountyName(),countyData.getStateId());

                if(countyCrime.getCountyName().equalsIgnoreCase("")){
                    path.attr("style",formatStyle("#FFFFFF"));
                }else{
                    String colorCode = findColorForViolent(countyData,countyCrime,fips);
                    path.attr("style",formatStyle(colorCode));
                }
                i++;
                if(i==32){
                    i=0;
                    progress = progress+1;
                    if(progress >= 98){
                        progress=100;
                    }
                    System.out.printf("progress in violent map: %d%%\n",progress);
                }
            }
        }
        printOutput.print(svgAll);
        printOutput.close();
    }


    public static void makePropertyMap() throws Exception{

        String originalSvgSourceForPropertyCrime = readFileSource("usCountiesOriginal.svg");
        Document originalSvgPropCrime = Jsoup.parse(originalSvgSourceForPropertyCrime,"",Parser.xmlParser());

        File propertyCrimeMap = new File("propertyCrimeMap.svg");// for property crime
        PrintWriter propertyCrimeOut = new PrintWriter(propertyCrimeMap);

        propertyCrimeOut.println(getDocHead("usCountiesOriginal.svg"));

        Elements svgAllProperty = originalSvgPropCrime.select("svg");// for property crime

        Elements gTagP = svgAllProperty.select("g");
        String defaultStyleP = gTagP.get(0).attr("style");

        Elements pathsP = svgAllProperty.select("path");// setting style for property crime map
        for(Element path:pathsP){
            if(!path.attr("id").equalsIgnoreCase("State_Lines") && !path.attr("id").equalsIgnoreCase("separator")){
                path.attr("style",defaultStyleP);
            }
        }


        int i=0;
        int progress = 0;
        for(Element path: pathsP){
            if(!path.attr("id").equalsIgnoreCase("State_Lines") && !path.attr("id").equalsIgnoreCase("separator")){
                String fips = path.attr("id");
                CountyDataGeneral countyData = readCountyDataFromFips(fips);
                CountyCrimeData countyCrime = readCountyCrimeData(countyData.getCountyName(),countyData.getStateId());

                if(countyCrime.getCountyName().equalsIgnoreCase("")){
                    path.attr("style",formatStyle("#FFFFFF"));
                }else{
                    String colorCode = findColorForPropertyCrime(countyData,countyCrime,fips);
                    path.attr("style",formatStyle(colorCode));
                }
                i++;
                if(i==32){
                    i=0;
                    progress = progress+1;
                    if(progress >= 98){
                        progress = 100;
                    }
                    System.out.printf("progress in Property map: %d%%",progress);
                }
            }
        }
        propertyCrimeOut.print(svgAllProperty);
        propertyCrimeOut.close();
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

    public static String findColorForViolent(CountyDataGeneral countyData,CountyCrimeData countyCrime,String fips){// for violent crime
        // determines what color to use based on the ratio of total crime to population. //per 1,000 people
        String result = "";

        if(countyCrime.getViolentCrime() == 0 || countyData.getPopulation() ==0){
            result = colorWheel.get(0);
        }else{
            double crimeRate = ((double)countyCrime.getViolentCrime()/ (double)countyData.getPopulation())*1000; // crime per 1000 people
            double crimeRateRounded = Math.round(crimeRate);

            if(crimeRateRounded >= 18){
                result = colorWheel.get(9);
            }else if(crimeRateRounded >= 16){
                result = colorWheel.get(8);
            }else if(crimeRateRounded >= 14){
                result = colorWheel.get(7);
            }else if(crimeRateRounded >= 12){
                result = colorWheel.get(6);
            }else if(crimeRateRounded >= 10){
                result = colorWheel.get(5);
            }else if(crimeRateRounded >=8){
                result = colorWheel.get(4);
            }else if(crimeRateRounded >= 6){
                result = colorWheel.get(3);
            }else if(crimeRateRounded >= 4){
                result = colorWheel.get(2);
            }else if(crimeRateRounded >= 2){
                result = colorWheel.get(1);
            }else{
                result = colorWheel.get(0);
            }

        }
        return result;
    }

    public static String findColorForPropertyCrime(CountyDataGeneral countyData,CountyCrimeData countyCrime,String fips){// for property crime
        String result = "";

        if(countyCrime.getPropertyCrime() == 0 || countyData.getPopulation() ==0){
            result = colorWheel.get(0);
        }else{
            double crimeRate = ((double)countyCrime.getPropertyCrime()/ (double)countyData.getPopulation())*1000;// property crime rate per 1000 people
            double crimeRateRounded = Math.round(crimeRate);

            if(crimeRateRounded >= 90){
                result = colorWheel.get(9);
            }else if(crimeRateRounded >= 80){
                result = colorWheel.get(8);
            }else if(crimeRateRounded >= 70){
                result = colorWheel.get(7);
            }else if(crimeRateRounded >= 60){
                result = colorWheel.get(6);
            }else if(crimeRateRounded >= 50){
                result = colorWheel.get(5);
            }else if(crimeRateRounded >= 40){
                result = colorWheel.get(4);
            }else if(crimeRateRounded >= 30){
                result = colorWheel.get(3);
            }else if(crimeRateRounded >= 20){
                result = colorWheel.get(2);
            }else if(crimeRateRounded >= 10){
                result = colorWheel.get(1);
            }else{
                result = colorWheel.get(0);
            }
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
                // I don't currently use many of these, But I'll leave them for future modification to support maps with arson rates and etc.
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


    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis =  new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

}
