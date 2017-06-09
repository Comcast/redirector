/**
 * Copyright 2017 Comcast Cable Communications Management, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Paul Guslisty (pguslisty@productengine.com)
 */


package com.comcast.redirector.api.redirectorOffline.service;

import com.comcast.redirector.api.config.RedirectorConfig;
import com.comcast.redirector.api.model.*;
import com.comcast.redirector.api.model.namespaced.NamespaceChangesStatus;
import com.comcast.redirector.api.model.namespaced.NamespacedList;
import com.comcast.redirector.api.model.namespaced.Namespaces;
import com.comcast.redirector.api.model.pending.PendingChange;
import com.comcast.redirector.api.model.pending.PendingChangesStatus;
import com.comcast.redirector.api.model.url.rule.URLRules;
import com.comcast.redirector.api.model.whitelisted.Whitelisted;
import com.comcast.redirector.api.model.whitelisted.WhitelistedStackUpdates;
import com.comcast.redirector.api.redirector.service.IWhiteListService;
import com.comcast.redirector.api.redirector.service.NamespacedListsService;
import com.comcast.redirector.api.redirector.service.NamespacesChangesService;
import com.comcast.redirector.api.redirector.service.pending.IChangesStatusService;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingDistributionDiffHelper;
import com.comcast.redirector.api.redirector.service.pending.helper.PendingWhitelistedDiffHelper;
import com.comcast.redirector.api.redirector.service.ruleengine.*;
import com.comcast.redirector.api.redirectorOffline.*;
import com.comcast.redirector.common.RedirectorConstants;
import com.comcast.redirector.common.serializers.JSONSerializer;
import com.comcast.redirector.common.serializers.Serializer;
import com.comcast.redirector.common.serializers.SerializerException;
import com.comcast.redirector.common.serializers.core.JsonSerializer;
import com.comcast.redirector.core.applications.Applications;
import com.comcast.redirector.core.modelupdate.data.ModelMetadata;
import com.comcast.redirector.dataaccess.client.IDataSourceConnector;
import com.comcast.redirector.ruleengine.repository.NamespacedListsBatch;
import com.google.common.jimfs.Jimfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.WebApplicationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.stream.Collectors.toMap;

@Service
public class RedirectorOfflineModeService {

    private static Logger log = LoggerFactory.getLogger(RedirectorOfflineModeService.class);

    private static final String NAMESPACEDLISTS_JSON = "namespacedlists-backup.json";
    private static final String NAMESPACEDLISTS_JSON_OLDFORMAT = "namespacedlists.json";
    private static final String NAMESPACES_CHANGES = "namespacesChanges";

    @Autowired
    private RedirectorConfig redirectorConfig;

    @Autowired
    private SnapshotManager snapshotManager;

    @Autowired
    @Qualifier("flavorRulesService")
    private IFlavorRulesService flavorRulesService;

    @Autowired
    @Qualifier("urlRulesService")
    private IUrlRulesService urlRulesService;

    @Autowired
    @Qualifier("templateFlavorRulesService")
    private IFlavorRulesService templateFlavorRulesService;

    @Autowired
    @Qualifier("templateUrlRulesService")
    private IUrlRulesService templateUrlRulesService;

    @Autowired
    private IDistributionService distributionService;

    @Autowired
    private IWhiteListService whiteListService;

    @Autowired
    @Qualifier("coreBackupChangesStatusService")
    private IChangesStatusService coreBackupPendingChangesService;

    @Autowired
    @Qualifier("changesStatusService") //todo: change variable name to 'changesStatusService'?
    private IChangesStatusService pendingChangesService;

    @Autowired
    private IUrlParamsService urlParamsService;

    @Autowired
    private IServerService serverService;

    @Autowired
    private IDataSourceConnector connector;

    @Autowired
    private NamespacedListsService namespacedListsService;

    @Autowired
    private NamespacesChangesService namespacesChangesService;

    private Namespaces namespacesFromBackup;

