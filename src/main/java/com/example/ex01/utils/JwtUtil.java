package com.example.ex01.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

public class JwtUtil {
    public static String getUsername(String token, String secretKey){
        return getClaims(token, secretKey).get("username", String.class);
    }
    public static String getRole(String token, String secretKey){
        return getClaims(token, secretKey).get("role", String.class);
    }
    public static Claims getClaims(String token, String secretKey){
        return Jwts.parser().setSigningKey(secretKey)
                            .parseClaimsJws(token).getBody();
    }
    public static boolean isExpried(String token,String secretKey ){
        // 토큰 만료 : true, 유효 토큰 false
        return getClaims(token, secretKey).getExpiration().before( new Date() );
    }

    public static  String createJwt(String username, String secretKey, String role){
        Long expiredMs = 1000 * 60l * 30;
        Claims claims = Jwts.claims();
        claims.put("username",username);
        claims.put("role",role);
        return Jwts.builder()
                .setClaims( claims ) //토큰에 키:벨류 저장
                .setIssuedAt( new Date(System.currentTimeMillis()) ) //토큰 생성날짜
                .setExpiration( new Date(System.currentTimeMillis()+expiredMs) )//만료날짜
                .signWith( SignatureAlgorithm.HS256 , secretKey )//사용자한테 전달되는 hash값
                .compact();//행당 토큰을 문자열로 포장
    }

}






