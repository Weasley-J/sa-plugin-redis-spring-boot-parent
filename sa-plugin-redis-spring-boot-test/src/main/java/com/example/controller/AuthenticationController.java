package com.example.controller;


import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.example.domain.Result;
import com.example.entity.AuthenticationRequest;
import com.example.entity.AuthenticationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录凭据v2
 *
 * @author weasley
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/public/prosper/cloud/auth")
public class AuthenticationController {

    /**
     * 获取登录凭证
     */
    @PostMapping("/login/token")
    public Result<AuthenticationResponse> getAuthToken(@RequestBody @Validated AuthenticationRequest request) {
        StpUtil.login(request.getOpenId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        AuthenticationResponse response = AuthenticationResponse.builder()
                .tokenName(tokenInfo.getTokenName())
                .tokenValue(tokenInfo.getTokenValue())
                .isLogin(tokenInfo.getIsLogin())
                .loginId(tokenInfo.getLoginId())
                .build();
        log.info("获取登录凭证 {} {}", JSONUtil.toJsonStr(request), JSONUtil.toJsonStr(response));
        return Result.ok(response);
    }

    /**
     * 查询登录状态
     */
    @GetMapping("/login/status")
    public Result<Boolean> getLoginStatus() {
        log.info("查询登录状态 {}", StpUtil.getLoginId());
        boolean login = StpUtil.isLogin();
        String msg = login ? "ph.auth.login.success" : "ph.auth.login.failure";
        if (login) return Result.success(msg, true);
        else return Result.fail(msg, false);
    }

    /**
     * 退出登录
     *
     * @apiNote 会清楚会话信息，需要重新登陆
     */
    @PostMapping("/login/out")
    public Result<Object> loginOut() {
        log.info("退出登录 {}", StpUtil.getLoginId());
        StpUtil.logout();
        return Result.ok();
    }
}
