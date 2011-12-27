package li.klass.fhem.util.device;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;
import li.klass.fhem.R;
import li.klass.fhem.data.FHEMService;
import li.klass.fhem.domain.Device;

public class DeviceActionUtil {
    public static void renameDevice(final Context context, final Device device) {
        final EditText input = new EditText(context);
        new AlertDialog.Builder(context)
                .setTitle(R.string.context_rename)
                .setView(input)
                .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newName = input.getText().toString();
                        if (FHEMService.INSTANCE.renameDevice(context, device, newName)) {
                            Toast.makeText(context, R.string.deviceRenameSuccess, Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    public static void deleteDevice(final Context context, final Device device) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.context_delete)
                .setMessage(R.string.areYouSure)
                .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (FHEMService.INSTANCE.deleteDevice(context, device)) {
                            Toast.makeText(context, R.string.deviceDeleteSuccess, Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

}
