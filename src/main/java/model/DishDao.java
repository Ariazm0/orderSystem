package model;

import util.OrderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DishDao {
    //往菜单里面新加菜品
    public void add(Dish dish) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        String sql = "insert into dishes values(null,?,?)";
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, dish.getName());
            statement.setInt(2,dish.getPrice());

            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("新加菜品失败");
            }
            System.out.println("新加菜品成功");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("新加菜品失败");
        } finally {
            DBUtil.close(connection,statement,null);
        }
    }
    //删除菜品
    public void delete(int dishId) throws OrderSystemException{
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        String sql = "delete from dishes where dishId = ?";
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, dishId);
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("删除菜品失败");
            }
            System.out.println("删除菜品成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("删除菜品失败");
        } finally {
            DBUtil.close(connection,statement,null);
        }
    }
    //查询所有菜品
    public List<Dish> selectAll() throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        List<Dish> dishes = new ArrayList<>();
        ResultSet rs = null;
        String sql = "select * from dishes";
        try {
            statement = connection.prepareStatement(sql);
            rs = statement.executeQuery();
            while (rs.next()) {
                Dish dish = new Dish();
                dish.setDishId(rs.getInt("dishId"));
                dish.setName(rs.getString("name"));
                dish.setPrice(rs.getInt("price"));
                dishes.add(dish);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("查看所有菜品失败");
        } finally {
            DBUtil.close(connection,statement,rs);
        }
        return dishes;
    }
    public Dish selectById(int dishId) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        String sql = "select * from dishes where dishId = ?";
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,dishId);
            rs = statement.executeQuery();
            if (rs.next()) {
                Dish dish = new Dish();
                dish.setDishId(rs.getInt("dishId"));
                dish.setName(rs.getString("name"));
                dish.setPrice(rs.getInt("price"));
                return dish;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("根据id查找菜品失败");
        } finally {
            DBUtil.close(connection,statement,rs);
        }
        return null;
    }

}
