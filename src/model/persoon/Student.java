//checked
package model.persoon;

import java.util.*;

public class Student extends Persoon {

	private int studentNummer;
	private String groepId;
	private Map<Integer, Boolean> beschikbaarheid = new HashMap<>();

	public Student(String pVoornaam, String pTussenvoegsel, String pAchternaam, String pWachtwoord,
			String pGebruikersnaam, int sStudentNummer, String sGroepId) {
		super(pVoornaam, pTussenvoegsel, pAchternaam, pWachtwoord, pGebruikersnaam);
		this.studentNummer = sStudentNummer;
		this.groepId = sGroepId;
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj) && obj instanceof Student) {
			Student s = (Student) obj;
			return this.studentNummer == s.studentNummer;
		}
		else {
			return false;
		}
	}

	public String getGroepId() {
		return this.groepId;
	}

	public void setGroepId(String pGroepId) {
		this.groepId = pGroepId;
	}

	public void setBeschikbaarheid(Integer lesID, Boolean beschikbaar){
		beschikbaarheid.put(lesID, beschikbaar);
	}

	public boolean beschikbaar(int lesID){
		Iterator it = beschikbaarheid.entrySet().iterator();
		while(it.hasNext()){	//loop through map
			Map.Entry pair = (Map.Entry)it.next();
			//als de student de les heeft geef de beschikbaarheid terug
			if(lesID ==  Integer.parseInt(pair.getKey().toString())) {
				return Boolean.parseBoolean(pair.getValue().toString());
			}
			it.remove();
		}
		//geef false terug als de student de les niet heeft
		return false;
	}

	public Map<Integer, Boolean> getBeschikbaarheid(){
		return this.beschikbaarheid;
	}

	public int getStudentNummer() {
		return this.studentNummer;
	}

	public String toString(){
		return String.format("%s\n %s\n %s\n %s\n", getStudentNummer() ,getRooster(), getBeschikbaarheid(), getGroepId());
	}
}
