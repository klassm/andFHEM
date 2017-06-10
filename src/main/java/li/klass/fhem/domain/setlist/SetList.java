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

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import li.klass.fhem.domain.setlist.typeEntry.NoArgSetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.NotFoundSetListEntry;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class SetList implements Serializable {
    private Map<String, SetListEntry> entries = Maps.newHashMap();

    public SetList parse(String text) {
        if (isEmpty(text)) return this;

        text = text.trim();

        String[] parts = text.split(" ");
        for (String part : parts) {
            handlePart(part);
        }

        return this;
    }

    private void handlePart(String part) {
        if (part.matches("[^:]+:noArg$")) {
            part = part.replaceAll(":noArg$", "");
        }
        if (!part.contains(":")) {
            entries.put(part, new NoArgSetListEntry(part));
            return;
        }

        String[] keyValue = part.split(":", 2);

        String key = StringUtils.trimToNull(keyValue[0]);
        key = key == null ? "state" : key;
        String value = keyValue.length == 2 ? keyValue[1] : "";
        if (StringUtils.isEmpty(value)) return;

        Optional<SetListItem> setListEntry = handle(key, value);
        if (setListEntry.isPresent()) {
            entries.put(key, setListEntry.get());
        }
    }

    private Optional<SetListItem> handle(String key, String value) {
        String[] parts = value.split(",");

        SetListItemType type = findType(parts);
        return type.getSetListItemFor(key, parts);
    }

    private SetListItemType findType(String[] parts) {
        for (SetListItemType type : SetListItemType.values()) {
            if (type.supports(parts)) {
                return type;
            }
        }
        return parts.length == 0 || (parts.length >= 1 && "colorpicker".equalsIgnoreCase(parts[0]))
                ? SetListItemType.NO_ARG
                : SetListItemType.GROUP;
    }

    public List<String> getSortedKeys() {
        List<String> keys = newArrayList(entries.keySet());
        Collections.sort(keys);
        return keys;
    }


    public Map<String, SetListEntry> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    public SetListEntry get(String key) {
        return entries.containsKey(key)
                ? entries.get(key)
                : new NotFoundSetListEntry(key);
    }

    public boolean contains(String... keys) {
        if (keys == null) return false;

        for (String key : keys) {
            if (!entries.containsKey(key)) {
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
            SetListEntry value = entries.get(key);
            parts.add(value.asText());
        }

        return StringUtils.join(parts, " ");
    }
}
