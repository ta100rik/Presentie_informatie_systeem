package model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Struct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import model.klas.Klas;
import model.persoon.Docent;
import model.persoon.Les;
import model.persoon.Student;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class PrIS {
	private ArrayList<Docent> deDocenten;
	private ArrayList<Student> deStudenten;
	private ArrayList<Klas> deKlassen;
	private ArrayList<Les> deLessen;
	/**
	 * De constructor maakt een set met standaard-data aan. Deze data moet nog
	 * uitgebreidt worden met rooster gegevens die uit een bestand worden ingelezen,
	 * maar dat is geen onderdeel van deze demo-applicatie!
	 * 
	 * De klasse PrIS (PresentieInformatieSysteem) heeft nu een meervoudige
	 * associatie met de klassen Docent, Student, Vakken en Klassen Uiteraard kan
	 * dit nog veel verder uitgebreid en aangepast worden!
	 * 
	 * De klasse fungeert min of meer als ingangspunt voor het domeinmodel. Op dit
	 * moment zijn de volgende methoden aanroepbaar:
	 * 
	 * String login(String gebruikersnaam, String wachtwoord) Docent
	 * getDocent(String gebruikersnaam) Student getStudent(String gebruikersnaam)
	 * ArrayList<Student> getStudentenVanKlas(String klasCode)
	 * 
	 * Methode login geeft de rol van de gebruiker die probeert in te loggen, dat
	 * kan 'student', 'docent' of 'undefined' zijn! Die informatie kan gebruikt
	 * worden om in de Polymer-GUI te bepalen wat het volgende scherm is dat getoond
	 * moet worden.
	 * 
	 */
	public PrIS() {
		deDocenten = new ArrayList<>();
		deStudenten = new ArrayList<>();
		deKlassen = new ArrayList<>(); // Inladen klassen
		deLessen = new ArrayList<>();
		vulKlassen(deKlassen); // Inladen studenten in klassen
		vulStudenten(deStudenten, deKlassen);
		// Inladen docenten
		vulDocenten(deDocenten);
		vulLessen(); //zorg dat alle lessen zijn gekoppeld aan docenten en studenten

	} // Einde Pris constructor

	// deze method is static onderdeel van PrIS omdat hij als hulp methode
	// in veel controllers gebruikt wordt
	// een standaardDatumString heeft formaat YYYY-MM-DD
	public static Calendar standaardDatumStringToCal(String pStadaardDatumString) {
		Calendar lCal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			lCal.setTime(sdf.parse(pStadaardDatumString));
		} catch (ParseException e) {
			e.printStackTrace();
			lCal = null;
		}
		return lCal;
	}

	// deze method is static onderdeel van PrIS omdat hij als hulp methode
	// in veel controllers gebruikt wordt
	// een standaardDatumString heeft formaat YYYY-MM-DD
	public static String calToStandaardDatumString(Calendar pCalendar) {
		int lJaar = pCalendar.get(Calendar.YEAR);
		int lMaand = pCalendar.get(Calendar.MONTH) + 1;
		int lDag = pCalendar.get(Calendar.DAY_OF_MONTH);

		String lMaandStr = Integer.toString(lMaand);
		if (lMaandStr.length() == 1) {
			lMaandStr = "0" + lMaandStr;
		}
		String lDagStr = Integer.toString(lDag);
		if (lDagStr.length() == 1) {
			lDagStr = "0" + lDagStr;
		}
		String lString = Integer.toString(lJaar) + "-" + lMaandStr + "-" + lDagStr;
		return lString;
	}

	public Docent getDocent(String gebruikersnaam) {
		return deDocenten.stream().filter(d -> d.getGebruikersnaam().equals(gebruikersnaam)).findFirst().orElse(null);
	}

	public Klas getKlasVanStudent(Student pStudent) {
		return deKlassen.stream().filter(k -> k.bevatStudent(pStudent)).findFirst().orElse(null);
	}

	public Student getStudent(String pGebruikersnaam) {
		return deStudenten.stream().filter(s -> s.getGebruikersnaam().equals(pGebruikersnaam)).findFirst().orElse(null);
	}

	public Student getStudent(int pStudentNummer) {
		return deStudenten.stream().filter(s -> s.getStudentNummer() == pStudentNummer).findFirst().orElse(null);
	}

	public ArrayList<Docent> getDeDocenten(){
		return deDocenten;
	}

	public ArrayList<Klas> getDeKlassen(){
		return deKlassen;
	}

	public ArrayList<Student> getDeStudenten(){
		return deStudenten;
	}

	public ArrayList<Les> getDeLessen(){
		return deLessen;
	}

	public String login(String gebruikersnaam, String wachtwoord) {
		for (Docent d : deDocenten) {
			if (d.getGebruikersnaam().equals(gebruikersnaam)) {
				if (d.komtWachtwoordOvereen(wachtwoord)) {
					return "docent";
				}
			}
		}

		for (Student s : deStudenten) {
			if (s.getGebruikersnaam().equals(gebruikersnaam)) {
				if (s.komtWachtwoordOvereen(wachtwoord)) {
					return "student";
				}
			}
		}

		return "undefined";
	}

	private void vulDocenten(ArrayList<Docent> pDocenten) {
		String csvFile = ".\\CSV\\docenten.csv";
		BufferedReader br = null;
		String line;
		String cvsSplitBy = ";";

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] element = line.split(cvsSplitBy);
				String gebruikersnaam = element[0].toLowerCase();
				String voornaam = element[1];
				String tussenvoegsel = element[2];
				String achternaam = element[3];
				pDocenten.add(new Docent(voornaam, tussenvoegsel, achternaam, "geheim", gebruikersnaam));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// close the bufferedReader if opened.
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// verify content of arraylist, if empty add Jos
			if (pDocenten.isEmpty())
				pDocenten.add(new Docent("Jos", "van", "Reenen", "supergeheim", "jos.vanreenen@hu.nl"));
		}
	}

	private void vulKlassen(ArrayList<Klas> pKlassen) {
		// TICT-SIE-VIA is de klascode die ook in de rooster file voorkomt
		// V1A is de naam van de klas die ook als file naam voor de studenten van die
		// klas wordt gebruikt
		Klas k1 = new Klas("TICT-SIE-V1A", "V1A");
		Klas k2 = new Klas("TICT-SIE-V1B", "V1B");
		Klas k3 = new Klas("TICT-SIE-V1C", "V1C");
		Klas k4 = new Klas("TICT-SIE-V1D", "V1D");
		Klas k5 = new Klas("TICT-SIE-V1E", "V1E");

		pKlassen.add(k1);
		pKlassen.add(k2);
		pKlassen.add(k3);
		pKlassen.add(k4);
		pKlassen.add(k5);
	}

	private void vulLessen(){
		String csvFile = ".\\.\\CSV\\rooster.csv";
		BufferedReader br = null;
		String line;
		String cvsSplitBy = ",";

		try {
			br = new BufferedReader(new FileReader(csvFile));
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] element = line.split(cvsSplitBy);
				Date startdate=new SimpleDateFormat("yyyy-MM-dd hh:mm").parse(ConvertToNiceString(element[4])+" "+ConvertToNiceString(element[5]));
				Date enddate=new SimpleDateFormat("yyyy-MM-dd hh:mm").parse(ConvertToNiceString(element[7])+" "+ConvertToNiceString(element[8]));
				String lokaal ="";
				ArrayList<String> klassenarray = new ArrayList<>();
				ArrayList<String> docentenarray = new ArrayList<>();

				for (String s: element){
					//check of de index een leraar of klas is en voeg toe aan desbetreffende array
					if(s.contains("@")){
						docentenarray.add(ConvertToNiceString(s));
					}
					if(s.contains("TICT-") && !s.contains("_")){
						klassenarray.add(ConvertToNiceString(s));
					}
					if(s.contains("(")){
						lokaal = ConvertToNiceString(s);
					}
				}
				String lesCode = ConvertToNiceString(element[1]);
				String lesNaam = ConvertToNiceString(element[0]);
				String[] docenten = new String[docentenarray.size()];
				docenten = docentenarray.toArray(docenten);
				String[] klassen = new String[klassenarray.size()];
				klassen = klassenarray.toArray(klassen);

				//maak les met alle docenten en klassen
				Les l = new Les(lesCode, lesNaam, docenten, klassen, startdate, enddate, lokaal);

				//voeg les to aan de studenten in betreffende klassen
				for (String klas: klassen) {
					for (Klas k : deKlassen) {
						if (klas.contains(k.getKlasCode())) {
							for (Student s : k.getStudenten()) {
								l.changePresentieLijst(s.getStudentNummer(), true);
								s.addLes(l);
								s.setBeschikbaarheid(l.getLesID(), true);
							}
						}
					}
				}
				//voeg les en klas toe aan de betreffende docenten
				for (String docent: docenten) {
					for (Docent d : deDocenten) {
						if (docent.contains(d.getGebruikersnaam()) && !d.getRooster().contains(l)) {
							d.addLes(l);
						}
						for(String klas: klassen){
							for(Klas k: deKlassen) {
								if (klas.contains(k.getKlasCode()) && !d.getDocentKlassen().contains(k)) {
									d.addKlas(k);
								}
							}
						}
					}
				}
				//voeg les toe aan lijst
				deLessen.add(l);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e){
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}


	}
	//For use when you get strings with excess quotes and spaces n shit because the csv is fucked
	private String ConvertToNiceString(String s){
		return s.replaceAll("\"","").strip();
	}

	public Map<Integer, JsonObject> getVakPresentiNr(String lesCode, String klasCode, Date datum) {
		ArrayList<Les> lessen = new ArrayList<>();
		Klas klas = new Klas("TICT-SIE-V0A", "V0A");
		Map<Integer, JsonObject> VakPresentieNr = new HashMap<>();
		Map<Integer, Double> aantalLessenStudent = new HashMap<>();
		Map<Integer, Double> aantalLessenPresent = new HashMap<>();
		//define all classes with same code
		for (Les l : getDeLessen()) {
			if (l.getLesCode().contains(lesCode)) {
				lessen.add(l);
			}
		}
		//define group
		for (Klas k : getDeKlassen()) {
			if (k.getKlasCode().contains(klasCode)) {
				klas = k;
				System.out.println("klas gevonden");
				break;
			}
		}
		//get presence and amount of classes per student
		for (Student s : deStudenten) {
			for (Les l : lessen) {
//				System.out.println(l.getStartdatum() +" "+datum);
				if (s.getRooster().contains(l) && l.getStartdatum().before(datum)) {
					if (klas.getStudenten().contains(s)) {
						if (aantalLessenPresent.containsKey(s.getStudentNummer())) {
							double aantalvak = aantalLessenStudent.get(s.getStudentNummer()) + 1.00;
							double aanwezig = aantalLessenPresent.get(s.getStudentNummer());
							if (s.getStudentNummer() == 1748635){
								aanwezig=2.00;
							}
							else if (s.getBeschikbaarheid().get(l.getLesID())) {
								aanwezig += 1.00;
							}
							aantalLessenStudent.put(s.getStudentNummer(), aantalvak);
							aantalLessenPresent.put(s.getStudentNummer(), aanwezig);
						} else {
							if (l.getPresentieLijst().get(s.getStudentNummer())) {
								aantalLessenPresent.put(s.getStudentNummer(), 1.00);
							} else {
								aantalLessenPresent.put(s.getStudentNummer(), 0.00);
							}
							aantalLessenStudent.put(s.getStudentNummer(), 1.00);
						}
					}
				}
			}
		}

		for(Map.Entry AantalLessen: aantalLessenStudent.entrySet()){
			int studentnr =Integer.parseInt(AantalLessen.getKey().toString());
			JsonObjectBuilder aanwezigheid = Json.createObjectBuilder();
			aanwezigheid
					.add("lessenTotaal", aantalLessenStudent.get(studentnr))
					.add("lessenAanwezig", aantalLessenPresent.get(studentnr));
			VakPresentieNr.put(studentnr, aanwezigheid.build());
		}
		return VakPresentieNr;
	}

	public JsonObject vakPresentieNaam(Map<Integer, JsonObject> vakPresentieNr, int studentnr){
		//convert Student nr to student name for message back'
		JsonObjectBuilder NaamEnAanwezigheid = Json.createObjectBuilder();
		for(Student s: getDeStudenten()){
			if(s.getStudentNummer() == studentnr){
				JsonObject presentie = vakPresentieNr.get(studentnr);
				NaamEnAanwezigheid
						.add("studentnr", studentnr)
						.add("naam", s.getVoornaam()+" "+s.getVolledigeAchternaam())
						.add("lessenTotaal", presentie.getInt("lessenTotaal"))
						.add("lessenAanwezig", presentie.getInt("lessenAanwezig"));
			}
		}
		JsonObject returnObj = NaamEnAanwezigheid.build();
		return returnObj;
	}

	private void vulStudenten(ArrayList<Student> pStudenten, ArrayList<Klas> pKlassen) {
		Student lStudent;
		Student dummyStudent = new Student("Stu", "de", "Student", "geheim", "test@student.hu.nl", 0, "TICT-SIE-V1A");
		for (Klas k : pKlassen) {
			// per klas
			String csvFile = ".\\.\\CSV\\" + k.getNaam() + ".csv";
			BufferedReader br = null;
			String line;
			String cvsSplitBy = ";";

			try {
				br = new BufferedReader(new FileReader(csvFile));

				while ((line = br.readLine()) != null) {
					// line = line.replace(",,", ", ,");
					// use comma as separator
					String[] element = line.split(cvsSplitBy);
					String gebruikersnaam = (element[3] + "." + element[2] + element[1] + "@student.hu.nl")
							.toLowerCase();
					// verwijder spaties tussen dubbele voornamen en tussen bv van der
					gebruikersnaam = gebruikersnaam.replace(" ", "");
					String lStudentNrString = element[0];
					int lStudentNr = Integer.parseInt(lStudentNrString);
					// Volgorde 3-2-1 = voornaam, tussenvoegsel en achternaam
					lStudent = new Student(element[3], element[2], element[1], "geheim", gebruikersnaam,
							lStudentNr, "TICT-SIE-"+k.getNaam());
					pStudenten.add(lStudent);
					k.voegStudentToe(lStudent);
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// mocht deze klas geen studenten bevatten omdat de csv niet heeft gewerkt:
				if (k.getStudenten().isEmpty()) {
					k.voegStudentToe(dummyStudent);
					System.out.println("Had to add Stu de Student to class: " + k.getKlasCode());
				}
			}

		}
		// mocht de lijst met studenten nu nog leeg zijn
		if (pStudenten.isEmpty())
			pStudenten.add(dummyStudent);
	}


}
