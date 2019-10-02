package com.mensa.zhmensa.models.categories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.menu.IMenu;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaListObservable;
import com.mensa.zhmensa.models.menu.UzhMenu;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.HttpUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.msebera.android.httpclient.Header;


public class UzhMensaCategory extends MensaCategory {

    private static final String PLATTE = "Platte";
    private static final String CAFETERIA_BOTANISCHER_GARTEN = "Cafeteria Botanischer Garten";

    private static final String TIERSPITAL = "Tierspital";
    private static final String UNTERE_MENSA_A = "Untere Mensa A";
    private static final String OBERE_MENSA_B = "Obere Mensa B";
    private static final String LICHTHOF = "Lichthof";
    private static final String ZENTRUM_FUR_ZAHNMEDIZIN = "Zentrum Für Zahnmedizin";
    private static final String CAFETERIA_ATRIUM = "Cafeteria Atrium";
    private static final String IRCHEL = "Irchel";
    private static final String BINZMUHLE = "Binzmühle";
    private static final String CAFETERIA_SEEROSE = "Cafeteria Seerose";
    private static final String RAMI_59_VEGAN = "Rämi 59 (vegan)";
    private static final String CAFETERIA_CITYPORT = "Cafeteria Cityport";

    public static String[] UZH_MENSA_ZENTRUM = {PLATTE, UNTERE_MENSA_A, OBERE_MENSA_B, LICHTHOF, RAMI_59_VEGAN, ZENTRUM_FUR_ZAHNMEDIZIN};
    public static String[] UZH_MENSA_IRCHEL= { TIERSPITAL, IRCHEL, CAFETERIA_SEEROSE, CAFETERIA_ATRIUM};;
    public static String[] UZH_OERLIKON= { BINZMUHLE, CAFETERIA_CITYPORT};
    /**
     *
     142 : Mensa Feed - Mensa UZH Irchel
     143 : Mensa Feed - Cafeteria UZH Plattenstrasse
     144 : Mensa Feed - Cafeteria UZH Botanischer Garten
     146 : Mensa Feed - Cafeteria UZH Tierspital
     147 : Mensa Feed - Mercato UZH Zentrum - Mercato UZH Zentrum (Untere Mensa)
     148 : Mensa Feed - Mensa UZH Zentrum - Mensa UZH Zentrum (Obere Mensa)
     149 : Mensa Feed - Mercato UZH Zentrum - Mercato UZH Zentrum (Untere Mensa) - Abendessen
     150 : Mensa Feed - Mensa UZH Zentrum Lichthof Rondell
     176 : Mensa Feed - Cafeteria UZH Irchel Atrium
     180 : Mensa Feed - Mensa UZH Irchel
     184 : Mensa Feed - Mensa UZH Binzmühle
     241 : Mensa Feed - Cafeteria UZH Irchel Seerose - Mensa UZH Irchel - Cafeteria Seerose - Mittagessen
     256 : Mensa Feed - Cafeteria UZH Irchel Seerose - Mensa UZH Irchel - Cafeteria Seerose - Abendessen
     391 : Mensa Feed - Cafeteria UZH Cityport

     */


    private static final MensaApiRoute[] ROUTES = new MensaApiRoute[]{
            new MensaApiRoute(143, PLATTE, Mensa.MenuCategory.LUNCH, "11.00 - 13:30"),
            new MensaApiRoute(144, CAFETERIA_BOTANISCHER_GARTEN, Mensa.MenuCategory.ALL_DAY, null),
            new MensaApiRoute(146, TIERSPITAL, Mensa.MenuCategory.LUNCH, null),
            new MensaApiRoute(147, UNTERE_MENSA_A, Mensa.MenuCategory.LUNCH, "11.00 - 14.00"),
            new MensaApiRoute(148, OBERE_MENSA_B, Mensa.MenuCategory.LUNCH, "11.00 - 14:00"),
            new MensaApiRoute(149, UNTERE_MENSA_A, Mensa.MenuCategory.DINNER, "17:00 - 19:30"),
            new MensaApiRoute(150, LICHTHOF, Mensa.MenuCategory.LUNCH, null),
            new MensaApiRoute(151, ZENTRUM_FUR_ZAHNMEDIZIN, Mensa.MenuCategory.LUNCH, "11.00 - 13:30"),
            new MensaApiRoute(176, CAFETERIA_ATRIUM, Mensa.MenuCategory.ALL_DAY, null),
            new MensaApiRoute(180, IRCHEL, Mensa.MenuCategory.LUNCH, "11:00 - 14:00"),
            new MensaApiRoute(184, BINZMUHLE, Mensa.MenuCategory.LUNCH, null),
            new MensaApiRoute(241, CAFETERIA_SEEROSE, Mensa.MenuCategory.LUNCH, null),
            new MensaApiRoute(256, CAFETERIA_SEEROSE, Mensa.MenuCategory.DINNER, null),
            new MensaApiRoute(256, IRCHEL, Mensa.MenuCategory.DINNER, null),
            new MensaApiRoute(346, RAMI_59_VEGAN, Mensa.MenuCategory.LUNCH, null),
            new MensaApiRoute(391, CAFETERIA_CITYPORT, Mensa.MenuCategory.ALL_DAY, null)
    };
    /**
     *         "UZH untere Mensa A": 147,
     *         "UZH obere Mensa B": 148,
     *         "UZH Lichthof": 150,
     *         "UZH Irchel": 180,
     *         "UZH Tierspital": 146,
     *         "UZH Zentrum Für Zahnmedizin": 151,
     *         "UZH Platte": 143,
     *          "UZH Rämi 59 (vegan)": 346,
     *            "UZH untere Mensa A (abend)": 149,
     * "UZH Irchel (abend)": 256
     */

