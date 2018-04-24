package MarschelFinal;

public class CountyCrimeData {
    public String countyName;
    public int violentCrime;
    public int propertyCrime;
    public int totalCrime;
    public String stateId;
    public String stateFull;


    public CountyCrimeData(){
        this.countyName="";
        this.violentCrime=0;
        this.propertyCrime=0;
        this.totalCrime=0;
        this.stateId="";
    }


    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public int getViolentCrime() {
        return violentCrime;
    }

    public void setViolentCrime(int violentCrime) {
        this.violentCrime = violentCrime;
        calTotalCrime();
    }

    public int getPropertyCrime() {
        return propertyCrime;
    }

    public void setPropertyCrime(int propertyCrime) {
        this.propertyCrime = propertyCrime;
        calTotalCrime();
    }

    public int getTotalCrime() {
        return totalCrime;
    }

    public void calTotalCrime() {
        this.totalCrime = this.violentCrime+this.propertyCrime;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getStateFull() {
        return stateFull;
    }

    public void setStateFull(String stateFull) {
        this.stateFull = stateFull;
    }
}
