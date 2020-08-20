package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Dish;
import model.Order;
import model.OrderDao;
import model.User;
import util.OrderSystemException;
import util.OrderSystemUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/order")
public class OrderServlet extends HttpServlet {
    private Gson gson = new GsonBuilder().create();
    //仔细观察这几个api发现只有新增订单带了body，而且body是一个整数数组
    static class Response {
        public int ok;
        public String reason;
    }
    //新增订单
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("当前尚未登录");
            }
            User user = (User)session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("当前尚未登录");
            }
            if (user.getIsAdmin() == 1) {
                throw new OrderSystemException("您当前是管理员，不能新增订单");
            }
            String body = OrderSystemUtil.readBody(req);
            Integer[] dishIds = gson.fromJson(body,Integer[].class);
            Order order = new Order();
            List<Dish> dishes = new ArrayList<>();
            order.setUserId(user.getUserId());
            for (Integer dishId:dishIds) {
                Dish dish = new Dish();
                dish.setDishId(dishId);
                dishes.add(dish);
            }
            order.setDishes(dishes);
            OrderDao orderDao = new OrderDao();
            orderDao.add(order);
            int ok = 1;
            response.reason = "";

        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
        } finally {
            resp.setContentType("application/json; charset=utf-8");
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }
    }
    //查看所有订单，只有管理员能够查看

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        List<Order> orders = new ArrayList<>();
        try {
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("当前尚未登录");
            }
            User user = (User) session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("当前尚未登录");
            }
            OrderDao orderDao = new OrderDao();
            String orderIdStr = req.getParameter("orderId");
            if (orderIdStr == null) {
                //不存在就查看所有订单
                //如果是普通用户就查看自己的订单，否则查看所有订单
                if (user.getIsAdmin() == 0) {
                    orders = orderDao.selectByUserId(user.getUserId());
                } else {
                    orders = orderDao.selectAll();
                }
                //构造响应结果
                String jsonString = gson.toJson(orders);
                resp.getWriter().write(jsonString);
            } else {
                //有orderId就查看指定订单
                int orderId = Integer.parseInt(orderIdStr);
                Order order = new Order();
                order = orderDao.selectById(orderId);
                if (user.getIsAdmin() == 0 &&
                        user.getUserId() != order.getUserId()) {
                    throw new OrderSystemException("当前您无法查看他人订单");
                }
                String jsonString = gson.toJson(order);
                resp.getWriter().write(jsonString);
            }

        }catch (OrderSystemException e) {
            String jsonString = gson.toJson(orders);
            resp.getWriter().write(jsonString);
        }
    }
    //修改订单状态

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Response response = new Response();
        req.setCharacterEncoding("utf-8");
        try {
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("当前尚未登录");
            }
            User user = (User)session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("当前尚未登录");
            }
            if (user.getIsAdmin() == 0) {
                throw new OrderSystemException("您无权修改订单状态");
            }
            String orderIdStr = req.getParameter("orderId");
            String isDoneStr = req.getParameter("isDone");
            if (orderIdStr == null || isDoneStr == null) {
                throw new OrderSystemException("参数有误");
            }
            int orderId = Integer.parseInt(orderIdStr);
            int isDone = Integer.parseInt(isDoneStr);
            OrderDao orderDao = new OrderDao();
            orderDao.changeState(orderId,isDone);
            response.ok = 1;
            response.reason = "";
        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
        } finally {
            resp.setContentType("application/json; charset=utf-8");
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }
    }
}
