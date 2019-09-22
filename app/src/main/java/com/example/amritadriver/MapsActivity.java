package com.example.amritadriver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.Intent.ACTION_GET_CONTENT;
import static android.content.Intent.createChooser;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    FirebaseAuth mauth=FirebaseAuth.getInstance();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();
    Uri uri;
    Task<Uri> photouri;
    String uid,url;


    ImageView photo,navimage;
    DatabaseReference photourl;
    FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();
    StorageReference storageReference=firebaseStorage.getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final LinearLayout linearLayout3 = findViewById(R.id.dri);
        linearLayout3.setVisibility(View.INVISIBLE);
        final ProgressBar loading = findViewById(R.id.progressBar);
        loading.setVisibility(View.INVISIBLE);
        Button signout = findViewById(R.id.button2);
        photo = findViewById(R.id.photo);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
choosephoto();
            }
        });
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mauth.signOut();
                finish();
            }
        });
        Button button = findViewById(R.id.button);
        TextView create = findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapsActivity.this, "please contact transport officer", Toast.LENGTH_SHORT).show();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.setVisibility(View.VISIBLE);

                EditText em = findViewById(R.id.email);
                EditText pa = findViewById(R.id.pass);
                String email = em.getText().toString();
                String pass = pa.getText().toString();
                if (email.equals("")) {
                    em.setError("enter email id");
                    loading.setVisibility(View.INVISIBLE);

                } else if (pass.equals("")) {
                    pa.setError("enter date of birth");
                    loading.setVisibility(View.INVISIBLE);
                } else {
                    mauth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isComplete()) {
                                FirebaseUser user = mauth.getCurrentUser();
                                uid = user.getUid();

                                DatabaseReference usertype = databaseReference.child("Driver");
                                final DatabaseReference userid = usertype.child(uid);
                                DatabaseReference name = userid.child("name");
                                DatabaseReference route = userid.child("busro");
                                DatabaseReference phno = userid.child("phno");
                                photourl=userid.child("photourl");
                                photourl.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            overridePendingTransition(0,0);
                                            url=dataSnapshot.getValue(String.class);
                                            Picasso.get().load(url).into(photo);

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                name.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        TextView dname = findViewById(R.id.textView);
                                        dname.setText(dataSnapshot.getValue(String.class));

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                route.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        TextView dname = findViewById(R.id.textView2);
                                        dname.setText(dataSnapshot.getValue(String.class).toUpperCase());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                LinearLayout linearLayout = findViewById(R.id.login);

                                linearLayout.setVisibility(View.INVISIBLE);
                                linearLayout3.setVisibility(View.VISIBLE);
                                loading.setVisibility(View.INVISIBLE);

                                FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(MapsActivity.this);
                                final LocationRequest locationRequest = new LocationRequest();
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationRequest.setInterval(2000);
                                locationRequest.setFastestInterval(2000);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                        return;
                                    }
                                }
                                fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {

                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        DatabaseReference location = userid.child("location");
                                        Calendar calendar = Calendar.getInstance();
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMMM-yyyy, hh:mm:ss aa");
                                        String time = dateFormat.format(calendar.getTime());
                                        double lat = locationResult.getLastLocation().getLatitude();
                                        double lon = locationResult.getLastLocation().getLongitude();
                                        LatLng my = new LatLng(lat, lon);
                                        DatabaseReference latlangbus = location.child("latlang");
                                        latlangbus.setValue(my);
                                        DatabaseReference timeup = latlangbus.child("time");
                                        timeup.setValue("last updated at :" + time);
                                        Log.e("location", "lat= " + locationResult.getLastLocation().getLatitude() + "   long= " + locationResult.getLastLocation().getLongitude() + "   updated time at:" + time);
                                        Toast.makeText(MapsActivity.this, "location updated lat= " + locationResult.getLastLocation().getLatitude() + "  long = " + locationResult.getLastLocation().getLongitude() + "   updated time at:" + time, Toast.LENGTH_SHORT).show();


                                    }
                                }, getMainLooper());


                            }


                        }

                    });
                }
            }
        });
    }
    private void choosephoto() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(ACTION_GET_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startActivityForResult(createChooser(intent,"hello"),4);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 4 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            upload();
        }

    }

    private void upload() {
        if(uri != null){
            final StorageReference storageReference1=storageReference.child("driver/"+uid);
            storageReference1.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MapsActivity.this, "complered"+taskSnapshot.getStorage().getDownloadUrl(), Toast.LENGTH_SHORT).show();
                    Log.d("gr","fdfd  ");
                }


            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MapsActivity.this, "fail", Toast.LENGTH_SHORT).show();

                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Picasso.get().load(url).into(photo);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                }
            });
            getdata();
        }
        else{
            Toast.makeText(this, "efrfgdfx", Toast.LENGTH_SHORT).show();
        }
    }

    private void getdata() {

        final StorageReference storageReference1=storageReference.child("student/"+uid);
        storageReference1.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Toast.makeText(MapsActivity.this, "compl"+task, Toast.LENGTH_SHORT).show();
                Log.d("datad","cd"+task);
            }
        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                overridePendingTransition(0,0);

                Picasso.get().load(uri).into(navimage);
                photourl.setValue(uri.toString());
                Picasso.get().load(uri).into(photo);

                url=uri.toString();

            }
        });
        }


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
