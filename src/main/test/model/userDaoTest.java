package model;

import org.junit.Test;
import util.OrderSystemException;

import java.io.ObjectStreamException;

import static org.junit.Assert.*;

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
        user.setAdmin(0);
        user.setName("张蜜");
        user.setPassword("1100");
        userDao userDao = new userDao();
        int ret = userDao.register(user);
        System.out.println(ret);
    }

    @Test
    public void login() throws OrderSystemException {
        String name = "张蜜";
        userDao userDao = new userDao();
        User user = userDao.login(name);
        System.out.println(user);
    }

    @Test
    public void selectById() throws OrderSystemException{
        int userId = 1;
        userDao userDao = new userDao();
        User user = userDao.selectById(userId);
        System.out.println(user);
    }
}