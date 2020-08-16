package model;

import org.junit.Test;
import util.OrderSystemException;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: Ariazm
 * Date: 2020-08-16
 * Time: 17:38
 */
public class dishDaoTest {

    @Test
    public void add() throws OrderSystemException {
        Dish dish = new Dish();
        dish.setDishId(1);
        dish.setName("西红柿炒鸡蛋");
        dish.setPrice(24);
        Dish dish1 = new Dish();
        dish.setDishId(2);
        dish.setName("水煮肉片");
        dish.setPrice(24);

        DishDao DishDao = new DishDao();
        DishDao.add(dish);
        DishDao.add(dish1);
    }

    @Test
    public void delete() throws OrderSystemException {
        DishDao DishDao = new DishDao();
        DishDao.delete(1);
    }

    @Test
    public void selectAll() throws OrderSystemException {
        DishDao DishDao = new DishDao();
        DishDao.selectAll();
    }

    @Test
    public void selectById() throws OrderSystemException {
        DishDao DishDao = new DishDao();
        DishDao.selectById(2);
    }
}