    private DataListenersFacade dataListenersFacade;

    @Autowired
    private Serializer xmlSerializer;
    
    @Autowired
    private Serializer jsonSerializer;
    
    private Serializer jsonFasterxmlSerializer;

    public RedirectorOfflineModeService() {
        jsonFasterxmlSerializer = JsonSerializer.serializerIncludeNonNull();
    }

    @PostConstruct
    public void init () {
        dataListenersFacade = new DataListenersFacade(connector);
    }

    public OfflineChangesStatus calculateOfflinePendingChanges(String serviceName, InputStream coreBackupInputStream) throws IOException, SerializerException {
        namespacesFromBackup = null;

        Snapshot snapshot = createSnapshotFromCoreBackupAndInitNamespacedListsFromBackup(serviceName, coreBackupInputStream);

        int modelVersion = dataListenersFacade.getModelVersion(serviceName);

        NamespaceChangesStatus namespaceChangesStatus = calculateOfflineNamespaceChange();

        if (modelVersion > snapshot.getVersion() && namespaceChangesStatus.getNamespaceChanges().isEmpty()) {
            throw new WebApplicationException("Current data model version is higher than version of data model you are trying to import.");
        } else if (modelVersion > snapshot.getVersion()) {
            namespacesChangesService.save(NAMESPACES_CHANGES, namespaceChangesStatus);
            return new OfflineChangesStatus(null, namespaceChangesStatus);
        } else {
            pendingChangesService.savePendingChangesStatus(serviceName, new PendingChangesStatus());
        }

        PendingChangesStatus pendingChangesStatus = calculateDifference(snapshot);
        coreBackupPendingChangesService.savePendingChangesStatus(serviceName, pendingChangesStatus);

        namespacesChangesService.save(NAMESPACES_CHANGES, namespaceChangesStatus);

        return new OfflineChangesStatus(pendingChangesStatus, namespaceChangesStatus);

    }

    private NamespaceChangesStatus calculateOfflineNamespaceChange() {
        NamespaceChangesStatus namespaceChangesStatus = new NamespaceChangesStatus();

        if (namespacesFromBackup != null) {

            Predicate<Map.Entry<String, NamespacedList>> fresherThanBackup = namespacedListEntry ->
                    namespacedListEntry.getValue().getVersion() > namespacesFromBackup.getVersion();

            Map<String, NamespacedList> namespacedMapCurrent = namespacedListsService.getAllNamespacedLists().
                    getNamespaces().stream().collect(toMap(NamespacedList::getName, Function.identity()));

            if (namespacedMapCurrent.entrySet().stream().noneMatch(fresherThanBackup)) {

                Map<NamespacedList, ActionType> namespaceChanges = OfflineRedirectorHelper.getNamespaceChanges(
                        namespacedMapCurrent, namespacesFromBackup.getNamespaces());

                namespaceChangesStatus.setNamespaceChanges(namespaceChanges);

                return namespaceChangesStatus;
            }
        }

        return namespaceChangesStatus;
    }

    private void createNamespacePackFromCoreBackup(ZipInputStream zipInputStream) throws IOException, SerializerException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (int chunk = zipInputStream.read(); chunk != -1; chunk = zipInputStream.read()) {
                out.write(chunk);
            }

