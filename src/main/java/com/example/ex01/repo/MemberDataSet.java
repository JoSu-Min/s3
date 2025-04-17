package com.example.ex01.repo;

import com.example.ex01.dto.MemberDTO;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
@Repository
public class MemberDataSet {
    private ArrayList<MemberDTO> DB;
    public MemberDataSet(){
        DB = new ArrayList<>();
        DB.add(new MemberDTO("aaa","aaa","ROLE_api","nan"));
        DB.add(new MemberDTO("bbb","bbb","ROLE_api","nan"));
        DB.add(new MemberDTO("ccc","ccc","ROLE_api","nan"));
    }
    public int insert(MemberDTO dto){
        //insert dto;
        for( MemberDTO d : DB){
            if(d.getUsername().equals( dto.getUsername()) ){
                return 0;
            }
        }
        DB.add(dto);
        return 1;
    }
    public ArrayList<MemberDTO> getList(){
        return DB;
    }
    public int update(MemberDTO dto,String id){
        for(int i=0 ; i < DB.size() ; i++){
            if( DB.get(i).getUsername().equals(id) ){
                DB.remove( i );
                DB.add(dto);
                return 1;
            }
        }
        return 0;
    }
    public int mDelete( String id ){
        for(int i=0 ; i< DB.size() ; i++){
            if( DB.get(i).getUsername().equals(id) ){
                DB.remove(i);
                return 1;
            }
        }
        return 0;
    }
    public int login(String username,String password){
        int result = -1 ;
        for(MemberDTO d : DB){
            if( d.getUsername().equals(username) ){
                result = 1;
                if( d.getPassword().equals(password) ){
                    result = 0;
                }
                break;
            }
        }
        return result;
    }
    public MemberDTO getOne( String username ){
        for(MemberDTO dto : DB){
            if(dto.getUsername().equals( username ) )
                return dto;
        }
        return null;
    }
}











