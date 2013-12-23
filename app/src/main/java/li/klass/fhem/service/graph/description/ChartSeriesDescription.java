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

import li.klass.fhem.AndFHEMApplication;

public class ChartSeriesDescription implements Parcelable, Serializable {

    private String columnName;
    private String fileLogSpec;
    private boolean showDiscreteValues = false;
    private boolean showRegression = false;
    private boolean showSum = false;
    private double sumDivisionFactor = 0;
    private SeriesType seriesType;
    private String fallBackYAxisName;
    private String dbLogSpec;


    public static ChartSeriesDescription getDiscreteValuesInstance(int columnName,
                                                                   String fileLogSpec,
                                                                   String dbLogSpec,
                                                                   SeriesType seriesType) {
        return new ChartSeriesDescription(columnName, fileLogSpec, dbLogSpec, true, false,
                false, 0, seriesType);
    }

    public static ChartSeriesDescription getRegressionValuesInstance(int columnName,
                                                                     String fileLogSpec,
                                                                     String dbLogSpec,
                                                                     SeriesType seriesType) {
        return new ChartSeriesDescription(columnName, fileLogSpec, dbLogSpec, false, true,
                false, 0, seriesType);
    }

    public static ChartSeriesDescription getSumInstance(int columnName, String fileLogSpec,
                                                        String dbLogSpec,
                                                        double divisionFactor,
                                                        SeriesType seriesType) {
        return new ChartSeriesDescription(columnName, fileLogSpec, dbLogSpec, false, false, true,
                divisionFactor, seriesType);
    }

    public ChartSeriesDescription(String columnName, String fileLogSpec, String dbLogSpec) {
        this.columnName = columnName;
        this.dbLogSpec = dbLogSpec;
        this.fileLogSpec = fileLogSpec;
    }

    public ChartSeriesDescription(String columnName, String fileLogSpec,String dbLogSpec,
                                  String fallBackYAxisName) {
        this.columnName = columnName;
        this.fileLogSpec = fileLogSpec;
        this.dbLogSpec = dbLogSpec;
        this.fallBackYAxisName = fallBackYAxisName;
    }

    public ChartSeriesDescription(int columnName, String fileLogSpec, String dbLogSpec,
                                  boolean showDiscreteValues,
                                  boolean showRegression, boolean showSum, double sumDivisionFactor,
                                  SeriesType seriesType) {
        this.columnName = AndFHEMApplication.getContext().getString(columnName);
        this.fileLogSpec = fileLogSpec;
        this.dbLogSpec = dbLogSpec;
        this.showDiscreteValues = showDiscreteValues;
        this.showRegression = showRegression;
        this.showSum = showSum;
        this.sumDivisionFactor = sumDivisionFactor;
        this.seriesType = seriesType;
    }

    // I am actually used by Android to create the parcel!!
    @SuppressWarnings("unused")
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

    public ChartSeriesDescription(int columnName, String fileLogSpec, String dbLogSpec,
                                  SeriesType seriesType) {
        this(columnName, fileLogSpec, dbLogSpec, false, false, false, 0, seriesType);
    }

    private ChartSeriesDescription(Bundle bundle) {
        this.columnName = bundle.getString("COLUMN_NAME");
        this.fileLogSpec = bundle.getString("FILELOG_SPEC");
        this.dbLogSpec = bundle.getString("DBLOG_SPEC");
        this.showDiscreteValues = bundle.getBoolean("SHOW_DISCRETE_VALUES");
        this.showRegression = bundle.getBoolean("SHOW_REGRESSION");
        this.showSum = bundle.getBoolean("SHOW_SUM");
        this.sumDivisionFactor = bundle.getDouble("SUM_DIVISION_FACTOR");
        this.fallBackYAxisName = bundle.getString("FALLBACK_Y_AXIS_NAME");

        String chart_type = bundle.getString("CHART_TYPE");
        if (chart_type != null) {
            this.seriesType = SeriesType.valueOf(chart_type);
        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString("COLUMN_NAME", columnName);
        bundle.putString("FILELOG_SPEC", fileLogSpec);
        bundle.putString("DBLOG_SPEC", dbLogSpec);
        bundle.putString("FALLBACK_Y_AXIS_NAME", fallBackYAxisName);
        bundle.putBoolean("SHOW_DISCRETE_VALUES", showDiscreteValues);
        bundle.putBoolean("SHOW_SUM", showSum);
        bundle.putBoolean("SHOW_REGRESSION", showRegression);
        bundle.putDouble("SUM_DIVISION_FACTOR", sumDivisionFactor);
        if (seriesType != null) {
            bundle.putString("CHART_TYPE", seriesType.name());
        }
        parcel.writeBundle(bundle);
    }

    public String getColumnName() {
        return columnName;
    }

    public SeriesType getSeriesType() {
        return seriesType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChartSeriesDescription that = (ChartSeriesDescription) o;

        return columnName.equals(that.columnName);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getFallBackYAxisName() {
        return fallBackYAxisName;
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

    public String getFileLogSpec() {
        return fileLogSpec;
    }

    public String getDbLogSpec() {
        return dbLogSpec;
    }
}
