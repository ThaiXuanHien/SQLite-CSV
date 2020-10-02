package com.example.sqliteexample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterRecord extends RecyclerView.Adapter<AdapterRecord.ViewHolder> {

    private Context context;
    private ArrayList<ModelRecord> recordArrayList;
    private MyDBHelper myDBHelper;

    public AdapterRecord(Context context, ArrayList<ModelRecord> recordArrayList) {
        this.context = context;
        this.recordArrayList = recordArrayList;
        myDBHelper = new MyDBHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_record, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final ModelRecord record = recordArrayList.get(position);
        holder.nameTV.setText(record.getName());
        holder.phoneTV.setText(record.getPhone());
        holder.emailTV.setText(record.getEmail());
        holder.dobTV.setText(record.getDob());
        // if user does not attach image then imgURI will be null, so set a default image in that cause
        if (record.getImage().equals("null")) {
            holder.profileIV.setImageResource(android.R.drawable.ic_delete);
        } else {
            holder.profileIV.setImageURI(Uri.parse(record.getImage()));
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RecordDetailActivity.class);
                intent.putExtra("RECORD_ID", record.getId());
                context.startActivity(intent);
            }
        });

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreDialog(position + "", record.getId(), record.getName(), record.getPhone(),
                        record.getEmail(), record.getDob(), record.getBio(), record.getImage(), record.getAddedTime(), record.getUpdatedTime());


            }
        });
        Log.d("ImagePath", "OnBindViewHolder" + record.getImage());

    }

    private void showMoreDialog(String position, final String id, final String name, final String phone, final String email, final String dob, final String bio,
                                final String image, final String addedTime, final String updatedTime) {
        // options to display in dialog
        String[] options = {"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // handle item click
                if (which == 0) { // Edit is clicked
                    Intent intent = new Intent(context, AddUpdateRecordActivity.class);
                    intent.putExtra("ID", id);
                    intent.putExtra("NAME", name);
                    intent.putExtra("PHONE", phone);
                    intent.putExtra("EMAIL", email);
                    intent.putExtra("DOB", dob);
                    intent.putExtra("BIO", bio);
                    intent.putExtra("IMAGE", image);
                    intent.putExtra("ADDED_TIME", addedTime);
                    intent.putExtra("UPDATED_TIME", updatedTime);
                    intent.putExtra("isEditMode", true);
                    context.startActivity(intent);
                } else if (which == 1) {
                    myDBHelper.deleteData(id);
                    ((MainActivity) context).onResume();
                }
            }
        });
        builder.create().show();
    }

    @Override
    public int getItemCount() {
        return recordArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileIV;
        TextView nameTV, phoneTV, emailTV, dobTV;
        ImageButton moreBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileIV = itemView.findViewById(R.id.profileIV);
            nameTV = itemView.findViewById(R.id.nameTV);
            phoneTV = itemView.findViewById(R.id.phoneTV);
            emailTV = itemView.findViewById(R.id.emailTV);
            dobTV = itemView.findViewById(R.id.DOBTV);
            moreBtn = itemView.findViewById(R.id.moreBtn);
        }
    }
}
