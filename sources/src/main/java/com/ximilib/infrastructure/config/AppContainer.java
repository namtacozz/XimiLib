package com.ximilib.infrastructure.config;

import com.ximilib.application.port.DashboardQueryPort;
import com.ximilib.application.port.PasswordHasher;
import com.ximilib.application.port.UserQueryPort;
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
import com.ximilib.domain.repository.DashboardRepository;
import com.ximilib.domain.repository.UserRepository;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import com.ximilib.infrastructure.persistence.impl.BookRepositoryImpl;
import com.ximilib.infrastructure.persistence.impl.CirculationRepositoryImpl;
import com.ximilib.infrastructure.persistence.impl.DashboardRepositoryImpl;
import com.ximilib.infrastructure.persistence.impl.UserRepositoryImpl;
import com.ximilib.infrastructure.security.SecurityUtil;

/**
 * AppContainer: Pure DI Container (Composition Root) khởi tạo và liên kết các thành phần hệ thống.
 * Tuân thủ mục 4 trong ARCHITECTURE.md.
 */
public class AppContainer {

    private static AppContainer instance;

    private final DatabaseConnectionManager connectionManager;
    private final PasswordHasher passwordHasher;
    private final BookRepository bookRepository;
    private final BookCatalogService bookCatalogService;
    private final UserRepository userRepository;
    private final UserQueryPort userQueryPort;
    private final AuthenticationService authenticationService;
    private final CirculationRepository circulationRepository;
    private final CirculationService circulationService;
    private final DashboardRepository dashboardRepository;
    private final DashboardQueryPort dashboardQueryPort;
    private final DashboardService dashboardService;

    private AppContainer() {
        this.connectionManager = new DatabaseConnectionManager();
        this.passwordHasher = new SecurityUtil();
        this.bookRepository = new BookRepositoryImpl(connectionManager);
        this.bookCatalogService = new BookCatalogServiceImpl(bookRepository);
        UserRepositoryImpl userRepo = new UserRepositoryImpl(connectionManager);
        this.userRepository = userRepo;
        this.userQueryPort = userRepo;
        this.authenticationService = new AuthenticationServiceImpl(userRepository, userQueryPort, passwordHasher);
        this.circulationRepository = new CirculationRepositoryImpl(connectionManager);
        this.circulationService = new CirculationServiceImpl(circulationRepository);
        DashboardRepositoryImpl dashRepo = new DashboardRepositoryImpl(connectionManager);
        this.dashboardRepository = dashRepo;
        this.dashboardQueryPort = dashRepo;
        this.dashboardService = new DashboardServiceImpl(dashboardQueryPort);
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

    public UserQueryPort getUserQueryPort() {
        return userQueryPort;
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

    public DashboardRepository getDashboardRepository() {
        return dashboardRepository;
    }

    public DashboardQueryPort getDashboardQueryPort() {
        return dashboardQueryPort;
    }

    public DashboardService getDashboardService() {
        return dashboardService;
    }
}
