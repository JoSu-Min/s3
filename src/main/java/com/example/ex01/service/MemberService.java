package com.example.ex01.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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

    @Value("${s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;

    @Autowired
    MemberDataSet ds;
    private final MemberRepo repo;
    private final HttpSession session;
    private final PasswordEncoder passwordEncoder;

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
            // 비밀번호 암호화
            dto.setPassword(passwordEncoder.encode(dto.getPassword()));
            repo.save( new MemberEntity( dto ) ) ;
            result = 1;
            /*
            Path path = Paths.get(DIR + fileName);
            Files.createDirectories( path.getParent() );
            if( !file.isEmpty() )
                file.transferTo( path );
             */
            if( !file.isEmpty() ){
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType( file.getContentType() );
                metadata.setContentLength( file.getSize() );
                amazonS3.putObject(bucket, dto.getFileName(),
                        file.getInputStream(), metadata );
            }

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
    public int update(MemberDTO dto, String id, MultipartFile file) {
        if (dto.getUsername() == null || dto.getRole() == null) {
            return -1; // 필수 정보가 없을 경우
        }

        MemberEntity entity = repo.findByUsername(id);
        if (entity != null) {
            // 비밀번호가 변경된 경우에만 암호화
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                entity.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            entity.setUsername(dto.getUsername());
            entity.setRole(dto.getRole());

            // 파일이 제공된 경우
            if (file != null && !file.isEmpty()) {
                // 기존 파일 삭제
                if (entity.getFileName() != null && !entity.getFileName().isEmpty()) {
                    amazonS3.deleteObject(bucket, entity.getFileName());
                }

                // 새로운 파일 저장
                String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
                entity.setFileName(fileName); // 파일 이름 업데이트
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());

                try {
                    amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
                } catch (IOException e) {
                    e.printStackTrace();
                    return -2; // 파일 업로드 중 오류 발생
                }
            }

            repo.save(entity); // 업데이트된 엔티티 저장
            return 1; // 성공적으로 업데이트됨
        }
        return 0; // 사용자를 찾을 수 없음
    }
/*    public int mDelete(String id, String fileName){
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
    } */

   /* S3 BUCKET */
    public int mDelete(String id, String fileName){
        //return ds.mDelete(id);
        MemberEntity entity = repo.findByUsername( id );
        if(entity != null){
            repo.delete( entity );
            try{
                amazonS3.deleteObject(bucket, fileName);
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
            // 암호화된 비밀번호 비교
            if(passwordEncoder.matches(password, entity.getPassword())){
                result = 0;
                map.put("token", JwtUtil.createJwt(username,
                                    secretKey, entity.getRole() ));
            }
        }
        map.put("result", result);
        return map;
    }
    public MemberDTO getOne( String username ){
        MemberEntity entity = repo.findByUsername(username);
        if(entity == null) {
            return new MemberDTO(); // 빈 DTO 객체 반환
        }
        return new MemberDTO(entity);
    }

/*    public byte[] getImage(String fileName ){
        Path filePath = Paths.get(DIR+fileName);
        byte[] imageBytes = {0};
        try {
            imageBytes = Files.readAllBytes(filePath);
            log.info("imageByte : {}", imageBytes );
        }catch (Exception e){
            e.printStackTrace();
        }
        return imageBytes;
    } */

    /* S3 BUCKET */
    public byte[] getImage(String fileName ){
        byte[] imageBytes = {0};
        try {
            // S3에서 파일 다운로드
            S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucket, fileName));
            imageBytes = IOUtils.toByteArray(s3Object.getObjectContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageBytes;
    }

}







