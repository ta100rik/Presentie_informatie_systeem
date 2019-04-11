package controller;

import model.PrIS;
import model.persoon.Les;
import model.persoon.Student;
import server.Conversation;
import server.Handler;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StudentController implements Handler {
    private PrIS informatieSysteem;

    /**
     * De LoginController klasse moet alle algemene aanvragen afhandelen. Methode
     * handle() kijkt welke URI is opgevraagd en laat dan de juiste methode het werk
     * doen. Je kunt voor elke nieuwe URI een nieuwe methode schrijven.
     *
     * @param infoSys - het toegangspunt tot het domeinmodel
     */
    public StudentController(PrIS infoSys) {
        informatieSysteem = infoSys;
    }

    public void handle(Conversation conversation) {
        if (conversation.getRequestedURI().startsWith("/student/ophalen")) {
            returnStudent(conversation);
        } else if(conversation.getRequestedURI().startsWith("/student/presentie/ophalen")){
            returnStudentAanwezigheid(conversation);
        } else if(conversation.getRequestedURI().startsWith("/student/presentie/setafmelden")){
            setAfgemeld(conversation);
        } else if(conversation.getRequestedURI().startsWith("/student/setwachtwoord")){
            setWachtwoord(conversation);
        }
    }

    public void setWachtwoord(Conversation conversation){
        JsonObject JsonObjIn = (JsonObject) conversation.getRequestBodyAsJSON();
        String huidigWachtwoord ="";
        String nieuwWachtwoord ="";
        String gebruikersnaam = "zyad.osseyran@student.hu.nl";

        try{
            huidigWachtwoord = JsonObjIn.getString("currPass");
            nieuwWachtwoord = JsonObjIn.getString("newPass");
            gebruikersnaam = JsonObjIn.getString("userName");
        } catch (NullPointerException e){
            System.out.println(e);
        }


        for(Student s: informatieSysteem.getDeStudenten()){
            if(s.getGebruikersnaam().contains(gebruikersnaam)){
                if(huidigWachtwoord.contentEquals(s.getWachtwoord())){
                    s.setWachtwoord(nieuwWachtwoord);
                    conversation.sendJSONMessage("wachtwoord gewijzigd");
                }
                else{
                    conversation.sendJSONMessage("wachtwoord komt niet overeen!");
                }
            }
            else{
                conversation.sendJSONMessage("gebruiker bestaat niet!");
            }
        }
    }

    public void returnStudent(Conversation conversation){
        JsonObject JsonObjIn = (JsonObject) conversation.getRequestBodyAsJSON();
        String userName = "zyad.osseyran@student.hu.nl";
        try{
            userName = JsonObjIn.getString("userName");
        } catch (NullPointerException e){
            System.out.println(e);
        }
        JsonObjectBuilder studentbuilder = Json.createObjectBuilder();
        JsonObjectBuilder rooster = Json.createObjectBuilder();
        JsonObjectBuilder aanwezigheid = Json.createObjectBuilder();
        for(Student s: informatieSysteem.getDeStudenten()){
            if(userName.contains(s.getGebruikersnaam())) {
                int lesindex = 0;
                for (Les l : s.getRooster()) {
                    JsonObjectBuilder lesJson = Json.createObjectBuilder();
                    lesJson.add("les", l.returnAsJson()).add("aanwezig", l.getPresentieMap().get(s.getStudentNummer()));
                    rooster.add(String.format("%s", lesindex), lesJson);
                    lesindex += 1;
                }
                studentbuilder
                        .add("studentID", s.getStudentNummer())
                        .add("gebruikersnaam",s.getGebruikersnaam())
                        .add("voornaam", s.getVoornaam())
                        .add("achternaam", s.getVolledigeAchternaam())
                        .add("rooster", rooster);
            }

        }
        String messageOut = studentbuilder.build().toString();
        conversation.sendJSONMessage(messageOut);
    }
    public void returnStudentAanwezigheid(Conversation conversation){
        JsonObject JsonObjIn = (JsonObject) conversation.getRequestBodyAsJSON();
        System.out.println(JsonObjIn.toString());
        String userName = "zyad.osseyran@student.hu.nl";
        Date huidigeDatum = new Date("2019/02/20");
        SimpleDateFormat getformat =  new SimpleDateFormat("yyyy-MM-dd");
        try{
            huidigeDatum = getformat.parse(JsonObjIn.getString("datum"));
            userName = JsonObjIn.getString("userName");
            System.out.println(huidigeDatum);
        }catch (ParseException e){
            System.out.println(e);
        }catch (NullPointerException e){
            System.out.println(e);
        }
        Map<String, Integer> aanwezigheid = new HashMap<>();
        Map<String, Integer> totaal = new HashMap<>();
        JsonObjectBuilder returnObject = Json.createObjectBuilder();

        for(Student s: informatieSysteem.getDeStudenten()){
            if(s.getGebruikersnaam().contains(userName)) {
                for (Les l : s.getRooster()) {
                    int aanwezigheidcounter = 0;
                    int totaalcounter = 0;
                    if (l.getStartdatum().before(huidigeDatum)) {
                        boolean aanwezig = l.getStudentAanwezigheid(s.getStudentNummer());
                        if (aanwezigheid.containsKey(l.getLesCode())) {
                            aanwezigheidcounter = aanwezigheid.get(l.getLesCode());
                            totaalcounter = totaal.get(l.getLesCode());
//                            System.out.println(l.getLesCode()+"\n"+totaalcounter+"\n"+aanwezigheidcounter);
                        }
                        if (aanwezig) {
                            aanwezigheidcounter += 1;
                        }
                        totaalcounter += 1;
                        totaal.put(l.getLesCode(), totaalcounter);
                        aanwezigheid.put(l.getLesCode(), aanwezigheidcounter);
                    }
                }
            }
        }
        for(Map.Entry entry: totaal.entrySet()){
            DecimalFormat df2 = new DecimalFormat(".##");
            String lescode = entry.getKey().toString();
            double aanwezig = Double.parseDouble(aanwezigheid.get(entry.getKey().toString()).toString());
            double totaalaantal = Double.parseDouble(entry.getValue().toString());
            double gemiddelde = aanwezig/totaalaantal*100;
//            System.out.println(lescode+": "+gemiddelde);
            if(!lescode.contains("TOETS")) {
                returnObject.add(lescode, df2.format(gemiddelde));
            }
        }

        String messageOut = returnObject.build().toString();
        conversation.sendJSONMessage(messageOut);
    }

    public void setAfgemeld(Conversation conversation){
        JsonObject JsonObjIn = (JsonObject) conversation.getRequestBodyAsJSON();
        int studentNr = 1748635;
        boolean beschikbaar = true;
        int lesID = 10;
        boolean works = false;

        try{
            studentNr = JsonObjIn.getInt("studentnr");
            beschikbaar = JsonObjIn.getBoolean("beschikbaar");
            lesID = JsonObjIn.getInt("lesID");
            works = true;
        } catch (NullPointerException e){
            System.out.println(e);
        }

        boolean gelukt = false;
        if(works) {
            System.out.println("works");
            for (Student s : informatieSysteem.getDeStudenten()) {
                if (s.getStudentNummer() == studentNr) {
                    s.setBeschikbaarheid(lesID, beschikbaar);
                    for(Les l: informatieSysteem.getDeLessen()){
                        if(l.getLesID() == lesID){
                            System.out.println(beschikbaar);
                            l.changeStatusLijst(s.getStudentNummer(), beschikbaar);
                            l.changePresentieLijst(s.getStudentNummer(), !beschikbaar);
                            System.out.println(l.getStudentAanwezigheid(s.getStudentNummer()));
                        }
                    }
                    //System.out.println(s.getBeschikbaarheid().toString());
                    gelukt = true;
                    break;
                }
            }
        }
        if(gelukt){
            conversation.sendJSONMessage("afgemeld");
        }else {
            conversation.sendJSONMessage("afmelden niet gelukt");
        }
    }
}
