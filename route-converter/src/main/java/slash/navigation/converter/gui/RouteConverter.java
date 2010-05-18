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

package slash.navigation.converter.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import slash.common.io.Files;
import slash.common.io.Platform;
import slash.common.io.Version;
import slash.common.log.LoggingHelper;
import slash.navigation.babel.BabelException;
import slash.navigation.base.BaseNavigationPosition;
import slash.navigation.base.NavigationFormat;
import slash.navigation.base.Wgs84Position;
import slash.navigation.converter.gui.actions.*;
import slash.navigation.converter.gui.helper.JMenuHelper;
import slash.navigation.converter.gui.mapview.AbstractMapViewListener;
import slash.navigation.converter.gui.mapview.MapView;
import slash.navigation.converter.gui.mapview.MapViewListener;
import slash.navigation.converter.gui.models.PositionsModel;
import slash.navigation.converter.gui.models.PositionsSelectionModel;
import slash.navigation.converter.gui.panels.AnalysePanel;
import slash.navigation.converter.gui.panels.BrowsePanel;
import slash.navigation.converter.gui.panels.ConvertPanel;
import slash.navigation.gpx.Gpx11Format;
import slash.navigation.gui.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * A small graphical user interface for the route conversion.
 *
 * @author Christian Pesch
 */

public abstract class RouteConverter extends SingleFrameApplication {
    private static final Logger log = Logger.getLogger(RouteConverter.class.getName());
    private final Preferences preferences = Preferences.userNodeForPackage(getClass());

    public static RouteConverter getInstance() {
        return (RouteConverter) Application.getInstance();
    }

    public static ResourceBundle getBundle() {
        return getInstance().getContext().getBundle();
    }

    public static String getTitle() {
        Version version = Version.parseVersionFromManifest();
        return MessageFormat.format(getBundle().getString("title"), version.getVersion(), version.getDate());
    }

    private static final String OPEN_PATH_PREFERENCE = "source";
    private static final String OPEN_FORMAT_PREFERENCE = "sourceFormat";
    private static final String SAVE_PATH_PREFERENCE = "target";
    private static final String TARGET_FORMAT_PREFERENCE = "targetFormat";
    private static final String ADD_POSITION_LONGITUDE_PREFERENCE = "addPositionLongitude";
    private static final String ADD_POSITION_LATITUDE_PREFERENCE = "addPositionLatitude";
    public static final String AUTOMATIC_UPDATE_CHECK_PREFERENCE = "automaticUpdateCheck";
    public static final String PREFIX_NUMBER_WITH_ZEROS = "prefixNumberWithZeros";
    public static final String SPACE_BETWEEN_NUMBER_AND_COMMENT_PREFERENCE = "spaceBetweenNumberAndComment";
    public static final String RECENTER_AFTER_ZOOMING_PREFERENCE = "recenterAfterZooming";
    public static final String PEDESTRIANS_PREFERENCE = "pedestrians";
    public static final String AVOID_HIGHWAYS_PREFERENCE = "avoidHighways";
    private static final String SELECT_DUPLICATE_PREFERENCE = "selectDuplicate";
    private static final String SELECT_BY_DISTANCE_PREFERENCE = "selectByDistance";
    private static final String SELECT_BY_ORDER_PREFERENCE = "selectByOrder";
    private static final String SELECT_BY_SIGNIFICANCE_PREFERENCE = "selectBySignificance";
    private static final String SEARCH_POSITION_PREFERENCE = "searchPosition";
    private static final String DIVIDER_LOCATION_PREFERENCE = "dividerLocation";

    private static final String DEBUG_PREFERENCE = "debug";
    private static final String USERNAME_PREFERENCE = "userName";
    private static final String PASSWORD_PREFERENCE = "userAuthentication";
    private static final String CATEGORY_PREFERENCE = "category";
    private static final String UPLOAD_ROUTE_PREFERENCE = "uploadRoute";

