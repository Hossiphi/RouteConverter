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
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Copyright (C) 2007 Christian Pesch. All Rights Reserved.
*/

package slash.navigation.converter.gui.mapview;

import org.jdesktop.jdic.browser.WebBrowserEvent;
import org.jdesktop.jdic.browser.WebBrowserListener;
import org.jdesktop.jdic.browser.WebBrowser;
import org.jdesktop.jdic.init.JdicManager;
import slash.navigation.BaseNavigationPosition;
import slash.navigation.Wgs84Position;
import slash.navigation.converter.gui.RouteConverter;
import slash.navigation.converter.gui.models.CharacteristicsModel;
import slash.navigation.converter.gui.models.PositionsModel;
import slash.navigation.util.Calculation;
import slash.navigation.util.Conversion;
import slash.navigation.util.Externalization;
import slash.navigation.util.Platform;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Displays the positions of a route.
 *
 * @author Christian Pesch
 */

public class MapView {
    private static final Preferences preferences = Preferences.userNodeForPackage(MapView.class);
    private static final Logger log = Logger.getLogger(MapView.class.getName());
    private static final String MAP_TYPE_PREFERENCE = "mapType";
    private static final int MAXIMUM_POLYLINE_SEGMENT_LENGTH = 35;
    private static final int MAXIMUM_POLYLINE_POSITION_COUNT = 1500;
    private static final int MAXIMUM_DIRECTIONS_SEGMENT_LENGTH = 20;
    private static final int MAXIMUM_DIRECTIONS_POSITION_COUNT = 200;
    private static final int MAXIMUM_MARKER_SEGMENT_LENGTH = 4;
    private static final int MAXIMUM_MARKER_POSITION_COUNT = 16;
    private static final int MAXIMUM_SELECTION_COUNT = 10;
    private static final int[] ZOOMLEVEL_SCALE = {
            400000000,
            200000000,
            100000000,
            50000000,
            25000000,
            12500000,
            6400000,
            3200000,
            1600000,
            800000,
            400000,
            200000,
            100000,
            50000,
            25000,
            12500,
            6400,
            3200
    };
    private static final int MAXIMUM_ZOOMLEVEL_FOR_SIGNIFICANCE_CALCULATION = 16;

    private WebBrowser webBrowser;
    private ServerSocket dragListenerServerSocket;
    private PositionsModel positionsModel;
    private CharacteristicsModel characteristicsModel;
    private Thread mapViewRouteUpdater, mapViewPositionUpdater, mapViewDragListener;
    private final Object notificationMutex = new Object();
    private boolean initialized = false, running = true,
            haveToRepaintImmediately = false,
            haveToUpdateRoute = false, haveToReplaceRoute = false,
            haveToUpdatePosition = false, firstStart = true;
    private int scrollBarSize = 0;
    private Map<Integer,BitSet> significantPositionCache = new HashMap<Integer,BitSet>(ZOOMLEVEL_SCALE.length);

    public static boolean isSupportedPlatform() {
        return Platform.isLinux() || Platform.isMac() || Platform.isWindows();
    }

    public MapView(PositionsModel positionsModel, CharacteristicsModel characteristicsModel) {
        initialize();
        setModel(positionsModel, characteristicsModel);
    }

