package com.example.ex01.service;

import com.example.ex01.domain.MemberEntity;
import com.example.ex01.dto.MemberDTO;
import com.example.ex01.repo.MemberDataSet;
import com.example.ex01.repo.MemberRepo;
import com.example.ex01.utils.JwtUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Member;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    @Value("${jwt.secretKey}")
    private String secretKey;
    @Autowired
    MemberDataSet ds;
    private final MemberRepo repo;
    private final HttpSession session;

    final String DIR = "uploads/";


    public int insert(MemberDTO dto, MultipartFile file){
        int result = 0;
        //result = ds.insert(dto);
        try {
            String fileName = null;
            if( file.isEmpty() ){
                fileName = "nan";
            }else{
                fileName = UUID.randomUUID().toString() + "-"+
                        file.getOriginalFilename();
            }
            dto.setFileName( fileName );
            repo.save( new MemberEntity( dto ) ) ;
            result = 1;
            Path path = Paths.get(DIR + fileName);
            Files.createDirectories( path.getParent() );
            if( !file.isEmpty() )
                file.transferTo( path );

        } catch (Exception e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
        return result;
    }
    public Map<String, Object> getList(int start ){
        start = start > 0? start-1: start;
        int size = 3; //한페이지 3개 글
        Pageable pageable = PageRequest.of(start, size,
                Sort.by( Sort.Order.desc("id")) );
        Page<MemberEntity> page = repo.findAll( pageable );
        List<MemberEntity> listE = page.getContent();
        Map<String,Object> map = new HashMap<>();
        map.put("list", listE.stream().map(entity
                                -> new MemberDTO(entity)).toList() );
        map.put("totalPages", page.getTotalPages() );
        map.put("currentPage", page.getNumber()+1 );
        return map;
        //return ds.getList();
        /*
        return repo.findAll().stream()
                .map( entity -> new MemberDTO(entity) )
                .toList();
         */
    }
    public int update(MemberDTO dto,String id){
        /*
         수정
         사용자가 파일을 선택했다면, 기존 파일 삭제 후 새로운 파일 저장
         선택하지 않았다면 기존파일 그냥 두면 됨
         */

        if(dto.getUsername() == null || dto.getPassword() == null ||
                        dto.getRole() == null )
            return -1;
        //return ds.update(dto, id);
        MemberEntity entity = repo.findByUsername( dto.getUsername() );
        if( entity != null ){
            //entity.setUsername();
            entity.setPassword( dto.getPassword() );
            entity.setRole( dto.getRole() );
            repo.save( entity );
            return 1;
        }
        return 0;
    }
    public int mDelete(String id, String fileName){
        //return ds.mDelete(id);
        MemberEntity entity = repo.findByUsername( id );
        if(entity != null){
            repo.delete( entity );
            try{
                Path path = Paths.get(DIR+fileName);
                Files.deleteIfExists( path );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return 1;
        }
        return 0;
    }

    public Map<String, Object> login( String username, String password ){
        int result = -1;
        Map<String, Object> map = new HashMap<>();
        MemberEntity entity = repo.findByUsername(username);
        if( entity != null ){
            result = 1;
            if(entity.getPassword().equals(password) ){
                result = 0;
                map.put("token", JwtUtil.createJwt(username,
                                    secretKey, entity.getRole() ));
            }
        }
        map.put("result", result);
        return map;
    }
    public MemberDTO getOne( String username ){
        //MemberDTO dto = null;
        //dto = ds.getOne( username );
        //return dto;
        return new MemberDTO( repo.findByUsername(username) );
    }

    public byte[] getImage(String fileName ){
        Path filePath = Paths.get(DIR+fileName);
        byte[] imageBytes = {0};
        try {
            imageBytes = Files.readAllBytes(filePath);
            log.info("imageByte : {}", imageBytes );
        }catch (Exception e){
            e.printStackTrace();
        }
        return imageBytes;
    }
}







