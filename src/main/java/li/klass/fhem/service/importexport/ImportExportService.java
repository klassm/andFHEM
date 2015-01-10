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

package li.klass.fhem.service.importexport;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.inject.Inject;

import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.service.intent.NotificationIntentService;
import li.klass.fhem.service.room.FavoritesService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.CloseableUtil;
import li.klass.fhem.util.ReflectionUtil;
import li.klass.fhem.util.io.FileSystemService;
import li.klass.fhem.util.preferences.SharedPreferencesService;

public class ImportExportService {

    private final Map<String, String> sharedPreferencesExportKeys;

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm");
    public static final String SHARED_PREFERENCES_FILE_NAME = "sharedPreferences.json";
    private Logger LOGGER = LoggerFactory.getLogger(ImportExportService.class);

    @Inject
    SharedPreferencesService sharedPreferencesService;

    @Inject
    FileSystemService fileSystemService;

    @Inject public ImportExportService(ApplicationProperties applicationProperties) {
        sharedPreferencesExportKeys = ImmutableMap.<String, String>builder()
                .put("CONNECTIONS", ConnectionService.PREFERENCES_NAME)
                .put("FAVORITES", FavoritesService.PREFERENCES_NAME)
                .put("NOTIFICATIONS", NotificationIntentService.PREFERENCES_NAME)
                .put("DEFAULT", applicationProperties.getApplicationSharedPreferencesName())
                .build();
    }

    public File exportSettings() {
        Map<String, Map<String, ?>> toExport = Maps.newHashMap();

        for (Map.Entry<String, String> exportValue : sharedPreferencesExportKeys.entrySet()) {
            Map<String, ?> values = sharedPreferencesService.listAllFrom(exportValue.getValue());
            toExport.put(exportValue.getKey(), toExportValues(values));
        }

        return createZipFrom(toExport);
    }

    public Map<String, String> toExportValues(Map<String, ?> values) {
        Map<String, String> toExport = Maps.newHashMap();
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            toExport.put(entry.getKey(), entry.getValue() + "/" + entry.getValue().getClass().getName());
        }
        return toExport;
    }


    public Map<String, ?> toImportValues(Map<String, String> values) {
        Map<String, Object> toImport = Maps.newHashMap();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            int separator = entry.getValue().lastIndexOf("/");
            Class<?> clazz = ReflectionUtil.classForName(entry.getValue().substring(separator + 1));
            String value = entry.getValue().substring(0, separator);
            toImport.put(entry.getKey(), typedValueFor(value, clazz));
        }
        return toImport;
    }

    private Object typedValueFor(String value, Class<?> type) {
        if (type.isAssignableFrom(Integer.class)) {
            return Integer.parseInt(value);
        } else if  (type.isAssignableFrom(Float.class)) {
            return Float.parseFloat(value);
        } else if (type.isAssignableFrom(Boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (type.isAssignableFrom(String.class)) {
            return value;
        } else {
            throw new IllegalArgumentException("don't know how to handle " + type.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public void importSettings(File file) {
        ZipFile zipFile;
        InputStreamReader importReader = null;

        try {
            zipFile = new ZipFile(file);
            zipFile.extractFile(SHARED_PREFERENCES_FILE_NAME, fileSystemService.getCacheDir().getAbsolutePath());

            importReader = new InputStreamReader(new FileInputStream(new File(fileSystemService.getCacheDir(), SHARED_PREFERENCES_FILE_NAME)));
            Map<String, Map<String, String>> content = new Gson().fromJson(importReader, Map.class);
            for (Map.Entry<String, Map<String, String>> entry : content.entrySet()) {
                sharedPreferencesService.writeAllIn(sharedPreferencesExportKeys.get(entry.getKey()), toImportValues(entry.getValue()));
            }
        } catch (Exception e) {
            LOGGER.error("importSettings(" + file.getAbsolutePath() + ") - cannot import", e);
        } finally {
            CloseableUtil.close(importReader);
        }
    }

    private File createZipFrom(Map<String, Map<String, ?>> toExport) {

        try {
            String exportedJson = new Gson().toJson(toExport);

            File exportFile = new File(getExportDirectory(), getBackupFileName());
            LOGGER.info("export file location is {}", exportFile.getAbsolutePath());
            ZipFile zipFile = new ZipFile(exportFile);

            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setFileNameInZip(SHARED_PREFERENCES_FILE_NAME);
            parameters.setSourceExternalStream(true);

            zipFile.addStream(new ByteArrayInputStream(exportedJson.getBytes(Charsets.UTF_8)), parameters);

            return exportFile;
        } catch (ZipException e) {
            LOGGER.error("cannot create zip", e);
            throw new IllegalStateException(e);
        }
    }

    private String getBackupFileName() {
        return "andFHEM-" + DATE_TIME_FORMATTER.print(DateTime.now()) + ".backup";
    }

    public File getExportDirectory() {
        return fileSystemService.getOrCreateDirectoryIn(fileSystemService.getDocumentsFolder(), "andFHEM");
    }
}
