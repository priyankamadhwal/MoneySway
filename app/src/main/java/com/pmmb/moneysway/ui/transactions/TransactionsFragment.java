package com.pmmb.moneysway.ui.transactions;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pmmb.moneysway.R;
import com.pmmb.moneysway.models.Transaction;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class TransactionsFragment extends Fragment {

    private FloatingActionButton fab;
    private DatabaseReference mRef;
    private AlertDialog alertDialog;
    private View dialogView;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;
    private Uri filePath;
    FirebaseStorage storage;
    StorageReference storageReference;
    String imageId;

    private final int PICK_IMAGE_REQUEST = 7;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_transactions, container, false);

        recyclerView = root.findViewById(R.id.transactions_list);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        fetchTransactions();

        mRef = FirebaseDatabase.getInstance().getReference();


        fab = root.findViewById(R.id.fab_transactions);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTransactionDialog();
            }
        });


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        return root;
    }

    private void fetchTransactions() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("transactions");

        FirebaseRecyclerOptions<Transaction> options =
                new FirebaseRecyclerOptions.Builder<Transaction>()
                        .setQuery(query, new SnapshotParser<Transaction>() {
                            @NonNull
                            @Override
                            public Transaction parseSnapshot(@NonNull DataSnapshot snapshot) {
                                Date date = null;
                                try {
                                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                                    date = format.parse(snapshot.getKey().substring(1));
                                }
                                catch (ParseException e) {
                                    Log.e("TransactionDateError", "Parse error", e);
                                }

                                SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
                                String transaction_date = format.format(date);

                                return new Transaction(transaction_date,
                                        snapshot.child("type").getValue().toString(),
                                        getResources().getString(R.string.currency) + " " + snapshot.child("amount").getValue().toString(),
                                        snapshot.child("description").getValue().toString(),
                                        snapshot.getKey(),
                                        "Category : " + snapshot.child("category").getValue().toString(),
                                        "Payment Method : " + snapshot.child("payment_method").getValue().toString());
                            }
                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<Transaction, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.transaction_list_item, parent, false);

                return new ViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position,Transaction model) {
                holder.setTransactionDate(model.getTimestamp());
                holder.setTransactionType(model.getType());
                holder.setTransactionAmount(model.getAmount());
                holder.setTransactionDescription(model.getDescription());
                holder.setTransactionCategory(model.getCategory());
                holder.setTransactionPaymentMethod(model.getPayment_method());
                holder.setTransactionMemo(model.getMemo());

                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(getActivity(), String.valueOf(position), Toast.LENGTH_SHORT).show();
                        TextView transaction_item_date = view.findViewById(R.id.transaction_item_date);
                        String string_date = transaction_item_date.getText().toString();
                        Date date = null;
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
                            date = format.parse(string_date);
                        }
                        catch (ParseException e) {
                            Log.e("TransactionDateError", "Parse error", e);
                        }

                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                        String image_id = "-" + format.format(date);
                        adapter.getRef(position).removeValue();
                        storageReference.child("memos/"+ image_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // File deleted successfully
                                Log.i("TransactionItemDelete", "onSuccess: deleted file");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // An error occurred!
                                Log.i("TransactionItemDelete", "onFailure: did not delete file");
                            }
                        });
                    }
                });
            }

        };
        recyclerView.setAdapter(adapter);
    }

    private void showAddTransactionDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();

        dialogView = inflater.inflate(R.layout.dialog_add_transaction, null);
        dialogBuilder.setView(dialogView);

        alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        alertDialog.getWindow().setLayout((int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics())), WindowManager.LayoutParams.WRAP_CONTENT);

        final Button button_add_transaction = dialogView.findViewById(R.id.button_add_transaction);
        final Button button_cancel_transaction = dialogView.findViewById(R.id.button_cancel_transaction);
        final RadioGroup radio_group_transaction_type = dialogView.findViewById(R.id.radio_group_transaction_type);
        final Spinner spinner_categories = dialogView.findViewById(R.id.spinner_categories);
        final Spinner spinner_payment_methods = dialogView.findViewById(R.id.spinner_payment_methods);
        final ImageView image_view_memo = dialogView.findViewById(R.id.image_view_memo);

        radio_group_transaction_type.clearCheck();
        spinner_categories.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[] {"Select a category"}));
        spinner_payment_methods.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[] {"Select a payment method"}));

        button_add_transaction.setOnClickListener(buttonAddTransactionOnClickListener);
        button_cancel_transaction.setOnClickListener(buttonCancelTransactionOnClickListener);
        spinner_categories.setOnItemSelectedListener(spinnerOnItemSelectedListener);
        spinner_payment_methods.setOnItemSelectedListener(spinnerOnItemSelectedListener);
        radio_group_transaction_type.setOnCheckedChangeListener(radioGroupTransactionTypeOnCheckedChangeListener);
        mRef.child("payment_methods").addListenerForSingleValueEvent(paymentMethodsValueEventListener);
        image_view_memo.setOnClickListener(imageViewMemoOnClickListener);

        radio_group_transaction_type.check(R.id.radio_button_income);
    }

    private View.OnClickListener buttonAddTransactionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (dialogView != null) {
                RadioGroup radio_group_transaction_type = dialogView.findViewById(R.id.radio_group_transaction_type);
                RadioButton selectedType = dialogView.findViewById(radio_group_transaction_type.getCheckedRadioButtonId());
                EditText edit_text_amount = dialogView.findViewById(R.id.edit_text_amount);
                EditText edit_text_description = dialogView.findViewById(R.id.edit_text_description);
                Spinner spinner_categories = dialogView.findViewById(R.id.spinner_categories);
                Spinner spinner_payment_methods = dialogView.findViewById(R.id.spinner_payment_methods);

                String type = selectedType.getText().toString();
                String amount = edit_text_amount.getText().toString();
                String description = edit_text_description.getText().toString();
                String memo = "img path";
                String category = spinner_categories.getSelectedItem().toString();
                String payment_method = spinner_payment_methods.getSelectedItem().toString();

                String current_timestamp = "-" + new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

                Transaction newTransaction = new Transaction(type, amount, description, memo, category, payment_method);
                mRef.child("transactions").child(current_timestamp).setValue(newTransaction);

                imageId = current_timestamp;

                uploadImage();

                alertDialog.dismiss();
                Toast.makeText(getActivity(), "Transaction added successfully", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener buttonCancelTransactionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            alertDialog.dismiss();
        }
    };

    private AdapterView.OnItemSelectedListener spinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorText));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private RadioGroup.OnCheckedChangeListener radioGroupTransactionTypeOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, final int checkedId) {
            mRef.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<String> categories = new ArrayList<String>();
                    categories.add("Select a category");
                    DataSnapshot categories_list = null;
                    switch (checkedId) {
                        case R.id.radio_button_income:
                            categories_list = dataSnapshot.child("income");
                            break;
                        case R.id.radio_button_expense:
                            categories_list = dataSnapshot.child("expense");
                            break;

                    }
                    for (DataSnapshot category: categories_list.getChildren()) {
                        String newCategory = category.getValue().toString();
                        newCategory = newCategory.substring(0,1).toUpperCase() + newCategory.substring(1);
                        categories.add(newCategory);
                    }

                    if (dialogView != null) {
                        final Spinner spinner_categories = dialogView.findViewById(R.id.spinner_categories);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, categories.toArray(new String[categories.size()]));
                        spinner_categories.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    };

    private ValueEventListener paymentMethodsValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<String> payment_methods = new ArrayList<String>();
            payment_methods.add("Select a payment method");
            for (DataSnapshot payment_method : dataSnapshot.getChildren()) {
                String newPaymentMethod = payment_method.getValue().toString();
                newPaymentMethod = newPaymentMethod.substring(0, 1).toUpperCase() + newPaymentMethod.substring(1);
                payment_methods.add(newPaymentMethod);
            }
            if (dialogView != null) {
                final Spinner spinner_payment_methods = dialogView.findViewById(R.id.spinner_payment_methods);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, payment_methods.toArray(new String[payment_methods.size()]));
                spinner_payment_methods.setAdapter(adapter);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private View.OnClickListener imageViewMemoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {
            filePath = data.getData();
        }
    }

    private void uploadImage() {

        if(filePath != null)
        {
            StorageReference ref = storageReference.child("memos/"+ imageId);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Sorry! Failed to upload image.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout root;
        public TextView transaction_item_date;
        public TextView transaction_item_type;
        public TextView transaction_item_amount;
        public TextView transaction_item_description;
        public TextView transaction_item_category;
        public TextView transaction_item_payment_method;
        public ImageView transaction_item_memo;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.transactions_list_root);
            transaction_item_date = itemView.findViewById(R.id.transaction_item_date);
            transaction_item_type = itemView.findViewById(R.id.transaction_item_type);
            transaction_item_amount = itemView.findViewById(R.id.transaction_item_amount);
            transaction_item_description = itemView.findViewById(R.id.transaction_item_description);
            transaction_item_category = itemView.findViewById(R.id.transaction_item_category);
            transaction_item_payment_method = itemView.findViewById(R.id.transaction_item_payment_method);
            transaction_item_memo = itemView.findViewById(R.id.transaction_item_memo);
        }

        public void setTransactionDate(String string) {
            transaction_item_date.setText(string);
        }

        public void setTransactionType(String string) {
            transaction_item_type.setText(string);
        }


        public void setTransactionAmount(String string) {
            transaction_item_amount.setText(string);
        }

        public void setTransactionDescription(String string) {
            transaction_item_description.setText(string);
        }

        public void setTransactionCategory(String string) {
            transaction_item_category.setText(string);
        }

        public void setTransactionPaymentMethod(String string) {
            transaction_item_payment_method.setText(string);
        }



        public void setTransactionMemo(String string) {
           GlideApp.with(getActivity())
                   .load( storageReference.child("memos/"+ string))
                   .centerCrop()
                   .placeholder(R.drawable.placeholder_img_memo)
                   .into(transaction_item_memo);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}