package model;

import util.OrderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    //添加订单
    //订单是两个表关联的，分别是order_user和order_dish这两个表
    //一个订单中可能有多个菜，所以要给这个表一次性插入多个记录
    public void add(Order order) throws OrderSystemException {
        //1.先操作order_user表
        addOrderUser(order);
        //2.再操作order_dish表
        //    执行 add 方法的时候, order 对象中的 orderId 字段还是空着的呢~~
        //    这个字段要交给数据库, 由自增主键来决定.
        addOrderDish(order);
    }
    private void addOrderUser(Order order) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "insert into order_user values(null,?,now(),0)";
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setInt(1,order.getUserId());
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("插入订单失败");
            }
            //把自增主键的值读出来
            rs = statement.getGeneratedKeys();
            if (rs.next()) {
                // 理解参数 1. 读取 rs 的结果时, 可以使用列名, 也可以使用下标.
                // 由于一个表中的自增列可以有多个. 返回的时候都返回回来了. 下标填成 1
                // 就表示想获取到第一个自增列生成的值.
                order.setOrderId(rs.getInt(1));

            }
            System.out.println("插入订单第一步成功");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("插入订单失败");
        } finally {
            DBUtil.close(connection,statement,rs);
        }
    }
    public void addOrderDish(Order order) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        String sql = "insert into order_dish values(?,?)";
        try {
            //关闭自动提交
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(sql);
            //由于一个订单包含多个菜品，就需要遍历 Order 中包含菜品的数组，把每个记录都读取出来
            //遍历 dishes 给SQL添加多个 values的值
            List<Dish> dishes = order.getDishes();
            for (Dish dish: dishes) {
                //orderId 是刚刚在进行插入 order_user 表的时候，获取到的自增主键
                statement.setInt(1,order.getOrderId());
                statement.setInt(2,dish.getDishId());
                //给sql增加一个片段
                statement.addBatch();
            }
            //执行sql语句(并不是真的执行)
            statement.executeBatch();//把刚才的sql语句进行执行
            //发送给服务器(真的执行)，commit可以执行多个sql命令，一次调用commit就可以统一发送给服务器
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            //如果插入出现异常，就认为整体的插入失败，回滚之前的插入order_user 表中的内容
            deleteOrderUser(order.getOrderId());
        } finally {
            DBUtil.close(connection,statement,null);
        }
    }
    public void deleteOrderUser(int orderId) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        String sql = "delete from order_user where orderId = ?";
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,orderId);
            int ret  = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("回滚失败");
            }
            System.out.println("删除成功");

        } catch (SQLException e) {
            e.printStackTrace();
            throw  new OrderSystemException("回滚失败");
        } finally {
            DBUtil.close(connection,statement,null);
        }
    }

    public List<Order> selectAll() {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        String sql = "select * from order_user";
        List<Order> orders = new ArrayList<>();
        try {
            statement = connection.prepareStatement(sql);
            rs = statement.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("orderId"));
                order.setUserId(rs.getInt("userId"));
                order.setTime(rs.getTimestamp("time"));
                order.setIsDone(rs.getInt("isDone"));
                orders.add(order);
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.close(connection,statement,rs);
        }
        return orders;
    }

    public List<Order> selectByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        String sql = "select * from order_user where userId = ?";
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,userId);
            rs = statement.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("orderId"));

                order.setUserId(rs.getInt("userId"));
                order.setTime(rs.getTimestamp("time"));
                order.setIsDone(rs.getInt("isDone"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection,statement,rs);
        }
        return orders;
    }

    public Order selectById(int orderId) throws OrderSystemException {
        Order order= buildOrder(orderId);
        List<Integer> dishIds = selectDishIds(orderId);
        order = getDishDetail(order,dishIds);
        return order;
    }

    public Order buildOrder(int orderId) {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String sql = "select * from order_user where orderId = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1,orderId);
            rs  = statement.executeQuery();
            if (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("orderId"));
                order.setIsDone(rs.getInt("isDone"));
                order.setUserId(rs.getInt("userId"));
                order.setTime(rs.getTimestamp("time"));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection,statement,rs);
        }
        return null;
    }

    public List<Integer> selectDishIds(int orderId) {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<Integer> dishIds = new ArrayList<>();
        String sql = "select * from order_dish where orderId = ?";
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,orderId);
            rs = statement.executeQuery();
            while (rs.next()) {
                dishIds.add(rs.getInt("dishId"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection,statement,rs);
        }
        return dishIds;
    }

    public Order getDishDetail (Order order,List<Integer> dishIds) throws OrderSystemException {
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

    public void changeState (int orderId,int isDone) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        String sql = "updata order_user set idDone = ? where orderId = ?";
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,isDone);
            statement.setInt(2,orderId);
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("修改订单状态失败");
            }
            System.out.println("修改订单状态成功");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("修改订单状态失败");
        } finally {
            DBUtil.close(connection,statement,null);
        }

    }

}
