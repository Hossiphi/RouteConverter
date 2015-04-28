/*
    This file is part of RouteConverter.

    RouteConverter is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    RouteConverter is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RouteConverter; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Copyright (C) 2007 Christian Pesch. All Rights Reserved.
*/
package slash.navigation.download.tools;

import org.apache.commons.cli.*;
import slash.navigation.common.BoundingBox;
import slash.navigation.datasources.*;
import slash.navigation.datasources.binding.*;
import slash.navigation.download.Checksum;
import slash.navigation.download.Download;
import slash.navigation.download.DownloadManager;
import slash.navigation.download.FileAndChecksum;
import slash.navigation.download.tools.base.BaseDownloadTool;
import slash.navigation.graphhopper.PbfUtil;
import slash.navigation.maps.helpers.MapUtil;
import slash.navigation.rest.Post;

import javax.xml.bind.JAXBException;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.apache.commons.cli.OptionBuilder.withArgName;
import static slash.common.io.Directories.ensureDirectory;
import static slash.navigation.datasources.DataSourceManager.DOT_ZIP;
import static slash.navigation.datasources.helpers.DataSourcesUtil.*;
import static slash.navigation.download.Action.*;
import static slash.navigation.download.State.Failed;
import static slash.navigation.download.State.NotModified;

/**
 * Updates the resources from the DataSources catalog from websites
 *
 * @author Christian Pesch
 */

public class UpdateCatalog extends BaseDownloadTool {
    private static final Logger log = Logger.getLogger(ScanWebsite.class.getName());
    private static final String MIRROR_ARGUMENT = "mirror";
    private static final String DOT_MAP = ".map";
    private static final String DOT_PBF = ".pbf";

    private DataSourceManager dataSourceManager;
    private java.io.File mirror;
    private int updateCount = 0;

    private void open() throws IOException {
        dataSourceManager = new DataSourceManager(new DownloadManager(new java.io.File(getSnapshotDirectory(), "update-queue.xml")));
        dataSourceManager.getDownloadManager().loadQueue();
    }

    private void close() {
        dataSourceManager.getDownloadManager().saveQueue();
        dataSourceManager.dispose();
    }

    private void update() throws IOException, JAXBException {
        DataSource source = loadDataSource(getId());
        open();

        DatasourceType datasourceType = asDatasourceType(source);
        for (File file : source.getFiles()) {
            updateFile(datasourceType, file);
        }

        for (Map map : source.getMaps()) {
            updateMap(datasourceType, map);
        }

        for (Theme theme : source.getThemes()) {
            updateTheme(datasourceType, theme);
        }

        if (getDownloadableCount(datasourceType) > 0)
            updateUris(datasourceType);

        log.info(format("Updated %d URIs out of %d URIs", updateCount,
                source.getFiles().size() + source.getMaps().size() + source.getThemes().size()));
        close();
    }

