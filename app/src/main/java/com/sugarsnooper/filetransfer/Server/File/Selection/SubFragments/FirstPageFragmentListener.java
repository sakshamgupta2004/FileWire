package com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments;

import android.os.Bundle;

public interface FirstPageFragmentListener {
    void onSwitchToNextFragment();
    void onSwitchToNextFragment(Bundle bundle, int typeVolume);
}
