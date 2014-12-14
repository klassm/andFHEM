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

package li.klass.fhem.domain.setlist;

import com.google.common.collect.Maps;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class SetList implements Serializable  {
    private Map<String, SetListValue> entries = Maps.newHashMap();

    public void parse(String text) {
        if (isEmpty(text)) return;

        text = text.trim();

        String[] parts = text.split(" ");
        for (String part : parts) {
            handlePart(part);
        }
    }

    private void handlePart(String part) {
        if (! part.contains(":")) {
            entries.put(part, SetListEmptyValue.INSTANCE);
            return;
        }

        String[] keyValue = part.split(":");
        String key = keyValue[0];
        String value = keyValue.length == 2 ? keyValue[1] : "";

        entries.put(key, handleValue(value));
    }

    private SetListValue handleValue(String value) {
        String[] parts = value.split(",");

        if (parts.length == 4 && parts[0].equals("slider")) {
            return new SetListSliderValue(parts);
        }

        return new SetListGroupValue(parts);
    }

    public List<String> getSortedKeys() {
        List<String> keys = newArrayList(entries.keySet());
        Collections.sort(keys);
        return keys;
    }


    public Map<String, SetListValue> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    public SetListValue get(String key) {
        return entries.get(key);
    }

    public boolean contains(String ... keys) {
        if (keys == null) return false;

        for (String key : keys) {
            if (! entries.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    public int size() {
        return entries.size();
    }

    @Override
    public String toString() {
        List<String> keys = getSortedKeys();

        List<String> parts = newArrayList();
        for (String key : keys) {
            SetListValue value = entries.get(key);
            String text = value.asText();
            if (text == null) {
                parts.add(key);
            } else {
                parts.add(key + ":" + text);
            }
        }

        return StringUtils.join(parts, " ");
    }
}
