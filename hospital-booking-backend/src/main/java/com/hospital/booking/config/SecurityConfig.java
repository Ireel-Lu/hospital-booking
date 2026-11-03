package com.hospital.booking.config;

import com.hospital.booking.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.DigestUtils;

/**
 * Spring Security安全配置类
 * 配置认证、授权、JWT过滤器等安全相关功能
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    /** JWT认证过滤器，用于解析Token并设置用户身份 */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** 用户详情服务，用于从数据库加载用户信息 */
    private final UserDetailsService userDetailsService;

    /**
     * 白名单：无需登录即可访问的接口路径
     */
    private static final String[] WHITE_LIST = {
            //认证相关
            "/auth/login",
            "/auth/register",

            //公共查询
            "/department/list",
            "/doctor/list",
            "/doctor/source-setting/list",

            //患者相关功能
            "/appointment/create",
            "/appointment/pay/*",
            "/appointment/my",
            "/appointment/cancel/*",
            "medical-record/patient/*",
            "/prescription/list/*",
            "ai/recommend",

            //文档和静态资源：Swagger、静态文件、Websocket
            "/doc/**",
            "/swagger-ui/**",
            "v3/api-docs/**",
            "/webjars/**",
            "/swagger-resources/**",
            "/static/**",
            "uploads/**",
            "/ws/**"
    };

    /**
     * 配置安全过滤连
     * 包括：禁用CSRF、启用CORS、无状态Session、路经授权规则、添加JWT过滤器
     * @param http HttpSecurity配置对象
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())                          // 禁用CSRF（使用JWT不需要）
                .cors(cors -> {})                                      // 启用CORS跨域
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 无状态Session（JWT认证）
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()      // 放行所有OPTIONS预检请求
                        .requestMatchers(WHITE_LIST).permitAll()                     // 放行白名单路径
                        .requestMatchers("/admin/**").hasRole("ADMIN")                // 管理员路径需要ADMIN角色
                        .requestMatchers("/doctor/**").hasAnyRole("DOCTOR", "ADMIN") // 医生路径需要DOCTOR或ADMIN角色
                        .requestMatchers("/patient/**").hasAnyRole("PATIENT", "ADMIN")// 患者路径需要PATIENT或ADMIN角色
                        .anyRequest().authenticated()                                 // 其余路径需要认证
                )
                .authenticationProvider(authenticationProvider())                   // 设置认证提供者
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);      // 添加JWT过滤器

        return http.build();
    }


    /**
     * 获取认证管理器Bean
     * 用于处理用户认证请求
     * @param config AuthenticationConfiguration 配置对象
     * @return AuthenticationManager示例
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)throws Exception{
        return config.getAuthenticationManager();
    }

    /**
     * 创建认证提供者Bean
     * 使用DaoAuthenticationProvider 实现基于数据库的认证
     * @return AuthenticationProvider 实例
     */
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    /**
     * 创建加密器Bean
     * 使用MD5算法对密码进行加密
     * @return PassworEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return DigestUtils.md5DigestAsHex(rawPassword.toString().getBytes());
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encode(rawPassword).equals(encodedPassword);
            }
        };
    }



}
