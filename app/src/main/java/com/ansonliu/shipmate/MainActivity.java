package com.ansonliu.shipmate;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, PhoneNumberDialogListener, CancelPickupDialogListener {

    final String LOG_TAG = "PEPIN";
    private GoogleMap mMap;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;

    private final String serverBaseURL = "https://navy-shipmate.herokuapp.com";

    private List<Marker> vanMarkers;

    private LatLng currentLatLng;

    private Button pickupButton;

    private RequestQueue queue;

    private SharedPreferences prefs;

    Handler mHandler = new Handler();

    private String android_id;

    int currentStatus;
    boolean shipmateRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        InstructionDialog dialog = new InstructionDialog();
        dialog.show(getFragmentManager(), "InstructionDialog");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, 1, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER

        //Check for user phone number
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String numberString = prefs.getString("userPhoneNumber", "");
        if (numberString.length() != 10) {
            showPhoneNumberDialog();
        }

        /* http://stackoverflow.com/questions/16869482/how-to-get-unique-device-hardware-id-in-android */
        android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (android_id == null || android_id.length() == 0) {
            android_id = "android";
        }

        queue = Volley.newRequestQueue(this);

        vanMarkers = new ArrayList<Marker>();

        pickupButton = (Button) findViewById(R.id.pickupButton);
        pickupButton.setTextColor(0xFFFFFFFF);
        pickupButton.setText("Connecting");

        Button callButton = (Button) findViewById(R.id.callButton);
        callButton.setText(Html.fromHtml("\u260E\uFE0E"));
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = "tel:4103205961";
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
                startActivity(callIntent);
            }
        });
    }

    public void showPhoneNumberDialog() {
        PhoneNumberDialogListenerFragment dialog = new PhoneNumberDialogListenerFragment();

        //pass programmatic phone number to dialog
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String numberString = prefs.getString("userPhoneNumber", "");
        if (numberString.length()== 0) {
            TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
            numberString = tMgr.getLine1Number();
            if (numberString != null && numberString.length() > 10) {
                numberString =  numberString.substring(numberString.length() - 10);
            }
        }
        Bundle extrasShow = new Bundle();
        extrasShow.putString("phoneNumber", numberString);
        dialog.setArguments(extrasShow);
        dialog.show(getFragmentManager(), "PhoneNumberDialogListenerFragment");
    }

    public void setInterfaceForStatus(Integer newStatus) {
        Log.e(LOG_TAG, "Set interface for status " + Integer.toString(newStatus));

        if (shipmateRunning == false)
            return;


        switch (newStatus) {
            case 0: //inactive, cancelled
                pickupButton.setText("Request Pickup");
                pickupButton.setBackgroundColor(0xFF286090); //dark blue
                pickupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        LatLng academyCoord = new LatLng(38.982048, -76.483933);
                        if (getLastBestLocation() == null && currentLatLng == null) {
                            Toast.makeText(MainActivity.this, "No Location Available\nTurn on Location", Toast.LENGTH_LONG).show();
                            return;
                        }
                        LatLng bestLocation = new LatLng(getLastBestLocation().getLatitude(), getLastBestLocation().getLongitude());
                        if (currentLatLng != null) {
                            bestLocation = currentLatLng;;
                        }

                        //check if shipmate within 80500meters (50mi) of bancroft
                        float[] results = new float[1];
                        Location.distanceBetween(academyCoord.latitude, academyCoord.longitude,
                                bestLocation.latitude, bestLocation.longitude,
                                results);
                        Log.e(LOG_TAG, Double.toString(bestLocation.latitude) + "|" + Double.toString(bestLocation.longitude));
                        Log.e(LOG_TAG, Float.toString(results[0]));
                        if (results[0] > 80500) {
                            Toast.makeText(MainActivity.this, "Out of SHIPMATE pickup range. Call SHIPMATE directly. ", Toast.LENGTH_LONG).show();
                            return;
                        }

                        pickupButton.setText("Requesting");
                        pickupButton.setOnClickListener(null);

                        StringRequest sr = new StringRequest(Request.Method.POST, serverBaseURL+"/newPickup", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject tmp = new JSONObject(response);
                                    currentStatus = tmp.getInt("status");

                                    pickupButton.setText("Calling SHIPMATE phone in 3 seconds");
                                    pickupButton.setBackgroundColor(0xFFEC971F); //yellow
                                    pickupButton.setOnClickListener(null);
                                    //delay phone call
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(3000);// change the time according to your need
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //call shipmate phone number
                                                        String number = "tel:4103205961";
                                                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
                                                        startActivity(callIntent);

                                                        setInterfaceForStatus(currentStatus);
                                                    }
                                                });
                                            } catch (Exception e) {
                                                Log.e(LOG_TAG, e.toString());
                                            }
                                        }
                                    }).start();
                                } catch (JSONException e) {
                                    Log.e(LOG_TAG, e.getLocalizedMessage());
                                    getPhoneNumberStatusAndSetInterfaceWithDelay();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(LOG_TAG, error.toString());
                                getPhoneNumberStatusAndSetInterfaceWithDelay();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("phoneNumber", prefs.getString("userPhoneNumber", ""));
                                if (currentLatLng != null) {
                                    params.put("latitude", Double.toString(currentLatLng.latitude));
                                    params.put("longitude", Double.toString(currentLatLng.longitude));
                                } else if (getLastBestLocation() != null) {
                                    Location lastLocation = getLastBestLocation();
                                    //Log.e(LOG_TAG, "Using network location " + lastLocation.toString());
                                    params.put("latitude", Double.toString(lastLocation.getLatitude()));
                                    params.put("longitude", Double.toString(lastLocation.getLongitude()));
                                } else {
                                    Toast.makeText(MainActivity.this, "No Location Available\nTurn on Location", Toast.LENGTH_LONG).show();
                                }
                                params.put("phrase", android_id);
                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("Content-Type", "application/x-www-form-urlencoded");
                                return params;
                            }
                        };
                        queue.add(sr);
                    }
                });
                break;
            case 1: //pending driver
                pickupButton.setText("Pending Driver");
                pickupButton.setBackgroundColor(0xFFEC971F); //yellow
                pickupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CancelPickupDialogListenerFragment dialog = new CancelPickupDialogListenerFragment();
                        dialog.show(getFragmentManager(), "CancelPickupDialogListenerFragment");
                    }
                });
                getPhoneNumberStatusAndSetInterfaceWithDelay();
                break;
            case 2: //driver enroute
                pickupButton.setText("Driver Enroute");
                pickupButton.setBackgroundColor(0xFF31B0D5); //lights blue
                pickupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CancelPickupDialogListenerFragment dialog = new CancelPickupDialogListenerFragment();
                        dialog.show(getFragmentManager(), "CancelPickupDialogListenerFragment");
                    }
                });
                getPhoneNumberStatusAndSetInterfaceWithDelay();
                break;
            case 3: //pickup complete
                pickupButton.setText("Pickup Complete");
                pickupButton.setBackgroundColor(0xFF449D44); //green
                pickupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setInterfaceForStatus(0);
                    }
                });
                break;
            default: //unknown
                setInterfaceToConnectionError();
                getPhoneNumberStatusAndSetInterfaceWithDelay();
                break;
        }
    }

    private void setInterfaceToConnectionError() {
        pickupButton.setText("Connection Error");
        pickupButton.setBackgroundColor(0xFFC9302C); //red
        pickupButton.setOnClickListener(null);
    }

    private void getPhoneNumberStatusAndSetInterfaceWithDelay() {
        /* http://stackoverflow.com/questions/22962884/androidimplementing-delay-in-message-in-handlerthread */
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);// change the time according to your need
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getPhoneNumberStatusAndSetInterface();
                        }
                    });
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }
        }).start();
    }

    private void getPhoneNumberStatusAndSetInterface() {
        Log.e(LOG_TAG, "getting status");

        StringRequest sr = new StringRequest(Request.Method.POST, serverBaseURL+"/getPickupInfo", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject tmp = new JSONObject(response);
                    currentStatus = tmp.getInt("status");
                    setInterfaceForStatus(currentStatus);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getLocalizedMessage());
                    getPhoneNumberStatusAndSetInterfaceWithDelay();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error.toString());
                getPhoneNumberStatusAndSetInterfaceWithDelay();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("phoneNumber", prefs.getString("userPhoneNumber", ""));
                if (currentLatLng != null) {
                    params.put("latitude", Double.toString(currentLatLng.latitude));
                    params.put("longitude", Double.toString(currentLatLng.longitude));
                } else if(getLastBestLocation() != null) {
                    Location lastLocation = getLastBestLocation();
                    Log.e(LOG_TAG, "Using network location " + lastLocation.toString());
                    params.put("latitude", Double.toString(lastLocation.getLatitude()));
                    params.put("longitude", Double.toString(lastLocation.getLongitude()));
                }
                params.put("phrase", android_id);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);
    }

    private void cancelPhoneNumberPickup() {
        StringRequest sr = new StringRequest(Request.Method.POST, serverBaseURL+"/cancelPickup", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject tmp = new JSONObject(response);
                    currentStatus = tmp.getInt("status");
                    setInterfaceForStatus(currentStatus);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getLocalizedMessage());
                    getPhoneNumberStatusAndSetInterfaceWithDelay();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error.toString());
                getPhoneNumberStatusAndSetInterfaceWithDelay();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("phoneNumber", prefs.getString("userPhoneNumber", ""));
                if (currentLatLng != null) {
                    params.put("latitude", Double.toString(currentLatLng.latitude));
                    params.put("longitude", Double.toString(currentLatLng.longitude));
                } else {
                    Location lastLocation = getLastBestLocation();
                    Log.e(LOG_TAG, "Using network location " + lastLocation.toString());
                    params.put("latitude", Double.toString(lastLocation.getLatitude()));
                    params.put("longitude", Double.toString(lastLocation.getLongitude()));
                }
                params.put("phrase", android_id);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);

        pickupButton.setText("Cancelling");
        pickupButton.setBackgroundColor(0xFFC9302C); //red
        pickupButton.setOnClickListener(null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setPadding(0, 0, 0, 210); //move google logo up from below pickup button

        LatLng annapolis = new LatLng(38.9844, -76.4889);
        MapsInitializer.initialize(getApplicationContext());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(annapolis));

        getVanLocationAndPlaceOnMap();
    }

    public void getVanLocationAndPlaceOnMap() {
        final String vanUrl = "http://navy-shipmate.herokuapp.com/getVanLocations";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, vanUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        //remove current markers from map
                        for (Marker marker : vanMarkers)
                            marker.remove();

                        if (jsonArray == null || jsonArray.length() == 0) {
                            shipmateRunning = false;
                            getVanLocationWithDelay();

                            pickupButton.setText("SHIPMATE not running");
                            pickupButton.setBackgroundColor(0xFF444444); //gray
                            pickupButton.setOnClickListener(null);

                            return;
                        }

                        if (shipmateRunning == false) { //If shipmate was previously not running, get current pickup info
                            shipmateRunning = true;
                            getPhoneNumberStatusAndSetInterface();
                        }

                        vanMarkers = new ArrayList<Marker>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject jsonObj = jsonArray.getJSONObject(i);
                                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.van_transparent);
                                bm = Bitmap.createScaledBitmap(bm, 150, 87, false);
                                Marker vanMarker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(jsonObj.getDouble("latitude"), jsonObj.getDouble("longitude")))
                                        .title("Van")
                                        .icon(BitmapDescriptorFactory.fromBitmap(bm)));
                                vanMarkers.add(vanMarker);
                            } catch (JSONException e) {
                                Log.e(LOG_TAG, e.getLocalizedMessage());
                                setInterfaceToConnectionError();
                            }
                        }
                        getVanLocationWithDelay();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Van location error response "+error.toString()); setInterfaceToConnectionError(); getVanLocationWithDelay();
            }
        }
        );
        queue.add(jsonArrayRequest);
    }

    public void getVanLocationWithDelay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                    try {
                        Thread.sleep(10000);// change the time according to your need
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                getVanLocationAndPlaceOnMap();
                            }
                        });
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.toString());
                    }
            }
        }).start();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15);
        mMap.animateCamera(cameraUpdate);
    }

    /**
     * @return the last know best location
     */
    private Location getLastBestLocation() {
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

    /*
 * Implementation of onCreateOptionsMenu. Create the option menu items.
 * @param menu the Menu to create.
 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_view_options_menu, menu);
        return true;
    }

    /*
     * Implementation of onOptionsItemSelected. Determines which menu item was pressed and acts accordingly.
     * @param item The MenuItem that was pressed.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch(item.getItemId()){
            case R.id.menu_about:
                Intent aboutIntent = new Intent(getBaseContext(), AboutActivity.class);
                startActivityForResult(aboutIntent, 10);
                return true;
            case R.id.menu_change_phone_number:
                showPhoneNumberDialog();
                return true;
            case R.id.menu_help:
                Intent intent = new Intent(getBaseContext(), HelpActivity.class);
                startActivityForResult(intent, 1775);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onDialogSubmit(String phoneNumber) {
        Log.e(LOG_TAG, phoneNumber);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userPhoneNumber", phoneNumber);
        editor.commit();
    }

    public void onCancelPickupYes() {
        cancelPhoneNumberPickup();
    }
}