    protected JPanel contentPane;
    private JSplitPane splitPane;
    private JPanel mapPanel;
    private MapView mapView;
    private static final GridConstraints MAP_PANEL_CONSTRAINTS = new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            new Dimension(0, 0), new Dimension(0, 0), new Dimension(2000, 2640), 0, true);

    private JTabbedPane tabbedPane;
    private JPanel convertPanel, analysePanel, browsePanel;
    private LazyTabInitializer tabInitializer;

    private String[] args;

    // application lifecycle callbacks

    protected void initialize(String[] args) {
        LoggingHelper.logToFile();
        if (preferences.getBoolean(DEBUG_PREFERENCE, false)) {
            LoggingHelper.logToStdOut();
        }
        this.args = args;
    }

    protected void startup() {
        log.info("Started " + getTitle() + " on " + Platform.getPlatform() + " with " + Platform.getJvm() + " and " + Platform.getMaximumMemory() + " MByte heap");
        show();
        checkJreVersion();
        new Updater().implicitCheck(frame);
        parseArgs(args);
    }

    private void checkJreVersion() {
        String currentVersion = System.getProperty("java.version");
        String minimumVersion = "1.6.0_14";
        if (!Platform.isCurrentAtLeastMinimumVersion(currentVersion, minimumVersion))
            JOptionPane.showMessageDialog(frame, MessageFormat.format(getBundle().getString("jre-too-old-warning"), currentVersion, minimumVersion), frame.getTitle(), JOptionPane.WARNING_MESSAGE);
    }

    private void parseArgs(String[] args) {
        if (args.length > 0) {
            getConvertPanel().openUrls(Files.toUrls(args));
        } else {
            getConvertPanel().newFile();
        }
    }

    private void show() {
        createFrame(getTitle(), "slash/navigation/converter/gui/RouteConverter.png", contentPane, null, createMenuBar());

        addExitListener(new ExitListener() {
            public boolean canExit(EventObject event) {
                return getConvertPanel().confirmDiscard();
            }

            public void willExit(EventObject event) {
            }
        });

        tabInitializer = new LazyTabInitializer();
        tabbedPane.addChangeListener(tabInitializer);

        openFrame();

        mapView = createMapView("slash.navigation.converter.gui.mapview.EclipseSWTMapView");
        if (mapView == null)
            mapView = createMapView("slash.navigation.converter.gui.mapview.JdicMapView");
        if (mapView != null && mapView.isSupportedPlatform()) {
            mapPanel.setVisible(true);
            openMapView();
        } else {
            mapPanel.setVisible(false);
        }

        initializeActions();
    }

    private void openFrame() {
        new Thread(new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        openFrame(contentPane);
                    }
                });
            }
        }, "FrameOpener").start();
    }

    private MapView createMapView(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return (MapView) clazz.newInstance();
        } catch (Exception e) {
            log.info("Cannot instantiate " + className + ": " + e.getMessage());
        }
        return null;
    }

    private void openMapView() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                mapView.initialize(getConvertPanel().getPositionsModel(),
                        getConvertPanel().getCharacteristicsModel(),
                        preferences.getBoolean(PEDESTRIANS_PREFERENCE, false),
                        preferences.getBoolean(AVOID_HIGHWAYS_PREFERENCE, true)
                );
                addMapViewListener(new AbstractMapViewListener() {
                    public void selectedPosition(int index) {
                        getConvertPanel().selectPosition(index);
                    }
                });

                @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
                Throwable cause = mapView.getInitializationCause();
                boolean enablePrintActions = false;
                if (mapView.getComponent() == null || cause != null) {
                    StringWriter stackTrace = new StringWriter();
                    cause.printStackTrace(new PrintWriter(stackTrace));
                    mapPanel.add(new JLabel(MessageFormat.format(getBundle().getString("start-browser-error"), stackTrace.toString().replaceAll("\n", "<p>"))), MAP_PANEL_CONSTRAINTS);
                } else {
                    mapPanel.add(mapView.getComponent(), MAP_PANEL_CONSTRAINTS);
                    enablePrintActions = true;
                }

                int location = preferences.getInt(DIVIDER_LOCATION_PREFERENCE, -1);
                if (location > 0)
                    splitPane.setDividerLocation(location);
                else
                    splitPane.setDividerLocation(300);

                splitPane.addPropertyChangeListener(new PropertyChangeListener() {
                    private int location = 0;

                    public void propertyChange(PropertyChangeEvent e) {
                        if (!isMapViewAvailable())
                            return;

                        if (e.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
                            if (splitPane.getDividerLocation() != location) {
                                location = splitPane.getDividerLocation();
                                mapView.resize();
                                preferences.putInt(DIVIDER_LOCATION_PREFERENCE, splitPane.getDividerLocation());
                            }
                        }
                    }
                });

                ActionManager actionManager = Application.getInstance().getContext().getActionManager();
                actionManager.enable("print-map", enablePrintActions);
                actionManager.enable("print-map-and-route", enablePrintActions);
                actionManager.enable("print-elevation-profile", enablePrintActions);
            }
        });
    }

    protected void shutdown() {
        if (mapView != null)
            mapView.dispose();
        super.shutdown();

        log.info("Shutdown " + getTitle() + " on " + Platform.getPlatform() + " with " + Platform.getJvm());
    }

    // Java5/6 abstractions

    public abstract ExternalPrograms createExternalPrograms();

    protected abstract BrowsePanel createBrowsePanel();

    protected abstract ConvertPanel createConvertPanel();

    // Preferences handling

    public Preferences getPreferences() {
        return preferences;
    }

    public String getOpenFormatPreference() {
        return preferences.get(OPEN_FORMAT_PREFERENCE, Gpx11Format.class.getName());
    }

    public void setOpenFormatPreference(String format) {
        preferences.put(OPEN_FORMAT_PREFERENCE, format);
    }

    public String getOpenPathPreference() {
        return preferences.get(OPEN_PATH_PREFERENCE, "");
    }

    public void setOpenPathPreference(String file) {
        preferences.put(OPEN_PATH_PREFERENCE, file);
    }

    public String getSaveFormatPreference() {
        return preferences.get(TARGET_FORMAT_PREFERENCE, Gpx11Format.class.getName());
    }

    public void setSaveFormatPreference(String format) {
        preferences.put(TARGET_FORMAT_PREFERENCE, format);
    }

    public String getSavePathPreference(NavigationFormat format) {
        return preferences.get(SAVE_PATH_PREFERENCE + format.getName(), "");
    }

    public void setSavePathPreference(NavigationFormat format, String parent) {
        preferences.put(SAVE_PATH_PREFERENCE + format.getName(), parent);
    }

    private BaseNavigationPosition getLastMapCenter() {
        double longitude = preferences.getDouble(ADD_POSITION_LONGITUDE_PREFERENCE, -41.0);
        double latitude = preferences.getDouble(ADD_POSITION_LATITUDE_PREFERENCE, 41.0);
        return new Wgs84Position(longitude, latitude, null, null, null, null);
    }

    public void setLastMapCenter(BaseNavigationPosition position) {
        preferences.putDouble(ADD_POSITION_LONGITUDE_PREFERENCE, position.getLongitude());
        preferences.putDouble(ADD_POSITION_LATITUDE_PREFERENCE, position.getLatitude());
    }

    boolean isAutomaticUpdateCheck() {
        return preferences.getBoolean(AUTOMATIC_UPDATE_CHECK_PREFERENCE, true);
    }

    public boolean getPrefixNumberWithZerosPreference() {
        return preferences.getBoolean(PREFIX_NUMBER_WITH_ZEROS, false);
    }

    public boolean getSpaceBetweenNumberAndCommentPreference() {
        return preferences.getBoolean(SPACE_BETWEEN_NUMBER_AND_COMMENT_PREFERENCE, false);
    }

    public int getSelectDuplicatePreference() {
        return preferences.getInt(SELECT_DUPLICATE_PREFERENCE, 5);
    }

    public void setSelectDuplicatePreference(int selectDuplicatePreference) {
        preferences.putInt(SELECT_DUPLICATE_PREFERENCE, selectDuplicatePreference);
    }

    public int getSelectByDistancePreference() {
        return preferences.getInt(SELECT_BY_DISTANCE_PREFERENCE, 1000);
    }

    public void setSelectByDistancePreference(int selectByDistancePreference) {
        preferences.putInt(SELECT_BY_DISTANCE_PREFERENCE, selectByDistancePreference);
    }

    public int getSelectByOrderPreference() {
        return preferences.getInt(SELECT_BY_ORDER_PREFERENCE, 5);
    }

    public void setSelectByOrderPreference(int selectByOrderPreference) {
        preferences.putInt(SELECT_BY_ORDER_PREFERENCE, selectByOrderPreference);
    }

    public int getSelectBySignificancePreference() {
        return preferences.getInt(SELECT_BY_SIGNIFICANCE_PREFERENCE, 20);
    }

    public void setSelectBySignificancePreference(int selectBySignificancePreference) {
        preferences.putInt(SELECT_BY_SIGNIFICANCE_PREFERENCE, selectBySignificancePreference);
    }

    public String getSearchPositionPreference() {
        return preferences.get(SEARCH_POSITION_PREFERENCE, "");
    }

    public void setSearchPositionPreference(String searchPositionPreference) {
        preferences.put(SEARCH_POSITION_PREFERENCE, searchPositionPreference);
    }

    public String getUserNamePreference() {
        return preferences.get(USERNAME_PREFERENCE, "");
    }

    public String getPasswordPreference() {
        return new String(preferences.getByteArray(PASSWORD_PREFERENCE, new byte[0]));
    }

    public void setUserNamePreference(String userNamePreference, String passwordPreference) {
        // TODO unifiy username password stuff with that from UploadDialog
        preferences.put(USERNAME_PREFERENCE, userNamePreference);
        preferences.putByteArray(PASSWORD_PREFERENCE, passwordPreference.getBytes());
    }

    public File getUploadRoutePreference() {
        File path = new File(preferences.get(UPLOAD_ROUTE_PREFERENCE, ""));
        return Files.findExistingPath(path);
    }

    public void setUploadRoutePreference(File path) {
        preferences.put(UPLOAD_ROUTE_PREFERENCE, path.getPath());
    }

    public String getCategoryPreference() {
        return preferences.get(CATEGORY_PREFERENCE, "");
    }

    public void setCategoryPreference(String category) {
        preferences.put(CATEGORY_PREFERENCE, category);
    }

    // dialogs for external components

    public void handleBabelError(final BabelException e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(frame, MessageFormat.format(getBundle().getString("babel-error"), e.getBabelPath()), frame.getTitle(), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void handleOutOfMemoryError() {
        // get some air to breath
        System.gc();
        System.runFinalization();

        final long limitBefore = Platform.getMaximumMemory();
        final long limitAfter = limitBefore * 2;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(frame, MessageFormat.format(getBundle().getString("out-of-memory-error"), limitBefore, limitAfter), frame.getTitle(), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void handleOpenError(final Throwable throwable, final String path) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                throwable.printStackTrace();
                log.severe("Open error: " + throwable.getMessage());
                JLabel labelOpenError = new JLabel(MessageFormat.format(getBundle().getString("open-error"), Files.shortenPath(path), throwable.getMessage()));
                labelOpenError.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        createExternalPrograms().startMail(frame);
                    }
                });
                JOptionPane.showMessageDialog(frame, labelOpenError, frame.getTitle(), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void handleOpenError(final Throwable throwable, final List<URL> urls) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                throwable.printStackTrace();
                log.severe("Open error: " + throwable.getMessage());
                JLabel labelOpenError = new JLabel(MessageFormat.format(getBundle().getString("open-error"), Files.printArrayToDialogString(urls.toArray(new URL[urls.size()])), throwable.getMessage()));
                labelOpenError.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        createExternalPrograms().startMail(frame);
                    }
                });
                JOptionPane.showMessageDialog(frame, labelOpenError, frame.getTitle(), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void handleUnsupportedFormat(final String path) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                log.severe("Unsupported format: " + path);
                JOptionPane.showMessageDialog(frame,
                        MessageFormat.format(getBundle().getString("unsupported-format"), Files.shortenPath(path)),
                        frame.getTitle(), JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    // helpers for external components

    public void addFilesToCatalog(List<File> files) {
        getBrowsePanel().addFilesToCatalog(files);
    }

    public void addUrlToCatalog(String string) {
        getBrowsePanel().addUrlToCatalog(string);
    }

    public void openPositionList(List<URL> urls) {
        getConvertPanel().openPositionList(urls);
    }

    public void selectPositionsOnMap(int[] selectPositions) {
        if (isMapViewAvailable())
            mapView.setSelectedPositions(selectPositions);
    }

    public void insertAllWaypoints() {
        if (isMapViewAvailable()) {
            int[] selectedRows = getPositionsView().getSelectedRows();
            getConvertPanel().clearSelection();
            mapView.insertAllWaypoints(selectedRows);
        }
    }

    public void insertOnlyTurnpoints() {
        if (isMapViewAvailable()) {
            int[] selectedRows = getPositionsView().getSelectedRows();
            getConvertPanel().clearSelection();
            mapView.insertOnlyTurnpoints(selectedRows);
        }
    }

    private void printMap(boolean withRoute) {
        mapView.print(withRoute);
    }

    private void printElevationProfile() {
        getAnalysePanel().print();
    }

    public JTable getPositionsView() {
        return getConvertPanel().getPositionsView();
    }

    public int selectDuplicatesWithinDistance(int duplicate) {
        return getConvertPanel().selectDuplicatesWithinDistance(duplicate);
    }

    public int selectPositionsThatRemainingHaveDistance(int distance) {
        return getConvertPanel().selectPositionsThatRemainingHaveDistance(distance);
    }

    public int[] selectAllButEveryNthPosition(int order) {
        return getConvertPanel().selectAllButEveryNthPosition(order);
    }

    public int selectInsignificantPositions(int significance) {
        return getConvertPanel().selectInsignificantPositions(significance);
    }

    public void clearSelection() {
        getConvertPanel().clearSelection();
    }

    public void addCoordinatesToPositions() {
        getConvertPanel().addCoordinatesToPositions();
    }

    public void addElevationToPositions() {
        getConvertPanel().addElevationToPositions();
    }

    public void addSpeedToPositions() {
        getConvertPanel().addSpeedToPositions();
    }

    public void addPostalAddressToPositions() {
        getConvertPanel().addPostalAddressToPositions();
    }

    public void addPopulatedPlaceToPositions() {
        getConvertPanel().addPopulatedPlaceToPositions();
    }

    public void revertPositions() {
        getPositionsModel().revert();
        getConvertPanel().clearSelection();
    }

    public void renameRoute(String name) {
        getConvertPanel().renameRoute(name);
    }

    // map view related helpers

    public boolean isMapViewAvailable() {
        return mapView != null && mapView.isInitialized();
    }

    public BaseNavigationPosition getMapCenter() {
        return isMapViewAvailable() ? mapView.getCenter() : getLastMapCenter();
    }

    public void addMapViewListener(MapViewListener mapViewListener) {
        mapView.addMapViewListener(mapViewListener);
    }

    public void setPedestrians(boolean pedestrians) {
        if (mapView != null)
            mapView.setPedestrians(pedestrians);
    }

    public void setAvoidHighways(boolean avoidHighways) {
        if (mapView != null)
            mapView.setAvoidHighways(avoidHighways);
    }

    public void setRecenterAfterZooming(boolean recenterAfterZooming) {
        if (mapView != null)
            mapView.setRecenterAfterZooming(recenterAfterZooming);
    }

    // elevation view related helpers

    public PositionsModel getPositionsModel() {
        return getConvertPanel().getPositionsModel();
    }

    public PositionsSelectionModel getPositionsSelectionModel() {
        return getConvertPanel().getPositionsSelectionModel();
    }

    // tab related helpers

    public boolean isBrowsePanelSelected() {
        return tabbedPane.getSelectedComponent().equals(browsePanel);
    }

    public boolean isConvertPanelSelected() {
        return tabbedPane.getSelectedComponent().equals(convertPanel);
    }

    private AnalysePanel getAnalysePanel() {
        return tabInitializer.getAnalysePanel();
    }

    private BrowsePanel getBrowsePanel() {
        return tabInitializer.getBrowsePanel();
    }

    private ConvertPanel getConvertPanel() {
        return tabInitializer.getConvertPanel();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane = new JSplitPane();
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(341);
        splitPane.setDividerSize(10);
        splitPane.setOneTouchExpandable(true);
        splitPane.setOpaque(true);
        splitPane.setResizeWeight(1.0);
        contentPane.add(splitPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(880, 580), null, 0, false));
        splitPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
        mapPanel = new JPanel();
        mapPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mapPanel.setMinimumSize(new Dimension(-1, -1));
        mapPanel.setPreferredSize(new Dimension(300, 560));
        splitPane.setLeftComponent(mapPanel);
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(3);
        splitPane.setRightComponent(tabbedPane);
        convertPanel = new JPanel();
        convertPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab(ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("convert-tab"), convertPanel);
        analysePanel = new JPanel();
        analysePanel.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab(ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("analyse-tab"), analysePanel);
        browsePanel = new JPanel();
        browsePanel.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab(ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("browse-tab"), browsePanel);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private class LazyTabInitializer implements ChangeListener {
        private Map<Component, Runnable> lazyInitializers = new HashMap<Component, Runnable>();
        private Map<Component, Object> initialized = new HashMap<Component, Object>();

        LazyTabInitializer() {
            lazyInitializers.put(convertPanel, new Runnable() {
                public void run() {
                    ConvertPanel panel = createConvertPanel();
                    convertPanel.add(panel.getRootComponent());
                    initialized.put(convertPanel, panel);
                }
            });

            lazyInitializers.put(analysePanel, new Runnable() {
                public void run() {
                    AnalysePanel panel = new AnalysePanel();
                    analysePanel.add(panel.getRootComponent());
                    initialized.put(analysePanel, panel);
                }
            });

            lazyInitializers.put(browsePanel, new Runnable() {
                public void run() {
                    BrowsePanel panel = createBrowsePanel();
                    browsePanel.add(panel.getRootComponent());
                    initialized.put(browsePanel, panel);
                }
            });
        }

        private synchronized AnalysePanel getAnalysePanel() {
            initialize(analysePanel);
            return (AnalysePanel) initialized.get(analysePanel);
        }

        private synchronized BrowsePanel getBrowsePanel() {
            initialize(browsePanel);
            return (BrowsePanel) initialized.get(browsePanel);
        }

        private synchronized ConvertPanel getConvertPanel() {
            initialize(convertPanel);
            return (ConvertPanel) initialized.get(convertPanel);
        }

        private void initialize(Component selected) {
            Runnable runnable = lazyInitializers.get(selected);
            if (runnable != null) {
                lazyInitializers.remove(selected);

                Constants.startWaitCursor(frame.getRootPane());
                try {
                    runnable.run();
                }
                finally {
                    Constants.stopWaitCursor(frame.getRootPane());
                }
            }
        }

        public void stateChanged(ChangeEvent e) {
            Component selected = ((JTabbedPane) e.getSource()).getSelectedComponent();
            initialize(selected);

            if (isBrowsePanelSelected())
                frame.getRootPane().setDefaultButton(getBrowsePanel().getDefaultButton());
            else
                frame.getRootPane().setDefaultButton(getConvertPanel().getDefaultButton());
        }
    }

    private JMenuBar createMenuBar() {
        JMenu fileMenu = JMenuHelper.createMenu("file");
        fileMenu.add(JMenuHelper.createItem("new-file"));
        fileMenu.add(JMenuHelper.createItem("open"));
        fileMenu.add(JMenuHelper.createItem("save"));
        fileMenu.add(JMenuHelper.createItem("save-as"));
        fileMenu.add(JMenuHelper.createItem("upload"));
        JMenu printMenu = JMenuHelper.createMenu("print");
        printMenu.add(JMenuHelper.createItem("print-map"));
        printMenu.add(JMenuHelper.createItem("print-map-and-route"));
        printMenu.add(JMenuHelper.createItem("print-elevation-profile"));
        fileMenu.add(printMenu);
        fileMenu.addSeparator();
        // TODO add items for last used files
        fileMenu.add(JMenuHelper.createItem("exit"));

        JMenu editMenu = JMenuHelper.createMenu("edit");
        editMenu.add(JMenuHelper.createItem("cut"));
        editMenu.add(JMenuHelper.createItem("copy"));
        editMenu.add(JMenuHelper.createItem("paste"));
        editMenu.add(JMenuHelper.createItem("select-all"));
        editMenu.addSeparator();
        editMenu.add(JMenuHelper.createItem("new-position"));
        editMenu.add(JMenuHelper.createItem("delete"));

        JMenu viewMenu = JMenuHelper.createMenu("view");
        viewMenu.add(JMenuHelper.createItem("hide-map"));
        viewMenu.add(JMenuHelper.createItem("hide-positionlist"));
        viewMenu.addSeparator();
        JMenu columnMenu = JMenuHelper.createMenu("show-column");
        // TODO add table menu items here
        viewMenu.add(columnMenu);

        JMenu toolsMenu = JMenuHelper.createMenu("tools");
        toolsMenu.add(JMenuHelper.createItem("insert-positions", new InsertPositionsAction()));
        toolsMenu.add(JMenuHelper.createItem("geocode-position", new GeocodePositionAction()));
        toolsMenu.add(JMenuHelper.createItem("complement-positions", new ComplementPositionsAction()));
        toolsMenu.add(JMenuHelper.createItem("delete-positions", new DeletePositionsAction()));
        toolsMenu.add(JMenuHelper.createItem("revert-positions", new RevertPositionListAction()));

        JMenu extrasMenu = JMenuHelper.createMenu("extras");
        extrasMenu.add(JMenuHelper.createItem("options", new OptionsAction()));

        JMenu helpMenu = JMenuHelper.createMenu("help");
        helpMenu.add(JMenuHelper.createItem("help-topics", new HelpTopicsAction()));
        helpMenu.add(JMenuHelper.createItem("search-for-updates", new SearchForUpdatesAction()));
        helpMenu.add(JMenuHelper.createItem("about", new AboutAction()));

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(toolsMenu);
        menuBar.add(extrasMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void initializeActions() {
        ActionManager actionManager = getInstance().getContext().getActionManager();
        actionManager.register("exit", new ExitAction());
        actionManager.register("print-map", new PrintMapAction(false));
        actionManager.register("print-map-and-route", new PrintMapAction(true));
        actionManager.register("print-elevation-profile", new PrintElevationProfileAction());
        actionManager.register("hide-map", new MoveSplitPaneDividerAction(splitPane, 0));
        actionManager.register("hide-positionlist", new MoveSplitPaneDividerAction(splitPane, Integer.MAX_VALUE));

    }

    private class PrintMapAction extends FrameAction {
        private boolean withRoute;

        private PrintMapAction(boolean withRoute) {
            this.withRoute = withRoute;
        }

        public void run() {
            printMap(withRoute);
        }
    }

    private class PrintElevationProfileAction extends FrameAction { // TODO move to analyse panel
        public void run() {
            printElevationProfile();
        }
    }
}
