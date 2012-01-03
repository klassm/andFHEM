package li.klass.fhem.util.device;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;
import li.klass.fhem.service.device.DeviceService;

public class DeviceActionUtil {
    public static void renameDevice(final Context context, final Device device) {
        final EditText input = new EditText(context);
        input.setText(device.getName());
        new AlertDialog.Builder(context)
                .setTitle(R.string.context_rename)
                .setView(input)
                .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newName = input.getText().toString();
                        DeviceService.INSTANCE.renameDevice(context, device, newName);
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
                        DeviceService.INSTANCE.deleteDevice(context, device);
                    }
                }).setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    public static void moveDevice(final  Context context,final Device device) {
        final EditText input = new EditText(context);
        input.setText(device.getRoom());
        new AlertDialog.Builder(context)
                .setTitle(R.string.context_move)
                .setView(input)
                .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newRoom = input.getText().toString();
                        DeviceService.INSTANCE.moveDevice(context, device, newRoom);
                    }
                }).setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    public static void setAlias(final Context context, final Device device) {
        final EditText input = new EditText(context);
        input.setText(device.getAlias());
        new AlertDialog.Builder(context)
                .setTitle(R.string.context_alias)
                .setView(input)
                .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newAlias = input.getText().toString();
                        DeviceService.INSTANCE.setAlias(context, device, newAlias);
                    }
                }).setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }
}
