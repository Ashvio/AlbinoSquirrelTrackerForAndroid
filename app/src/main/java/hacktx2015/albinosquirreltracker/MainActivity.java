package hacktx2015.albinosquirreltracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private EditText descriptionEditText;

    //location
    private LocationManager locationManager;
    private Location location;

    //camera
    static final int REQUEST_IMAGE_CAPTURE = 1;


    //map
    private SupportMapFragment mapFragment;
    private GoogleMap map;

    private List<ParseObject> squirrelSightings = new ArrayList<ParseObject>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Parse.initialize(this, "G2bKUVRQVhuHfKFUFC2gapL6zlQ2C5rBAPhpXhyi", "WJb4v3TFR38xg1zIS0yTRrAwdt7PbTpw2kGdUFBc");
        Log.d("mainActivity", "close");//fix this?


        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);

        mapFragment = new SupportMapFragment(){
            @Override
        public void onActivityCreated(Bundle savedInstanceState)
            {
                super.onActivityCreated(savedInstanceState);
                map = mapFragment.getMap();
                map.setMyLocationEnabled(true);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.284920, -97.733964) , 14.0f) );
                onMapReady(map, new LatLng(30.28, -97.7));
            }

        };
        getSupportFragmentManager().beginTransaction().add(R.id.map, mapFragment).commit();

   //     mapView.onCreate(savedInstanceState);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("location");

        query.findInBackground(new FindCallback<ParseObject>()
        {


            public void done(List<ParseObject> list, ParseException e)
            {
                if (e == null) {
                    int length = list.size();
                    for (int i = 0; i < length; i++) {
                        ParseObject p = list.get(i);
                        Log.d("mainActivity", "PRINT DIS " + p.toString());
                    }
                }
                else
                {
                    Log.d("mainActivity", "there was an error");
                }
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }



    //adds map markers
    public void onMapReady(GoogleMap map, LatLng latLng) {
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Hello world"));
    }

    //public void fetchLocations() {
      //  ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("location");
        //for (int varunisstillwrong = 0; varunisstillwrong < parseQuery.l
        //parseQuery.getFirstInBackground();

    //}

    private void dispatchPictureTakeIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    //    Log.d("mainActivity", "tried to take picture");
        if (takePictureIntent.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        //    Log.d("mainActivity", "successfully took picture");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");


            final ParseObject parseObject = new ParseObject("SquirrelSighting");
            parseObject.put("description", descriptionEditText.getText().toString());
            if (location!= null) {
                parseObject.put("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            final ParseFile imageFile = new ParseFile(stream.toByteArray());
            imageFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e==null)
                    {
                        parseObject.put("image", imageFile);

                        parseObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e!=null)
                                {
                                    Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_LONG).show();

                                }
                                else
                                {
                                    Toast.makeText(MainActivity.this, "save success", Toast.LENGTH_LONG).show();

                                }
                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_LONG).show();

                    }

                }
            });



            //do something with imageBitmap
         //   Log.d("mainActivity", "The bitmap is " + imageBitmap.toString());
        }
    }



    public void buttonPressed(View view){

        //get description
        String description = "" + descriptionEditText.getText();

        //launch camera activity
        dispatchPictureTakeIntent(); //need to do something with the bitmap in onActivityResult method, add prompt

        //get location
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null); //change to GPS_PROVIDER
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("mainActivity","location found at: " + location.getLatitude() + " " +location.getLongitude() + " Description is: " + descriptionEditText.getText());
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15); //change this
        map.moveCamera(center);
        map.animateCamera(zoom);
        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        Log.d("mainActivity", "status changed");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("mainActivity", "on provider enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("mainActivity", "on provider disabled");
    }
}
