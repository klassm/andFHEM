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

import java.io.Serializable;

public class ChartSeriesDescription implements Parcelable, Serializable {

    private int columnName;
    private String columnSpecification;
    private boolean showDiscreteValues;
    private boolean showRegression = false;
    private boolean showSum = false;
    private double sumDivisionFactor;

    public static ChartSeriesDescription getDiscreteValuesInstance(int columnName, String columnSpecification) {
        return new ChartSeriesDescription(columnName, columnSpecification, true, false, false, 0);
    }

    public static ChartSeriesDescription getRegressionValuesInstance(int columnName, String columnSpecification) {
        return new ChartSeriesDescription(columnName, columnSpecification, false, true, false, 0);
    }

    public static ChartSeriesDescription getSumInstance(int columnName, String columnSpecification, double divisionFactor) {
        return new ChartSeriesDescription(columnName, columnSpecification, false, false, true, divisionFactor);
    }

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

    public ChartSeriesDescription(int columnName, String columnSpecification, boolean showDiscreteValues, boolean showRegression, boolean showSum, double sumDivisionFactor) {
        this.columnName = columnName;
        this.columnSpecification = columnSpecification;
        this.showDiscreteValues = showDiscreteValues;
        this.showRegression = showRegression;
        this.showSum = showSum;
        this.sumDivisionFactor = sumDivisionFactor;
    }

    public ChartSeriesDescription(int columnName, String columnSpecification) {
        this(columnName, columnSpecification, false, false, false, 0);
    }

    private ChartSeriesDescription(Bundle bundle) {
        this.columnName = bundle.getInt("COLUMN_NAME");
        this.columnSpecification = bundle.getString("COLUMN_SPEC");
        this.showDiscreteValues = bundle.getBoolean("SHOW_DISCRETE_VALUES");
        this.showRegression = bundle.getBoolean("SHOW_REGRESSION");
        this.showSum = bundle.getBoolean("SHOW_SUM");
        this.sumDivisionFactor = bundle.getDouble("SUM_DIVISION_FACTOR");
    }

    public int getColumnName() {
        return columnName;
    }

    public boolean isShowDiscreteValues() {
        return showDiscreteValues;
    }

    public boolean isShowSum() {
        return showSum;
    }

    public boolean isShowRegression() {
        return showRegression;
    }

    public double getSumDivisionFactor() {
        return sumDivisionFactor;
    }

    public String getColumnSpecification() {
        return columnSpecification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChartSeriesDescription that = (ChartSeriesDescription) o;

        return columnName == that.columnName;

    }

    @Override
    public int hashCode() {
        return columnName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        Bundle bundle = new Bundle();
        bundle.putInt("COLUMN_NAME", columnName);
        bundle.putString("COLUMN_SPEC", columnSpecification);
        bundle.putBoolean("SHOW_DISCRETE_VALUES", showDiscreteValues);
        bundle.putBoolean("SHOW_SUM", showSum);
        bundle.putBoolean("SHOW_REGRESSION", showRegression);
        bundle.putDouble("SUM_DIVISION_FACTOR", sumDivisionFactor);
        parcel.writeBundle(bundle);
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