    public UzhMensaCategory(String displayName, int pos) {
        super(displayName, Arrays.asList("Rämi 59(vegan)", TIERSPITAL, UNTERE_MENSA_A, LICHTHOF), pos);
    }

    public UzhMensaCategory(String displayName, @NonNull List<String> knownMensaIds, int pos) {
        super(displayName, knownMensaIds, pos);
    }


    @NonNull
    @Override
    public List<MensaListObservable> loadMensasFromAPI(String languageCode) {
        List<MensaListObservable> obsList = new ArrayList<>();
        for(Mensa.Weekday day: Mensa.Weekday.values()) {

            for (MensaApiRoute route : ROUTES) {
                final MensaListObservable obs = new MensaListObservable(day, route.mealType);
                obsList.add(obs);
                RequestParams par = new RequestParams();
            //    par.ci
              //  par.put(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
                final String apiUrl = "https://zfv.ch/" + languageCode+ "/menus/rssMenuPlan?type=uzh2&menuId=" + route.id + "&dayOfWeek=" + (day.day + 1);
                Log.d("UZHCategory loadMFAPI", "Loading UZH Mensas. Calling url: " + apiUrl);
                HttpUtils.getByUrl(apiUrl, par, new XMLResponseHandler(obs, route));
            }
        }
        return obsList;
    }



    private static class MensaApiRoute {
        final int id;
        final String name;
        final Mensa.MenuCategory mealType;
        final String openingHours;

        MensaApiRoute(int id, String name, Mensa.MenuCategory mealType, String openingHours) {
            this.mealType = mealType;
            this.id = id;
            this.name = name;
            this.openingHours = openingHours;
        }
    }

    private class XMLResponseHandler extends AsyncHttpResponseHandler {
        private final MensaListObservable observable;
        private final MensaApiRoute apiRoute;
        private long startTime;

        XMLResponseHandler(MensaListObservable observable, MensaApiRoute apiRoute) {
            this.observable = observable;
            this.apiRoute = apiRoute;
        }

        @Override
        public void onStart() {
            super.onStart();
            startTime = System.currentTimeMillis();
            Log.d("UzhMensaCategory" , "request started ");
        }


        @Override
        public void onSuccess(int statusCode, Header[] headers, @NonNull byte[] responseBody) {
            Log.d("UzhMensaCategory", "on sucess for route: " + apiRoute.name + " time: " + (startTime - System.currentTimeMillis()) + " ms");
       //     Log.e("reposne", new String(responseBody));
            try {
                Mensa m = parseXML(apiRoute.name, responseBody, observable.day, observable.mealType);

                observable.addNewMensa(m);

            } catch (IOException e) {
                Log.e("UZHMensa.parse", "could not parse string: " + new String(responseBody) + " error: " + e.getMessage());
                e.printStackTrace();
            } catch (SAXException e) {
                Log.e("UZHMensa.parse", "could not parse string: " + new String(responseBody) + " error: " + e.getMessage());
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                Log.e("UZHMensa.parse", "could not parse string: " + new String(responseBody) + " error: " + e.getMessage());
                e.printStackTrace();
            }

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, @Nullable byte[] responseBody, @Nullable Throwable error) {
            Log.d("UzhMensaCategory", "on fail for route: " + apiRoute.name + " time: " + (startTime - System.currentTimeMillis()) + " ms");
            if(error == null || error.getMessage() == null) {
                Log.e("id:", apiRoute.id+ "", error);
                error.printStackTrace();
                return;
            }
            Log.e("fail", error.getMessage() == null ? "null" : error.getMessage(), error);
            if(responseBody != null) {
                Log.e("response: ", new String(responseBody), error);
            }

            Log.e("id:", apiRoute.id+ "", error);

            error.printStackTrace();
        }


        @NonNull
        private Mensa parseXML(String name, byte[] xmlFile, Mensa.Weekday day, Mensa.MenuCategory mealType) throws  IOException, SAXException, ParserConfigurationException {


            String response = new String(xmlFile);
            String resp = response.replaceAll("> +",">").replaceAll(" +<", "<").replace("\n","");

            Log.d("UzhMensaCat.parseXML", "Got ans: " + resp)   ;

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(resp.getBytes()));
            Element element=doc.getDocumentElement();
            element.normalize();



            NodeList nList = doc.getElementsByTagName("summary");
            Node summaryNode = nList.item(0);


