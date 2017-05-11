package arab_open_university.com.bususer;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by akhalaf on 5/6/2017.
 */

public class Route {

    GoogleMap mMap;
    Context context;
    String lang;
    public Polyline line;
    private OnPostExecuteListener mPostExecuteListener = null;



    static String LANGUAGE_SPANISH = "es";
    static String LANGUAGE_ENGLISH = "en";
    static String LANGUAGE_FRENCH = "fr";
    static String LANGUAGE_GERMAN = "de";
    static String LANGUAGE_CHINESE_SIMPLIFIED = "zh-CN";
    static String LANGUAGE_CHINESE_TRADITIONAL = "zh-TW";

    static String TRANSPORT_DRIVING = "driving";
    static String TRANSPORT_WALKING = "walking";
    static String TRANSPORT_BIKE = "bicycling";
    static String TRANSPORT_TRANSIT = "transit";


    public boolean drawRoute(GoogleMap map, Context c, ArrayList<LatLng> points, boolean withIndications, String language, boolean optimize) {
        mMap = map;
        context = c;
        lang = language;
        if (points.size() == 2) {
            String url = makeURL(points.get(0).latitude, points.get(0).longitude, points.get(1).latitude, points.get(1).longitude, "driving");
            new connectAsyncTask(url, withIndications).execute();
            return true;
        } else if (points.size() > 2) {
            String url = makeURL(points, "driving", optimize);
            new connectAsyncTask(url, withIndications).execute();
            return true;
        }

        return false;

    }

    public boolean drawRoute(GoogleMap map, Context c, ArrayList<LatLng> points, String language, boolean optimize) {
        mMap = map;
        context = c;
        lang = language;
        if (points.size() == 2) {
            String url = makeURL(points.get(0).latitude, points.get(0).longitude, points.get(1).latitude, points.get(1).longitude, "driving");
            new connectAsyncTask(url, false).execute();
            return true;
        } else if (points.size() > 2) {
            String url = makeURL(points, "driving", optimize);
            new connectAsyncTask(url, false).execute();
            return true;
        }

        return false;

    }


    public boolean drawRoute(GoogleMap map, Context c, ArrayList<LatLng> points, String mode, boolean withIndications, String language, boolean optimize) {
        mMap = map;
        context = c;
        lang = language;
        if (points.size() == 2) {
            String url = makeURL(points.get(0).latitude, points.get(0).longitude, points.get(1).latitude, points.get(1).longitude, mode);
            new connectAsyncTask(url, withIndications).execute();
            return true;
        } else if (points.size() > 2) {
            String url = makeURL(points, mode, optimize);
            new connectAsyncTask(url, withIndications).execute();
            return true;
        }

        return false;

    }

    //


    public void drawRoute(GoogleMap map, Context c, LatLng source, LatLng dest, boolean withIndications, String language) {
        mMap = map;
        context = c;

        String url = makeURL(source.latitude, source.longitude, dest.latitude, dest.longitude, "driving");
        new connectAsyncTask(url, withIndications).execute();
        lang = language;

    }


    public void drawRoute(GoogleMap map, Context c, LatLng source, LatLng dest, String language, OnPostExecuteListener postExecuteListener) throws  Exception{
        mMap = map;
        context = c;
        mPostExecuteListener = postExecuteListener;
        if (mPostExecuteListener == null)
            throw new Exception("Param cannot be null.");

        String url = makeURL(source.latitude, source.longitude, dest.latitude, dest.longitude, "driving");
        new connectAsyncTask(url, false).execute();
        lang = language;

    }


    public void drawRoute(GoogleMap map, Context c, LatLng source, LatLng dest, String mode, boolean withIndications, String language) {
        mMap = map;
        context = c;

        String url = makeURL(source.latitude, source.longitude, dest.latitude, dest.longitude, mode);
        new connectAsyncTask(url, withIndications).execute();
        lang = language;

    }

