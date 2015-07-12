package com.takefive.plugins.jira.wechat.api.template;

import com.google.gson.Gson;

public class Member {
  private String userid;
  private String name;
  private int[] department;
  private String position;
  private String mobile;
  private int gender;
  private int enable;
  private String email;
  private String weixinid;
  private String avatar_mediaid;
  
  public Member() {}
  
  public String getUserid() {
    return userid;
  }
  
  public void setUserid(String userid) {
    this.userid = userid;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public int[] getDepartment() {
    return department;
  }
  
  public void setDepartment(int[] department) {
    this.department = department;
  }
  
  public String getPosition() {
    return position;
  }
  
  public void setPosition(String position) {
    this.position = position;
  }
  
  public String getMobile() {
    return mobile;
  }
  
  public void setMobile(String mobile) {
    this.mobile = mobile;
  }
  
  public int getGender() {
    return gender;
  }
  
  public void setGender(int gender) {
    this.gender = gender;
  }
  
  public int getEnable() {
    return enable;
  }
  
  public void setEnable(int enable) {
    this.enable = enable;
  }
  
  public String getEmail() {
    return email;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }
  
  public String getWeChatId() {
    return weixinid;
  }
  
  public void setWeChatId(String weixinid) {
    this.weixinid = weixinid;
  }
  
  public String getAvatarMediaId() {
    return avatar_mediaid;
  }
  
  public void setAvatarMediaId(String avatar_mediaid) {
    this.avatar_mediaid = avatar_mediaid;
  }
  
  
  public String toJson() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }
}
