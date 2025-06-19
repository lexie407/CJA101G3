package com.toiukha.members.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.toiukha.email.EmailService;

import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class MembersService {

    @Autowired
    private MembersRepository membersRepository;

    @Autowired
    private EmailService emailService;

  
    // 1. 註冊與驗證流程
    

    
//      註冊會員：補全欄位、存入資料庫、寄出驗證信
     
    @Transactional
    public MembersVO registerMember(MembersVO membersVO) {
        // 設定預設值（防止前端或惡意請求竄改）
        membersVO.setMemStatus((byte) 0); // 未啟用
        membersVO.setMemRegTime(new Timestamp(System.currentTimeMillis()));
        membersVO.setMemPoint(0);
        membersVO.setMemLogErrCount((byte) 0);
        membersVO.setMemGroupAuth((byte) 0);
        membersVO.setMemGroupPoint((byte)0);
        membersVO.setMemStoreAuth((byte) 0);
        membersVO.setMemStorePoint((byte)0);

        // 儲存會員資料
        MembersVO savedMember = membersRepository.save(membersVO);

        // 發送驗證信
        emailService.sendVerificationEmail(savedMember.getMemEmail(), savedMember.getMemId());

        return savedMember;
    }

    
//      驗證 token 並啟用會員帳號
     
    @Transactional
    public boolean verifyAndActivateMember(String token) {
        Integer memId = emailService.verifyToken(token);
        if (memId == null) return false;

        Optional<MembersVO> opt = membersRepository.findById(memId);
        if (opt.isPresent()) {
            MembersVO member = opt.get();
            member.setMemStatus((byte) 1); // 設為啟用
            membersRepository.save(member);
            return true;
        }
        return false;
    }

  
    // 2. 忘記密碼流程
    

    
//    處理忘記密碼流程：找會員、寄出重設連結（不告知是否存在）
    
    public void processForgotPassword(String email) {
        MembersVO member = membersRepository.findByMemEmail(email).orElse(null);

        // 一律假裝成功，避免帳號被探測
        if (member != null) {
            emailService.sendResetPasswordEmail(email, member.getMemId());
        }

        // 不管有沒有帳號都導向「已寄信」頁面
    }

    // 3. 登入流程

//      驗證帳號密碼（平常登入用）
    public MembersVO validateLogin(String account, String password) {
        Optional<MembersVO> optional = membersRepository.findByMemAcc(account);

        if (optional.isPresent()) {
            MembersVO member = optional.get();

            // 實務上建議這裡用加密比對
            if (member.getMemPwd().equals(password)) {
                return member;
            }
        }
        return null;
    }

    // 4. 查詢與修改

//      更新會員資料（edit 頁用）
    public void updateMembers(MembersVO membersVO) {
        membersRepository.save(membersVO); // save() 會自動進行 update
    }

//      根據 memId 取得會員資料（edit/init 頁用）
    public MembersVO getOneMember(Integer memId) {
        return membersRepository.findById(memId).orElse(null);
    }

//     根據帳號取得會員
    public MembersVO getByMemAcc(String memAcc) {
        return membersRepository.findByMemAcc(memAcc).orElse(null);
    }

//    根據 email 取得會員
    public MembersVO getByEmail(String email) {
        return membersRepository.findByMemEmail(email).orElse(null);
    }

//     5. 重複驗證（for 註冊時判斷是否重複）

    public boolean existsByMemAcc(String memAcc) {
        return membersRepository.existsByMemAcc(memAcc);
    }

    public boolean existsByMemEmail(String memEmail) {
        return membersRepository.existsByMemEmail(memEmail);
    }
    
    
    // 取得所有會員（下拉清單／列出所有會員用）
    
   public List<MembersVO> findAllMembers() {
       return membersRepository.findAll();
   }
    
}
