package org.iatoki.judgels.jophiel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.Utilities;
import org.iatoki.judgels.jophiel.models.daos.interfaces.EmailDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.UserDao;
import org.iatoki.judgels.jophiel.models.domains.EmailModel;
import org.iatoki.judgels.jophiel.models.domains.UserModel;
import org.iatoki.judgels.jophiel.models.metas.MetaEmail;
import play.mvc.Http;

import javax.persistence.NoResultException;
import java.util.List;

public class UserServiceImpl implements UserService {

    private UserDao userDao;
    private EmailDao emailDao;

    public UserServiceImpl(UserDao userDao, EmailDao emailDao) {
        this.userDao = userDao;
        this.emailDao = emailDao;
    }

    @Override
    public User findUserById(long userId) {
        UserModel userRecord = userDao.findById(userId);
        EmailModel emailRecord = emailDao.findByUserJid(userRecord.jid);

        User user = new User(userRecord.id, userRecord.username, userRecord.name, emailRecord.email);

        return user;
    }

    @Override
    public User findUserByJid(String userJid) {
        UserModel userRecord = userDao.findByJid(userJid);
        EmailModel emailRecord = emailDao.findByUserJid(userRecord.jid);

        User user = new User(userRecord.id, userRecord.username, userRecord.name, emailRecord.email);

        return user;
    }

    @Override
    public boolean isUserJidExist(String userJid) {
        return (userDao.findByJid(userJid) != null);
    }

    @Override
    public void createUser(String username, String name, String email, String password) {
        UserModel userModel = new UserModel();
        userModel.username = username;
        userModel.name = name;
        userModel.password = JophielUtilities.hashSHA256(password);

        userDao.persist(userModel, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());

        EmailModel emailModel = new EmailModel(email);
        emailModel.userJid = userModel.jid;

        emailDao.persist(emailModel, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());
    }

    @Override
    public void updateUser(long userId, String username, String name, String email, String password) {
        UserModel userModel = userDao.findById(userId);
        EmailModel emailModel = emailDao.findByUserJid(userModel.jid);

        userModel.username = username;
        userModel.name = name;
        userModel.password = JophielUtilities.hashSHA256(password);

        userDao.edit(userModel, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());

        emailModel.email = email;

        emailDao.edit(emailModel, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());
    }

    @Override
    public void deleteUser(long userId) {
        UserModel userModel = userDao.findById(userId);
        EmailModel emailModel = emailDao.findByUserJid(userModel.jid);

        emailDao.remove(emailModel);
        userDao.remove(userModel);
    }

    @Override
    public Page<User> pageUser(long page, long pageSize, String sortBy, String order, String filterString) {
        List<String> userUserJid = userDao.findUserJidByFilter(filterString);
        List<String> emailUserJid = emailDao.findUserJidByFilter(filterString);

        ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
        setBuilder.addAll(userUserJid);
        setBuilder.addAll(emailUserJid);

        ImmutableSet<String> userJidSet = setBuilder.build();
        long totalPage = userJidSet.size();
        ImmutableList.Builder<User> listBuilder = ImmutableList.builder();

        if (totalPage > 0) {
            List<String> sortedUserJid = null;
            if (sortBy.equals(MetaEmail.EMAIL)) {
                sortedUserJid = emailDao.sortUserJid(userJidSet, sortBy, order);
            } else {
                sortedUserJid = userDao.sortUserJid(userJidSet, sortBy, order);
            }

            List<UserModel> userModels = userDao.findBySetOfUserJid(sortedUserJid, page * pageSize, pageSize);
            List<EmailModel> emailModels = emailDao.findBySetOfUserJid(sortedUserJid, page * pageSize, pageSize);

            for (int i = 0; i < userModels.size(); ++i) {
                UserModel userModel = userModels.get(i);
                EmailModel emailModel = emailModels.get(i);
                listBuilder.add(new User(userModel.id, userModel.username, userModel.name, emailModel.email));
            }
        }

        Page<User> ret = new Page<>(listBuilder.build(), totalPage, page, pageSize);
        return ret;
    }

    @Override
    public boolean login(String usernameOrEmail, String password) {
        try {
            UserModel userModel = userDao.findByUsername(usernameOrEmail);

            if (userModel == null) {
                EmailModel emailModel = emailDao.findByEmail(usernameOrEmail);
                if (emailModel != null) {
                    userModel = userDao.findByJid(emailModel.userJid);
                }
            }

            if ((userModel != null) && (userModel.password.equals(JophielUtilities.hashSHA256(password)))) {
                Http.Context.current().session().put("user", userModel.jid);
                return true;
            } else {
                return false;
            }
        } catch (NoResultException e) {
            return false;
        }
    }

}
