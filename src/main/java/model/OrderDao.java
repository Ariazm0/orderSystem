package model;

import com.mysql.jdbc.ConnectionFeatureNotAvailableException;
import util.OrderSystemException;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    // 新增订单
    // 订单是和两个表关联的.
    // 第一个表 order_user
    // 第二个表 order_dish, 一个订单中可能会涉及点多个菜, 就需要给这个表一次性插入多个记录.
    public void add(Order order) throws OrderSystemException {
        // 1. 先操作 order_user 表
        addOrderUser(order);
        // 2. 再操作 order_dish 表
        //    执行 add 方法的时候, order 对象中的 orderId 字段还是空着的呢~~
        //    这个字段要交给数据库, 由自增主键来决定.
        addOrderDish(order);
    }

    private void addOrderUser(Order order) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "insert into order_user values(null, ?, now(), 0)";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            // 加上 RETURN_GENERATED_KEYS 选项, 插入的同时就会把数据库自动生成的自增主键的值获取到
            statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setInt(1, order.getUserId());
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("插入订单失败");
            }
            // 把自增主键的值给读取出来.
            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                // 理解参数 1. 读取 resultSet 的结果时, 可以使用列名, 也可以使用下标.
                // 由于一个表中的自增列可以有多个. 返回的时候都返回回来了. 下标填成 1
                // 就表示想获取到第一个自增列生成的值.
                order.setOrderId(resultSet.getInt(1));
            }
            System.out.println("插入订单第一步成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("插入订单失败");
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
    }

    // 把菜品信息给插入到表 order_dish 中.
    private void addOrderDish(Order order) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "insert into order_dish values(?, ?)";
        PreparedStatement statement = null;
        try {
            //关闭自动提交
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(sql);
            // 由于一个订单对应到多个菜品, 就需要遍历 Order 中包含的菜品数组, 把每个记录都取出来
            //遍历 dishes 给 SQL 添加多个 values 的值
            List<Dish> dishes = order.getDishes();
            for (Dish dish : dishes)  {
                // OrderId 是在刚刚进行插入 order_user 表的时候, 获取到的自增主键
                statement.setInt(1, order.getOrderId());
                statement.setInt(2, dish.getDishId());
                statement.addBatch(); // 给 sql 新增一个片段.
            }
            // 执行 SQL (并不是真的执行)
            statement.executeBatch(); // 把刚才的 sql 进行执行.
            // 发送给服务器 (真的执行), commit 可以去执行多个 SQL, 一次调用 commit 统一发给服务器.
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            // 如果上面的操作出现异常, 就认为整体的新增订单操作失败, 回滚之前的插入 order_user 表的内容
            deleteOrderUser(order.getOrderId());
        } finally {
            // 关闭数据库连接
            DBUtil.close(connection, statement, null);
        }
    }

    //删除 order_user 表中的记录.
    private void deleteOrderUser(int orderId) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "delete from order_user where orderId = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, orderId);
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("回滚失败");
            }
            System.out.println("回滚成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("回滚失败");
        } finally {
            DBUtil.close(connection, statement, null);
        }
    }

    public List<Order> selectAll() {
        List<Order> orders = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        String sql = "select * from order_user";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                // 此时 order 对象中, 没有 dishes 字段的.
                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp("time"));
                order.setIsDone(resultSet.getInt("isDone"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
        return orders;
    }

    public List<Order> selectByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        String sql = "select * from order_user where userId = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                // 此时 order 对象中, 没有 dishes 字段的.
                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp("time"));
                order.setIsDone(resultSet.getInt("isDone"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
        return orders;
    }

    public Order selectById(int orderId) throws OrderSystemException {
        // 先根据 orderId 得到一个 Order 对象
        Order order = buildOrder(orderId);
        // 根据 orderId 得到该 orderId 对应的菜品 id 列表
        List<Integer> dishIds = selectDishIds(orderId);
        // 根据 菜品 id 列表, 查询 dishes 表, 获取到菜品详情
        order = getDishDetail(order, dishIds);
        return order;
    }

    private Order buildOrder(int orderId) {
        Connection connection = DBUtil.getConnection();
        String sql = "select * from order_user where orderId = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, orderId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp("time"));
                order.setIsDone(resultSet.getInt("isDone"));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
        return null;
    }

    // 查找 order_dish 表
    private List<Integer> selectDishIds(int orderId) {
        List<Integer> dishIds = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        String sql = "select * from order_dish where orderId = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, orderId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                dishIds.add(resultSet.getInt("dishId"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
        return dishIds;
    }

    private Order getDishDetail(Order order, List<Integer> dishIds) throws OrderSystemException {
        // 1. 准备好要返回的结果
        List<Dish> dishes = new ArrayList<>();
        // 2. 遍历 dishIds 在 dishes 表中查.  (前面已经 有现成的方法了, 直接调用.)
        DishDao dishDao = new DishDao();
        for (Integer dishId : dishIds) {
            Dish dish = dishDao.selectById(dishId);
            dishes.add(dish);
        }
        // 3. 把 dishes 设置到 order 对象中
        order.setDishes(dishes);
        return order;
    }

    public void changeState(int orderId, int isDone) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "update order_user set isDone = ? where orderId = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, isDone);
            statement.setInt(2, orderId);
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("修改订单状态失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("修改订单状态失败");
        } finally {
            DBUtil.close(connection, statement, null);
        }
    }
}
