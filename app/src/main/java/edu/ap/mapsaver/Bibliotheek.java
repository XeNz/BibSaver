package edu.ap.mapsaver;

public class Bibliotheek {
    private String naam;
    private Double longitude;
    private Double latidude;

    public Bibliotheek(String naam, Double longitude, Double latidude) {
        this.naam = naam;
        this.longitude = longitude;
        this.latidude = latidude;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatidude() {
        return latidude;
    }

    public void setLatidude(Double latidude) {
        this.latidude = latidude;
    }

}
