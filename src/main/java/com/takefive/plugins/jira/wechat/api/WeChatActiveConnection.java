package com.takefive.plugins.jira.wechat.api;

import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.takefive.plugins.jira.wechat.api.template.Department;
import com.takefive.plugins.jira.wechat.api.template.Member;
import com.takefive.plugins.jira.wechat.api.template.TextMessage;
import com.takefive.plugins.jira.wechat.util.DateUtils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles active WeChat connection requests (eg. sending messages).
 * 
 * @author lafickens
 *
 */
public class WeChatActiveConnection {
  
  private static final Logger logger = LoggerFactory.getLogger(WeChatActiveConnection.class);

  private AsyncHttpClient client;
  private final PluginSettingsFactory pluginSettingsFactory;
  private PluginSettings pluginSettings;
  
  private String corpId;
  private String corpSecret;

  public WeChatActiveConnection(PluginSettingsFactory pluginSettingsFactory) {
    client = new AsyncHttpClient();
    this.pluginSettingsFactory = pluginSettingsFactory;
    pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
    corpId = (String) pluginSettings.get("jira-wechat.corpId");
    corpSecret = (String) pluginSettings.get("jira-wechat.corpSecret");
  }

  private String getAuthenticationURL(String corpId, String corpSecret) {
    return String.format("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s", corpId, corpSecret);
  }
  
  private String getSendMessageURL(String accessToken) {
    return String.format("https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=%s", accessToken);
  }
  
  private String getAddOrUpdateMemberURL(String accessToken) {
    return String.format("https://qyapi.weixin.qq.com/cgi-bin/user/create?access_token=%s", accessToken);
  }
  
  private String getAddOrUpdateDepartmentURL(String accessToken) {
    return String.format("https://qyapi.weixin.qq.com/cgi-bin/department/create?access_token=%s", accessToken);
  }
  
  private JSONObject send(String url, String message) throws ConnectionException {
    Future<Response> f = client.preparePost(url).execute();
    try {
      Response r = f.get();
      JSONObject responseJson = new JSONObject(r.getResponseBody());
      if (responseJson.getInt("errcode") != 0) {
        throw new ConnectionException("WeChat server returns error code: " + responseJson.getInt("errorcode"));
      }
      return responseJson;
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new ConnectionException("Connection failure");
    } catch (JSONException e) {
      e.printStackTrace();
      throw new ConnectionException("Corrupt data");
    } catch (ExecutionException e) {
      e.printStackTrace();
      throw new ConnectionException("Connection failure");
    } catch (IOException e) {
      e.printStackTrace();
      throw new ConnectionException("Connection failure");
    }
  }

  public String getAccessToken() throws ConnectionException {
    logger.debug("Retrieving token");
    
    // Check if token exists already and is within expiration.
    String settingsAccessToken = (String) pluginSettings.get("jira-wechat.accessToken");
    int settingsExpiresIn = (Integer) pluginSettings.get("jira-wechat.tokenExpiresIn");
    if (settingsAccessToken != null && settingsExpiresIn > DateUtils.getUnixTime()) {
      return settingsAccessToken;
    }
    
    // If token doesn't exist or has already expired, request a new one.
    Future<Response> f = client.prepareGet(getAuthenticationURL(corpId, corpSecret)).execute();
    try {
      Response r = f.get();
      JSONObject tokenJson = new JSONObject(r.getResponseBody());
      if (tokenJson.has("errcode")) {
        throw new ConnectionException("(Access token) WeChat server returns error code: " + tokenJson.getInt("errcode"));
      }
      String accessToken = tokenJson.getString("access_token");
      int expiresAt = tokenJson.getInt("expires_in");
      pluginSettings.put("jira-wechat.accessToken", accessToken);
      pluginSettings.put("jira-wechat.tokenExpiresIn", DateUtils.getUnixTime() + expiresAt);
      return accessToken;
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new ConnectionException("Connection failure");
    } catch (JSONException e) {
      e.printStackTrace();
      throw new ConnectionException("Corrupt data");
    } catch (ExecutionException e) {
      e.printStackTrace();
      throw new ConnectionException("Connection failure");
    } catch (IOException e) {
      e.printStackTrace();
      throw new ConnectionException("Connection failure");
    }
  }
  
  public void sendTextMessage(TextMessage message) throws ConnectionException {
    logger.debug("Sending message");
    
    String token = getAccessToken();
    String url = getSendMessageURL(token);
    send(url, message.toJson());
  }
  
  public void addOrUpdateMember(Member member) throws ConnectionException {
    logger.debug("Adding or updating member");
    
    String token = getAccessToken();
    String url = getAddOrUpdateMemberURL(token);
    send(url, member.toJson());
  }
  
  public void addOrUpdateDepartment(Department department) throws ConnectionException {
    logger.debug("Adding or updating department");
    
    String token = getAccessToken();
    String url = getAddOrUpdateDepartmentURL(token);
    send(url, department.toJson());
  }
}
