package com.dianping.zebra.admin.service;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.zebra.Constants;
import com.dianping.zebra.admin.dao.HeartbeatMapper;
import com.dianping.zebra.admin.dto.*;
import com.dianping.zebra.admin.entity.HeartbeatEntity;
import com.dianping.zebra.group.exception.DalException;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
* updateStatus:
* <p>
* -1 = 没有升级任何
* </p>
* <p>
* 0 = 升级成功
* </p>
* <p>
* 1 = 部分升级
* </p>
* <p>
* 2 = 没有接入dal，所以没有被dal监控到
* </p>
*
* @author damonzhu,Dozer
*/
public class ReportServiceImpl implements ReportService {
    @Autowired
    private HeartbeatMapper heartbeatMapper;

    private static final String NOT_FOUND = "N/A";

    @Autowired
    private CmdbService cmdbService;

    @Autowired
    private DatabaseRealtimeService databaseRealtimeService;

    private void buildApp(AppDto app, HeartbeatEntity hb) {
        MachineDto machine = app.findOrCreateMachine(hb.getIp());
        machine.setIp(hb.getIp());
        machine.setVersion(hb.getVersion());

        DatasourceDto ds = machine.findOrCreateDatasource(hb.getDatasourceBeanName());

        ds.setName(hb.getDatabaseName());
        ds.setUsername(hb.getUsername());
        ds.setBeanName(hb.getDatasourceBeanName());
        ds.setType(hb.getDatasourceBeanClass());
        ds.setInitPoolSize(hb.getInitPoolSize());
        ds.setJdbcUrl(hb.getJdbcUrl());
        ds.setMaxPoolSize(hb.getMaxPoolSize());
        ds.setMinPoolSize(hb.getMinPoolSize());
        ds.setReplaced(hb.isReplaced());
    }

    @Override
    public void createOrUpdate(HeartbeatEntity entity) {
        if (!Strings.isNullOrEmpty(entity.getAppName())) {
            entity.setAppName(entity.getAppName().toLowerCase());
        }

        if (Strings.isNullOrEmpty(entity.getDatabaseName())) {
            entity.setDatabaseName(NOT_FOUND);
        }
        if (Strings.isNullOrEmpty(entity.getDatasourceBeanClass())) {
            entity.setDatasourceBeanClass(NOT_FOUND);
        }
        if (Strings.isNullOrEmpty(entity.getVersion())) {
            entity.setVersion(NOT_FOUND);
        }
        if (Strings.isNullOrEmpty(entity.getUsername())) {
            entity.setUsername(NOT_FOUND);
        }

        if (!Strings.isNullOrEmpty(entity.getJdbcUrl())) {
            String[] parts = entity.getJdbcUrl().split(":");
            if (parts != null && parts.length > 2) {
                entity.setDatabaseType(parts[1].toLowerCase());
            }
        } else {
            entity.setJdbcUrl(NOT_FOUND);
            entity.setDatabaseType(NOT_FOUND);
        }

        List<HeartbeatEntity> old = heartbeatMapper.getHeartbeat(entity.getAppName(), entity.getIp(), entity.getDatasourceBeanName());

        if (old.size() > 1) {
            heartbeatMapper.deleteHeartbeat(entity.getAppName(), entity.getIp(), entity.getDatasourceBeanName());
        } else if (old.size() == 1) {
            entity.setId(old.get(0).getId());
            entity.setCreateTime(old.get(0).getCreateTime());
            entity.setUpdateTime(new Date());
            heartbeatMapper.updateHeartbeat(entity);
        } else {
            entity.setUpdateTime(new Date());
            entity.setCreateTime(new Date());
            heartbeatMapper.insertHeartbeat(entity);
        }

    }


    @Override
    public AppDto getApp(String appName, boolean isProduct) {
        AppDto app = new AppDto(appName);

        Transaction transaction = Cat.newTransaction("Report", "App");

        try {
            List<HeartbeatEntity> all = heartbeatMapper.getAll();

            for (HeartbeatEntity hb : all) {
                if (hb.getAppName().equalsIgnoreCase(Constants.PHOENIX_APP_NO_NAME) && hb.getIp() != null) {
                    String name = cmdbService.getAppName(hb.getIp());

                    if (!name.equalsIgnoreCase(Constants.PHOENIX_APP_NO_NAME)) {
                        hb.setAppName(name);

                        heartbeatMapper.updateHeartbeat(hb);
                    }
                }

                buildApp(app, hb);
            }
        } catch (DalException e) {
            Cat.logError(e);
        }

        if (isProduct) {
            new ConnectionInfoVisitor2().visitApp(app);
        }
        new StatisticsVisitor().visitApp(app);

        transaction.setStatus(Message.SUCCESS);
        transaction.complete();
        return app;
    }

