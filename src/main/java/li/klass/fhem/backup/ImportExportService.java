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

package li.klass.fhem.backup;

import android.content.Context;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.exception.ZipExceptionConstants;
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

import li.klass.fhem.connection.backend.ConnectionService;
import li.klass.fhem.devices.favorites.backend.FavoritesService;
import li.klass.fhem.service.NotificationService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.CloseableUtil;
import li.klass.fhem.util.ReflectionUtil;
import li.klass.fhem.util.io.FileSystemService;
import li.klass.fhem.util.preferences.SharedPreferencesService;

import static com.google.common.base.Preconditions.checkArgument;

public class ImportExportService {
    public enum ImportStatus {
        SUCCESS, INVALID_FILE, WRONG_PASSWORD
    }

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm");
    public static final String SHARED_PREFERENCES_FILE_NAME = "sharedPreferences.json";
    private Logger LOGGER = LoggerFactory.getLogger(ImportExportService.class);

    @Inject
    SharedPreferencesService sharedPreferencesService;

    @Inject
    FileSystemService fileSystemService;

    @Inject
    FavoritesService favoritesService;

    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    public ImportExportService() {
    }

    protected Map<String, String> getSharedPreferencesExportKeys(Context context) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder()
                .put("CONNECTIONS", ConnectionService.Companion.getPREFERENCES_NAME())
                .put("NOTIFICATIONS", NotificationService.PREFERENCES_NAME)
                .put("DEFAULT", applicationProperties.getApplicationSharedPreferencesName(context));
        for (String preferenceName : favoritesService.getPreferenceNames(context)) {
            builder.put("FAVORITE_" + preferenceName, preferenceName);
        }
        return builder.build();
    }

    public File exportSettings(String password, Context context) {
        Map<String, Map<String, ?>> toExport = Maps.newHashMap();

        for (Map.Entry<String, String> exportValue : getSharedPreferencesExportKeys(context).entrySet()) {
            Map<String, ?> values = sharedPreferencesService.listAllFrom(exportValue.getValue(), context);
            toExport.put(exportValue.getKey(), toExportValues(values));
        }

        return createZipFrom(toExport, Optional.fromNullable(password));
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
        } else if (type.isAssignableFrom(Float.class)) {
            return Float.parseFloat(value);
        } else if (type.isAssignableFrom(Boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (type.isAssignableFrom(String.class)) {
            return value;
        } else if (type.isAssignableFrom(Double.class)) {
            return Double.parseDouble(value);
        } else {
            throw new IllegalArgumentException("don't know how to handle " + type.getName());
        }
    }

    public boolean isValidZipFile(File file) {
        try {
            ZipFile zipFile = new ZipFile(file);
            return zipFile.isValidZipFile();
        } catch (ZipException e) {
            LOGGER.error("error while reading zip file", e);
            return false;
        }
    }

    public boolean isEncryptedFile(File file) {
        try {
            ZipFile zipFile = new ZipFile(file);
            checkArgument(zipFile.isValidZipFile());
            return zipFile.isEncrypted();
        } catch (ZipException e) {
            LOGGER.error("error while reading zip file", e);
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public ImportStatus importSettings(File file, String password, Context context) {
        ZipFile zipFile;
        InputStreamReader importReader = null;

        try {
            zipFile = new ZipFile(file);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(MoreObjects.firstNonNull(password, ""));
            }

            if (zipFile.getFileHeader(SHARED_PREFERENCES_FILE_NAME) == null) {
                return ImportStatus.INVALID_FILE;
            }

            zipFile.extractFile(SHARED_PREFERENCES_FILE_NAME, fileSystemService.getCacheDir(context).getAbsolutePath());

            importReader = new InputStreamReader(new FileInputStream(new File(fileSystemService.getCacheDir(context), SHARED_PREFERENCES_FILE_NAME)));
            Map<String, Map<String, String>> content = new Gson().fromJson(importReader, Map.class);
            for (Map.Entry<String, Map<String, String>> entry : content.entrySet()) {
                sharedPreferencesService.writeAllIn(getSharedPreferencesExportKeys(context).get(entry.getKey()), toImportValues(entry.getValue()), context);
            }
            return ImportStatus.SUCCESS;

        } catch (ZipException e) {
            LOGGER.error("importSettings(" + file.getAbsolutePath() + ") - cannot import", e);
            if (e.getCode() == ZipExceptionConstants.WRONG_PASSWORD || e.getMessage().contains("Wrong Password")) {
                return ImportStatus.WRONG_PASSWORD;
            } else {
                return ImportStatus.INVALID_FILE;
            }
        } catch (Exception e) {
            LOGGER.error("importSettings(" + file.getAbsolutePath() + ") - cannot import", e);
            return ImportStatus.INVALID_FILE;
        } finally {
            CloseableUtil.close(importReader);
        }
    }

    private File createZipFrom(Map<String, Map<String, ?>> toExport, Optional<String> password) {

        ByteArrayInputStream stream = null;
        try {
            String exportedJson = new Gson().toJson(toExport);

            File exportFile = new File(getExportDirectory(), getBackupFileName());
            LOGGER.info("export file location is {}", exportFile.getAbsolutePath());
            ZipFile zipFile = new ZipFile(exportFile);


            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setFileNameInZip(SHARED_PREFERENCES_FILE_NAME);
            parameters.setSourceExternalStream(true);
            if (password.isPresent()) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
                parameters.setPassword(password.get());
            }

            stream = new ByteArrayInputStream(exportedJson.getBytes(Charsets.UTF_8));
            zipFile.addStream(stream, parameters);

            return exportFile;
        } catch (ZipException e) {
            LOGGER.error("cannot create zip", e);
            throw new IllegalStateException(e);
        } finally {
            CloseableUtil.close(stream);
        }
    }

    private String getBackupFileName() {
        return "andFHEM-" + DATE_TIME_FORMATTER.print(DateTime.now()) + ".backup";
    }

    public File getExportDirectory() {
        return fileSystemService.getOrCreateDirectoryIn(fileSystemService.getDocumentsFolder(), "andFHEM");
    }
}
