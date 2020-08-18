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
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: Ariazm
 * Date: 2020-08-18
 * Time: 15:42
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
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
        public String name;
        public int isAdmin;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            //1.读取body数据
            String body = OrderSystemUtil.readBody(req);
            //2.将请求的body解析成request对象
            Request request = gson.fromJson(body,Request.class);
            //3.按照用户名来查找，并进行密码验证
            UserDao userDao = new UserDao();
            User user = userDao.login(request.name);
            if (user == null || !user.getPassword().equals(request.password)) {
                throw new OrderSystemException("用户名已经存在或者密码错误");
            }
            //5.如果登录成功就创建session对象[重要]
            HttpSession session = req.getSession(true);
            session.setAttribute("user",user);
            response.ok = 1;
            response.reason = "";
            response.name = user.getName();
            response.isAdmin = user.getIsAdmin();

        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
        } finally {
            resp.setContentType("application/json; charset=utf-8");
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }

    }
    //对应到检测的API状态

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            //1.获取当前用户的session，如果为获取到，说明当前没有用户登录
            HttpSession session = req.getSession();
            if (session == null) {
                throw new OrderSystemException("当前未登录");
            }
            User user = (User)session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("当前未登录");
            }
            response.ok = 1;
            response.reason = "";
            response.isAdmin = user.getIsAdmin();
            response.name = user.getName();
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
