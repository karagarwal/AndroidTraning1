package com.agarwal.training1;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class UploadImageAdapter extends RecyclerView.Adapter<UploadImageAdapter.ViewHolder> {

    public List<String> fileNameList;

    public UploadImageAdapter(List<String> fileNameList, List<String> fileDoneList) {
        this.fileNameList = fileNameList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
      return  null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        String filename = fileNameList.get(position);
        viewHolder.fileNameView.setText(filename);
    }

    @Override
    public int getItemCount() {
        return fileNameList.size();
    }

    public class  ViewHolder extends  RecyclerView.ViewHolder{
        View mView;
        public TextView fileNameView;

        public ViewHolder(View itemView){
            super(itemView);
        }
    }
}