            this.namespacesFromBackup = jsonFasterxmlSerializer.deserialize(out.toString(), Namespaces.class);
        }
    }

    private Snapshot createSnapshotFromCoreBackupAndInitNamespacedListsFromBackup(String serviceName, InputStream coreBackup) throws IOException, SerializerException {
        Snapshot snapshot = new Snapshot(serviceName);

        if (coreBackup != null) {
            ZipInputStream zipInputStream = new ZipInputStream(coreBackup);
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    zipEntry = zipInputStream.getNextEntry();
                }

                if (zipEntry.getName().contains(NAMESPACEDLISTS_JSON)) {
                    createNamespacePackFromCoreBackup(zipInputStream);
                }

                if (zipEntry.getName().contains(serviceName + "/")) {
                    String[] pathParts = zipEntry.getName().split("/");
                    String fileName = pathParts[pathParts.length - 1];
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        for (int chunk = zipInputStream.read(); chunk != -1; chunk = zipInputStream.read()) {
                            out.write(chunk);
                        }
                        switch (fileName) {
                            case RedirectorConstants.BACKUPFILE_SELECT_SERVER:
                                SelectServer selectServer = xmlSerializer.deserialize(out.toString(), SelectServer.class);
                                snapshot.setFlavorRules(selectServer);
                                snapshot.setDistribution(selectServer.getDistribution());
                                snapshot.setServers(selectServer.getDistribution().getDefaultServer());
                                break;
                            case RedirectorConstants.BACKUPFILE_URL_RULES:
                                URLRules urlRules = xmlSerializer.deserialize(out.toString(), URLRules.class);
                                snapshot.setUrlRules(urlRules);
                                snapshot.setDefaultUrlParams(urlRules.getDefaultStatement());
                                break;
                            case RedirectorConstants.BACKUPFILE_FLAVOR_TEMPLATES:
                                snapshot.setTemplatePathRules(jsonSerializer.deserialize(out.toByteArray(), SelectServer.class));
                                break;
                            case RedirectorConstants.BACKUPFILE_URL_TEMPLATES:
                                snapshot.setTemplateUrlRules(jsonSerializer.deserialize(out.toByteArray(), URLRules.class));
                                break;
                            case RedirectorConstants.BACKUPFILE_WHITELIST:
                                snapshot.setWhitelist(xmlSerializer.deserialize(out.toString(), Whitelisted.class));
                                break;
                            case RedirectorConstants.BACKUPFILE_WHITELIST_UPDATES:
                                snapshot.setWhitelistedStackUpdates(xmlSerializer.deserialize(out.toString(), WhitelistedStackUpdates.class));
                                break;
                            case RedirectorConstants.BACKUPFILE_MODELMETADATA:
                                snapshot.setVersion(Integer.valueOf(jsonSerializer.deserialize(out.toByteArray(), String.class)));
                                break;
                        }
                    }
                }
            }
        }
        return snapshot;
    }

    private PendingChangesStatus calculateDifference(Snapshot snapshot){
        String serviceName = snapshot.getApplication();
        PendingChangesStatus pendingChangesStatus = new PendingChangesStatus();

        Map<String, PendingChange> flavorRulesChanges = OfflineRedirectorHelper.getRulesPendingChange(
                flavorRulesService.getRulesInMap(serviceName), snapshot.getFlavorRules().getItems());

        Map<String, PendingChange> urlRulesChanges = OfflineRedirectorHelper.getRulesPendingChange(
                urlRulesService.getAllRulesInMap(serviceName), snapshot.getUrlRules().getItems());

        Map<String, PendingChange> flavorTemplatesChanges = OfflineRedirectorHelper.getRulesPendingChange(
                templateFlavorRulesService.getRulesInMap(serviceName), snapshot.getTemplatePathRules().getItems());

        Map<String, PendingChange> urlTemplatesChanges = OfflineRedirectorHelper.getRulesPendingChange(
                templateUrlRulesService.getAllRulesInMap(serviceName), snapshot.getTemplateUrlRules().getItems());

        pendingChangesStatus.setPathRules(flavorRulesChanges);
        pendingChangesStatus.setUrlRules(urlRulesChanges);
        pendingChangesStatus.setTemplatePathRules(flavorTemplatesChanges);
        pendingChangesStatus.setTemplateUrlPathRules(urlTemplatesChanges);
        pendingChangesStatus.setDistributions(PendingDistributionDiffHelper.getDistributionsDiff(snapshot.getDistribution(), distributionService.getDistribution(serviceName)));
        pendingChangesStatus.setWhitelisted(PendingWhitelistedDiffHelper.getWhitelistedDiff(snapshot.getWhitelist(), whiteListService.getWhitelistedStacks(serviceName)));

        // set default server changes
        Map<String, PendingChange> serverChange = OfflineRedirectorHelper.getServerPendingChange(
                serverService.getServer(serviceName), snapshot.getServers());
        if (serverChange != null) {
            pendingChangesStatus.setServers(serverChange);
        }

        // set default urlParams changes
        Map<String, PendingChange> urlParamsChange = OfflineRedirectorHelper.getDefaultUrlParams(
                urlParamsService.getDefaultUrlParams(serviceName), snapshot.getDefaultUrlParams());
        if (urlParamsChange != null) {
            pendingChangesStatus.setUrlParams(urlParamsChange);
        }

        return pendingChangesStatus;
    }

    public Snapshot getSnapshot(String serviceName) {
        Snapshot snapshot = null;
        try {
            byte[] snapshotByteArray = snapshotManager.getSnapshot(serviceName);
            if (snapshotByteArray != null && snapshotByteArray.length > 0) {
                snapshot = jsonSerializer.deserialize(snapshotByteArray, Snapshot.class);
            }
        } catch (Exception e) {
            throw new WebApplicationException("Failed to get snapshot for " + serviceName + " application. " + e.getMessage(), e);
        }
        return snapshot;
    }

    public Namespaces getNamespacedList() {

        Namespaces namespaces;
        try {
            byte[] namespacesByteArray = snapshotManager.getSnapshot(SnapshotFilesPathHelper.SnapshotEntity.NAMESPACED_LISTS.toString());
            if (namespacesByteArray != null && namespacesByteArray.length > 0) {
                namespaces = jsonSerializer.deserialize(namespacesByteArray, Namespaces.class);
            }
            else {
                throw new Exception("Snapshot of Namespaced Lists not found.");
            }
        } catch (Exception e) {
            throw new WebApplicationException("Failed to get Namespaced Lists. " + e.getMessage(), e);
        }

        return namespaces;
    }

    public com.comcast.redirector.api.model.RedirectorConfig getConfig() {

        com.comcast.redirector.api.model.RedirectorConfig config;
        try {
            byte[] configByteArray = snapshotManager.getSnapshot(SnapshotFilesPathHelper.SnapshotEntity.CONFIG.toString());
            if (configByteArray != null && configByteArray.length > 0) {
                config = jsonSerializer.deserialize(configByteArray, com.comcast.redirector.api.model.RedirectorConfig.class);
            }
            else {
                throw new Exception("Snapshot of Redirector Settings not found.");
            }
        } catch (Exception e) {
            throw new WebApplicationException("Failed to get RedirectorSettings. " + e.getMessage(), e);
        }

        return config;
    }

    public AppNames getApplicationNames() {

        AppNames appNames;
        try {
            byte[] appNamesBytes = snapshotManager.getSnapshot(SnapshotFilesPathHelper.SnapshotEntity.APPLICATIONS.toString());
            if (appNamesBytes != null && appNamesBytes.length > 0) {
                appNames = jsonSerializer.deserialize(appNamesBytes, AppNames.class);
            }
            else {
                throw new Exception("Snapshot of applications names list not found.");
            }
        }catch (Exception e) {
            throw new WebApplicationException("Failed to get list of application names. " + e.getMessage(), e);
        }

        return appNames;
    }

    public SnapshotList getAllSnapshots() {

        AppNames appNames = getApplicationNames();
        List<Snapshot> snapshots = new ArrayList<>(appNames.getAppNames().size());
        Iterator<String> appsIter = appNames.getAppNames().iterator();
        while(appsIter.hasNext()) {
            String appName = appsIter.next();
            Snapshot snapshot = getSnapshot(appName);
            if (snapshot != null) {
                snapshots.add(getSnapshot(appName));
            }
            else {
                appsIter.remove();
                log.warn("Failed to load snapshot for app: {}", appName);
            }
        }

        SnapshotList allSnapshots = new SnapshotList();
        allSnapshots.setItems(snapshots);
        allSnapshots.setNamespaces(getNamespacedList());
        allSnapshots.setApplicationsNames(appNames);
        allSnapshots.setConfig(getConfig());

        return allSnapshots;
    }

    public byte[] createXREApplicationBackup(SnapshotList snapshotList) throws SerializerException, IOException {
        byte[] backup;
        FileSystem jimfs = Jimfs.newFileSystem();
        final String rootBackupDirectory = "XRERedirectorBackup";
        final Path rootPath = jimfs.getPath(rootBackupDirectory);
        deleteDirecotry(rootPath.resolve(rootBackupDirectory));
        Path backupArchive = Paths.get(redirectorConfig.getSnapshotBasePath()).resolve("xreApplicationBackup.zip");
        Files.deleteIfExists(backupArchive);
        final Path rootDir = Files.createDirectories(rootPath.resolve(rootBackupDirectory));

        Applications applications = new Applications(snapshotList.getApplicationsNames().getAppNames());
        NamespacedListsBatch namespacedListBatch = new NamespacedListsBatch();
        namespacedListBatch.setDataNodeVersion(snapshotList.getNamespaces().getDataNodeVersion());

        for(NamespacedList namespace: snapshotList.getNamespaces().getNamespaces()) {
            namespacedListBatch.addValues(namespace.getName(), namespace.getValues());
        }

        Namespaces namespaces = new Namespaces();
        namespaces.setNamespaces(snapshotList.getNamespaces().getNamespaces());
        namespaces.setVersion(new Date().getTime());

        Path applicationsFile = rootDir.resolve("applications.json");
        Files.write(applicationsFile, jsonFasterxmlSerializer.serialize(applications, true).getBytes("UTF-8"));
        Path namespacedListsBatch = rootDir.resolve(NAMESPACEDLISTS_JSON_OLDFORMAT);
        Files.write(namespacedListsBatch, jsonFasterxmlSerializer.serialize(namespacedListBatch, true).getBytes("UTF-8"));
        Path namespacesFile = rootDir.resolve(NAMESPACEDLISTS_JSON);
        Files.write(namespacesFile, jsonFasterxmlSerializer.serialize(namespaces, true).getBytes("UTF-8"));

        for(Snapshot snapshot: snapshotList.getItems()) {
            String stackSnapshot = snapshot.getStackBackup();
            SelectServer selectServer = ModelFactory.newSelectServer(snapshot.getFlavorRules().getItems(), snapshot.getDistribution(), snapshot.getServers());
            URLRules urlRules = ModelFactory.newUrlRules(snapshot.getUrlRules().getItems(), snapshot.getDefaultUrlParams().getUrlRule());
            Whitelisted whitelisted = ModelFactory.newWhitelisted(snapshot.getWhitelist());
            WhitelistedStackUpdates whitelistedUpdates = ModelFactory.newWhitelistedStackUpdates(snapshot.getWhitelistedStackUpdates());
            URLRules urlTemplates = ModelFactory.newUrlRules(snapshot.getTemplateUrlRules().getItems(), null);
            SelectServer flavorTemplates = ModelFactory.newSelectServer(snapshot.getTemplatePathRules().getItems(), null, null);
            ModelMetadata modelMetadata = new ModelMetadata();
            modelMetadata.setVersion(snapshot.getVersion());

            String xmlSelectServer = xmlSerializer.serialize(selectServer);
            String xmlUrlRules = xmlSerializer.serialize(urlRules);
            String xmlWhitelisted = xmlSerializer.serialize(whitelisted);
            String xmlWhitelistedUpdates = xmlSerializer.serialize(whitelistedUpdates);
            byte[] jsonModelMetadata = jsonSerializer.serializeToByteArray(modelMetadata);
            byte[] urlTemplatesArray = jsonSerializer.serializeToByteArray(urlTemplates);
            byte[] flavorTemplatesArray = jsonSerializer.serializeToByteArray(flavorTemplates);

            Path appPath = rootDir.resolve(snapshot.getApplication());
            Path appDir = Files.createDirectory(appPath);

            Path manualBackupFile = appDir.resolve("manualbackup.json");
            Files.write(manualBackupFile, stackSnapshot.getBytes("UTF-8"));

            Path modelMetadataFile = appDir.resolve("modelmetadata.json");
            Files.write(modelMetadataFile, jsonModelMetadata);

            Path urlTemplatesFile = appDir.resolve(RedirectorConstants.BACKUPFILE_URL_TEMPLATES);
            Files.write(urlTemplatesFile, urlTemplatesArray);

            Path flavorTemplatesFile = appDir.resolve(RedirectorConstants.BACKUPFILE_FLAVOR_TEMPLATES);
            Files.write(flavorTemplatesFile, flavorTemplatesArray);

            Path selectServerFile = appDir.resolve("selectserver.xml");
            Files.write(selectServerFile, xmlSelectServer.getBytes("UTF-8"));

            Path urlRulesFile = appDir.resolve("urlrules.xml");
            Files.write(urlRulesFile, xmlUrlRules.getBytes("UTF-8"));

            Path whitelistFile = appDir.resolve("whitelist.xml");
            Files.write(whitelistFile, xmlWhitelisted.getBytes("UTF-8"));

            Path whitelistUpdateFile = appDir.resolve("whitelist_updates.xml");
            Files.write(whitelistUpdateFile, xmlWhitelistedUpdates.getBytes("UTF-8"));
        }

        URI uri = URI.create("jar:file:" + backupArchive.toUri().getPath());
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        try (FileSystem zipFileSystem = FileSystems.newFileSystem(uri, env)){

            Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    String fileName = null;
                    if (dir != null) {
                        fileName = getFileName(dir);
                    }
                    final Path dirToCreate = zipFileSystem.getPath(fileName);
                    if (Files.notExists(dirToCreate) && !rootBackupDirectory.equals(fileName)) {
                        Files.createDirectories(dirToCreate);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path dest;
                    if (file != null) {
                        String fileName = getFileName(file);
                        if (("applications.json").equals(fileName) || (NAMESPACEDLISTS_JSON_OLDFORMAT).equals(fileName) || (NAMESPACEDLISTS_JSON).equals(fileName)) {
                            dest = zipFileSystem.getPath(fileName);
                        } else {
                            String parentFileName = null;
                            Path parent = file.getParent();
                            if (parent != null) {
                                parentFileName = getFileName(parent);
                            }
                            dest = zipFileSystem.getPath(parentFileName, fileName);
                        }
                        Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                    return FileVisitResult.CONTINUE;
                }

                private String getFileName(Path file) {
                    String fileName = null;
                    Path path = file.getFileName();
                    if (path != null) {
                        fileName = path.toString();
                    }
                    return fileName;
                }
            });
        }
        backup = Files.readAllBytes(backupArchive);
        return backup;
    }

    public boolean hasOfflinePendingChanges(String serviceName) {
        return !coreBackupPendingChangesService.getPendingChangesStatus(serviceName).isPendingChangesEmpty();
    }

    public boolean hasOfflineChanges(OfflineChangesStatus offlineChangesStatus) {

        if (offlineChangesStatus == null) {
            return true;
        }

        PendingChangesStatus pendingChangesStatus = offlineChangesStatus.getPendingChangesStatus();
        NamespaceChangesStatus namespaceChangesStatus = offlineChangesStatus.getNamespaceChangesStatus();

        if (pendingChangesStatus != null && !pendingChangesStatus.isPendingChangesEmpty()) {
            return false;
        }

        if (namespaceChangesStatus != null && !namespaceChangesStatus.isNamespacesChangesEmpty()) {
            return false;
        }

        return true;
    }

    private void deleteDirecotry(Path path) throws IOException {
        if (Files.exists(path)){
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
        }
    }
}
