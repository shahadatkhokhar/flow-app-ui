package com.expenses.flow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void updateCreditListInFirebase(ArrayList<ItemList> creditList){
        db.collection("Users").document(GlobalContent.getUserEmail())
                .update(
                        "CreditList",creditList,
                        "TotalCredit", GlobalContent.getTotalCreditAmount())
                .addOnSuccessListener(documentReference -> Log.d("Success", "DocumentSnapshot creditList updated "))
                .addOnFailureListener(e -> Log.w("Failure", "Error adding document", e));
    }

    public static void updateDebitListInFirebase(ArrayList<ItemList> debitList){
        db.collection("Users").document(GlobalContent.getUserEmail())
                .update(
                        "DebitList",debitList,
                        "TotalDebit", GlobalContent.getTotalDebitAmount())
                .addOnSuccessListener(documentReference -> Log.d("Success", "DocumentSnapshot creditList updated "))
                .addOnFailureListener(e -> Log.w("Failure", "Error adding document", e));
    }

    public static void readFromFirebase(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("Users").document(GlobalContent.getUserEmail());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d("Read", "DocumentSnapshot data: " + document.getData());
                    ArrayList recievedDebitList = (ArrayList) document.get("DebitList");

                    ArrayList<ItemList> tempDebitList = new ArrayList<>();
                    assert recievedDebitList != null;
                    if(!(recievedDebitList.isEmpty())){
                        for(int counter=0;counter<recievedDebitList.size();counter++){
                            HashMap<HashMap<String,String>,HashMap<String, Integer>> item = (HashMap<HashMap<String, String>, HashMap<String, Integer>>) recievedDebitList.get(counter);
                            String itemName = String.valueOf(item.get("itemName"));
                            String itemValue = String.valueOf(item.get("itemAmount"));
                            Log.d("item", String.valueOf(item.get("itemName")+", "+ item.get("itemAmount")));

                            if(!itemName.equalsIgnoreCase("null")&&!itemValue.equalsIgnoreCase("null")) {
                                ItemList debit = new ItemList(String.valueOf(item.get("itemName")), Integer.parseInt(String.valueOf(item.get("itemAmount"))));
                                tempDebitList.add(debit);
                            }
                            else if(!String.valueOf(item.get("n")).equalsIgnoreCase("null") && !String.valueOf(item.get("o")).equalsIgnoreCase("null")){
                                ItemList debit = new ItemList(String.valueOf(item.get("n")), Integer.parseInt(String.valueOf(item.get("o"))));
                                tempDebitList.add(debit);
                            }
                        }
                    }
                    GlobalContent.setDebitList(tempDebitList);

                    ArrayList recievedCreditList = (ArrayList) document.get("CreditList");
                    ArrayList<ItemList> tempCreditList = new ArrayList<>();
                    assert recievedCreditList != null;
                    if(!(recievedCreditList.isEmpty())) {
                        for (int counter = 0; counter < recievedCreditList.size(); counter++) {
                            HashMap<HashMap<String, String>, HashMap<String, Integer>> item = (HashMap<HashMap<String, String>, HashMap<String, Integer>>) recievedCreditList.get(counter);
                            String itemName = String.valueOf(item.get("itemName"));
                            String itemValue = String.valueOf(item.get("itemAmount"));

                            Log.d("item", item.get("itemName") + ", " + item.get("itemAmount"));

                            if (!itemName.equalsIgnoreCase("null") && !itemValue.equalsIgnoreCase("null")) {
                                ItemList credit = new ItemList(String.valueOf(item.get("itemName")), Integer.parseInt(String.valueOf(item.get("itemAmount"))));
                                tempCreditList.add(credit);
                            }
                            else if(!String.valueOf(item.get("n")).equalsIgnoreCase("null") && !String.valueOf(item.get("o")).equalsIgnoreCase("null")){
                                ItemList credit = new ItemList(String.valueOf(item.get("n")), Integer.parseInt(String.valueOf(item.get("o"))));
                                tempCreditList.add(credit);
                            }
                        }
                    }
                    GlobalContent.setCreditList(tempCreditList);

                    if(document.get("TotalCredit") != null) {
                        GlobalContent.setTotalCredit((Long) document.get("TotalCredit"));
                    }else {
                        GlobalContent.setTotalCredit(0L);
                    }

                    if(document.get("TotalDebit") != null) {
                        GlobalContent.setTotalDebit((Long) document.get("TotalDebit"));
                    }else {
                        GlobalContent.setTotalDebit(0L);
                    }
                    String profilePhotoUrl = (String) document.get("ProfileImage");
                    Bitmap[] image = {null};
                    Thread t= new Thread(() -> {
                        try {
                            URL url = new URL(profilePhotoUrl);
                            image[0] = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                            if (image[0] != null) {
                                Log.d("Image", "Success");
                                GlobalContent.setProfileImage(image[0]);
                            } else {
                                Log.d("Image", "Failure");

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.d("Read", "No such document");
                }
            } else {
                Log.d("Read", "get failed with ", task.getException());
            }
        });
    }

}
