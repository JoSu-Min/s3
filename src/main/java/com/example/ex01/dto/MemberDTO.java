package com.example.ex01.dto;

import com.example.ex01.domain.MemberEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberDTO {
    private String username;
    private String password;
    private String role;
    private String fileName;
    //originFileName
    //changeFileName
    public MemberDTO(MemberEntity entity){
        this.username = entity.getUsername();
        this.password = entity.getPassword();
        this.role = entity.getRole();
        this.fileName = entity.getFileName();
    }
}
