package li.klass.fhem.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fragments.FragmentType;

public class FHEMUrlActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        if (data == null) return;
        if (data.getHost() == null) return;

        String host = data.getHost();

        if (host.startsWith("room=")) {
            show(FragmentType.ROOM_DETAIL, BundleExtraKeys.ROOM_NAME, host.replace("room=", ""));
        } else if (host.startsWith("device=")) {
            show(FragmentType.DEVICE_DETAIL, BundleExtraKeys.DEVICE_NAME, host.replace("device=", ""));
        } else if (host.equalsIgnoreCase("all_devices")) {
            show(FragmentType.ALL_DEVICES);
        } else if (host.equalsIgnoreCase("room_list")) {
            show(FragmentType.ROOM_LIST);
        } else if (host.equalsIgnoreCase("favorites")) {
            show(FragmentType.FAVORITES);
        } else {
            Intent intent = new Intent(Actions.EXECUTE_COMMAND);
            intent.putExtra(BundleExtraKeys.COMMAND, host.replace("cmd=", ""));
            startService(intent);
        }
        finish();
    }

    private void show(FragmentType fragmentType) {
        show(fragmentType, null, null);
    }

    private void show(FragmentType fragmentType, String extraKey, String extraValue) {
        Intent intent = new Intent(this, AndFHEMMainActivity.class);
        intent.putExtra(BundleExtraKeys.FRAGMENT, fragmentType);

        if (extraKey != null) {
            intent.putExtra(extraKey, extraValue);
        }
        startActivity(intent);
    }
}
