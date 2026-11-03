package com.hospital.booking.filter;

import com.hospital.booking.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 拦截所有请求，解析请求头中的JWT Token，验证用户身份并设置Security上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    //用于解析Token的解析和验证
    private final JwtUtils jwtUtils;

    //用户详情服务，用于根据用户名加载用户信息
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if("OPTIONS".equalsIgnoreCase(request.getMethod())){
            log.debug("放行 OPTIONS 请求： {}",request.getRequestURI());
            filterChain.doFilter(request,response);
            return;
        }

        //从请求头获取Authorization
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        //提取Bearer Token（格式: Bearer xxx)
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.substring(7);
            try {
                //获取用户名
                username = jwtUtils.getUsernameFromToken(token);
            }catch(Exception e){
                log.warn("Token解析失败：{}",e.getMessage());
            }
        }

        // 如果用户名不为空且当前没有认证信息，则进行认证
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 根据用户名加载用户详情
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 验证Token有效性
            if (jwtUtils.validateToken(token, userDetails)) {
                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // 设置请求详情
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 将认证对象存入Security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 将用户ID和角色设置到请求属性中，供Controller使用
                request.setAttribute("userId", jwtUtils.getUserIdFromToken(token));
                request.setAttribute("role", jwtUtils.getRoleFromToken(token));
            }
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);

    }
}
