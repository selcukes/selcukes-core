/*
 * Copyright (c) Ramesh Babu Prudhvi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.selcukes.core.helper;

import io.github.selcukes.core.exception.SelcukesException;
import io.github.selcukes.core.exception.WebDriverBinaryException;
import io.github.selcukes.core.logging.Logger;
import io.github.selcukes.core.logging.LoggerFactory;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Objects;
import java.util.Set;

@UtilityClass
public class FileHelper {

    private final Logger LOGGER = LoggerFactory.getLogger(FileHelper.class);
    protected final String SUPPORT_FOLDER = "support";
    public final String RESOURCE_SEPARATOR = "/";

    public String driversFolder(String path) {
        File file = new File(path);
        for (String item : Objects.requireNonNull(file.list())) {

            if (SUPPORT_FOLDER.equals(item)) {
                return file.getAbsolutePath() + RESOURCE_SEPARATOR + SUPPORT_FOLDER + RESOURCE_SEPARATOR;
            }
        }
        return driversFolder(file.getParent());
    }

    public void setFileExecutable(String fileName) {
        try {
            final Path filepath = Paths.get(fileName);
            final Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(filepath);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(filepath, permissions);
        } catch (Exception e) {
            LOGGER.error(e::getMessage);
            throw new WebDriverBinaryException("Unable to set WebDriver Binary file as executable :" + e);
        }
    }

    public String systemFilePath(String filePath) {
        String fileSeparator = System.getProperty("file.separator");

        if (!fileSeparator.isEmpty() && "\\".equals(fileSeparator)) {
            int beginIndex = 0;

            if (filePath.startsWith(RESOURCE_SEPARATOR)) {
                beginIndex = 1;
            }

            return filePath.substring(beginIndex).replace(RESOURCE_SEPARATOR, "\\");
        }

        return filePath;
    }

    public void createDirectory(File dirName) {
        boolean dirExists = dirName.exists();
        if (!dirExists) {
            LOGGER.trace(() -> "Creating directory: " + dirName.getName());
            dirExists = dirName.mkdirs();
            if (dirExists) {
                LOGGER.trace(() -> dirName.getName() + " directory created...");
            }
        }
    }

    public void deleteFilesInDirectory(File dirName) {
        try {
            FileUtils.cleanDirectory(dirName);
        } catch (IOException e) {
            LOGGER.error(e::getMessage);
            throw new SelcukesException("\"" + dirName + "\" is not a directory or a file to delete.", e);
        }
    }

    public void deleteFile(File fileName) {
        if (fileName.exists())
            FileUtils.deleteQuietly(fileName);
    }

    public void renameFile(File from, File to) {
        if (!from.renameTo(to)) {
            throw new SelcukesException("Failed to rename " + from + " to " + to);
        }
    }

    public Path createTempDirectory() {
        try {
            return Files.createTempDirectory(null);
        } catch (IOException e) {
            throw new SelcukesException("Unable to create temp directory : ", e);
        }
    }

    public File createTempFile() {
        try {
            File tempFile = File.createTempFile("WDB", ".temp");
            tempFile.deleteOnExit();
            return tempFile;
        } catch (IOException e) {
            throw new SelcukesException("Unable to create temp file : ", e);
        }
    }

    public byte[] toByteArray(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new SelcukesException("Unable to convert file to ByteArray  : ", e);
        }
    }

    public String getTempDir() {
        return createTempFile().getParent();
    }

    public File loadResource(String file) {
        return new File(Objects.requireNonNull(FileHelper.class.getClassLoader().getResource(file)).getFile());
    }

    public File loadThreadResource(String file) {
        return new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(file)).getFile());
    }

    public InputStream loadThreadResourceAsStream(String file) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
    }

    public InputStream loadResourceAsStream(String file) {
        return FileHelper.class.getClassLoader().getResourceAsStream(file);
    }
}

