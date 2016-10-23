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

package li.klass.fhem.service.graph.gplot;

import android.content.Context;

import com.google.common.base.Optional;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.service.CommandExecutionService;

import static com.google.common.collect.Maps.EntryTransformer;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformEntries;

@Singleton
public class GPlotHolder {
    public static final EntryTransformer<String, GPlotDefinition, Optional<GPlotDefinition>> TO_OPTIONAL_DEFINITION =
            new EntryTransformer<String, GPlotDefinition, Optional<GPlotDefinition>>() {
                @Override
                public Optional<GPlotDefinition> transformEntry(String key, GPlotDefinition value) {
                    return Optional.of(value);
                }
            };

    private final Map<String, Optional<GPlotDefinition>> definitions = newHashMap();

    private boolean areDefaultFilesLoaded = false;

    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    GPlotParser gPlotParser;

    @Inject
    public GPlotHolder() {
    }

    private void loadDefaultGPlotFiles() {
        if (areDefaultFilesLoaded) {
            return;
        }
        areDefaultFilesLoaded = true;

        Map<String, Optional<GPlotDefinition>> defaultFiles = transformEntries(gPlotParser.getDefaultGPlotFiles(), TO_OPTIONAL_DEFINITION);
        definitions.putAll(defaultFiles);
    }

    public Optional<GPlotDefinition> definitionFor(String name, boolean isConfigDb) {
        loadDefaultGPlotFiles();

        if (definitions.containsKey(name)) {
            return definitions.get(name);
        }

        Context applicationContext = AndFHEMApplication.getContext();
        Optional<String> result = isConfigDb
                ? Optional.fromNullable(commandExecutionService.executeSafely("configdb fileshow ./www/gplot/" + name + ".gplot", Optional.<String>absent(), applicationContext))
                : commandExecutionService.executeRequest("/gplot/" + name + ".gplot", applicationContext);

        if (result.isPresent()) {
            definitions.put(name, gPlotParser.parseSafe(result.get()));
        } else {
            definitions.put(name, Optional.<GPlotDefinition>absent());
        }

        return definitions.get(name);
    }

    public void reset() {
        definitions.clear();
        areDefaultFilesLoaded = false;
    }
}
