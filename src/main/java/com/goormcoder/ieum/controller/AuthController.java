package com.goormcoder.ieum.controller;

import com.goormcoder.ieum.domain.Member;
import com.goormcoder.ieum.dto.request.LoginDto;
import com.goormcoder.ieum.dto.request.MemberCreateDto;
import com.goormcoder.ieum.dto.response.JwtTokenDto;
import com.goormcoder.ieum.dto.response.MemberFindDto;
import com.goormcoder.ieum.jwt.JwtProvider;
import com.goormcoder.ieum.service.MemberService;
import com.goormcoder.ieum.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Auth", description = "인증 관련 API")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtProvider jwtProvider;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "자체 회원 가입")
    @PostMapping("/join")
    public ResponseEntity<MemberFindDto> create(@RequestBody MemberCreateDto createDto) {
        Member member = memberService.createByLoginId(createDto);
        return ResponseEntity.ok(MemberFindDto.of(member));
    }

    @Operation(summary = "자체 로그인")
    @PostMapping("/login")
    public ResponseEntity<JwtTokenDto> login(@RequestBody LoginDto loginDto) {
        Member member = memberService.findByLoginIdAndPassword(loginDto.loginId(), loginDto.password());
        JwtTokenDto jwtToken = jwtProvider.generateToken(member);
        refreshTokenService.save(jwtToken.refreshToken());
        return ResponseEntity.ok(jwtToken);
    }

    @Operation(summary = "액세스 토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestBody String refreshToken) {
        if (!refreshTokenService.isExists(refreshToken)) {
            return ResponseEntity.ok("만료되었거나 유효하지 않은 리프래시 토큰입니다.");
        }
        UUID memberId = jwtProvider.getMemberIdFromRefreshToken(refreshToken);
        Member member = memberService.findById(memberId);
        String accessToken = jwtProvider.generateAccessToken(member);
        return ResponseEntity.ok(accessToken);
    }

}