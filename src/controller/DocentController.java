package controller;

import model.PrIS;
import model.persoon.Les;
import model.persoon.Docent;
import server.Conversation;
import server.Handler;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class DocentController implements Handler {
    private PrIS informatieSysteem;

    /**
     * De LoginController klasse moet alle algemene aanvragen afhandelen. Methode
     * handle() kijkt welke URI is opgevraagd en laat dan de juiste methode het werk
     * doen. Je kunt voor elke nieuwe URI een nieuwe methode schrijven.
     *
     * @param infoSys - het toegangspunt tot het domeinmodel
     */

    public DocentController(PrIS infoSys) {
        informatieSysteem = infoSys;
    }

    public void handle(Conversation conversation) {
        if (conversation.getRequestedURI().startsWith("/docent/ophalen")) {
            returnDocent(conversation);
        }
    }
    public void returnDocent(Conversation conversation){
        JsonObject JsonObjIn = (JsonObject) conversation.getRequestBodyAsJSON();
//        String userName = JsonObjIn.getString("userName");
        String userName = "alex.jongman@hu.nl";
        JsonObjectBuilder docentbuilder = Json.createObjectBuilder();
        JsonObjectBuilder rooster = Json.createObjectBuilder();
        for(Docent s: informatieSysteem.getDeDocenten()){
            if(userName.contains(s.getGebruikersnaam())){
                int lesindex = 0;
                for(Les l: s.getRooster()){
                    rooster.add(String.format("%s",lesindex), l.returnAsJson());
                    lesindex+=1;
                }
                docentbuilder
                        .add("docentID", s.getDocentNummer())
                        .add("gebruikersnaam",s.getGebruikersnaam())
                        .add("voornaam", s.getVoornaam())
                        .add("achternaam", s.getVolledigeAchternaam())
                        .add("rooster", rooster);
            }
        }
        String messageOut = docentbuilder.build().toString();

        conversation.sendJSONMessage(messageOut);

    }
}
