package ru.netology.servlet;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.netology.configuration.JavaConfig;
import ru.netology.controller.PostController;
import ru.netology.exception.NotFoundException;
import ru.netology.repository.PostRepository;
import ru.netology.service.PostService;
import ru.netology.utils.HttpMethod;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MainServlet extends HttpServlet {
    private PostController controller;

    @Override
    public void init() {
        final var context = new AnnotationConfigApplicationContext(JavaConfig.class);
        controller = context.getBean(PostController.class);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        // если деплоились в root context, то достаточно этого
        try {
            final var path = req.getRequestURI();
            final var method = req.getMethod();
            // primitive routing
            if (method.equals(HttpMethod.GET.name()) && path.equals("/api/posts")) {
                controller.all(resp);
                return;
            }
            if (method.equals(HttpMethod.GET.name()) && path.matches("/api/posts/\\d+")) {
                final var id = getId(path);
                try {
                  controller.getById(id, resp);
                } catch (NotFoundException e) {
                  resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            }
            if (method.equals(HttpMethod.POST.name()) && path.equals("/api/posts")) {
                try {
                    controller.save(req.getReader(), resp);
                } catch (NotFoundException e) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            }
            if (method.equals(HttpMethod.DELETE.name()) && path.matches("/api/posts/\\d+")) {
                final var id = getId(path);
                try {
                    controller.removeById(id, resp);
                } catch (NotFoundException e) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                return;
            }
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private Long getId(String path) {
        return Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
    }
}

