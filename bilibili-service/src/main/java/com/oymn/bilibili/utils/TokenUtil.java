package com.oymn.bilibili.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.oymn.bilibili.exception.ConditionException;

import java.util.Calendar;
import java.util.Date;

public class TokenUtil {
    
    private final static String ISSUER = "签发者";

    public static String generateToken(Long userId) throws Exception {
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, 30);
        
        return JWT.create()
                .withKeyId(String.valueOf(userId))   //存放用户id
                .withIssuer(ISSUER)     //设置签发者
                .withExpiresAt(calendar.getTime())   //设置过期时间
                .sign(algorithm);

    }
    
    public static Long verifyToken(String token){
        
        try {
            Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());

            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            String userId = jwt.getKeyId();
            return Long.valueOf(userId);
            
        } catch (TokenExpiredException e) {
            throw new ConditionException("555","token过期!");
        }catch (Exception e){
            throw new ConditionException("非法用户token!");
        }
        
    }
}
