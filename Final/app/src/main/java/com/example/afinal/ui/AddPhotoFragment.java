package com.example.afinal.ui;

import static android.app.Activity.RESULT_OK;
import static android.os.Build.VERSION_CODES.R;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;



public class AddPhotoFragment extends Fragment {
    private ImageView img;
    private LinearLayout layoutLabels;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> getContentLauncher;
    private String currentPhotoPath;
    private Uri selectedPhotoUri;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_add_photo, container, false);

        img = root.findViewById(R.id.img);
        Button btnCamera = root.findViewById(R.id.btnCamera);
        Button btnUpload = root.findViewById(R.id.btnUpload);
        layoutLabels = root.findViewById(R.id.layoutLabels);

        fetchLabelsFromFirestore();

        btnUpload.setOnClickListener(v -> uploadImageToFirebase() );

        img.setOnClickListener(view -> {
            Log.d("Gallery", "Opening gallery...");
            getContentLauncher.launch("image/*");
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK) {
                        displayCapturedImage();
                    }
                });

        getContentLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if(uri != null) {
                        Log.d("Gallery", "Selected photo uri: " + uri);
                        selectedPhotoUri = uri;
                        try {
                            Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().getContentResolver(), uri));
                            img.setImageBitmap(bitmap);
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
        );

        return root;
    }
    private void uploadImageToFirebase() {
        if(currentPhotoPath != null || selectedPhotoUri != null) {
            Uri photoUri = (currentPhotoPath != null) ? Uri.fromFile(new File(currentPhotoPath)) : selectedPhotoUri;

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            StorageReference photoRef = storageRef.child("images").child(userId).child(photoUri.getLastPathSegment());

            photoRef.putFile(photoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(requireContext(), "Photo uploaded successfully", Toast.LENGTH_SHORT).show();

                        photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();

                            List<String> checkedLabels = retrieveCheckedLabels();

                            storeDataInFirestore(downloadUrl, checkedLabels);
                        });
                    }) .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Photo upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        } else {
            Toast.makeText(requireContext(), "No photo to upload.", Toast.LENGTH_SHORT).show();
        }
    }
    private List<String> retrieveCheckedLabels() {
        List<String> checkedLabels = new ArrayList<>();

        for (int i = 0; i < layoutLabels.getChildCount(); i++) {
            View view = layoutLabels.getChildAt(i);

            if(view instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.isChecked()) {
                    String label = checkBox.getText().toString();
                    checkedLabels.add(label);
                }
            }
        }
        return checkedLabels;
    }

    private void storeDataInFirestore(String photoUrl, List<String> checkedLabels) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser != null && isAdded()) {
            String userId = currentUser.getUid();

            DocumentReference userRef = db.collection("users"). document(userId);

            Map<String, Object> data = new HashMap<>();
            data.put("userRef", userRef);
            data.put("photoUrl", photoUrl);
            data.put("labels", checkedLabels);

            String documentId = db.collection("users").document().getId();

            db.collection("user_photos")
                    .document(documentId)
                    .set(data)
                    .addOnSuccessListener(documentReference -> {

                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
        }
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        "tr.com.cemre.imageindexer.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);

                selectedPhotoUri = photoURI;
            }
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();

        Log.d("ImagePath", "Current photo path: " + currentPhotoPath);

        return image;
    }
    private void displayCapturedImage() {
        if(currentPhotoPath != null) {
            img.setImageURI(Uri.fromFile(new File(currentPhotoPath)));
        }
    }
    private void fetchLabelsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("labels")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                String label = document.getString("label");

                                CheckBox checkBox = createCheckBox(label);

                                layoutLabels.addView(checkBox);
                            }
                        }
                    }
                });
    }
    private CheckBox createCheckBox(String label) {
        CheckBox checkBox = new CheckBox(requireContext());
        checkBox.setText(label);

        checkBox.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.purple)));

        return checkBox;
    }
}