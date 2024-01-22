package com.example.afinal.ui;

import android.content.res.ColorStateList;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.afinal.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {
    private LinearLayout postsLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        postsLayout = root.findViewById(R.id.posts);


        fetchUserPhotos();
        fetchUserLabels(root);

        return root;
    }
    private void fetchUserPhotos() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference userPhotosCollection = db.collection("user_photos");

        userPhotosCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        String photoUrl = document.getString("photoUrl");
                        List<String> labels = (List<String>) document.get("labels");
                        DocumentReference userRef = (DocumentReference) document.get("userRef");

                        createPost(photoUrl, labels, userRef);
                    }
                } else {
                    Log.d("GalleryFragment", "Error getting documents for user_photos: ", task.getException());
                }
            }
        });
    }
    private void createPost(String photoUrl, List<String> labels, DocumentReference userRef) {
        View postView = getLayoutInflater().inflate(R.layout.post_layout, null);

        ImageView imageView = postView.findViewById(R.id.postImageView);
        Glide.with(requireContext()).load(photoUrl).into(imageView);

        TextView labelsTextView = postView.findViewById(R.id.tvPostLabels);
        labelsTextView.setText(TextUtils.join(", ", labels));
        labelsTextView.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.purple)));

        fetchUserName(userRef, postView);

        postsLayout.addView(postView);
    }
    private void fetchUserName(DocumentReference userRef, View postView) {
        if(userRef != null) {
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if(documentSnapshot.exists()) {
                    String userName = documentSnapshot.getString("name");

                    TextView nameTextView = postView.findViewById(R.id.tvPostName);
                    if(nameTextView != null) {
                        nameTextView.setText(userName);
                        nameTextView.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.purple)));
                    }
                }
            }).addOnFailureListener(e -> {
                e.printStackTrace();
            });
        } else {
            Log.d("GalleryFragment", "User reference is null.");
        }
    }
    private void fetchUserLabels(View root) {
        // Access a Cloud Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to your "labels" collection
        CollectionReference labelsCollection = db.collection("labels");

        // Fetch the data for labels
        labelsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> labelList = new ArrayList<>();

                    // Iterate through the documents in the collection
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Assuming "label" is the field name in your Firestore document
                        String label = document.getString("label");

                        // Add the label to the list
                        if (label != null) {
                            labelList.add(label);
                        }
                    }

                    AutoCompleteTextView dropdown = root.findViewById(R.id.dropdown);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, labelList);
                    dropdown.setAdapter(adapter);
                } else {
                    // Handle errors
                    Log.d("GalleryFragment", "Error getting documents for labels: ", task.getException());
                }
            }
        });
    }
}