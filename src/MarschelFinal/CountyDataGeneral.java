package MarschelFinal;

public class CountyDataGeneral {
    public String stateId;
    public String stateName;
    public String fips;
    public String countyName;
    public int population=0;

    public CountyDataGeneral(){
        this.stateId ="";
        this.stateName = "";
        this.fips = "";
        this.countyName = "";
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getFips() {
        return fips;
    }

    public void setFips(String fips) {
        this.fips = fips;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }
}
