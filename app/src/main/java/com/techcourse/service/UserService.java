package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import nextstep.jdbc.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final PlatformTransactionManager transactionManager;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = new DataSourceTransactionManager(DataSourceConfig.getInstance());
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        final TransactionStatus transactionStatus =
                transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userDao.insert(user);
            transactionManager.commit(transactionStatus);
        } catch (final Exception e) {
            transactionManager.rollback(transactionStatus);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final TransactionStatus transactionStatus =
                transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            transactionManager.commit(transactionStatus);
        } catch (final Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
