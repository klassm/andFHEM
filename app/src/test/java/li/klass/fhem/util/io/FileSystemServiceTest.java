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

package li.klass.fhem.util.io;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.io.File;

import li.klass.fhem.testutil.MockitoRule;

import static org.assertj.core.api.Assertions.assertThat;

public class FileSystemServiceTest {

    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @InjectMocks
    public FileSystemService fileSystemService;

    @Test
    public void should_create_directory_if_it_does_not_exist() {
        // given
        File tempDir = new File(System.getProperty("java.io.tmpdir"));

        // when
        File newDirectory = fileSystemService.getOrCreateDirectoryIn(tempDir, "someDirectory");

        // then
        assertThat(newDirectory).exists();
        assertThat(newDirectory).isDirectory();
        assertThat(newDirectory.getAbsolutePath()).isEqualToIgnoringCase(tempDir.getAbsolutePath() + File.separator + "someDirectory");
        newDirectory.delete();
    }

    @Test
    public void should_return_an_existing_directory_with_the_same_name() {
        // given
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        prepare(new File(tempDir, "someValue")).mkdir();

        // when
        File newDirectory = fileSystemService.getOrCreateDirectoryIn(tempDir, "someValue");

        // then
        assertThat(newDirectory).exists();
        assertThat(newDirectory).isDirectory();
        newDirectory.delete();
    }

    private File prepare(File file) {
        if (file.exists()) {
            file.delete();
        }
        file.deleteOnExit();
        return file;
    }
}