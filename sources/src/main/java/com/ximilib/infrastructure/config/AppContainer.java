package com.ximilib.infrastructure.config;

import com.ximilib.application.port.PasswordHasher;
import com.ximilib.application.service.AuthenticationService;
import com.ximilib.application.service.BookCatalogService;
import com.ximilib.application.service.CirculationService;
import com.ximilib.application.service.DashboardService;
import com.ximilib.application.service.impl.AuthenticationServiceImpl;
import com.ximilib.application.service.impl.BookCatalogServiceImpl;
import com.ximilib.application.service.impl.CirculationServiceImpl;
import com.ximilib.application.service.impl.DashboardServiceImpl;
import com.ximilib.domain.repository.BookRepository;
import com.ximilib.domain.repository.CirculationRepository;
import com.ximilib.domain.repository.UserRepository;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import com.ximilib.infrastructure.persistence.impl.BookRepositoryImpl;
import com.ximilib.infrastructure.persistence.impl.CirculationRepositoryImpl;
import com.ximilib.infrastructure.persistence.impl.DashboardRepositoryImpl;
import com.ximilib.infrastructure.persistence.impl.UserRepositoryImpl;
import com.ximilib.infrastructure.security.SecurityUtil;

public class AppContainer {

    private static AppContainer instance;

    private final DatabaseConnectionManager connectionManager;
    private final PasswordHasher passwordHasher;
    private final BookRepository bookRepository;
    private final BookCatalogService bookCatalogService;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final CirculationRepository circulationRepository;
    private final CirculationService circulationService;
    private final DashboardServiceImpl dashboardService;
    private final com.ximilib.application.port.BookCommentPort bookCommentPort;
    private final com.ximilib.application.port.FeedbackPort feedbackPort;
    private final com.ximilib.application.port.ReportPort reportPort;
    private final com.ximilib.application.port.ReportExporter reportExporter;
    private final com.ximilib.application.port.SystemSettingsPort systemSettingsPort;

    private AppContainer() {
        this.connectionManager = new DatabaseConnectionManager();
        this.passwordHasher = new SecurityUtil();
        BookRepositoryImpl bookRepo = new BookRepositoryImpl(connectionManager);
        this.bookRepository = bookRepo;
        this.bookCatalogService = new BookCatalogServiceImpl(bookRepository);
        UserRepositoryImpl userRepo = new UserRepositoryImpl(connectionManager);
        this.userRepository = userRepo;
        this.authenticationService = new AuthenticationServiceImpl(userRepository, passwordHasher);
        CirculationRepositoryImpl circRepo = new CirculationRepositoryImpl(connectionManager);
        this.circulationRepository = circRepo;
        com.ximilib.application.port.TransactionCoordinator txCoordinator = new com.ximilib.infrastructure.persistence.connection.JdbcTransactionCoordinator(connectionManager);
        this.circulationService = new CirculationServiceImpl(circulationRepository, null, txCoordinator);
        DashboardRepositoryImpl dashRepo = new DashboardRepositoryImpl(connectionManager);
        this.dashboardService = new DashboardServiceImpl(dashRepo);
        this.bookCommentPort = new com.ximilib.infrastructure.persistence.impl.BookCommentRepositoryImpl(connectionManager);
        this.feedbackPort = new com.ximilib.infrastructure.persistence.impl.FeedbackRepositoryImpl(connectionManager);
        this.reportPort = new com.ximilib.infrastructure.persistence.impl.ReportRepositoryImpl(connectionManager);
        this.reportExporter = new com.ximilib.infrastructure.reporting.ExcelReportExporter();
        this.systemSettingsPort = new com.ximilib.infrastructure.persistence.impl.SystemSettingsRepositoryImpl(connectionManager);
    }

    public static synchronized AppContainer getInstance() {
        if (instance == null) {
            instance = new AppContainer();
        }
        return instance;
    }

    public DatabaseConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public BookRepository getBookRepository() {
        return bookRepository;
    }

    public BookCatalogService getBookCatalogService() {
        return bookCatalogService;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public PasswordHasher getPasswordHasher() {
        return passwordHasher;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public CirculationRepository getCirculationRepository() {
        return circulationRepository;
    }

    public CirculationService getCirculationService() {
        return circulationService;
    }

    public DashboardService getDashboardService() {
        return dashboardService;
    }

    public com.ximilib.application.port.BookCommentPort getBookCommentPort() {
        return bookCommentPort;
    }

    public com.ximilib.application.port.FeedbackPort getFeedbackPort() {
        return feedbackPort;
    }

    public com.ximilib.application.port.ReportPort getReportPort() {
        return reportPort;
    }

    public com.ximilib.application.port.ReportExporter getReportExporter() {
        return reportExporter;
    }

    public com.ximilib.application.port.SystemSettingsPort getSystemSettingsPort() {
        return systemSettingsPort;
    }
}
