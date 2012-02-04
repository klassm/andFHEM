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

package li.klass.fhem.service.graph.description;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class ChartSeriesDescription implements Parcelable {

    private int columnSpecification;
    private boolean showDiscreteValues;
    private boolean showRegression = false;
    private boolean showSum = false;

    public static final Creator<ChartSeriesDescription> CREATOR = new Creator<ChartSeriesDescription>() {

        @Override
        public ChartSeriesDescription createFromParcel(Parcel parcel) {
            Bundle bundle = parcel.readBundle();
            return new ChartSeriesDescription(bundle);
        }

        @Override
        public ChartSeriesDescription[] newArray(int size) {
            return new ChartSeriesDescription[size];
        }
    };

    public ChartSeriesDescription(int columnSpecification, boolean showDiscreteValues, boolean showRegression, boolean showSum) {
        this.columnSpecification = columnSpecification;
        this.showDiscreteValues = showDiscreteValues;
        this.showRegression = showRegression;
        this.showSum = showSum;
    }

    public ChartSeriesDescription(int columnSpecification, boolean showDiscreteValues) {
        this.columnSpecification = columnSpecification;
        this.showDiscreteValues = showDiscreteValues;
    }

    private ChartSeriesDescription(Bundle bundle) {
        this.columnSpecification = bundle.getInt("COLUMN_SPECIFICATION");
        this.showDiscreteValues = bundle.getBoolean("SHOW_DISCRETE_VALUES");
        this.showRegression = bundle.getBoolean("SHOW_REGRESSION");
        this.showSum = bundle.getBoolean("SHOW_SUM");
    }

    public int getColumnSpecification() {
        return columnSpecification;
    }

    public boolean isShowDiscreteValues() {
        return showDiscreteValues;
    }

    public boolean isShowSum() {
        return showSum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChartSeriesDescription that = (ChartSeriesDescription) o;

        return columnSpecification == that.columnSpecification;

    }

    @Override
    public int hashCode() {
        return columnSpecification;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        Bundle bundle = new Bundle();
        bundle.putInt("COLUMN_SPECIFICATION", columnSpecification);
        bundle.putBoolean("SHOW_DISCRETE_VALUES", showDiscreteValues);
        bundle.putBoolean("SHOW_SUM", showSum);
        bundle.putBoolean("SHOW_REGRESSION", showRegression);
        parcel.writeBundle(bundle);
    }

    public boolean isShowRegression() {
        return showRegression;
    }

    public static ChartSeriesDescription[] toArray(Parcelable[] parcelables) {
        ChartSeriesDescription[] seriesDescriptions = new ChartSeriesDescription[parcelables.length];
        for (int i = 0; i < parcelables.length; i++) {
            Parcelable series = parcelables[i];
            seriesDescriptions[i] = (ChartSeriesDescription) series;
        }
        return seriesDescriptions;
    }
}
