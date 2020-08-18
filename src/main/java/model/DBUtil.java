package model;



import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: Ariazm
 * Date: 2020-08-10
 * Time: 15:37
 */
public class DBUtil {
    public static final String URL = "jdbc:mysql://127.0.0.1:3306/orderSystem?characterEncoding=utf-8&useSSL=true";
    public static final String NAME = "root";
    public static final String PASSWORD = "";

    public static DataSource dataSource = null;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DBUtil.class) {
                if (dataSource == null) {
                    dataSource = new MysqlDataSource();
                    ((MysqlDataSource)dataSource).setUrl(URL);
                    ((MysqlDataSource)dataSource).setUser(NAME);
                    ((MysqlDataSource)dataSource).setPassword(PASSWORD);
                }
            }
        }
        return dataSource;
    }
    public static Connection getConnection() {
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库连接失败");
        }
        return null;
    }

    public static void close (Connection connection, PreparedStatement statement, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if(statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库关闭失败");
        }
    }

}
