package com.hospital.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secretStr;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Autowired(required = false)
    //代表 `stringRedisTemplate` 可以为空，如果项目没引入 Redis，不会启动报错，黑名单功能直接失效。
    private StringRedisTemplate stringRedisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist";

    /**
     * 获取加密密钥
    */
    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(secretStr.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色
     * @return token
     */
    public String generateToken(Long userId, String username, String role){
        Date currentdate = new Date();
        Date expireDate = new Date(currentdate.getTime()+expiration);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username",username)
                .claim("role",role)
                .setIssuedAt(currentdate)
                .setExpiration(expireDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    /**
     * 解析token，获取Claims
     * @param token token字符串
     * @return Claims ，解析失败返回null
     */
    public Claims getClaimsFromToken(String token){
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        }catch(Exception e) {
            log.error("解析token失败:{}", e);
            return null;
        }
    }

    /**
     * 获取用户Id
     * @param token token字符串
     * @return 用户ID，解析失败返回null
     */
    public Long getUserIdFromToken(String token){
        Claims claims = getClaimsFromToken(token);
        return claims !=null ? Long.parseLong(claims.getSubject()) : null;
    }
    /**
     * 获取用户名
     * @param token token字符串
     * @return 用户ID，解析失败返回null
     */
    public String getUsernameFromToken(String token){
        Claims claims = getClaimsFromToken(token);
        return claims !=null ? claims.get("username",String.class) : null;
    }
    /**
     * 获取用户角色
     * @param token token字符串
     * @return 用户ID，解析失败返回null
     */
    public String getRoleFromToken(String token){
        Claims claims = getClaimsFromToken(token);
        return claims !=null ? claims.get("role",String.class) : null;
    }

    /**
     * 校检token有效性（包含redis黑名单校检）
     */
    public boolean validateToken(String token){

        try{
            //1.检查token是否在redis黑名单中
            if (stringRedisTemplate != null) {
                String blackKey = TOKEN_BLACKLIST_PREFIX + token;

                //如果redis存在这个key，说明已经退出登录，token作废
                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(blackKey))) {
                    return false;
                }
            }

            //2.解析token拿到载荷Claims（存放用户名、过期时间等数据）
            Claims claims = getClaimsFromToken(token);
            if (claims == null) return false;

            //3.检查token是否过期
            // claims.getExpiration() → token到期时间
            // .before(new Date()) → 如果到期时间 在现在时间之前 = 已经过期
            return !claims.getExpiration().before(new Date());
        }catch(Exception e){
            // 只要出现异常：签名错误、token篡改、格式错误，全部返回false，校验失败
            return false;
        }
    }

    /**
     * SpringSecurity专用重载方法：登录校检token有效性
     */
    public boolean validateToken(String token, UserDetails userDetails){
        if(!validateToken(token))return false;
        String username = getUsernameFromToken(token);
        return username !=null && username.equals(userDetails.getUsername());
    }

    /**
     * 判断token时效性（是否过期）
     */
    private boolean isTokenExpired(String token){
        Claims claims = getClaimsFromToken(token);
        return claims != null && claims.getExpiration().before(new Date());
    }

    /**
     * 将token加入黑名单（退出登录时调用）
     * @param token token字符串
     */
    public void invalidateToken(String token){
        //1. Redis 没装配或 token 为空时直接返回，不影响登出流程
        if(stringRedisTemplate == null || token == null || token.isBlank())return;

        //2. token 已无法解析或已过期，无需再写入黑名单
        Claims claims = getClaimsFromToken(token);
        if(claims == null || claims.getExpiration() == null)return;

        //3. 黑名单有效期 = JWT 剩余有效期，过期后 Redis 自动清理，避免无限增长
        long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
        if(ttl <= 0)return;

        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        stringRedisTemplate.opsForValue().set(blacklistKey,"1",ttl, TimeUnit.MILLISECONDS);

    }

}
