package li.klass.fhem.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;

public class FHEMUrlActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        if (data == null) return;
        if (data.getHost() == null) return;

        String host = data.getHost();

        Intent intent = new Intent(Actions.EXECUTE_COMMAND);
        intent.putExtra(BundleExtraKeys.COMMAND, host);
        startService(intent);


        finish();
    }
}
