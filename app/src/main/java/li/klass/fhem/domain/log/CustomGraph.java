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

package li.klass.fhem.domain.log;

import java.io.Serializable;

public class CustomGraph implements Serializable {
    public final String columnSpecification;
    public final String description;
    public final String yAxisName;

    public CustomGraph(String columnSpecification, String description, String yAxisName) {
        this.columnSpecification = columnSpecification;
        this.description = description;
        this.yAxisName = yAxisName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomGraph that = (CustomGraph) o;

        return !(description != null ? !description.equals(that.description) : that.description != null) &&
                !(columnSpecification != null ? !columnSpecification.equals(that.columnSpecification) : that.columnSpecification != null) &&
                !(yAxisName != null ? !yAxisName.equals(that.yAxisName) : that.yAxisName != null);
    }

    @Override
    public String toString() {
        return "CustomGraph{" +
                "columnSpecification='" + columnSpecification + '\'' +
                ", description='" + description + '\'' +
                ", yAxisName='" + yAxisName + '\'' +
                '}';
    }
}
