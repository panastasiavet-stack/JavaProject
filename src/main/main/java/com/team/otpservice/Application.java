package main.main.java.com.team.otpservice;

import com.sun.net.httpserver.HttpServer;
import com.team.otpservice.config.AppConfig;
import com.team.otpservice.controller.AdminController;
import com.team.otpservice.controller.AuthController;
import com.team.otpservice.controller.HealthController;
import com.team.otpservice.controller.OtpController;
import com.team.otpservice.dao.impl.JdbcOperationDao;
import com.team.otpservice.dao.impl.JdbcOtpCodeDao;
import com.team.otpservice.dao.impl.JdbcOtpConfigDao;
import com.team.otpservice.dao.impl.JdbcUserDao;
import com.team.otpservice.db.ConnectionFactory;
import com.team.otpservice.db.DatabaseInitializer;
import com.team.otpservice.http.HttpServerFactory;
import com.team.otpservice.http.Router;
import com.team.otpservice.scheduler.SchedulerManager;
import com.team.otpservice.security.AuthorizationService;
import com.team.otpservice.security.JwtService;
import com.team.otpservice.security.PasswordService;
import com.team.otpservice.service.AdminService;
import com.team.otpservice.service.AuthService;
import com.team.otpservice.service.NotificationRouter;
import com.team.otpservice.service.OtpConfigService;
import com.team.otpservice.service.OtpExpirationService;
import com.team.otpservice.service.OtpService;
import com.team.otpservice.service.UserService;
import com.team.otpservice.service.impl.EmailNotificationService;
import com.team.otpservice.service.impl.FileNotificationService;
import com.team.otpservice.service.impl.SmsNotificationService;
import com.team.otpservice.service.impl.TelegramNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        AppConfig config = AppConfig.load();
        ConnectionFactory connectionFactory = new ConnectionFactory(config.databaseConfig());

        DatabaseInitializer databaseInitializer = new DatabaseInitializer(connectionFactory);
        databaseInitializer.initSchema();

        JdbcUserDao userDao = new JdbcUserDao(connectionFactory);
        JdbcOtpConfigDao otpConfigDao = new JdbcOtpConfigDao(connectionFactory);
        JdbcOperationDao operationDao = new JdbcOperationDao(connectionFactory);
        JdbcOtpCodeDao otpCodeDao = new JdbcOtpCodeDao(connectionFactory);

        PasswordService passwordService = new PasswordService();
        JwtService jwtService = new JwtService(config.jwtConfig());
        AuthorizationService authorizationService = new AuthorizationService(jwtService);
        OtpConfigService otpConfigService = new OtpConfigService(otpConfigDao);
        UserService userService = new UserService(userDao, otpCodeDao, operationDao);
        AuthService authService = new AuthService(userDao, passwordService, jwtService);
        NotificationRouter notificationRouter = new NotificationRouter();
        notificationRouter.register(com.team.otpservice.model.DeliveryChannel.EMAIL, new EmailNotificationService(config.mailConfig()));
        notificationRouter.register(com.team.otpservice.model.DeliveryChannel.SMS, new SmsNotificationService(config.smppConfig()));
        notificationRouter.register(com.team.otpservice.model.DeliveryChannel.TELEGRAM, new TelegramNotificationService(config.telegramConfig()));
        notificationRouter.register(com.team.otpservice.model.DeliveryChannel.FILE, new FileNotificationService(config.fileStorageConfig()));

        OtpService otpService = new OtpService(operationDao, otpCodeDao, otpConfigService, notificationRouter, userDao);
        AdminService adminService = new AdminService(userService, otpConfigService);
        OtpExpirationService otpExpirationService = new OtpExpirationService(otpCodeDao);

        Router router = new Router(authorizationService);
        new AuthController(authService).register(router);
        new AdminController(adminService).register(router);
        new OtpController(otpService).register(router);
        new HealthController().register(router);

        HttpServer server = HttpServerFactory.create(config.serverPort(), router);
        SchedulerManager schedulerManager = new SchedulerManager(config.schedulerIntervalSeconds(), otpExpirationService);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");
            schedulerManager.stop();
            server.stop(1);
            connectionFactory.close();
        }));

        schedulerManager.start();
        server.start();
        logger.info("OTP service started on http://{}:{}", config.serverHost(), config.serverPort());
    }
}