            Log.d("node", summaryNode.toString());


            for (int i = 0; i < summaryNode.getChildNodes().getLength(); i++) {

                Node n = summaryNode.getChildNodes().item(i);
                if(n == null)
                    continue;

                Log.d("UzhMensaCat.parseXML", "Got Node: " + n.getNodeValue());

                if(n.getNodeName().equals("div")) {
                    // Found a new Mensa. Mensa are always in first div element
                    Mensa m = new Mensa(name, name);

                    try {
                        m.setMenuForDayAndCategory(day, mealType, getMensaFromDivNode(name, mealType, n));
                    } catch (MensaClosedException e) {
                        m.setClosed(true);
                    }

                    NodeList date = doc.getElementsByTagName("title");

                    if(date != null && date.getLength() > 0 && date.item(0).getFirstChild() != null) {

                        String dateString = date.item(0).getFirstChild().getNodeValue();

                        String dayString = Helper.getDayForPattern(day.day, "dd.MM.YYYY");

                        if(!dateString.contains(dayString)) {
                            Mensa m2 =  new Mensa(name, name);
                            m2.setClosed(m.isClosed());
                            return m2;
                        }
                    }
                    return m;
                }
            }

            return new Mensa("Error no div node found", "ERROR");
        }


        private String domAsString(Node rootnode) {
            StringBuilder retString = new StringBuilder(trimString(rootnode.getNodeValue()) + (rootnode.getNodeName().equals("#text") ? " " : "\n"));
            NodeList children = rootnode.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                retString.append(domAsString(children.item(i)));
            }

            return retString.toString();
        }
        private String trimString(@Nullable String stringToTrim) {
            if(stringToTrim == null) {
                return "";
            }
            return stringToTrim.replaceAll(" +", " ").trim();
        }

        @NonNull
        private List<IMenu> getMensaFromDivNode(String mensaName, Mensa.MenuCategory mealType, Node divNode) throws MensaClosedException{
            List<IMenu> menuList = new ArrayList<>();

            int pCounter = 0;
            UzhMenu currentMenu = null;
            boolean foundOpenMenu = false;

            for (int i = 0; i < divNode.getChildNodes().getLength(); i++) {
                Node h3Node = divNode.getChildNodes().item(i);

                switch (h3Node.getNodeName()) {
                    case "h3":
                        // Found Begin of menu
                        currentMenu = new UzhMenu(null, null, null, null, null);

                        currentMenu.setVegi(false);

                        menuList.add(currentMenu);

                        // found new Meal
                        Node titleNode = h3Node.getFirstChild();

                        if (titleNode == null) {
                            Log.e("UzhMensaCat.getMenufh3", "Error. Title node in h3 tag not found");
                            continue;
                        }

                        String name = titleNode.getNodeValue();

                        currentMenu.setName(name);
                        currentMenu.setId(Helper.getIdForMenu(mensaName, name, menuList.size(), mealType));

                        if (h3Node.getLastChild() != null && h3Node.getLastChild().getFirstChild() != null)
                            currentMenu.setPrices(trimString(h3Node.getLastChild().getFirstChild().getNodeValue()));

                        break;
                    case "p":
                        if (currentMenu == null)
                            continue;
                        if (pCounter == 0) {
                            // Found Description
                            String desc = domAsString(h3Node);
                            currentMenu.setDescription(desc);
                            foundOpenMenu = foundOpenMenu || ! (desc.contains("geschlossen")  ||  desc.contains("closed"));
                            pCounter++;
                        } else {
                            currentMenu.setAllergene(domAsString(h3Node));
                            pCounter = 0;
                        }
                        break;
                    case "table":


                        NodeList calorieNodes = h3Node.getChildNodes();

                        for (int j = 1; j < calorieNodes.getLength(); j++) {
                            Node calorieNode = calorieNodes.item(j);
                            if (calorieNode.getNodeName().equals("tr") && calorieNode.getChildNodes().getLength() != 0) {
                                currentMenu.addNutritionFact(trimString(domAsString(calorieNode.getFirstChild())), trimString(domAsString(calorieNode.getLastChild())));
                            }
                        }
                        Log.d("i", "i");
                        // FOUND TABLE WITH CALORIES
                        break;
                    case "img":

                        if(currentMenu == null)
                            continue;

                        Node altNode = h3Node.getAttributes().getNamedItem("alt");
                        if(altNode != null && altNode.getNodeValue() != null) {
                            String altStringLC =  altNode.getNodeValue().toLowerCase();
                            if(altStringLC.equals("vegan") || altStringLC.equals("vegetarian")  || altStringLC.equals("vegetarisch"))
                                currentMenu.setVegi(true);
                            else
                                Log.d("UZHMensaCat-alt", altStringLC + " is not vegetarian");
                        }
                        break;
                }

            }

            if(menuList.isEmpty() || !foundOpenMenu)
                throw new MensaClosedException();

            return menuList;
        }

    }


    @Nullable
    @Override
    public Integer getCategoryIconId() {
        return R.drawable.ic_uni;
    }


    private static class MensaClosedException extends Exception {

    }
}
