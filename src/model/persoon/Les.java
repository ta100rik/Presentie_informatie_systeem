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
        Map<Integer, Boolean> statLst = new HashMap<>();
        for(Map.Entry entry: presLst.entrySet()){
            statLst.put(Integer.parseInt(entry.getKey().toString()), !Boolean.parseBoolean(entry.getValue().toString()));
        }
        setStatusLijst(statLst);
//        System.out.println(presentieLijst);
//        System.out.println(statusLijst);
    }

    public void setStatusLijst(Map<Integer, Boolean> statLst){
        this.statusLijst = statLst;
    }
    public boolean getPresentie(int studentnr){
        return presentieLijst.get(studentnr);
    }
    public boolean getStatus(int stundentnr){
        return statusLijst.get(stundentnr);
    }

    public JsonObject getPresentieLijst(){
        JsonObjectBuilder presentielijst = Json.createObjectBuilder();
        for(Map.Entry entry: presentieLijst.entrySet()){
            presentielijst.add(entry.getKey().toString(), Boolean.parseBoolean(entry.getValue().toString()));
        }
        return presentielijst.build();
    }

    public JsonObject getStatusJson(){
        JsonObjectBuilder statuslijst = Json.createObjectBuilder();
        for(Map.Entry entry: statusLijst.entrySet()){
//            System.out.println(entry.getKey()+":"+entry.getValue());
            statuslijst.add(entry.getKey().toString(), Boolean.parseBoolean(entry.getValue().toString()));
        }
        return statuslijst.build();
    }

    public Map<Integer, Boolean> getPresentieMap(){
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



        for(Map.Entry presentie : getPresentieLijst().entrySet()) {
            boolean aanwezig = Boolean.parseBoolean(presentie.getValue().toString());
            String studentnr =  presentie.getKey().toString();
//            System.out.println(studentnr);
            boolean afgemeld = Boolean.parseBoolean(getStatusJson().get(studentnr).toString());
//            System.out.println("\n"+studentnr+"\n"+aanwezig+"\n"+afgemeld);
//            if (afgemeld) {
//                aanwezig = false;
//                afgemeld = true;
//            }
//            else {
//                aanwezig = true;
//                afgemeld = false;
//            }
//            System.out.println("\n"+studentnr+"\n"+aanwezig+"\n"+afgemeld);
            String studentnaam = studentnaamlijst.get(Integer.parseInt(studentnr));
            JsonObjectBuilder studentpresentie = Json.createObjectBuilder();
            studentpresentie
                    .add("studentnummer", studentnr)
                    .add("aanwezig", aanwezig)
                    .add("afgemeld", afgemeld)
                    .add("studentnaam", studentnaam);
            presentieLijst.add(String.format("%s", index), studentpresentie);
            index += 1;

//            System.out.println(studentnr + " aanwezig: " + aanwezig);
//            System.out.println(studentnr+ " afgemeld: " + afgemeld);


        }
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
