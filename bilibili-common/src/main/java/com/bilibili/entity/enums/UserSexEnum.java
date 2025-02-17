package com.bilibili.entity.enums;

public enum UserSexEnum {

    MAN(1,"男"),
    WOMAN(0,"女"),
    SECRECY(2,"保密");

    public Integer SexNumber;
    public String SexString;

    UserSexEnum(Integer SexNumber,String SexString){
        this.SexNumber = SexNumber;
        this.SexString = SexString;
    }

    public Integer getSexNumber(){
        return SexNumber;
    }

    public String getSexString(){
        return SexString;
    }

    public static UserSexEnum getSexBySexNumber(Integer number){
        for(UserSexEnum item:UserSexEnum.values()){
            if(number == item.getSexNumber()){
                return item;
            }
        }
        return null;
    }
}
