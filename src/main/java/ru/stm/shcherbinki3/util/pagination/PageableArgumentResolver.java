package ru.stm.shcherbinki3.util.pagination;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class PageableArgumentResolver implements HandlerMethodArgumentResolver {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT = "id";
    private static final Pageable.SortDirection DEFAULT_DIRECTION = Pageable.SortDirection.ASC;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Pageable.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        String pageParam = webRequest.getParameter("page");
        String sizeParam = webRequest.getParameter("size");
        String sortParam = webRequest.getParameter("sortBy");

        int page = pageParam != null ? Integer.parseInt(pageParam) : DEFAULT_PAGE;
        int size = sizeParam != null ? Integer.parseInt(sizeParam) : DEFAULT_SIZE;

        String sortBy = DEFAULT_SORT;
        Pageable.SortDirection direction = DEFAULT_DIRECTION;

        if (sortParam != null && !sortParam.isBlank()) {
            String[] parts = sortParam.split(",");
            sortBy = parts[0];
            if (parts.length > 1) {
                direction = Pageable.SortDirection.valueOf(parts[1].toUpperCase());
            }
        }

        return new Pageable(page, size, sortBy, direction);
    }
}

