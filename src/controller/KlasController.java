package controller;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import model.PrIS;
import model.persoon.Les;
import server.Conversation;
import server.Handler;

import model.klas.Klas;
import model.persoon.Student;

class KlasController implements Handler {
    private PrIS informatieSysteem;

    /**
     * De LoginController klasse moet alle algemene aanvragen afhandelen. Methode
     * handle() kijkt welke URI is opgevraagd en laat dan de juiste methode het werk
     * doen. Je kunt voor elke nieuwe URI een nieuwe methode schrijven.
     *
     * @param infoSys - het toegangspunt tot het domeinmodel
     */
    public KlasController(PrIS infoSys) {
        informatieSysteem = infoSys;
    }

    public void handle(Conversation conversation) {
        if (conversation.getRequestedURI().startsWith("/klas/ophalen")) {
            returnKlas(conversation);
        }
    }

    /**
     * Deze methode haalt eerst de opgestuurde JSON-data op. Daarna worden de
     * benodigde gegevens uit het domeinmodel gehaald. Deze gegevens worden dan weer
     * omgezet naar JSON en teruggestuurd naar de Polymer-GUI!
     *
     * @param conversation - alle informatie over het request
     */
    /**JSON-structure
    {
    "studentNr1":{
        "studentnaam":"nm",
        "aanwezigheid":{
             "lesIdNr1":boolean
             "lesIdNr2":boolean
        }
     }
     "studentNr2":{
        "studentnaam":"nm",
        "aanwezigheid":{
            "lesIdNr1":boolean
     "lesIdNr2":boolean
        }
     }
      etc.
    }
    */
    private void returnKlas(Conversation conversation) {
        JsonObject lJsonObjIn = (JsonObject) conversation.getRequestBodyAsJSON();

//      String lKlasCode = "TICT-SIE-V1D";
        String lKlasCode = lJsonObjIn.getString("klasCode");                      //Should have "TICT-SIE-V1D"-format
        JsonObjectBuilder aJsonObjectBuilder = Json.createObjectBuilder();
        JsonObjectBuilder sJsonObjectBuilder = Json.createObjectBuilder();
        JsonObjectBuilder kJsonObjectBuilder = Json.createObjectBuilder();
        for(Klas k : informatieSysteem.getDeKlassen()){
            //Zoek klascode van meegegeven klas
            if(k.getKlasCode().equals(lKlasCode)){
                //voeg alle studenten toe als entry in de json file met hun aanwezigheid per les
                for(Student s : k.getStudenten()) {
                    int snummer = s.getStudentNummer();
                    for(Les a: s.getRooster()){
                        aJsonObjectBuilder
                                .add(String.format("%s", a.getLesID()), a.getStudentAanwezigheid(snummer));
                    }
                    sJsonObjectBuilder
                            .add("studentnummer", snummer)
                            .add("studentnaam", s.getGebruikersnaam())
                            .add("aanwezigheid", aJsonObjectBuilder);

                    sJsonObjectBuilder.add("studentnummer", snummer).add("studentnaam", s.getGebruikersnaam()).add("aanwezigheid", aJsonObjectBuilder);
                    kJsonObjectBuilder.add(String.format("%s", snummer), sJsonObjectBuilder);

                }
            }
        }
            String lJsonOut = kJsonObjectBuilder.build().toString();

        conversation.sendJSONMessage(lJsonOut); // terugsturen naar de Polymer-GUI!
    }
}

