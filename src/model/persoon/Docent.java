package model.persoon;

import model.klas.Klas;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Docent extends Persoon {
	private static int aantalDocenten = 0;
	private int docentNummer;
	private ArrayList<Klas> docentKlassen = new ArrayList<>();

	public Docent(String voornaam, String tussenvoegsel, String achternaam, String wachtwoord, String gebruikersnaam) {
		super(voornaam, tussenvoegsel, achternaam, wachtwoord, gebruikersnaam);
		aantalDocenten+=1;
		docentNummer = aantalDocenten;
	}


	public void addKlas(Klas k){
		docentKlassen.add(k);
	}

	public ArrayList<Klas> getDocentKlassen(){
		return docentKlassen;
	}

	public String getDocentKlassenString(){
		return docentKlassen.toString();
	}

	public int getDocentNummer() {
		return docentNummer;
	}

	public void setDocentNummer(int docentNummer) {
		this.docentNummer = docentNummer;
	}

}
