package controller;

import model.PrIS;
import model.persoon.Docent;
import model.persoon.Les;
import model.persoon.Student;
import server.Conversation;
import server.Handler;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LesController implements Handler {
    private PrIS informatieSysteem;

    /**
     * De LoginController klasse moet alle algemene aanvragen afhandelen. Methode
     * handle() kijkt welke URI is opgevraagd en laat dan de juiste methode het werk
     * doen. Je kunt voor elke nieuwe URI een nieuwe methode schrijven.
     *
     * @param infoSys - het toegangspunt tot het domeinmodel
     */
    public LesController(PrIS infoSys) {
        informatieSysteem = infoSys;
    }

    public void handle(Conversation conversation) {
        if (conversation.getRequestedURI().startsWith("/les/ophalen")) {
            returnLes(conversation);
        } else if (conversation.getRequestedURI().startsWith("/les/rooster/ophalen")){
            returnRooster(conversation);
        }else if (conversation.getRequestedURI().startsWith("/les/gemiddeldepresentie/ophalen")){
            returnGemiddeldePresentie(conversation);
        }else if (conversation.getRequestedURI().startsWith("/les/setPresentie")){
            setLesPresentie(conversation);
        }
    }

    /**
     * Deze methode haalt eerst de opgestuurde JSON-data op. Daarna worden de
     * benodigde gegevens uit het domeinmodel gehaald. Deze gegevens worden dan weer
     * omgezet naar JSON en teruggestuurd naar de Polymer-GUI!
     *
     * @param conversation - alle informatie over het request
     */


    /**
     * Json Structure
     * {
     * "Lescode":stringLesCode
     * "Docenten": ""docent1", "docent2"..."
     * "Klassen":""klas1", "klas2"..."
     * "StartDatum": stringDatum
     * "EindDatum": stringDatum
     * "presentieLijst": {studentnr:boolean, studentnr2:boolean, ...}
     * }
     *
     * @param conversation
     */
    private void returnLes(Conversation conversation) {
        JsonObject lJsonObjIn = (JsonObject) conversation.getRequestBodyAsJSON();
        int lesID;
        try{
             lesID=lJsonObjIn.getInt("lesID");                      //Should be integer
        }catch (NullPointerException e){
            System.out.println(e);
            lesID = 10;
        }
        JsonObjectBuilder sJsonObjectBuilder = Json.createObjectBuilder();
        //voeg alle attributen toe
        for(Les l : informatieSysteem.getDeLessen()){
            if(lesID == l.getLesID()) {
                sJsonObjectBuilder.add("les", l.returnAsJson());
            }
        }
        String lJsonOut = sJsonObjectBuilder.build().toString();
        conversation.sendJSONMessage(lJsonOut); // terugsturen naar de Polymer-GUI!
    }
    //requites integer lesID and JsonObj presentie
    private void setLesPresentie(Conversation conversation){
        JsonObject lJsonObjIn = (JsonObject) conversation.getRequestBodyAsJSON();
        //set dummydata
        Integer lesID = 10;
        boolean gelukt = false;
        JsonObjectBuilder presentinit = Json.createObjectBuilder();
        JsonObject presentie = presentinit.build();
        for(Les l:informatieSysteem.getDeLessen()){
            if(l.getLesID() == lesID){
                presentie = l.getPresentieLijst();
            }
        }
        //get real data
        try {
            lesID = lJsonObjIn.getInt("lesID");
            presentie = lJsonObjIn.getJsonObject("presentie");
        }catch (NullPointerException e){
            System.out.println(e);
        }
        Map<Integer, Boolean> presentiemap = new HashMap<Integer, Boolean>();
        Iterator<String> keyItr = presentie.keySet().iterator();
        while(keyItr.hasNext()){
            String key = keyItr.next();
            boolean value = presentie.getBoolean(key);
            presentiemap.put(Integer.parseInt(key), value);
        }
        for(Les l: informatieSysteem.getDeLessen()){
            if(lesID == l.getLesID()){
                l.setPresentieLijst(presentiemap);
                gelukt = true;
            }
        }
        String message = "false";
        if(gelukt){
            message = "true";
        }
        conversation.sendJSONMessage(message);

    }

    /**
     * {"rooster":{k
     *      "5":{
     *      "lesID":5,
     *      "lesCode":"\"TICT-V1OODC-15_2018\"",
     *      "lesNaam":"\"OO Design & Construction_Werkcollege\""
     *      ,"docenten":"\"jos.vanreenen@hu.nl\"; "
     *      ,"klassen":"\"TICT-SIE-V1E\"; "
     *      ,"startTijd":"2019-30-05 08:30"
     *      ,"eindTijd":"2019-30-05 11:30"},
     *
     *      15":{
     *      "lesID":15,
     *      "lesCode":"\"TICT-V1GP-18_2018\"",
     *      "lesNaam":"\"Group Project_Werkcollege\"",
     *      "docenten":"\"jos.vanreenen@hu.nl\"; ",
     *      "klassen":"\"TICT-SIE-V1D\"; ",
     *      "startTijd":"2019-00-06 01:00",
     *      "eindTijd":"2019-30-06 02:30"},
     *
     *      etc.
     *
     *
     * @param conversation
     */

    private void returnRooster(Conversation conversation){
        JsonObject lJsonObjIn = (JsonObject) conversation.getRequestBodyAsJSON();
        String rol = "student";        //persoonsID maakt niet uit of docent of student
        String username = "zyad.osseyran@student.hu.nl";
        try {
            rol = lJsonObjIn.getString("rol");        //persoonsID maakt niet uit of docent of student
            username = lJsonObjIn.getString("username");
        } catch (NullPointerException e){
            System.out.println(e);
        }

        JsonObjectBuilder roosterJsonBuilder = Json.createObjectBuilder();
        JsonObjectBuilder rObjectBuilder = Json.createObjectBuilder();
        int index = 0;
        if (rol.contains("docent")){
            for(Docent d: informatieSysteem.getDeDocenten()){
                if(d.getGebruikersnaam().contains(username)){
                    for(Les l: d.getRooster()){
                        roosterJsonBuilder.add(String.format("%s",index), l.returnAsJson());
                        index+=1;
                    }
                    rObjectBuilder.add("rooster", roosterJsonBuilder);
                }
            }
        }else{
            for(Student s: informatieSysteem.getDeStudenten()){
                if(s.getGebruikersnaam().contains(username)){
                    for(Les l: s.getRooster()){
                        roosterJsonBuilder.add(String.format("%s",index), l.returnAsJson());
                        index+=1;
                    }
                    rObjectBuilder.add("rooster", roosterJsonBuilder);
                }
            }
        }
        String lJsonOut = rObjectBuilder.build().toString();

        conversation.sendJSONMessage(lJsonOut); // terugsturen naar de Polymer-GUI!
    }

    /**
     *  {"gemiddeldeAanwezigheid":{
     *      "studentnr1":{
     *          "volledige naam student": aanwezigheidpercentage(0.35)
     *          }
     *      "studentnr2":{
     *         "volledige naam student": aanwezigheidpercentage(1.0)
     *          }
     *      }
     *  }
     * @param conversation
     */
    private void returnGemiddeldePresentie(Conversation conversation) {
        JsonObject lJsonObjIn = (JsonObject) conversation.getRequestBodyAsJSON();
        String lesCode = "TCIF-V1AUI-17_2018";
        String klasCode = "TICT-SIE-V1A";
        Date huidigeDatum = new Date("2019/02/20");

        SimpleDateFormat requiredformat =  new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat getformat =  new SimpleDateFormat("dd/MM/yyyy");
        try{
            huidigeDatum = getformat.parse(lJsonObjIn.getString("datum"));
            String datumstring = String.format("%s/%s/%s", huidigeDatum.getYear(), huidigeDatum.getMonth(), huidigeDatum.getDay());
            huidigeDatum = requiredformat.parse(datumstring);
            lesCode = lJsonObjIn.getString("lesCode");
            klasCode = lJsonObjIn.getString("klasCode");
        }catch (NullPointerException e){
            System.out.println(e);
        }
        catch (ParseException e){
            System.out.println(e);
        }
        JsonObjectBuilder sJsonObjectBuilder = Json.createObjectBuilder();
        JsonObjectBuilder nr = Json.createObjectBuilder();
        Map<Integer, JsonObject> vakPresentieNr = informatieSysteem.getVakPresentiNr(lesCode, klasCode, huidigeDatum);
        int index = 0;
        for (Map.Entry nrpres : vakPresentieNr.entrySet()) {
            int studentnr = Integer.parseInt(nrpres.getKey().toString());
            JsonObject vakPresentieNaam = informatieSysteem.vakPresentieNaam(vakPresentieNr, studentnr);
            nr.add(String.format("%s", index), vakPresentieNaam);
            index+=1;
        }
        sJsonObjectBuilder.add("gemiddeldeAanwezigheid", nr);

        String lJsonOut = sJsonObjectBuilder.build().toString();

        conversation.sendJSONMessage(lJsonOut); // terugsturen naar de Polymer-GUI!
    }
}
