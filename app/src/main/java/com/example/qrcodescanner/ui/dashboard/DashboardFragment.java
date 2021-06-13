package com.example.qrcodescanner.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrcodescanner.MainActivity;
import com.example.qrcodescanner.R;
import com.example.qrcodescanner.databinding.FragmentDashboardBinding;
import com.example.qrcodescanner.ui.QRCodeResultsAdapter;
import com.example.qrcodescanner.ui.QRDetailsDAO;

import java.util.List;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    private QRCodeResultsAdapter mAdapter;
    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;
    private RecyclerView recyclerView;
    TextView txtVwNoData;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });


        txtVwNoData = binding.textDashboard;
        recyclerView = binding.qrCodeDetailsRecycler;
        List<QRDetailsDAO> allQRDetails = ((MainActivity) getActivity()).dbHandler.getAllQRCodeResults();

        mAdapter = new QRCodeResultsAdapter(getContext(),allQRDetails);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (allQRDetails!=null && allQRDetails.size()>0)
        {
            //Do Nothing
            Log.e(TAG, "QR listData :"+allQRDetails.size());
        }
        else
        {
            recyclerView.setVisibility(View.GONE);

            txtVwNoData.setVisibility(View.VISIBLE);
            txtVwNoData.setText(R.string.no_data);
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
        List<QRDetailsDAO> allQRDetails = ((MainActivity) getActivity()).dbHandler.getAllQRCodeResults();
        mAdapter = new QRCodeResultsAdapter(getContext(),allQRDetails);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (allQRDetails!=null && allQRDetails.size()>0)
        {
            //Do Nothing
            Log.e(TAG, "OTP SENT listData :"+allQRDetails.size());
        }
        else
        {
            recyclerView.setVisibility(View.GONE);

            txtVwNoData.setVisibility(View.VISIBLE);
            txtVwNoData.setText(R.string.no_data);
        }
        }
        catch (Exception e)
        {

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}