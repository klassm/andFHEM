/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.fhem;

import android.content.Context;
import android.content.Intent;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;

public class RequestResult<CONTENT> {
    public final CONTENT content;
    public final RequestResultError error;

    public RequestResult(CONTENT content) {
        this(content, null);
    }

    public RequestResult(RequestResultError error) {
        this(null, error);
    }

    public RequestResult(CONTENT content, RequestResultError error) {
        this.content = content;
        this.error = error;
    }

    public boolean handleErrors() {
        if (error  == null) return false;

        Context context = AndFHEMApplication.getContext();

        Intent intent = new Intent(Actions.CONNECTION_ERROR);
        intent.putExtra(BundleExtraKeys.STRING_ID, error.errorStringId);
        context.sendBroadcast(intent);

        return true;
    }
}
