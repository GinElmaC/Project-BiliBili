package com.bilibili.entity.enums;

public enum UserStatusEnum {

    DISABLE(0,"禁用"),
    ENABLE(1,"启用");

    private Integer StatusNumber;
    private String StatusString;

    UserStatusEnum(Integer statusNumber,String statusString){
        this.StatusNumber = statusNumber;
        this.StatusString = statusString;
    }

    public Integer getNumber(){
        return StatusNumber;
    }
    public String getString(){
        return StatusString;
    }

    public void setStirng(String string){
        this.StatusString = string;
    }

    public static UserStatusEnum getByStatusNumber(Integer number){
        for(UserStatusEnum item:UserStatusEnum.values()){
            if(item.getNumber() == number){
                return item;
            }
        }
        return null;
    }
}
