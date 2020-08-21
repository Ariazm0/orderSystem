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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private Gson gson = new GsonBuilder().create();

    // 读取的 JSON 请求对象
    static class Request {
        public String name;
        public String password;
    }

    // 构造的 JSON 响应对象
    static class Response {
        public int ok;
        public String reason;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            // 1. 读取 body 中的数据.
            String body = OrderSystemUtil.readBody(req);
            // 2. 把 body 数据解析成 Request 对象(GSON)
            Request request = gson.fromJson(body, Request.class);
            // 3. 查询数据库, 看看当前的用户名是否存在. (如果存在, 就提示已经被注册了)
            UserDao userDao = new UserDao();
            User existUser = userDao.selectByName(request.name);
            if (existUser != null) {
                // 当前用户名重复了. 就直接返回一个表示注册失败的信息.
                throw new OrderSystemException("当前用户名已经存在");
            }
            // 4. 把提交的用户名密码构造成 User 对象, 插入数据库
            User user = new User();
            user.setName(request.name);
            user.setPassword(request.password);
            user.setIsAdmin(0);
            userDao.add(user);
            response.ok = 1;
            response.reason = "";
        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
        } finally {
            // 5. 构造响应数据.
            String jsonString = gson.toJson(response);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write(jsonString);
        }
    }
}