    @Override
    public DatabaseDto getDatabase(String database, boolean isProduct) {
        Transaction transaction = Cat.newTransaction("Report", "Database");

        DatabaseDto database2 = getReportInternal().getDatabases().get(database);

        if (isProduct) {
            new ConnectionInfoVisitor().visitDatabase(database2);
        }

        new StatisticsVisitor().visitDatabase(database2);

        transaction.setStatus(Message.SUCCESS);
        transaction.complete();
        return database2;
    }

    @Override
    public ReportDto getReport(boolean isProduct) {
        Transaction transaction = Cat.newTransaction("Report", "All");

        ReportDto report = getReportInternal();

        if (isProduct) {
            new ConnectionInfoVisitor().visitReport(report);
        }

        new StatisticsVisitor().visitReport(report);

        transaction.setStatus(Message.SUCCESS);
        transaction.complete();
        return report;
    }

    public ReportDto getReportInternal() {
        ReportDto report = new ReportDto();
        try {
            List<HeartbeatEntity> all = heartbeatMapper.getAll();

            for (HeartbeatEntity hb : all) {
                if (!hb.getDatabaseName().equals("N/A")) {
                    DatabaseDto database = report.findOrCreateDatabase(hb.getDatabaseName());
                    database.setName(hb.getDatabaseName());

                    AppDto app = database.findOrCreateApp(hb.getAppName());
                    app.setName(hb.getAppName());

                    buildApp(app, hb);
                }
            }
        } catch (DalException e) {
            Cat.logError(e);
        }

        return report;
    }

    public class ConnectionInfoVisitor {

        private Map<String, String> connectedIps;

        private Map<String, Map<String, String>> connectedAllIps;

        public void visitReport(ReportDto report) {
            connectedAllIps = databaseRealtimeService.getAllConnectedIps();

            for (DatabaseDto database : report.getDatabases().values()) {
                visitDatabase(database);
            }
        }

        public void visitApp(AppDto app) {
            if (connectedIps != null) {
                for (Map.Entry<String, String> entry : connectedIps.entrySet()) {
                    String ip = entry.getKey();
                    String appName = entry.getValue();

                    if (appName.equalsIgnoreCase(app.getName())) {
                        MachineDto machine = app.findMachine(ip);

                        if (machine == null) {
                            machine = app.findOrCreateMachine(ip);
                            machine.setIntergrateWithDal(false);
                        }
                    }
                }
            }
        }

        public void visitDatabase(DatabaseDto database) {
            if (connectedAllIps == null) {
                connectedAllIps = databaseRealtimeService.getAllConnectedIps();
            }

            connectedIps = connectedAllIps.get(database.getName());

            for (AppDto app : database.getApps().values()) {
                visitApp(app);
            }

            if (connectedIps != null) {
                Set<String> connectedApps = new HashSet<String>();
                for (String app : connectedIps.values()) {
                    connectedApps.add(app);
                }

                for (String name : connectedApps) {
                    AppDto app = database.findApp(name);

                    if (app == null) {
                        app = database.findOrCreateApp(name);
                        visitApp(app);
                    }
                }
            }
        }
    }

    public class ConnectionInfoVisitor2 {
        public void visitApp(AppDto app) {
            Map<String, Map<String, String>> connectedIps = databaseRealtimeService.getAllConnectedIps();
            if (connectedIps != null) {
                for (Map.Entry<String, Map<String, String>> entry : connectedIps.entrySet()) {
                    Map<String, String> connections = entry.getValue();

                    for (Map.Entry<String, String> entry2 : connections.entrySet()) {
                        if (entry2.getValue().equalsIgnoreCase(app.getName())) {
                            String ip = entry2.getKey();
                            MachineDto machine = app.findMachine(ip);

                            if (machine == null) {
                                machine = app.findOrCreateMachine(ip);
                                machine.setIntergrateWithDal(false);
                            }
                        }
                    }
                }
            }
        }
    }

    public class StatisticsVisitor {

        private AppDto m_app;

        public void visitMachine(MachineDto machine) {
            for (DatasourceDto datasource : machine.getDatasources().values()) {
                visitDatasource(datasource);
            }
        }

