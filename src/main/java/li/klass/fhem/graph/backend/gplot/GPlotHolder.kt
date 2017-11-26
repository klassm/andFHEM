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

package li.klass.fhem.graph.backend.gplot

import android.app.Application
import android.content.Context
import com.google.common.base.Optional
import com.google.common.collect.Maps.*
import li.klass.fhem.update.backend.command.execution.Command
import li.klass.fhem.update.backend.command.execution.CommandExecutionService
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GPlotHolder @Inject constructor(
        val commandExecutionService: CommandExecutionService,
        val gPlotParser: GPlotParser,
        val application: Application
) {

    private val definitions = newHashMap<String, Optional<GPlotDefinition>>()

    private var areDefaultFilesLoaded = false

    private fun loadDefaultGPlotFiles() {
        if (areDefaultFilesLoaded) {
            return
        }
        areDefaultFilesLoaded = true

        val defaultFiles = transformEntries(gPlotParser.defaultGPlotFiles, TO_OPTIONAL_DEFINITION)
        definitions.putAll(defaultFiles)
    }

    fun definitionFor(name: String, isConfigDb: Boolean): Optional<GPlotDefinition> {
        loadDefaultGPlotFiles()

        LOGGER.info("definitionFor(name={}, isConfigDb={})", name, isConfigDb)
        if (definitions.containsKey(name)) {
            LOGGER.info("definitionFor(name={}, isConfigDb={}) - definition found in cache", name, isConfigDb)
            return definitions[name]!!
        }

        LOGGER.info("definitionFor(name={}, isConfigDb={}) - loading definition from remote", name, isConfigDb)

        val result = if (isConfigDb)
            Optional.fromNullable(commandExecutionService.executeSync(Command("configdb fileshow ./www/gplot/$name.gplot"), applicationContext))
        else
            commandExecutionService.executeRequest("/gplot/$name.gplot", applicationContext)

        if (result.isPresent) {
            LOGGER.info("definitionFor(name={}, isConfigDb={}) - done loading, putting to cache", name, isConfigDb)
            val gplot = gPlotParser.parseSafe(result.get())
            definitions.put(name, gplot)
            return gplot
        } else {
            LOGGER.info("definitionFor(name={}, isConfigDb={}) - could not execute request, putting nothing to cache", name, isConfigDb)
            return Optional.absent()
        }
    }

    fun reset() {
        definitions.clear()
        areDefaultFilesLoaded = false
    }

    private val applicationContext: Context get() = application.applicationContext

    companion object {
        private val TO_OPTIONAL_DEFINITION = EntryTransformer<String, GPlotDefinition, Optional<GPlotDefinition>> { key, value -> Optional.of(value!!) }

        private val LOGGER = LoggerFactory.getLogger(GPlotHolder::class.java)
    }
}