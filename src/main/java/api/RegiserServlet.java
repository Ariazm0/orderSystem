package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.User;
import model.UserDao;
import util.OrderSystemException;
import util.OrderSystemUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: Ariazm
 * Date: 2020-08-18
 * Time: 14:59
 */
@WebServlet("/register")
public class RegiserServlet extends HttpServlet {
    private Gson gson = new GsonBuilder().create();
    //读取json的请求
    static class Request {
        public String name;
        public String password;
    }
    //构造json的响应
    static class Response {
        public int ok;
        public String reason;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            //1.读取body中数据
            String body = OrderSystemUtil.readBody(req);
            //2.把body解析成request对象
            Request request = gson.fromJson(body,Request.class);
            //3.查看数据库，如果存在提示用户已经被注册
            UserDao userDao = new UserDao();
            User existUser = userDao.login(request.name);
            if (existUser != null) {
                throw new OrderSystemException("当前用户名已经存在");
            }
            //4.把用户插入到数据库
            User user = new User();
            user.setName(request.name);
            user.setPassword(request.password);
            user.setIsdmin(0);
            userDao.register(user);
            response.ok = 1;
            response.reason = "";
        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
        } finally {
            //5.构造响应数据
            String jsonString = gson.toJson(response);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write(jsonString);
        }
    }
}
