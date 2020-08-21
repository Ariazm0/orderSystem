package model;

import org.junit.Test;
import util.OrderSystemException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class OrderDaoTest {

    @Test
    public void add() throws OrderSystemException {
        Order order = new Order();
        List<Dish> dishes = new ArrayList<>();
        Dish dish = new Dish();
        dish.setDishId(2);
        dish.setPrice(2000);
        dish.setName("西红柿炒鸡蛋");
        Dish dish1 = new Dish();
        dish1.setDishId(3);
        dish1.setPrice(3400);
        dish1.setName("水煮肉片");
        dishes.add(dish);
        dishes.add(dish1);
        order.setDishes(dishes);
        order.setUserId(1);
        order.setIsDone(0);
        order.setOrderId(42);
        OrderDao orderDao = new OrderDao();
        orderDao.add(order);
    }

    @Test
    public void addOrderDish() {

    }

    @Test
    public void deleteOrderUser() {
    }

    @Test
    public void selectAll() {
    }

    @Test
    public void selectByUserId() {
    }

    @Test
    public void selectById() {
    }

    @Test
    public void buildOrder() {
    }

    @Test
    public void selectDishIds() {
    }

    @Test
    public void getDishDetail() {
    }

    @Test
    public void changeState() {
    }
}