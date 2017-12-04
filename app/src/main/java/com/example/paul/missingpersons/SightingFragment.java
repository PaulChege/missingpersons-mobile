package com.example.paul.missingpersons;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class SightingFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {
    TextView name_txt,age_txt,height_txt,weight_txt,body_type_txt,description_txt,location_last_seen_txt;
    Button report,gallery,camera,choose_location;
    ProgressBar progressBar;
    ImageView imageView;
    Bitmap bitmap;
    double latitude,longitude;

    private GoogleApiClient mGoogleApiClient;

    private static final int CAMERA_REQUEST = 1888;
    private static final int PLACE_PICKER_REQUEST = 1889;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sighting, container, false);
    }





    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = (ImageView) getActivity().findViewById(R.id.image);
        gallery =(Button) getActivity().findViewById(R.id.gallery);
        camera = (Button) getActivity().findViewById(R.id.camera);
        name_txt = (TextView) getActivity().findViewById(R.id.name);
        age_txt = (TextView) getActivity().findViewById(R.id.age);
        weight_txt = (TextView) getActivity().findViewById(R.id.weight);
        height_txt = (TextView) getActivity().findViewById(R.id.height);
        body_type_txt = (TextView) getActivity().findViewById(R.id.body_type);
        description_txt = (TextView) getActivity().findViewById(R.id.description);
        location_last_seen_txt = (TextView) getActivity().findViewById(R.id.location_last_seen);
        report = (Button) getActivity().findViewById(R.id.report);
        choose_location = (Button) getActivity().findViewById(R.id.choose_location);

        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        report.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                createSighting();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if everything is ok we will open image chooser
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 100);
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });



        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
            }
        }



        choose_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        getActivity().setTitle("Report Sighting");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getActivity().getPackageName()));
            startActivity(intent);
            return;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {

            //getting the image Uri
            Uri imageUri = data.getData();
            try {
                //getting bitmap object from uri
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);

                //displaying selected image to imageview
                imageView.setImageBitmap(bitmap);

                //calling the method uploadBitmap to upload image
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getActivity());
                location_last_seen_txt.setText(place.getName());
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
            }
        }
    }


    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


    public void createSighting(){
        final  String name = name_txt.getText().toString();
        final  String age = age_txt.getText().toString();
        final  String weight = weight_txt.getText().toString();
        final  String height = height_txt.getText().toString();
        final  String body_type = body_type_txt.getText().toString();
        final  String description = description_txt.getText().toString();
        final  String location= location_last_seen_txt.getText().toString();
        final Integer id  = SharedPrefManager.getInstance(getActivity()).getUser().getId();

        progressBar.setVisibility(View.VISIBLE);

        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, URLs.URL_CREATE_SIGHTING,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            //converting response to json object
                            JSONObject obj = new JSONObject(new String(response.data));

                            //if no error in response
                            if (obj.getString("status").equals("success")) {

                                Toast.makeText(getActivity(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), MainActivity.class));

                            } else {
                                Toast.makeText(getActivity(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progressBar.setVisibility(View.GONE);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Error: Could not connect to server", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);

                    }
                })
        {


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("age", age);
                params.put("height", height);
                params.put("weight",weight);
                params.put("body_type", body_type);
                params.put("location", location);
                params.put("description", description);
                params.put("public_user_id", id.toString() );
                params.put("latitude", String.valueOf(latitude));
                params.put("longitude", String.valueOf(longitude));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", SharedPrefManager.getInstance(getActivity()).getUser().getToken());
                return headers;
            }

            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        VolleySingleton.getInstance(getActivity()).addToRequestQueue(request);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
