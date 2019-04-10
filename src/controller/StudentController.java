package controller;

import model.PrIS;
import model.persoon.Les;
import model.persoon.Student;
import server.Conversation;
import server.Handler;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
        }
    }
    public void returnStudent(Conversation conversation){
        JsonObject JsonObjIn = (JsonObject) conversation.getRequestBodyAsJSON();
        String userName = JsonObjIn.getString("userName");
//        String userName = "zyad.osseyran@student.hu.nl";
        JsonObjectBuilder studentbuilder = Json.createObjectBuilder();
        JsonObjectBuilder rooster = Json.createObjectBuilder();
        JsonObjectBuilder aanwezigheid = Json.createObjectBuilder();
        for(Student s: informatieSysteem.getDeStudenten()){
            if(userName.contains(s.getGebruikersnaam())) {
                int lesindex = 0;
                for (Les l : s.getRooster()) {
                    JsonObjectBuilder lesJson = Json.createObjectBuilder();
                    lesJson.add("les", l.returnAsJson()).add("aanwezig", l.getPresentieLijst().get(s.getStudentNummer()));
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
        String userName = JsonObjIn.getString("userName");
        Date huidigeDatum = new Date();
        SimpleDateFormat format =  new SimpleDateFormat("yyyy/MM/dd hh:mm");
        try{
            huidigeDatum = format.parse(JsonObjIn.getString("datum"));
        }catch (ParseException e){
            conversation.sendJSONMessage(e.toString());
            huidigeDatum = new Date("2019/02/20 16:00");
        }
//        String userName = "zyad.osseyran@student.hu.nl";
        Map<String, Integer> aanwezigheid = new HashMap<>();
        Map<String, Integer> totaal = new HashMap<>();
        JsonObjectBuilder returnObject = Json.createObjectBuilder();

        for(Student s: informatieSysteem.getDeStudenten()){
            if(s.getGebruikersnaam().contains(userName)) {
                for (Les l : s.getRooster()) {
                    if (l.getStartdatum().before(huidigeDatum)) {
                        System.out.println(l.getLesCode() + l.getStartdatum());
                        System.out.println(l.getStudentAanwezigheid(s.getStudentNummer()));
                        boolean aanwezig = l.getStudentAanwezigheid(s.getStudentNummer());
                        int aanwezigheidcounter = 0;
                        int totaalcounter = 0;
                        if (aanwezigheid.containsKey(l.getLesCode())) {
                            aanwezigheidcounter = aanwezigheid.get(l.getLesCode());
                            totaalcounter = totaal.get(l.getLesCode());
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
            String lescode = entry.getKey().toString();
            System.out.println(lescode);
            int aanwezig = aanwezigheid.get(entry.getKey().toString());
            int totaalaantal = Integer.parseInt(entry.getValue().toString());
            System.out.println(aanwezig+"||");
            System.out.println(totaalaantal);
            if(!lescode.contains("TOETS")) {
                returnObject.add(lescode, aanwezig / totaalaantal * 100);
            }
        }

        String messageOut = returnObject.build().toString();
        conversation.sendJSONMessage(messageOut);
    }

    public void setAfgemeld(Conversation conversation){
        JsonObject JsonObjIn = (JsonObject) conversation.getRequestBodyAsJSON();
        String userName = JsonObjIn.getString("userName");
        boolean beschikbaar = JsonObjIn.getBoolean("beschikbaar");
        int lesID = JsonObjIn.getInt("lesID");
        boolean gelukt = false;
        for(Student s: informatieSysteem.getDeStudenten()){
            if(s.getGebruikersnaam().contains(userName)){
                s.setBeschikbaarheid(lesID, beschikbaar);
                gelukt = true;
                break;
            }
        }
        if(gelukt){
            conversation.sendJSONMessage("afgemeld");
        }else {
            conversation.sendJSONMessage("afmelden niet gelukt");
        }
    }
}
