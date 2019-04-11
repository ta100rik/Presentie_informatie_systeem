package model.persoon;

import model.klas.Klas;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Les {
    private static int aantalLessen = 0;
    private int lesID;
    private String lokaal;
    private String lesCode;
    private String lesNaam;
    private String[] docenten;
    private String[] klassen;
    private Date startdatum;
    private Date einddatum;
    private ArrayList<Klas> klassenarray;
    private Map<Integer, Boolean>statusLijst = new HashMap<>();
    private Map<Integer, Boolean>presentieLijst= new HashMap<>();

    public Les(String lLesCode, String lLesNaam, String[] ldocentUsername,
               String[] lklasCode, ArrayList<Klas> klassenarray, Date lstartdatum,  Date leindatum, String llokaal){
        aantalLessen +=1;
        lesID= aantalLessen;
        lesCode = lLesCode;
        lesNaam = lLesNaam;
        docenten = ldocentUsername;
        klassen = lklasCode;
        this.klassenarray = klassenarray;
        startdatum = lstartdatum;
        einddatum = leindatum;
        lokaal = llokaal;
    }

    public void changePresentieLijst(Integer studentnr, boolean aanwezig){
        presentieLijst.put(studentnr, aanwezig);
    }

    public void changeStatusLijst(Integer studentnr, boolean afgemeld){
        statusLijst.put(studentnr, afgemeld);
    }

    public void setPresentieLijst(Map<Integer, Boolean> presLst){
        this.presentieLijst = presLst;
    }
    public Map<Integer, Boolean> getPresentieLijst(){
        return presentieLijst;
    }

    public String getLesCode(){
        return lesCode;
    }

    public int getLesID(){
        return lesID;
    }

    public String[] getDocenten(){
        return docenten;
    }

    public String[] getKlassen(){
        return klassen;
    }

    public Date getStartdatum(){
        return startdatum;
    }

    public Date getEinddatum(){
        return einddatum;
    }

    public boolean getStudentAanwezigheid(Integer studentNummer){
        return presentieLijst.get(studentNummer);
    }

    private Map<Integer, Boolean> getStatusLijst(){
        return statusLijst;
    }

    public JsonObject returnAsJson(){
        JsonObjectBuilder les = Json.createObjectBuilder();
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        String alledocenten ="";
        String alleklassen ="";
        Map<Integer, String> studentnaamlijst = new HashMap<>();
        for(String d: docenten){
            alledocenten+=d.strip()+"; ";
        }
        for(String k: klassen){
            alleklassen+=k.strip()+"; ";
        }

        for(Klas k: klassenarray){
//            System.out.println(k.getKlasCode());
            for(Student s: k.getStudenten()){
                statusLijst.put(s.getStudentNummer(),s.getBeschikbaarheid().get(this.getLesID()));
                studentnaamlijst.put(s.getStudentNummer(), s.getVoornaam()+" "+s.getVolledigeAchternaam());
            }
        }
        JsonObjectBuilder presentieLijst = Json.createObjectBuilder();
        int index = 0;
        boolean aanwezig;
        boolean afgemeld = false;
        String studentnr;
        for(Map.Entry presentie : getPresentieLijst().entrySet()){
            studentnr = presentie.getKey().toString();
            String studentnaam = studentnaamlijst.get(Integer.parseInt(studentnr));
            aanwezig = Boolean.parseBoolean(presentie.getValue().toString());
            for(Map.Entry afmelding: getStatusLijst().entrySet()){
                if(afmelding.getKey().toString().contains(studentnr)){
                    afgemeld = Boolean.parseBoolean(afmelding.getValue().toString());
                    break;
                }
            }
            JsonObjectBuilder studentpresentie = Json.createObjectBuilder();
            studentpresentie
                    .add("studentnummer", studentnr)
                    .add("aanwezig", aanwezig)
                    .add("afgemeld", afgemeld)
                    .add("studentnaam", studentnaam);
            presentieLijst.add(String.format("%s", index), studentpresentie);
            index+=1;
        }
//        JsonObjectBuilder afmeldLijst = Json.createObjectBuilder();
//        index = 0;
//        for(Map.Entry status: statusLijst.entrySet()){
//            String studentnr = status.getKey().toString();
//            boolean afgemeld = Boolean.parseBoolean(status.getValue().toString());
//            JsonObjectBuilder studentafmelding = Json.createObjectBuilder();
//            studentafmelding.add("studentnummer", studentnr).add("afgemeld", afgemeld);
//            afmeldLijst.add(String.format("%s", index), studentafmelding);
//            index+=1;
//         }
//        System.out.println(date.format(startdatum));
        les
                .add("lesID", lesID)
                .add("lesCode", lesCode)
                .add("lesNaam", lesNaam)
                .add("docenten", alledocenten)
                .add("klassen", alleklassen)
                .add("startTijd", date.format(startdatum))
                .add("eindTijd", date.format(einddatum))
                .add("lokaal", lokaal)
                .add("presentie", presentieLijst);

        return les.build();
    }

    public String toString(){

        StringBuilder s = new StringBuilder(String.format("%s; %s; %s; ", lesID, lesCode, lesNaam));
        for(String d: docenten){
            s.append(d+"; ");
        }
        for(String k: klassen){
            s.append(k+"; ");
        }
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        s.append(String.format("%s; %s; ", date.format(startdatum), date.format(einddatum)));
        /**Iterator it = presentieLijst.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            String studentaanw = String.format("%s; %s; ", pair.getKey(), pair.getValue());
            s.append(studentaanw);
            it.remove();
        }*/
        return s.toString();
    }

}