    private void updateFile(DatasourceType datasourceType, File file) throws IOException {
        String url = datasourceType.getBaseUrl() + file.getUri();

        // HEAD for last modified, content length, etag
        Download download = head(url);
        if (download.getState().equals(NotModified))
            return;

        if (download.getState().equals(Failed)) {
            log.severe(format("Failed to download %s as a file", file.getUri()));
            return;
        }

        Checksum checksum = download.getFile().getActualChecksum();
        FileType fileType = createFileType(file.getUri(), singletonList(checksum), null);
        datasourceType.getFile().add(fileType);

        if (file.getUri().endsWith(DOT_ZIP)) {
            List<FragmentType> fragmentTypes = new ArrayList<>();

            try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(download.getFile().getFile()))) {
                ZipEntry entry = zipInputStream.getNextEntry();
                while (entry != null) {
                    if (!entry.isDirectory()) {
                        String key = extractKey(entry.getName());
                        if (key != null) {
                            log.info(format("Key %s maps to URI %s", key, file.getUri()));
                            fragmentTypes.add(createFragmentType(key, entry, zipInputStream));

                            // do not close zip input stream
                            zipInputStream.closeEntry();
                        }
                    }
                    entry = zipInputStream.getNextEntry();
                }
            }
            fileType.getFragment().addAll(fragmentTypes);

        } else if (file.getUri().endsWith(DOT_PBF)) {
            // GET with range for .pbf header
            if (!download.getFile().getFile().exists())
                download = downloadPartial(url, checksum.getContentLength());

            if (download.getState().equals(Failed)) {
                log.severe(format("Failed to download %s partially as a pbf", file.getUri()));
            } else {

                BoundingBox boundingBox = PbfUtil.extractBoundingBox(download.getFile().getFile());
                if (boundingBox != null)
                    fileType.setBoundingBox(asBoundingBoxType(boundingBox));
            }
        }

        updatePartially(datasourceType);
    }

    private void updateMap(DatasourceType datasourceType, Map map) throws IOException {
        String url = datasourceType.getBaseUrl() + map.getUri();

        // HEAD for last modified, content length, etag
        Download download = head(url);
        if (download.getState().equals(NotModified))
            return;

        if (download.getState().equals(Failed)) {
            log.severe(format("Failed to download %s as a map", map.getUri()));
            return;
        }

        Checksum checksum = download.getFile().getActualChecksum();
        MapType mapType = createMapType(map.getUri(), singletonList(checksum), null);
        datasourceType.getMap().add(mapType);

        // GET with range for .zip or .map header
        if (!download.getFile().getFile().exists())
            download = downloadPartial(url, checksum.getContentLength());

        if (download.getState().equals(Failed)) {
            log.severe(format("Failed to download %s partially as a map", map.getUri()));
            return;
        }

        if (map.getUri().endsWith(DOT_ZIP)) {
            try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(download.getFile().getFile()))) {
                ZipEntry entry = zipInputStream.getNextEntry();
                while (entry != null) {
                    if (!entry.isDirectory() && entry.getName().endsWith(DOT_MAP)) {
                        log.info(format("Found map %s in URI %s", entry.getName(), map.getUri()));
                        mapType.getFragment().add(createFragmentType(entry.getName(), entry.getTime(), entry.getSize()));

                        BoundingBox boundingBox = MapUtil.extractBoundingBox(zipInputStream, entry.getSize());
                        if (boundingBox != null)
                            mapType.setBoundingBox(asBoundingBoxType(boundingBox));

                        // do not close zip input stream and cope with partially copied zips
                        try {
                            zipInputStream.closeEntry();
                        } catch (EOFException e) {
                            // intentionally left empty
                        }
                    }

                    try {
                        entry = zipInputStream.getNextEntry();
                    } catch (EOFException e) {
                        entry = null;
                    }
                }
            }

        } else if (map.getUri().endsWith(DOT_MAP)) {
            log.info(format("Found map %s", map.getUri()));
            BoundingBox boundingBox = MapUtil.extractBoundingBox(download.getFile().getFile());
            if (boundingBox != null)
                mapType.setBoundingBox(asBoundingBoxType(boundingBox));
        } else
            log.warning(format("Ignoring %s as a map", map.getUri()));

        updatePartially(datasourceType);
    }

    private void updateTheme(DatasourceType datasourceType, Theme theme) throws IOException {
        String url = datasourceType.getBaseUrl() + theme.getUri();

        // GET for local mirror
        Download download = download(url);
        if (download.getState().equals(NotModified))
            return;

        if (download.getState().equals(Failed)) {
            log.severe(format("Failed to download %s as a theme", theme.getUri()));
            return;
        }

        Checksum checksum = download.getFile().getActualChecksum();
        ThemeType themeType = createThemeType(theme.getUri(), singletonList(checksum), null);
        datasourceType.getTheme().add(themeType);

        if (theme.getUri().endsWith(DOT_ZIP)) {
            List<FragmentType> fragmentTypes = new ArrayList<>();
            try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(download.getFile().getFile()))) {
                ZipEntry entry = zipInputStream.getNextEntry();
                while (entry != null) {
                    if (!entry.isDirectory()) {
                        log.info(format("Found theme file %s in URI %s", entry.getName(), theme.getUri()));
                        fragmentTypes.add(createFragmentType(entry.getName(), entry, zipInputStream));

                        // do not close zip input stream
                        zipInputStream.closeEntry();
                    }

                    entry = zipInputStream.getNextEntry();
                }
            }
            themeType.getFragment().addAll(fragmentTypes);
        }
    }

    private Download head(String url) {
        Download download = dataSourceManager.getDownloadManager().queueForDownload("HEAD for " + url, url, Head,
                null, new FileAndChecksum(createMirrorFile(url), null), null);
        dataSourceManager.getDownloadManager().waitForCompletion(singletonList(download));
        return download;
    }

    private Download download(String url) {
        Download download = dataSourceManager.getDownloadManager().queueForDownload("GET for " + url, url, Copy,
                null, new FileAndChecksum(createMirrorFile(url), null), null);
        dataSourceManager.getDownloadManager().waitForCompletion(singletonList(download));
        return download;
    }

    private Download downloadPartial(String url, long fileSize) throws IOException {
        Download download = dataSourceManager.getDownloadManager().queueForDownload("GET 16k for " + url, url, GetRange,
                null, new FileAndChecksum(createMirrorFile(url), new Checksum(null, fileSize, null)), null);
        dataSourceManager.getDownloadManager().waitForCompletion(singletonList(download));
        return download;
    }

    private java.io.File createMirrorFile(String url) {
        String filePath = url.substring(url.indexOf("//") + 2, url.lastIndexOf('/'));
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        java.io.File directory = new java.io.File(mirror, filePath);
        return new java.io.File(ensureDirectory(directory), fileName);
    }

    private static final Pattern KEY_PATTERN = Pattern.compile(".*([N|S]\\d{2}[E|W]\\d{3}).*", CASE_INSENSITIVE);

    private String extractKey(String string) {
        Matcher matcher = KEY_PATTERN.matcher(string);
        if (!matcher.matches()) {
            log.warning(string + " does not match key pattern");
            return null;
        }
        return matcher.group(1).toUpperCase();
    }

    private void updatePartially(DatasourceType datasourceType) throws IOException {
        if (getDownloadableCount(datasourceType) >= MAXIMUM_UPDATE_COUNT) {
            updateUris(datasourceType);

            datasourceType.getFile().clear();
            datasourceType.getMap().clear();
            datasourceType.getTheme().clear();
        }
    }

    private int getDownloadableCount(DatasourceType datasourceType) {
        return datasourceType.getFile().size() + datasourceType.getMap().size() + datasourceType.getTheme().size();
    }

    private String updateUris(DatasourceType dataSourceType) throws IOException {
        String xml = toXml(dataSourceType);
        log.info(format("Updating URIs:\n%s", xml));
        String dataSourcesUrl = getDataSourcesUrl();
        Post request = new Post(dataSourcesUrl, getCredentials());
        request.addFile("file", xml.getBytes());
        request.setAccept("application/xml");
        request.setSocketTimeout(SOCKET_TIMEOUT);

        String result = null;
        try {
            result = request.executeAsString();
            log.info(format("Updated URIs with result:\n%s", result));
            updateCount += getDownloadableCount(dataSourceType);
        }
        catch(Exception e) {
            log.severe(format("Cannot update URIs: %s", e));
        }
        return result;
    }

    private void run(String[] args) throws Exception {
        CommandLine line = parseCommandLine(args);
        setId(line.getOptionValue(ID_ARGUMENT));
        setDataSourcesServer(line.getOptionValue(DATASOURCES_SERVER_ARGUMENT));
        setDataSourcesUserName(line.getOptionValue(DATASOURCES_USERNAME_ARGUMENT));
        setDataSourcesPassword(line.getOptionValue(DATASOURCES_PASSWORD_ARGUMENT));
        mirror = new java.io.File(line.getOptionValue(MIRROR_ARGUMENT));
        update();
        System.exit(0);
    }

    @SuppressWarnings("AccessStaticViaInstance")
    private CommandLine parseCommandLine(String[] args) throws ParseException {
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption(withArgName(ID_ARGUMENT).hasArgs().isRequired().withLongOpt("id").
                withDescription("ID of the data source").create());
        options.addOption(withArgName(DATASOURCES_SERVER_ARGUMENT).hasArgs(1).withLongOpt("server").
                withDescription("Data sources server").create());
        options.addOption(withArgName(DATASOURCES_USERNAME_ARGUMENT).hasArgs(1).withLongOpt("username").
                withDescription("Data sources server user name").create());
        options.addOption(withArgName(DATASOURCES_PASSWORD_ARGUMENT).hasArgs(1).withLongOpt("password").
                withDescription("Data sources server password").create());
        options.addOption(withArgName(MIRROR_ARGUMENT).hasArgs(1).isRequired().withLongOpt("mirror").
                withDescription("Filesystem path to mirror resources").create());
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(getClass().getSimpleName(), options);
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
        new UpdateCatalog().run(args);
    }
}
