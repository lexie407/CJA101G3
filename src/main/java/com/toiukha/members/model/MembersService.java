package com.toiukha.members.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.toiukha.email.EmailService;
import com.toiukha.members.model.MembersRepository;
import com.toiukha.members.model.MembersVO;

import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MembersService {

    @Autowired
    private MembersRepository membersRepository;

    @Autowired
    private EmailService emailService;

    /**
     * 處理會員註冊邏輯：檢查帳號重複、存入資料庫、發送驗證信。
     */
    @Transactional
    public MembersVO registerMember(MembersVO membersVO) {
        // 補上後端設定欄位（使用者不能填寫的）
        membersVO.setMemStatus((byte) 0); // 預設為未啟用
        membersVO.setMemRegTime(new Timestamp(System.currentTimeMillis()));
        membersVO.setMemPoint(0);
        membersVO.setMemLogErrCount((byte) 0);
        membersVO.setMemGroupAuth((byte) 0);
        membersVO.setMemGroupPoint((byte) 0);
        membersVO.setMemStoreAuth((byte) 0);
        membersVO.setMemStorePoint((byte) 0);

        // 儲存會員資料
        MembersVO savedMember = membersRepository.save(membersVO);

        // 發送驗證信
        emailService.sendVerificationEmail(savedMember.getMemEmail(), savedMember.getMemId());

        return savedMember;
    }

    /**
     * 驗證 token 並啟用會員帳號
     */
    @Transactional
    public boolean verifyAndActivateMember(String token) {
        Integer memId = emailService.verifyToken(token);
        if (memId == null) return false;

        Optional<MembersVO> opt = membersRepository.findById(memId);
        if (opt.isPresent()) {
            MembersVO member = opt.get();
            member.setMemStatus((byte)1); // 設為啟用
            membersRepository.save(member);
            return true;
        }
        return false;
    }
    
    
    
    
    
    public boolean existsByMemAcc(String memAcc) {
        return membersRepository.existsByMemAcc(memAcc);
    }

    public boolean existsByMemEmail(String memEmail) {
        return membersRepository.existsByMemEmail(memEmail);
    }
    
    public MembersVO getByEmail(String email) {
        return membersRepository.findByMemEmail(email).orElse(null);
    }
    
    
    
    
    
    public MembersVO getOneMember(Integer memId) {
        Optional<MembersVO> optional = membersRepository.findById(memId);
        return optional.orElse(null); // 找不到就回傳 null
    }
} 
