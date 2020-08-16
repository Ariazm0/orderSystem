package model;

import util.OrderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: Ariazm
 * Date: 2020-08-14
 * Time: 16:09
 */
public class UserDao {

    //注册功能
    public int register(User user) throws OrderSystemException{
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        String sql = "insert into user values (null,?,?,?)";
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1,user.getName());
            statement.setString(2,user.getName());
            statement.setInt(3,user.isAdmin());
            int ret = statement.executeUpdate();
            if (ret == 1) {
                return ret;
            }
        }catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("注册用户失败");
        }finally {
            DBUtil.close(connection,statement,null);
        }
        return 0;
    }
     //登录
    public User login(String name) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            String sql = "select * from user where name = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1,name);
            rs = statement.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("userId"));
                user.setName(rs.getString("name"));
                user.setPassword(rs.getString("password"));
                user.setAdmin(rs.getInt("isAdmin"));
                System.out.println("登录成功");
                return user;
            }
        }catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("登陆失败");
        }finally {
            DBUtil.close(connection,statement,rs);
        }
        return null;
    }

    //根据id找用户  展示信息的时候用
    public User selectById (int userId) throws OrderSystemException{
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        String sql = "select * from user where userId = ?";
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,userId);
            rs = statement.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("userId"));
                user.setName(rs.getString("name"));
                user.setPassword(rs.getString("password"));
                user.setAdmin(rs.getInt("isAdmin"));
                System.out.println("查找成功");
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("按照id查找用户失败");
        } finally {
            DBUtil.close(connection,statement,rs);
        }
        return null;
    }


}
