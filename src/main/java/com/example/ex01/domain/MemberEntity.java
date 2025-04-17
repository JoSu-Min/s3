package com.example.ex01.domain;

import com.example.ex01.dto.MemberDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="member_test")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column( nullable = false)
    private String password;
    @Column(nullable = false)
    private String role;
    @Column(nullable = false)
    private String fileName;

    public MemberEntity(MemberDTO dto){
        this.username = dto.getUsername();
        this.password = dto.getPassword();
        this.role = dto.getRole();
        this.fileName = dto.getFileName();
    }

    @PrePersist
    public void perPersist(){
        if( this.fileName == null || this.fileName.equals("")){
            fileName = "nan";
        }
    }
}




