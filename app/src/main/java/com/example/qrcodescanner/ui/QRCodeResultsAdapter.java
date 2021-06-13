package com.example.qrcodescanner.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.qrcodescanner.R;
import org.jetbrains.annotations.NotNull;
import java.util.List;


public class QRCodeResultsAdapter extends RecyclerView.Adapter<QRCodeResultsAdapter.ViewHolder>{

    private static final String TAG = "OTPDetailsAdapterTab2";

    private List<QRDetailsDAO> mData;
    private LayoutInflater mInflater;
    Context mContext;

    public QRCodeResultsAdapter(Context context, List<QRDetailsDAO> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mContext = context;
    }

    @NotNull
    @Override
    public QRCodeResultsAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.qr_row_item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull QRCodeResultsAdapter.ViewHolder holder, int position) {
        if (mData != null && mData.size()>0 && mData.get(position)!=null)
        {
            try {
                holder.dataQR.setText(mData.get(position).getStrQRData());
                holder.timeStampQR.setText(mData.get(position).getTimeStampVal());
            }
            catch (Exception e)
            {
                Log.e(TAG,"error in onBind setting text values"+e.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView dataQR;
        TextView timeStampQR;

        ViewHolder(View itemView) {
            super(itemView);
            dataQR = itemView.findViewById(R.id.qr_data);
            timeStampQR = itemView.findViewById(R.id.qr_timestamp);
//            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }

}
