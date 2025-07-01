package com.toiukha.groupactivity.model;

/*
 * 聊天訊息DTO
 * 用於前後端之間傳遞聊天訊息
 */
public class GroupChatMsgDTO {
    
    private String userName;    // 發送者姓名
    private String message;     // 訊息內容
    private long timestamp;     // 時間戳
    private String type;        // 訊息類型：chat, system, join, leave
    private Integer actId;      // 活動ID（團隊ID）
    private Integer memberId;   // 會員ID

    // 預設建構子
    public GroupChatMsgDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    // 建構子 - 一般聊天訊息
    public GroupChatMsgDTO(String userName, String message, Integer actId, Integer memberId) {
        this();
        this.userName = userName;
        this.message = message;
        this.actId = actId;
        this.memberId = memberId;
        this.type = "chat";
    }

    // 建構子 - 系統訊息
    public static GroupChatMsgDTO createSystemMessage(String message, Integer actId) {
        GroupChatMsgDTO systemMessage = new GroupChatMsgDTO();
        systemMessage.userName = "系統";
        systemMessage.message = message;
        systemMessage.actId = actId;
        systemMessage.type = "system";
        return systemMessage;
    }

    // 建構子 - 加入/離開訊息
    public static GroupChatMsgDTO createJoinLeaveMessage(String userName, boolean isJoin, Integer actId) {
        GroupChatMsgDTO message = new GroupChatMsgDTO();
        message.userName = userName;
        message.message = userName + (isJoin ? " 加入了聊天室" : " 離開了聊天室");
        message.actId = actId;
        message.type = isJoin ? "join" : "leave";
        return message;
    }

    // Getter 和 Setter 方法
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getActId() {
        return actId;
    }

    public void setActId(Integer actId) {
        this.actId = actId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "userName='" + userName + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", actId=" + actId +
                ", memberId=" + memberId +
                '}';
    }
} 