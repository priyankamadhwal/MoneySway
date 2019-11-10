package com.pmmb.moneysway.ui.transactions;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private TextView text_view_transaction_title;
    private RadioGroup radio_group_transaction_type;
    private RadioButton radio_button_income;
    private RadioButton radio_button_expense;
    private EditText edit_text_amount;
    private ImageView image_view_memo;
    private EditText edit_text_description;
    private Spinner spinner_categories;
    private Spinner spinner_payment_methods;
    private Button button_add_transaction;
    private Button button_cancel_transaction;

    private String selectedCategory = null;
    private String selectedPaymentMethod = null;
    private Transaction selectedModel = null;
    private int selectedModelPosition = -1;

    private ProgressDialog progressDialog;

    private final int PICK_IMAGE_REQUEST = 7;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_transactions, container, false);

        recyclerView = root.findViewById(R.id.transactions_list);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mRef = FirebaseDatabase.getInstance().getReference();

        fetchTransactions();

        fab = root.findViewById(R.id.fab_transactions);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTransactionDialog("Add New Transaction",
                        new Transaction(1), "Add", "Cancel");
            }
        });

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Wait for a few minutes...");
        progressDialog.setTitle("In progress");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);

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

                                String transaction_date = changeDateFormat(snapshot.getKey(), "yyyyMMddHHmmssSSS", "MMM dd, yyyy hh:mm a");

                                return new Transaction(snapshot.getKey(),
                                        transaction_date,
                                        snapshot.child("type").getValue().toString(),
                                        getResources().getString(R.string.currency) + " " + snapshot.child("amount").getValue().toString(),
                                        snapshot.child("description").getValue().toString(),
                                        snapshot.child("memo").getValue().toString(),
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
            protected void onBindViewHolder(ViewHolder holder, final int position, final Transaction model) {
                holder.setTransactionDate(model.getTimestamp());
                holder.setTransactionType(model.getType());
                holder.setTransactionAmount(model.getAmount());
                holder.setTransactionDescription(model.getDescription());
                holder.setTransactionCategory(model.getCategory());
                holder.setTransactionPaymentMethod(model.getPayment_method());
                holder.setTransactionMemo(model.getId());

                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedModel = model;
                        selectedModelPosition = position;
                        showTransactionDialog(model.getTimestamp(),
                                model, "Update", "Delete");
                    }
                });
            }

        };
        recyclerView.setAdapter(adapter);
    }

    private void initializeDialogComponents() {
        text_view_transaction_title = dialogView.findViewById(R.id.text_view_transaction_title);
        radio_group_transaction_type = dialogView.findViewById(R.id.radio_group_transaction_type);
        radio_button_income = dialogView.findViewById(R.id.radio_button_income);
        radio_button_expense = dialogView.findViewById(R.id.radio_button_expense);
        edit_text_amount = dialogView.findViewById(R.id.edit_text_amount);
        image_view_memo = dialogView.findViewById(R.id.image_view_memo);
        edit_text_description = dialogView.findViewById(R.id.edit_text_description);
        spinner_categories = dialogView.findViewById(R.id.spinner_categories);
        spinner_payment_methods = dialogView.findViewById(R.id.spinner_payment_methods);
        button_add_transaction = dialogView.findViewById(R.id.button_add_transaction);
        button_cancel_transaction = dialogView.findViewById(R.id.button_cancel_transaction);
    }

    private void setDialogComponents(String title, Transaction transactionModel, String buttonAddText, String buttonCancelText) {

        text_view_transaction_title.setText(title);
        radio_group_transaction_type.clearCheck();
        String amount = transactionModel.getAmount();
        if (!amount.equals(""))
            amount = amount.substring(2);
        edit_text_amount.setText(amount);
        edit_text_description.setText(transactionModel.getDescription());
        selectedCategory = transactionModel.getCategory().substring(11);
        spinner_categories.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[] {selectedCategory}));
        selectedPaymentMethod = transactionModel.getPayment_method().substring(17);
        spinner_payment_methods.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[] {selectedPaymentMethod}));
        button_add_transaction.setText(buttonAddText);
        button_cancel_transaction.setText(buttonCancelText);

        if (title.toLowerCase().equals("add new transaction")) {
            button_add_transaction.setOnClickListener(buttonAddTransactionOnClickListener);
            button_cancel_transaction.setOnClickListener(buttonCancelTransactionOnClickListener);
        }
        else {
            button_add_transaction.setOnClickListener(buttonUpdateTransactionOnClickListener);
            button_cancel_transaction.setOnClickListener(buttonRemoveTransactionOnClickListener);
        }
        spinner_categories.setOnItemSelectedListener(spinnerOnItemSelectedListener);
        spinner_payment_methods.setOnItemSelectedListener(spinnerOnItemSelectedListener);
        radio_group_transaction_type.setOnCheckedChangeListener(radioGroupTransactionTypeOnCheckedChangeListener);
        mRef.child("payment_methods").addListenerForSingleValueEvent(paymentMethodsValueEventListener);
        image_view_memo.setOnClickListener(imageViewMemoOnClickListener);

        if (transactionModel.getType().toLowerCase().equals("income"))
            radio_group_transaction_type.check(R.id.radio_button_income);
        else
            radio_group_transaction_type.check(R.id.radio_button_expense);
    }

    private void showTransactionDialog(String title, Transaction transactionModel, String buttonAddText, String buttonCancelText) {

        filePath = null;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();

        dialogView = inflater.inflate(R.layout.dialog_add_transaction, null);
        dialogBuilder.setView(dialogView);

        alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        alertDialog.getWindow().setLayout((int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, getResources().getDisplayMetrics())), WindowManager.LayoutParams.WRAP_CONTENT);

        initializeDialogComponents();

        setDialogComponents(title, transactionModel, buttonAddText, buttonCancelText);
    }

    private void captureTransactionDialog() {

        RadioButton selectedType = dialogView.findViewById(radio_group_transaction_type.getCheckedRadioButtonId());
        final String type = selectedType.getText().toString();
        final String amount = edit_text_amount.getText().toString();
        final String description = edit_text_description.getText().toString();
        final String category = spinner_categories.getSelectedItem().toString();
        final String payment_method = spinner_payment_methods.getSelectedItem().toString();

        String t_id = "";

        if (selectedModel == null)
            t_id = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(new Date());
        else
            t_id = selectedModel.getId();

        final String transaction_id = t_id;

        if(filePath != null)
        {
            storageReference.child("memos/" + transaction_id).putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String memo = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                            uploadData(transaction_id, type, amount, description, memo, category, payment_method);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            uploadData(transaction_id, type, amount, description, "", category, payment_method);
                            Toast.makeText(getActivity(), "Sorry! Failed to upload image.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });
        }
        else
            uploadData(transaction_id, type, amount, description, "", category, payment_method);
    }

    private View.OnClickListener buttonAddTransactionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            progressDialog.show();
            captureTransactionDialog();
        }
    };


    private View.OnClickListener buttonCancelTransactionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismissTransactionDialog();
        }
    };


    private View.OnClickListener buttonUpdateTransactionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (selectedModel != null && selectedModelPosition != -1) {
                progressDialog.show();
                captureTransactionDialog();
            }
        }
    };

    private View.OnClickListener buttonRemoveTransactionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedModel != null && selectedModelPosition != -1) {
                progressDialog.show();
                if (selectedModel.memo.equals("")) {
                    adapter.getRef(selectedModelPosition).removeValue();
                    dismissTransactionDialog();
                } else {
                    storageReference.child("memos/" + selectedModel.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i("DeleteMemoImage", "onSuccess: deleted file");
                            adapter.getRef(selectedModelPosition).removeValue();
                            Toast.makeText(getActivity(), "Transaction deleted successfullly", Toast.LENGTH_SHORT).show();
                            dismissTransactionDialog();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e("DeleteMemoImage", "onFailure: did not delete file", exception);
                            Toast.makeText(getActivity(), "Something went wrong. Try again later.", Toast.LENGTH_SHORT).show();
                            dismissTransactionDialog();
                        }
                    });
                }
            }
        }
    };

    private void uploadData(String id, String type, String amount, String description, String memo, String category, String payment_method) {

        Transaction newTransaction = new Transaction(type, amount, description, memo, category, payment_method);
        mRef.child("transactions").child(id).setValue(newTransaction).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                String msg="";
                if (selectedModel != null)
                    msg = "Transaction updated successfully";
                else
                    msg = "Transaction added successfully";
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Sorry! Something went wrong.", Toast.LENGTH_SHORT).show();
                Log.e("UploadDataFailure", "error", e);
            }
        });

        dismissTransactionDialog();
    }

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

                        if (selectedCategory != null) {
                            int spinnerPosition = adapter.getPosition(selectedCategory);
                            if (spinnerPosition < 0)
                                spinnerPosition = 0;
                            spinner_categories.setSelection(spinnerPosition);
                        }
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

                if (selectedPaymentMethod != null) {
                    int spinnerPosition = adapter.getPosition(selectedPaymentMethod);
                    spinner_payment_methods.setSelection(spinnerPosition);
                }
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
                   .apply(RequestOptions.skipMemoryCacheOf(true))
                   .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                   .into(transaction_item_memo);
        }
    }

    private String changeDateFormat (String old_date, String old_format, String new_format) {
        try {
            Date date = null;
            try {
                SimpleDateFormat format = new SimpleDateFormat(old_format, Locale.getDefault());
                date = format.parse(old_date);
            } catch (ParseException e) {
                Log.e("TransactionDateError", "Parse error", e);
            }
            SimpleDateFormat format = new SimpleDateFormat(new_format, Locale.getDefault());
            return (format.format(date));
        }
        catch (NullPointerException e) {
            Log.e("changeDateFormat", "null pointer", e);
            return "";
        }
    }

    private void dismissTransactionDialog() {
        filePath = null;
        selectedCategory = null;
        selectedPaymentMethod = null;
        selectedModel = null;
        selectedModelPosition = -1;
        alertDialog.dismiss();
        progressDialog.dismiss();
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