    private void setModel(PositionsModel positionsModel, CharacteristicsModel characteristicsModel) {
        this.positionsModel = positionsModel;
        this.characteristicsModel = characteristicsModel;

        positionsModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                update((e.getFirstRow() == 0 && e.getLastRow() == Integer.MAX_VALUE) ||
                        (e.getType() == TableModelEvent.DELETE));
            }
        });
        characteristicsModel.addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent e) {
            }
            public void intervalRemoved(ListDataEvent e) {
            }
            public void contentsChanged(ListDataEvent e) {
                update(true);
            }
        });
    }

    public Canvas getCanvas() {
        return webBrowser;
    }

    private WebBrowser createWebBrowser() {
        try {
            /* for JDIC 0.9.3
            if (Platform.isLinux()) {
                Externalization.extractFile(JdicManager.class, "libmozembed-linux-gtk1.2.so");
                Externalization.extractFile(JdicManager.class, "libmozembed-linux-gtk2.so");
                Externalization.extractFile(JdicManager.class, "mozembed-linux-gtk1.2");
                Externalization.extractFile(JdicManager.class, "mozembed-linux-gtk2");
            }
            if (Platform.isMac())
                Externalization.extractFile(JdicManager.class, "libjdic.jnilib");
            if (Platform.isWindows()) {
                Externalization.extractFile(JdicManager.class, "IeEmbed.exe");
                scrollBarSize = 20;
            }
            */

            /* for JDIC from CVS
            BrowserEngineManager browserEngineManager = BrowserEngineManager.instance();
            if (Platform.isLinux())
                browserEngineManager.setActiveEngine(BrowserEngineManager.MOZILLA);
            if (Platform.isMac())
                browserEngineManager.setActiveEngine(BrowserEngineManager.WEBKIT);
            if (Platform.isWindows())
                browserEngineManager.setActiveEngine(BrowserEngineManager.IE);

            IWebBrowser webBrowser = browserEngineManager.getActiveEngine().getWebBrowser();
            webBrowser.setAutoDispose(false);
            */
            return new WebBrowser(false);
        } catch (Throwable t) {
            processThrowable(t, "Cannot create WebBrowser");
            return null;
        }
    }

    private boolean loadWebPage(WebBrowser webBrowser) {
        try {
            File html = Externalization.extractFile(getClass(), "routeconverter.html");
            webBrowser.setURL(html.toURI().toURL());
        } catch (Throwable t) {
            processThrowable(t, "Cannot initialize WebBrowser");
            return false;
        }
        return true;
    }

    private void processThrowable(Throwable t, String message) {
        log.severe(message + ": " + t.getMessage());
        t.printStackTrace();

        JOptionPane.showMessageDialog(null,
                MessageFormat.format(RouteConverter.BUNDLE.getString("start-browser-error"), message + ":" + t + ":" + t.getMessage()),
                RouteConverter.BUNDLE.getString("title"), JOptionPane.ERROR_MESSAGE);
    }

    private void initialize() {
        webBrowser = createWebBrowser();
        if (webBrowser == null)
            return;

        webBrowser.addWebBrowserListener(new WebBrowserListener() {
            public void downloadStarted(WebBrowserEvent event) {
            }

            public void downloadCompleted(WebBrowserEvent event) {
                if (Platform.isMac())
                    documentCompleted(event);
            }

            public void downloadProgress(WebBrowserEvent event) {
            }

            public void downloadError(WebBrowserEvent event) {
            }

            public void documentCompleted(WebBrowserEvent event) {
                synchronized (notificationMutex) {
                    initialized = getCanvas() != null && isCompatible();
                }

                if (isInitialized()) {
                    new Thread(new Runnable() {
                        public void run() {
                            initializeDragListener();
                            initializeAfterLoading();
                        }
                    }, "MapViewInitializer").start();
                }
            }

            public void titleChange(WebBrowserEvent event) {
            }

            public void statusTextChange(WebBrowserEvent event) {
            }

            public void initializationCompleted(WebBrowserEvent event) {
            }

            public void windowClose(WebBrowserEvent event) {
            }
        });

        if (!loadWebPage(webBrowser))
            return;

        initializeBrowserInteraction();
    }

    private void initializeBrowserInteraction() {
        getCanvas().addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                resize();
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
            }

            public void componentHidden(ComponentEvent e) {
            }
        });

        mapViewRouteUpdater = new Thread(new Runnable() {
            public void run() {
                long lastTime = 0;
                boolean recenter;
                while (true) {
                    List<BaseNavigationPosition> copiedPositions;
                    synchronized (notificationMutex) {
                        try {
                            notificationMutex.wait(1000);
                        } catch (InterruptedException e) {
                            // ignore this
                        }

                        if (!running)
                            return;
                        if (!hasPositions())
                            continue;
                        if (!isVisible())
                            continue;

                        long currentTime = System.currentTimeMillis();
                        if (haveToRepaintImmediately ||
                                haveToReplaceRoute ||
                                (haveToUpdateRoute && (currentTime - lastTime > 5 * 1000))) {
                            copiedPositions = filterPositionsWithoutCoordinates(positions);
                            recenter = haveToReplaceRoute;
                            haveToUpdateRoute = false;
                            haveToReplaceRoute = false;
                            haveToRepaintImmediately = false;
                        } else
                            continue;
                    }

                    copiedPositions = reducePositions(copiedPositions, recenter);
                    setCenterOfMap(copiedPositions, recenter);
                    switch (positionsModel.getRoute().getCharacteristics()) {
                        case Route:
                            addDirectionsToMap(copiedPositions);
                            break;
                        case Waypoints:
                            addMarkersToMap(copiedPositions);
                            break;
                        default:
                            addPolylinesToMap(copiedPositions);
                    }
                    log.info("MapView updated for " + copiedPositions.size() + " positions of type " +
                            positionsModel.getRoute().getCharacteristics() + ", recentering: " + recenter);
                    lastTime = System.currentTimeMillis();
                }
            }
        }, "MapViewRouteUpdater");
        mapViewRouteUpdater.start();

        mapViewPositionUpdater = new Thread(new Runnable() {
            public void run() {
                long lastTime = 0;
                while (true) {
                    int[] copiedSelectedPositions;
                    List<BaseNavigationPosition> copiedPositions;
                    synchronized (notificationMutex) {
                        try {
                            notificationMutex.wait(100);
                        } catch (InterruptedException e) {
                            // ignore this
                        }

                        if (!running)
                            return;
                        if (!hasPositions())
                            continue;
                        if (!isVisible())
                            continue;

                        long currentTime = System.currentTimeMillis();
                        if (haveToUpdatePosition && (currentTime - lastTime > 500)) {
                            haveToUpdatePosition = false;
                            copiedSelectedPositions = new int[selectedPositionIndices.length];
                            System.arraycopy(selectedPositionIndices, 0, copiedSelectedPositions, 0, copiedSelectedPositions.length);
                            copiedPositions = filterPositionsWithoutCoordinates(positions);
                        } else
                            continue;
                    }

                    List<BaseNavigationPosition> selected = filterSelectedPositions(copiedPositions, copiedSelectedPositions);
                    selectPositions(selected);
                    log.info("MapView position updated for " + selected.size() + " positions");
                    lastTime = System.currentTimeMillis();
                }
            }
        }, "MapViewPositionUpdater");
        mapViewPositionUpdater.start();
    }

    private int getMaximumPositionCount() {
        switch (positionsModel.getRoute().getCharacteristics()) {
            case Route:
                return MAXIMUM_DIRECTIONS_POSITION_COUNT;
            case Waypoints:
                return MAXIMUM_MARKER_POSITION_COUNT;
            default:
                return MAXIMUM_POLYLINE_POSITION_COUNT;
        }
    }

    private ServerSocket createDragListenerServerSocket() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            serverSocket.setSoTimeout(1000);
            int port = serverSocket.getLocalPort();
            log.info("MapView listens on port " + port + " for dragging");
            setDragListenerPort(port);
            return serverSocket;
        } catch (IOException e) {
            log.severe("Cannot open drag listener socket: " + e.getMessage());
            return null;
        }
    }

    private void initializeDragListener() {
        dragListenerServerSocket = createDragListenerServerSocket();
        if (dragListenerServerSocket == null)
            return;

        mapViewDragListener = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    synchronized (notificationMutex) {
                        if (!running) {
                            return;
                        }
                    }

                    Socket clientSocket = null;
                    BufferedReader is = null;
                    OutputStream os = null;
                    try {
                        List<String> lines = new ArrayList<String>();
                        clientSocket = dragListenerServerSocket.accept();
                        is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        os = clientSocket.getOutputStream();
                        while (true) {
                            try {
                                String line = is.readLine();
                                if (Conversion.trim(line) == null)
                                    break;
                                lines.add(line);
                            } catch (IOException e) {
                                log.severe("Cannot read line from drag listener port:" + e.getMessage());
                                break;
                            }
                        }
                        processDragListenerCallBack(lines);
                    } catch (SocketTimeoutException e) {
                        // intentionally left empty
                    } catch (IOException e) {
                        log.severe("Cannot listen at drag listener socket: " + e.getMessage());
                        break;
                    }
                    finally {
                        try {
                            if (is != null)
                                is.close();
                            if (os != null)
                                os.close();
                            if (clientSocket != null)
                                clientSocket.close();
                        } catch (IOException e) {
                            log.severe("Cannot close drag listener socket: " + e.getMessage());
                        }
                    }
                }
            }
        }, "MapViewDragListener");
        mapViewDragListener.start();
    }

    private void initializeAfterLoading() {
        // workaround for Linux and Mac versions where the window has to be resized to show a complete map
        if (Platform.isLinux() || Platform.isMac()) {
            forceResize();
        } else
            resize();
        update(true);
    }

    private BitSet calculateSignificantPositionsForZoomLevel(List<BaseNavigationPosition> positions, int zoomLevel) {
        BitSet significant = significantPositionCache.get(zoomLevel);
        if (significant == null) {
            significant = new BitSet(positions.size());

            // TODO could remove MAXIMUM_ZOOMLEVEL_FOR_SIGNIFICANCE_CALCULATION and filterVisiblePositions()
            // double threshold = zoomLevel > ZOOMLEVEL_SCALE.length - 1 ? ZOOMLEVEL_SCALE[zoomLevel] / 2500.0 : 1.0;
            if (zoomLevel <= MAXIMUM_ZOOMLEVEL_FOR_SIGNIFICANCE_CALCULATION){
                double threshold = ZOOMLEVEL_SCALE[zoomLevel] / 2500.0;
                for (int significantPosition : Calculation.getSignificantPositions(positions, threshold))
                    significant.set(significantPosition);
            } else {
                // on all zoom level about MAXIMUM_ZOOMLEVEL_FOR_SIGNIFICANCE_CALCULATION
                // print all positions since the calculation is too expensive
                significant.set(0, positions.size(), true);
            }
            significantPositionCache.put(zoomLevel, significant);
        }
        return significant;
    }

    private List<BaseNavigationPosition> reducePositions(List<BaseNavigationPosition> positions, boolean recenter) {
        if (positions.size() < 1)
            return positions;

        int zoomLevel = recenter ? getBoundsZoomLevel(positions) : getCurrentZoomLevel();
        BitSet pointStatus = calculateSignificantPositionsForZoomLevel(positions, zoomLevel);

        List<BaseNavigationPosition> result = new ArrayList<BaseNavigationPosition>();
        for (int i = 0; i < positions.size(); i++)
            if (pointStatus.get(i))
                result.add(positions.get(i));
        return result;
    }

    private List<BaseNavigationPosition> filterSelectedPositions(List<BaseNavigationPosition> positions, int[] selectedIndices) {
        List<BaseNavigationPosition> result = new ArrayList<BaseNavigationPosition>();
        for (int selectedIndex : selectedIndices) {
            if (selectedIndex >= positions.size())
                continue;
            result.add(positions.get(selectedIndex));
        }
        return result;
    }

    private List<BaseNavigationPosition> filterPositionsWithoutCoordinates(List<BaseNavigationPosition> positions) {
        List<BaseNavigationPosition> result = new ArrayList<BaseNavigationPosition>();
        for (BaseNavigationPosition position : positions) {
            if(position.hasCoordinates())
                result.add(position);
        }
        return result;
    }

    public void dispose() {
        long start = System.currentTimeMillis();
        synchronized (notificationMutex) {
            running = false;
            notificationMutex.notifyAll();
        }

        if (mapViewPositionUpdater != null) {
            try {
                mapViewPositionUpdater.join();
            } catch (InterruptedException e) {
                // intentionally left empty
            }
            long end = System.currentTimeMillis();
            log.info("MapViewPositionUpdater stopped after " + (end - start) + " ms");
        }

        if (mapViewRouteUpdater != null) {
            try {
                mapViewRouteUpdater.join();
            } catch (InterruptedException e) {
                // intentionally left empty
            }
            long end = System.currentTimeMillis();
            log.info("MapViewRouteUpdater stopped after " + (end - start) + " ms");
        }

        if (webBrowser != null) {
            webBrowser.dispose();

            start = System.currentTimeMillis();
            while (true) {
                long end = System.currentTimeMillis();
                if (end - start > 5000 || !webBrowser.isInitialized()) {
                    log.info("MapView dispose stopped after " + (end - start) + " ms");
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // intentionally left empty
                }
            }
        }

        if (dragListenerServerSocket != null) {
            try {
                dragListenerServerSocket.close();
            } catch (IOException e) {
                log.severe("Cannot close drag listener socket:" + e.getMessage());
            }
            long end = System.currentTimeMillis();
            log.info("MapViewDragListenerSocket stopped after " + (end - start) + " ms");
        }

        if (mapViewDragListener != null) {
            try {
                mapViewDragListener.join();
            } catch (InterruptedException e) {
                // intentionally left empty
            }
            long end = System.currentTimeMillis();
            log.info("MapViewDragListener stopped after " + (end - start) + " ms");
        }
    }

    private boolean isCompatible() {
        String result;
        synchronized (notificationMutex) {
            result = executeScript("window.isCompatible && isCompatible()");
        }
        return Boolean.parseBoolean(result);
    }

    public boolean isInitialized() {
        synchronized (notificationMutex) {
            return initialized;
        }
    }

    private boolean isVisible() {
        return getCanvas().getWidth() > 0;
    }

    private boolean hasPositions() {
        synchronized (notificationMutex) {
            return isInitialized() && positions != null;
        }
    }

    private void setDragListenerPort(int dragListenerPort) {
        synchronized (notificationMutex) {
            executeScript("setDragListenerPort(" + dragListenerPort + ")");
        }
    }

    private boolean hasBeenResizedToInvisible = false;

    public void resize() {
        if (!isInitialized() || !getCanvas().isShowing())
            return;

        synchronized (notificationMutex) {
            // if map is not visible remember to update and resize it again
            // once the map becomes visible again
            if (!isVisible()) {
                hasBeenResizedToInvisible = true;
                resizeMap();
            } else if (hasBeenResizedToInvisible) {
                hasBeenResizedToInvisible = false;
                update(true);
                forceResize();
            } else {
                resizeMap();
            }
        }
    }

    private void resizeMap() {
        synchronized (notificationMutex) {
            int width = Math.max(getCanvas().getWidth() - scrollBarSize, 0);
            int height = Math.max(getCanvas().getHeight() - scrollBarSize, 0);
            executeScript("resize(" + width + "," + height + ")");
        }
    }

    private void forceResize() {
        final Dimension dimension = getCanvas().getSize();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getCanvas().setSize(dimension.width - 1, dimension.height - 1);
                resize();

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        getCanvas().setSize(dimension);
                        resize();
                    }
                });
            }
        });
    }

    private List<BaseNavigationPosition> positions;

    private int getBoundsZoomLevel(List<BaseNavigationPosition> positions) {
        if ((positions == null) || (positions.size() < 1))
            return 0;

        Wgs84Position northEast = Calculation.getNorthEast(positions);
        Wgs84Position southWest = Calculation.getSouthWest(positions);

        StringBuffer buffer = new StringBuffer();
        buffer.append("map.getBoundsZoomLevel(new GLatLngBounds(").
                append("new GLatLng(").append(northEast.getLatitude()).append(",").
                append(northEast.getLongitude()).append("),").
                append("new GLatLng(").append(southWest.getLatitude()).append(",").
                append(southWest.getLongitude()).append(")").append("));\n");

        String zoomLevel = executeScript(buffer);
        return Conversion.parseInt(zoomLevel);
    }

    private int getCurrentZoomLevel() {
        String zoomLevel = executeScript("map.getZoom();");
        return Conversion.parseInt(zoomLevel);
    }

    private void update(boolean haveToReplaceRoute) {
        if (!isInitialized() || !getCanvas().isShowing())
            return;

        synchronized (notificationMutex) {
            this.positions = positionsModel.getRoute() != null ? positionsModel.getRoute().getPositions() : null;
            haveToUpdateRoute = true;
            if (haveToReplaceRoute) {
                this.haveToReplaceRoute = true;
                this.haveToUpdatePosition = true;
                significantPositionCache.clear();
            }
            log.fine(System.currentTimeMillis() + " update haveToReplaceRoute: " + haveToReplaceRoute + " positions: " + positions);
            notificationMutex.notifyAll();
        }
    }

    private int lastDirectionsCount = -1, lastPolylinesCount = -1, lastMarkersCount = -1, meters = 0, seconds = 0;

    private void removeOverlays() {
        if (lastDirectionsCount > 0) {
            for (int j = 0; j < Conversion.ceiling(lastDirectionsCount, MAXIMUM_DIRECTIONS_SEGMENT_LENGTH, false); j++) {
                StringBuffer buffer = new StringBuffer();
                int maximum = Math.min(lastDirectionsCount, (j + 1) * MAXIMUM_DIRECTIONS_SEGMENT_LENGTH);
                for (int i = j * MAXIMUM_DIRECTIONS_SEGMENT_LENGTH; i < maximum; i++)
                    buffer.append("map.removeOverlay(directions").append(i).append(".getPolyline());");
                executeScript(buffer);
            }
            lastDirectionsCount = -1;
        }

        if (lastPolylinesCount > 0) {
            for (int j = 0; j < Conversion.ceiling(lastPolylinesCount, MAXIMUM_POLYLINE_SEGMENT_LENGTH, false); j++) {
                StringBuffer buffer = new StringBuffer();
                int maximum = Math.min(lastPolylinesCount, (j + 1) * MAXIMUM_POLYLINE_SEGMENT_LENGTH);
                for (int i = j * MAXIMUM_POLYLINE_SEGMENT_LENGTH; i < maximum; i++)
                    buffer.append("map.removeOverlay(line").append(i).append(");");
                executeScript(buffer);
            }
            lastPolylinesCount = -1;
        }

        if (lastMarkersCount > 0) {
            for (int j = 0; j < Conversion.ceiling(lastMarkersCount, MAXIMUM_MARKER_SEGMENT_LENGTH, false); j++) {
                StringBuffer buffer = new StringBuffer();
                int maximum = Math.min(lastMarkersCount, (j + 1) * MAXIMUM_MARKER_SEGMENT_LENGTH);
                for (int i = j * MAXIMUM_MARKER_SEGMENT_LENGTH; i < maximum; i++)
                    buffer.append("map.removeOverlay(marker").append(i).append(");");
                executeScript(buffer);
            }
            lastMarkersCount = -1;
        }
    }

    private void addDirectionsToMap(List<BaseNavigationPosition> positions) {
        removeOverlays();

        meters = 0;
        seconds = 0;
        lastDirectionsCount = Conversion.ceiling(positions.size(), MAXIMUM_DIRECTIONS_SEGMENT_LENGTH, false);
        for (int j = 0; j < lastDirectionsCount; j++) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("var directions").append(j).append(" = createDirections();");
            buffer.append("var latlngs = [");
            int maximum = Math.min(positions.size(), (j + 1) * MAXIMUM_DIRECTIONS_SEGMENT_LENGTH + 1);
            for (int i = j * MAXIMUM_DIRECTIONS_SEGMENT_LENGTH; i < maximum; i++) {
                BaseNavigationPosition position = positions.get(i);
                buffer.append("new GLatLng(").append(position.getLatitude()).append(",").append(position.getLongitude()).append(")");
                if (i < maximum - 1)
                    buffer.append(",");
            }
            buffer.append("];\n");
            buffer.append("directions").append(j).append(".loadFromWaypoints(latlngs, " +
                    "{ preserveViewport: true, getPolyline: true });");
            executeScript(buffer);
        }
    }

    private BaseNavigationPosition getMapNorthEast(){
        String bounds = executeScript("map.getBounds();");
        String northEastLatitude = executeScript("map.getBounds().getNorthEast().lat();");
        String northEastLongitude = executeScript("map.getBounds().getNorthEast().lng();");
        return northEastLatitude != null && northEastLongitude != null ? new Wgs84Position(Double.parseDouble(northEastLongitude), Double.parseDouble(northEastLatitude), null, null, null) : null;
    }
    
    private BaseNavigationPosition getMapSouthWest(){
        String southWestLatitude = executeScript("map.getBounds().getSouthWest().lat();");
        String southWestLongitude = executeScript("map.getBounds().getSouthWest().lng();");
        return southWestLatitude != null && southWestLongitude != null ? new Wgs84Position(Double.parseDouble(southWestLongitude), Double.parseDouble(southWestLatitude), null, null, null) : null;
    }

    private List<BaseNavigationPosition> filterVisiblePositions(List<BaseNavigationPosition> positions) {
        BaseNavigationPosition northEast = getMapNorthEast();
        BaseNavigationPosition southWest = getMapSouthWest();
        if(northEast == null || southWest == null)
            return positions;

        // TODO this code seems to be never reached 
        double width = northEast.getLongitude() - southWest.getLongitude();
        double height = southWest.getLatitude() - northEast.getLatitude();
        northEast.setLongitude(northEast.getLongitude() + width);
        northEast.setLatitude(northEast.getLatitude() - height);
        southWest.setLongitude(southWest.getLongitude() - width);
        southWest.setLatitude(southWest.getLatitude() + height);

        List<BaseNavigationPosition> result = new ArrayList<BaseNavigationPosition>();
        result.add(positions.get(0));
        for (int i = 1; i < positions.size(); i++) {
            BaseNavigationPosition position = positions.get(i);
            if (Calculation.containsPosition(northEast, southWest, position)) {
                result.add(position);
            }
        }
        return result;
    }
    
    private void addPolylinesToMap(List<BaseNavigationPosition> positions) {
        removeOverlays();

        // TODO could remove this?!
        if (positions.size() > MAXIMUM_POLYLINE_POSITION_COUNT)
            positions = filterVisiblePositions(positions);

        lastPolylinesCount = Conversion.ceiling(positions.size(), MAXIMUM_POLYLINE_SEGMENT_LENGTH, true);
        for (int j = 0; j < lastPolylinesCount; j++) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("var latlngs = [");
            int maximum = Math.min(positions.size(), (j + 1) * MAXIMUM_POLYLINE_SEGMENT_LENGTH + 1);
            for (int i = j * MAXIMUM_POLYLINE_SEGMENT_LENGTH; i < maximum; i++) {
                BaseNavigationPosition position = positions.get(i);
                buffer.append("new GLatLng(").append(position.getLatitude()).append(",").append(position.getLongitude()).append(")");
                if (i < maximum - 1)
                    buffer.append(",");
            }
            buffer.append("];\n");
            buffer.append("var line").append(j).append(" = new GPolyline(latlngs,\"#0033FF\",2,1);\n");
            buffer.append("map.addOverlay(line").append(j).append(");");
            executeScript(buffer);
        }
    }

    private void addMarkersToMap(List<BaseNavigationPosition> positions) {
        removeOverlays();

        lastMarkersCount = positions.size();
        for (int j = 0; j < Conversion.ceiling(positions.size(), MAXIMUM_MARKER_SEGMENT_LENGTH, false); j++) {
            StringBuffer buffer = new StringBuffer();

            int maximum = Math.min(positions.size(), (j + 1) * MAXIMUM_MARKER_SEGMENT_LENGTH );
            for (int i = j * MAXIMUM_MARKER_SEGMENT_LENGTH; i < maximum; i++) {
                BaseNavigationPosition position = positions.get(i);
                buffer.append("var marker").append(i).append(" = new GMarker(new GLatLng(").
                        append(position.getLatitude()).append(",").append(position.getLongitude()).
                        append("), { title: \"").append(escape(position.getComment())).append("\", ").
                        append("clickable: false, icon: markerIcon });\n");
                buffer.append("map.addOverlay(marker").append(i).append(");\n");
            }
            executeScript(buffer);
        }
    }

    private void setCenterOfMap(List<BaseNavigationPosition> positions, boolean recenter) {
        StringBuffer buffer = new StringBuffer();
        // set map type only on first start
        if (firstStart) {
            String mapType = preferences.get(MAP_TYPE_PREFERENCE, "Map");
            buffer.append("setMapType(\"").append(mapType).append("\");\n");
        }

        // if there are positions center on first start or if we have to recenter
        if (positions.size() > 0 && (firstStart || recenter)) {
            Wgs84Position northEast = Calculation.getNorthEast(positions);
            Wgs84Position southWest = Calculation.getSouthWest(positions);
            buffer.append("var zoomLevel = map.getBoundsZoomLevel(new GLatLngBounds(").
                    append("new GLatLng(").append(northEast.getLatitude()).append(",").append(northEast.getLongitude()).append("),").
                    append("new GLatLng(").append(southWest.getLatitude()).append(",").append(southWest.getLongitude()).append(")").
                    append("));\n");
            Wgs84Position center = Calculation.center(positions);
            buffer.append("map.setCenter(new GLatLng(").append(center.getLatitude()).append(",").
                    append(center.getLongitude()).append("), zoomLevel);");
        }
        executeScript(buffer);
        firstStart = false;
    }

    private int lastSelectedPositionCount = -1;
    private List<BaseNavigationPosition> lastSelectedPositions;
    private int[] selectedPositionIndices = new int[0];

    private void selectPositions(List<BaseNavigationPosition> selectedPositions) {
        if (selectedPositions.size() > MAXIMUM_SELECTION_COUNT)
            selectedPositions = selectedPositions.subList(0, MAXIMUM_SELECTION_COUNT);

        // delete old
        if (lastSelectedPositionCount >= 0) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < lastSelectedPositionCount; i++) {
                buffer.append("map.removeOverlay(selected").append(i).append(");\n");
            }
            executeScript(buffer);
        }

        // build up new
        StringBuffer buffer = new StringBuffer();
        lastSelectedPositionCount = selectedPositions.size();
        lastSelectedPositions = selectedPositions;
        for (int i = 0; i < lastSelectedPositionCount; i++) {
            BaseNavigationPosition selectedPosition = selectedPositions.get(i);
            buffer.append("var selected").append(i).append(" = ");
            buffer.append("new GMarker(new GLatLng(").append(selectedPosition.getLatitude()).append(",")
                    .append(selectedPosition.getLongitude()).append("), { title: \"")
                    .append(escape(selectedPosition.getComment())).append("\", draggable: true });\n");
            buffer.append("addMarker(selected").append(i).append(",").append(i).append(");\n");
        }

        // pan to first position
        if (lastSelectedPositionCount > 0) {
            BaseNavigationPosition center = selectedPositions.get(0);
            buffer.append("centerMap(new GLatLng(").append(center.getLatitude()).append(",").
                    append(center.getLongitude()).append("));");
        }
        executeScript(buffer);
    }

    private String executeScript(StringBuffer buffer) {
        if(buffer.length() == 0)
            return null;

        if ((1000 <= buffer.length() % 1024) && (buffer.length() % 1024) <= 1018) {
            for (int i = 0; i < 20; i++) {
                buffer.append(';');
            }
        }
        return executeScript(buffer.toString());
    }

    private synchronized String executeScript(String string) {
        log.info(System.currentTimeMillis() + " executing script '" + string + "'");
        return webBrowser.executeScript(string);
    }

    private String escape(String string) {
        StringBuffer buffer = new StringBuffer(string);
        for (int i = 0; i < buffer.length(); i++) {
            char c = buffer.charAt(i);
            if (!(Character.isLetterOrDigit(c) || Character.isWhitespace(c) || c == '\'' || c == ',')) {
                buffer.deleteCharAt(i);
                i--;
            }
        }
        return buffer.toString();
    }

    public void setSelectedPositions(int[] selectedPositions) {
        synchronized (notificationMutex) {
            this.selectedPositionIndices = selectedPositions;
            haveToUpdatePosition = true;
            notificationMutex.notifyAll();
        }
    }

    public void print() {
        executeScript("window.print()");
    }

    public void testDragListenerPort() {
        executeScript("testDragListenerPort()");
    }

    private static final Pattern POSITION_LINE_PATTERN = Pattern.compile("^GET /dragend/(.*)/(.*)/(.*) .*$");
    private static final Pattern DIRECTION_LINE_PATTERN = Pattern.compile("^GET /load/(\\d*)/(\\d*) .*$");
    private static final Pattern MAP_TYPE_LINE_PATTERN = Pattern.compile("^GET /maptypechanged/(.*) .*$");
    private static final Pattern ZOOMLEVEL_PATTERN = Pattern.compile("^GET /zoomend/(.*)/(.*) .*$");
    private static final Pattern MAP_VIEW_MOVE_ENDS_PATTERN = Pattern.compile("^GET /moveend/(.*)/(.*) .*$");
    private static final Pattern TEST_LINE_PATTERN = Pattern.compile("^GET /test/(\\d+) .*$");

    private void processDragListenerCallBack(List<String> lines) {
        if (!isAuthenticated(lines))
            return;

        for (String line : lines) {
            Matcher positionMatcher = POSITION_LINE_PATTERN.matcher(line);
            if (positionMatcher.matches()) {
                int index = Conversion.parseInt(positionMatcher.group(1));
                Double latitude = Conversion.parseDouble(positionMatcher.group(2));
                Double longitude = Conversion.parseDouble(positionMatcher.group(3));
                modifyPosition(index, longitude, latitude);
            }

            Matcher directionMatcher = DIRECTION_LINE_PATTERN.matcher(line);
            if (directionMatcher.matches()) {
                meters += Conversion.parseInt(directionMatcher.group(1));
                seconds += Conversion.parseInt(directionMatcher.group(2));
            }

            Matcher mapTypeMatcher = MAP_TYPE_LINE_PATTERN.matcher(line);
            if (mapTypeMatcher.matches()) {
                String mapType = mapTypeMatcher.group(1);
                preferences.put(MAP_TYPE_PREFERENCE, mapType);
            }

            Matcher zoomMatcher = ZOOMLEVEL_PATTERN.matcher(line);
            if (zoomMatcher.matches()) {
                synchronized (notificationMutex) {
                    haveToRepaintImmediately = true;
                    notificationMutex.notifyAll();
                }
            }

            Matcher moveendMather = MAP_VIEW_MOVE_ENDS_PATTERN.matcher(line);
            if (moveendMather.matches()) {
                synchronized (notificationMutex) {
                    if (getCurrentZoomLevel() >= MAXIMUM_ZOOMLEVEL_FOR_SIGNIFICANCE_CALCULATION)
                        haveToRepaintImmediately = true;
                    notificationMutex.notifyAll();
                }
            }

            Matcher testMatcher = TEST_LINE_PATTERN.matcher(line);
            if (testMatcher.matches()) {
                String dragListenerPort = testMatcher.group(1);
                JOptionPane.showMessageDialog(getCanvas(), "Got a request from the map view on port " + dragListenerPort + ".");
            }
        }
    }

    private void modifyPosition(int index, Double longitude, Double latitude) {
        BaseNavigationPosition modify = lastSelectedPositions.get(index);
        modify.setLatitude(latitude);
        modify.setLongitude(longitude);

        // repaint route immediately, simulates update(true) without recentering
        synchronized (notificationMutex) {
            haveToRepaintImmediately = true;
            significantPositionCache.clear();
            notificationMutex.notifyAll();
        }

        // notify views about change, leads to update(false)
        int row = positions.indexOf(modify);
        positionsModel.fireTableRowsUpdated(row, row);

        // give time for repainting of the route
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // intentionally left empty
        }

        // repaint position marker
        synchronized (notificationMutex) {
            haveToUpdatePosition = true;
            notificationMutex.notifyAll();
        }
    }

    private boolean isAuthenticated(List<String> lines) {
        Map<String, String> map = asMap(lines);
        String host = Conversion.trim(map.get("Host"));
        String id = Conversion.trim(map.get("id"));
        return host != null && host.equals("localhost:" + dragListenerServerSocket.getLocalPort()) &&
                id != null && id.equals("Jx3dQUv4");
    }

    private static final Pattern NAME_VALUE_PATTERN = Pattern.compile("^(.+?):(.+)$");

    private Map<String, String> asMap(List<String> lines) {
        Map<String, String> map = new HashMap<String, String>();
        for (String line : lines) {
            Matcher matcher = NAME_VALUE_PATTERN.matcher(line);
            if (matcher.matches())
                map.put(matcher.group(1), matcher.group(2));
        }
        return map;
    }
}