    private String makeURL(ArrayList<LatLng> points, String mode, boolean optimize) {
        StringBuilder urlString = new StringBuilder();

        if (mode == null)
            mode = "driving";

        urlString.append("http://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(points.get(0).latitude);
        urlString.append(',');
        urlString.append(points.get(0).longitude);
        urlString.append("&destination=");
        urlString.append(points.get(points.size() - 1).latitude);
        urlString.append(',');
        urlString.append(points.get(points.size() - 1).longitude);

        urlString.append("&waypoints=");
        if (optimize)
            urlString.append("optimize:true|");
        urlString.append(points.get(1).latitude);
        urlString.append(',');
        urlString.append(points.get(1).longitude);

        for (int i = 2; i < points.size() - 1; i++) {
            urlString.append('|');
            urlString.append(points.get(i).latitude);
            urlString.append(',');
            urlString.append(points.get(i).longitude);
        }


        urlString.append("&sensor=true&mode=" + mode);


        return urlString.toString();
    }

    private String makeURL(double sourcelat, double sourcelog, double destlat, double destlog, String mode) {
        StringBuilder urlString = new StringBuilder();

        if (mode == null)
            mode = "driving";

        urlString.append("http://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=" + mode + "&alternatives=true&language=" + lang);
        return urlString.toString();
    }


    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


    private class connectAsyncTask extends AsyncTask<Void, Void, String> {
        //private ProgressDialog progressDialog;
        String url;
        boolean steps;

        connectAsyncTask(String urlPass, boolean withSteps) {
            url = urlPass;
            steps = withSteps;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          /*  progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();*/
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(url);

            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //progressDialog.hide();
            if(mPostExecuteListener != null && result != null){
                mPostExecuteListener.onPostExecute(result);


            }
        }
    }

    public int  drawPath(String result, boolean withSteps) {

        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            List<List<HashMap<String, String>>> getDuration = parse(json);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);


            /*for (int z = 0; z < list.size() - 1; z++) {
                LatLng src = list.get(z);
                LatLng dest = list.get(z + 1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
                        .width(15).color(Color.BLUE).geodesic(true));
            }*/

            PolylineOptions options = new PolylineOptions().width(20)
                    .color(Color.parseColor("#3046B0")).geodesic(true);
            for (int z = 0; z < list.size(); z++) {
                LatLng point = list.get(z);
                options.add(point);
            }
            if(mMap != null)
                line = mMap.addPolyline(options);
            int duration = 0;

           /* for(int x = 0; x < routeArray.length(); x++){
                JSONArray legs = routeArray.getJSONArray(x);
                for(int y = 0; y < legs.length(); i++) {
                    JSONObject durations = legs.getJSONArray("legs").getJSONObject(y);
                    duration += durations.getJSONObject("duration").getInt("value");
                }
            }*/



            /*for(int x = 0; x < getDuration.size(); x ++){
                List<HashMap<String, String>> path = getDuration.get(x);
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    if(j==0){    // Get distance from the list
                        String distance = (String) point.get("distance");
                        Log.i("Map_Route", distance);
                        continue;
                    }else if(j==1){ // Get duration from the list
                        String duration = (String)point.get("duration");
                        Log.i("Map_Route", duration);
                        continue;
                    }
                }
            }*/


            //  if (withSteps) {
            JSONArray arrayLegs = routes.getJSONArray("legs");
            JSONObject legs = arrayLegs.getJSONObject(0);
            JSONArray stepsArray = legs.getJSONArray("steps");
            //put initial point

            for (int i = 0; i < stepsArray.length(); i++) {
                Step step = new Step(stepsArray.getJSONObject(i));
                  /*  mMap.addMarker(new MarkerOptions()
                            .position(step.location)
                            .title(step.distance)
                            .snippet(step.instructions)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));*/
                duration += step.duration;
            }
            //    }

            Log.i("Map_Route", "MapDuration " + duration );
            return  duration;

        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * Class that represent every step of the directions. It store distance, location and instructions
     */
    private class Step {
        public String distance;
        public LatLng location;
        public String instructions;
        public int duration;

        Step(JSONObject stepJSON) {
            JSONObject startLocation;
            try {

                distance = stepJSON.getJSONObject("distance").getString("text");
                startLocation = stepJSON.getJSONObject("start_location");
                location = new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"));
                duration = stepJSON.getJSONObject("duration").getInt("value");
                try {
                    instructions = URLDecoder.decode(Html.fromHtml(stepJSON.getString("html_instructions")).toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        JSONObject jDistance = null;
        JSONObject jDuration = null;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");

                List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){

                    /** Getting distance from the json data */
                    jDistance = ((JSONObject) jLegs.get(j)).getJSONObject("distance");
                    HashMap<String, String> hmDistance = new HashMap<String, String>();
                    hmDistance.put("distance", jDistance.getString("text"));

                    /** Getting duration from the json data */
                    jDuration = ((JSONObject) jLegs.get(j)).getJSONObject("duration");
                    HashMap<String, String> hmDuration = new HashMap<String, String>();
                    hmDuration.put("duration", jDuration.getString("text"));

                    /** Adding distance object to the path */
                    path.add(hmDistance);

                    /** Adding duration object to the path */
                    path.add(hmDuration);


                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                            path.add(hm);
                        }
                    }
                }
                routes.add(path);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }

        return routes;
    }


    public static interface OnPostExecuteListener {
        void onPostExecute(String result);
    }

}
