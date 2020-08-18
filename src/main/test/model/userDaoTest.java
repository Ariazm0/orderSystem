package model;

import org.junit.Test;
import util.OrderSystemException;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: Ariazm
 * Date: 2020-08-14
 * Time: 16:44
 */
public class userDaoTest {

    @Test
    public void register() throws OrderSystemException {
        User user = new User();
        user.setIsdmin(0);
        user.setName("aria");
        user.setPassword("1100");
        UserDao userDao = new UserDao();
        int ret = userDao.register(user);
        System.out.println(ret);
    }

    @Test
    public void login() throws OrderSystemException {
        String name = "张蜜";
        UserDao userDao = new UserDao();
        User user = userDao.login(name);
        System.out.println(user);
    }

    @Test
    public void selectById() throws OrderSystemException{
        int userId = 1;
        UserDao userDao = new UserDao();
        User user = userDao.selectById(userId);
        System.out.println(user);
    }
}