package com.example.afinal.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.afinal.R;
import com.example.afinal.model.LabelModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class AddLabelFragment extends Fragment {

    EditText label,desc;
    LinearLayout Layout;
    Button add;

    FirebaseFirestore db=FirebaseFirestore.getInstance();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.fragment_add_label,container,false);

        label=root.findViewById(R.id.ptAddLabelTitel);
        desc=root.findViewById(R.id.mtAddLabelDesc);
        Layout=root.findViewById(R.id.addLabelLayout);
        add=root.findViewById(R.id.btnAddLabel);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Submit(v);
            }
        });
        getLabel();
        return root;
    }
    public void Submit(View v){
        String strlabel=label.getText().toString();
        String strdesc=desc.getText().toString();
        if(!strlabel.isEmpty() && !strdesc.isEmpty()){
            LabelModel labels =new LabelModel(strlabel,strdesc);
            db.collection("LabelModel").add(labels).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(getContext(),"Başarılı. ",Toast.LENGTH_SHORT).show();
                    getLabel();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),"Hata oluştu. ",Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(getContext(),"Lütfen bilgileri doldurun. ",Toast.LENGTH_SHORT).show();
        }

    }

    public void getLabel(){
        Layout.removeAllViews();
        db.collection("LabelModel").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document:task.getResult()){
                        LinearLayout minLayout = new LinearLayout(requireContext());
                        TextView tv = new TextView(requireContext());
                        ImageView img = new ImageView(requireContext());
                        img.setImageResource(R.drawable.baseline_bookmark_added_24);
                        tv.setText(document.getString("Label"));
                        tv.setTextSize(20);
                        tv.setTextColor(Color.BLACK);
                        minLayout.addView(img);
                        minLayout.addView(tv);
                        Layout.addView(minLayout);
                    }
                } else {
                    Toast.makeText(getContext(), "Veri bağlantı sağlayacınızı bakın. ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}