        public void visitApp(AppDto app) {
            m_app = app;
            boolean hasNotIntegratedWithDal = false;

            for (MachineDto machine : app.getMachines().values()) {
                if (machine.isIntergrateWithDal()) {
                    visitMachine(machine);
                } else {
                    hasNotIntegratedWithDal = true;
                }
            }

            if (hasNotIntegratedWithDal) {
                app.setUpdateStatus(2);
            } else {
                if (app.getGroupDataSource() == 0 && app.getReplacedSingleDataSource() == 0
                        && app.getReplacedDpdlDataSource() == 0
                        && (app.getC3p0DataSource() != 0 || app.getDpdlDataSource() != 0)) {
                    app.setUpdateStatus(-1);
                } else if ((app.getReplacedSingleDataSource() + app.getReplacedDpdlDataSource() + app.getGroupDataSource()) == app
                        .getTotalDataSource()) {
                    app.setUpdateStatus(0);
                } else {
                    app.setUpdateStatus(1);
                }
            }
        }

        public void visitDatabase(DatabaseDto database) {
            boolean hasNotIntegratedWithDal = false;

            for (AppDto app : database.getApps().values()) {
                visitApp(app);

                database.incGroupDataSource(app.getGroupDataSource());
                database.incDpdlDataSource(app.getDpdlDataSource());
                database.incC3p0DataSource(app.getC3p0DataSource());
                database.incSingleDataSource(app.getSingleDataSource());
                database.incOtherDataSource(app.getOtherDataSource());
                database.incReplacedSingleDataSource(app.getReplacedSingleDataSource());
                database.incReplacedDpdlDataSource(app.getReplacedDpdlDataSource());
                database.incTotalDataSource(app.getTotalDataSource());

                if (app.getUpdateStatus() == 2) {
                    hasNotIntegratedWithDal = true;
                }
            }

            if (hasNotIntegratedWithDal) {
                database.setUpdateStatus(2);
            } else {
                if (database.getGroupDataSource() == 0 && database.getReplacedSingleDataSource() == 0
                        && database.getReplacedDpdlDataSource() == 0
                        && (database.getC3p0DataSource() != 0 || database.getDpdlDataSource() != 0)) {
                    database.setUpdateStatus(-1);
                } else if ((database.getReplacedSingleDataSource() + database.getReplacedDpdlDataSource() + database
                        .getGroupDataSource()) == database.getTotalDataSource()) {
                    database.setUpdateStatus(0);
                } else {
                    database.setUpdateStatus(1);
                }
            }
        }

        public void visitDatasource(DatasourceDto datasource) {
            String type = datasource.getType();
            if (type.equals("com.dianping.zebra.group.jdbc.GroupDataSource")) {
                m_app.incGroupDataSource();
            } else if (type.equals("com.dianping.dpdl.sql.DPDataSource")) {
                m_app.incDpdlDataSource();
                if (datasource.getReplaced()) {
                    m_app.incReplacedDpdlDataSource();
                }
            } else if (type.equals("com.dianping.zebra.group.jdbc.SingleDataSource")) {
                m_app.incSingleDataSource();
            } else if (type.equals("com.mchange.v2.c3p0.ComboPooledDataSource")) {
                m_app.incC3p0DataSource();
                if (datasource.getReplaced()) {
                    m_app.incReplacedSingleDataSource();
                }
            } else {
                m_app.incOtherDataSource();
            }

            m_app.incTotalDataSource();
        }

        public void visitReport(ReportDto report) {
            for (DatabaseDto database : report.getDatabases().values()) {
                visitDatabase(database);

                report.incGroupDataSource(database.getGroupDataSource());
                report.incDpdlDataSource(database.getDpdlDataSource());
                report.incC3p0DataSource(database.getC3p0DataSource());
                report.incSingleDataSource(database.getSingleDataSource());
                report.incOtherDataSource(database.getOtherDataSource());
                report.incReplacedSingleDataSource(database.getReplacedSingleDataSource());
                report.incReplacedDpdlDataSource(database.getReplacedDpdlDataSource());
                report.incTotalDataSource(database.getTotalDataSource());

                switch (database.getUpdateStatus()) {
                    case -1:
                        report.incFailureDatabase();
                        break;
                    case 0:
                        report.incSuccessDatabase();
                        break;
                    default:
                        report.incHalfDoneDatabase();
                }
            }
        }
    }
}
