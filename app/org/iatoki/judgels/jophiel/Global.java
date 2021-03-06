package org.iatoki.judgels.jophiel;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import org.iatoki.judgels.commons.AWSFileSystemProvider;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.JudgelsProperties;
import org.iatoki.judgels.commons.LocalFileSystemProvider;
import org.iatoki.judgels.jophiel.controllers.ClientController;
import org.iatoki.judgels.jophiel.controllers.UserActivityController;
import org.iatoki.judgels.jophiel.controllers.UserController;
import org.iatoki.judgels.jophiel.controllers.apis.ClientAPIController;
import org.iatoki.judgels.jophiel.controllers.apis.UserAPIController;
import org.iatoki.judgels.jophiel.controllers.apis.UserActivityAPIController;
import org.iatoki.judgels.jophiel.models.daos.hibernate.AccessTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.AuthorizationCodeHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.ClientHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.EmailHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.ForgotPasswordHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.IdTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.RedirectURIHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.RefreshTokenHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.UserActivityHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.hibernate.UserHibernateDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.AccessTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.AuthorizationCodeDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ClientDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.EmailDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ForgotPasswordDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.IdTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RefreshTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserActivityDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import play.Application;
import play.Play;
import play.mvc.Controller;

import java.util.HashMap;
import java.util.Map;

public final class Global extends org.iatoki.judgels.commons.Global {

    private final Map<Class, Controller> cache;

    public Global() {
        cache = new HashMap<>();
    }

    @Override
    public void onStart(Application application) {
        org.iatoki.judgels.jophiel.BuildInfo$ buildInfo = org.iatoki.judgels.jophiel.BuildInfo$.MODULE$;
        JudgelsProperties.buildInstance(buildInfo.name(), buildInfo.version());

        super.onStart(application);
    }

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        if (!cache.containsKey(controllerClass)) {
            if (controllerClass.equals(UserController.class)) {
                UserService userService = createUserService();
                ClientService clientService = createClientService();

                UserController userController = new UserController(clientService, userService);
                cache.put(UserController.class, userController);
            } else if (controllerClass.equals(UserActivityController.class)) {
                UserService userService = createUserService();
                ClientService clientService = createClientService();

                UserActivityController userActivityController = new UserActivityController(clientService, userService);
                cache.put(UserActivityController.class, userActivityController);
            } else if (controllerClass.equals(ClientController.class)) {
                UserService userService = createUserService();
                ClientService clientService = createClientService();

                ClientController clientController = new ClientController(clientService, userService);
                cache.put(ClientController.class, clientController);
            } else if (controllerClass.equals(ClientAPIController.class)) {
                UserService userService = createUserService();
                ClientService clientService = createClientService();

                ClientAPIController clientAPIController = new ClientAPIController(clientService, userService);
                cache.put(ClientAPIController.class, clientAPIController);
            } else if (controllerClass.equals(UserAPIController.class)) {
                ClientService clientService = createClientService();
                UserService userService = createUserService();

                UserAPIController userAPIController = new UserAPIController(clientService, userService);
                cache.put(UserAPIController.class, userAPIController);
            } else if (controllerClass.equals(UserActivityAPIController.class)) {
                ClientService clientService = createClientService();
                UserService userService = createUserService();

                UserActivityAPIController userActivityAPIController = new UserActivityAPIController(clientService, userService);
                cache.put(UserActivityAPIController.class, userActivityAPIController);
            }
        }
        return controllerClass.cast(cache.get(controllerClass));
    }

    private UserService createUserService() {
        UserDao userDao = new UserHibernateDao();
        EmailDao emailDao = new EmailHibernateDao();
        ForgotPasswordDao forgotPasswordDao = new ForgotPasswordHibernateDao();
        UserActivityDao userActivityDao = new UserActivityHibernateDao();
        ClientDao clientDao = new ClientHibernateDao();
        FileSystemProvider avatarProvider;
        if (JophielProperties.getInstance().isUseAWS()) {
            AmazonS3Client s3Client;
            if (Play.isProd()) {
                s3Client = new AmazonS3Client();
            } else {
                s3Client = new AmazonS3Client(new BasicAWSCredentials(JophielProperties.getInstance().getaWSAccessKey(), JophielProperties.getInstance().getaWSSecretKey()));
            }
            avatarProvider = new AWSFileSystemProvider(s3Client, JophielProperties.getInstance().getaWSAvatarBucketName(), JophielProperties.getInstance().getaWSAvatarCloudFrontURL(), JophielProperties.getInstance().getaWSAvatarBucketRegion());
        } else {
            avatarProvider = new LocalFileSystemProvider(JophielProperties.getInstance().getAvatarDir());
        }

        UserService userService = new UserServiceImpl(userDao, emailDao, forgotPasswordDao, userActivityDao, clientDao, avatarProvider);

        return userService;
    }

    private ClientService createClientService() {
        ClientDao clientDao = new ClientHibernateDao();
        RedirectURIDao redirectURIDao = new RedirectURIHibernateDao();
        AuthorizationCodeDao authorizationCodeDao = new AuthorizationCodeHibernateDao();
        AccessTokenDao accessTokenDao = new AccessTokenHibernateDao();
        RefreshTokenDao refreshTokenDao = new RefreshTokenHibernateDao();
        IdTokenDao idTokenDao = new IdTokenHibernateDao();

        ClientService clientService = new ClientServiceImpl(clientDao, redirectURIDao, authorizationCodeDao, accessTokenDao, refreshTokenDao, idTokenDao);

        return clientService;
    }

}