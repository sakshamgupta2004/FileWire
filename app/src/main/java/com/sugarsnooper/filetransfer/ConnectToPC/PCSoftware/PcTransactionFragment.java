package com.sugarsnooper.filetransfer.ConnectToPC.PCSoftware;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Server.Send_Activity;
import com.sugarsnooper.filetransfer.Server.ServerService;
import com.sugarsnooper.filetransfer.Server.ServerStatus;
import com.sugarsnooper.filetransfer.Strings;
import com.sugarsnooper.filetransfer.TinyDB;

import java.util.ArrayList;

import static com.sugarsnooper.filetransfer.ConnectToPC.PCSoftware.PC_ConnectActivity.hostToConnectTo;

public class PcTransactionFragment extends Fragment {
    private boolean isServerStarted = false;
    private String host = "";
    private TinyDB tinyDB;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        tinyDB = new TinyDB(getContext());
        ArrayList<String> timestamps =
                tinyDB.getListString("transferDates");

        if (!timestamps.contains(Strings.dateString))
        {
            timestamps.add(Strings.dateString);
        }
        tinyDB.putListString("transferDates", timestamps);
        return inflater.inflate(R.layout.pc_connection_transfers_software_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        host = hostToConnectTo;
        ( (TextView) view.findViewById(R.id.status)).setText("Connected To PC");
        view.findViewById(R.id.button_select_files_transfer_to_pc_software).setClickable(false);
        ( (ExtendedFloatingActionButton) view.findViewById(R.id.button_select_files_transfer_to_pc_software)).setText(getString(R.string.please_wait));
        ServerService.newInstance(getContext(), null, new ServerStatus() {
            @Override
            public void onConnectionSuccess(String s) {
                isServerStarted = true;
                try {
                    requireActivity().runOnUiThread(() -> {
                        view.findViewById(R.id.button_select_files_transfer_to_pc_software).setClickable(true);
                        ( (ExtendedFloatingActionButton) view.findViewById(R.id.button_select_files_transfer_to_pc_software)).setText(getString(R.string.select_files2));
                        view.findViewById(R.id.button_select_files_transfer_to_pc_software).setOnClickListener(v ->
                                startActivity(new Intent(requireActivity(), Send_Activity.class).putExtra(Strings.FileSelectionRequest, true).putExtra("HOST", host)));
                    });
                }
                catch (Exception e){

                }
            }

            @Override
            public void onConnectionFailed(String wifi_disabled) {
                isServerStarted = false;
                try {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireActivity(), "Some Error has Occurred\nPlease try again", Toast.LENGTH_LONG).show();
                        requireActivity().finish();
                    });
                }
                catch (Exception e){

                }
            }
        });

    }
}
