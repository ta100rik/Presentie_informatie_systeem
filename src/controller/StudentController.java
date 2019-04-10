package controller;

import model.PrIS;
import model.persoon.Les;
import model.persoon.Student;
import server.Conversation;
import server.Handler;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

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
}
