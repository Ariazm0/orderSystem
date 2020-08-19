package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import util.OrderSystemException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LogoutServlet extends HttpServlet {
    private Gson gson = new GsonBuilder().create();
    static class Response {
        public int ok;
        public String reason;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("您尚未登陆");
            }
            session.removeAttribute("user");
            response.ok = 1;
            response.reason = "";
        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
        }finally {
            resp.setContentType("application/json; charset=utf-8");
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }

    }
}
