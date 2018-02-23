package com.whis.app.model

import com.fasterxml.jackson.annotation.JsonProperty

class Admin {

    @JsonProperty("id") var id: Long = 0
    @JsonProperty("username") var username: String = ""
    @JsonProperty("password") var password: String = ""
    @JsonProperty("name") var name: String = ""
    @JsonProperty("last_login_time") var lastLoginTime: Long = 0
    @JsonProperty("weight") var weight: Int = 0
    @JsonProperty("create_time") var createTime: Long = 0
    @JsonProperty("update_time") var updateTime: Long = 0
}