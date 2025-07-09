package com.toiukha.members.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.toiukha.email.EmailService;

import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class MembersService {

	@Autowired
	private MembersRepository membersRepository;
	
	@Autowired
    private StringRedisTemplate redisTemplate;

	@Autowired
	private EmailService emailService;

	// ─── 新增：鎖定機制常數 ───
	public static final int MAX_ERR = 3;
	private static final long LOCK_DURATION_MS = 15 * 60 * 1000; // 15 分鐘

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
		membersVO.setMemGroupPoint((byte) 0);
		membersVO.setMemStoreAuth((byte) 0);
		membersVO.setMemStorePoint((byte) 0);

		// 儲存會員資料
		MembersVO savedMember = membersRepository.save(membersVO);
		
		 // 產生唯一驗證碼
		String token = UUID.randomUUID().toString();
		// 存進 Redis，30 分鐘有效
	    redisTemplate.opsForValue()
	        .set("verify:" + token, 
	             savedMember.getMemId().toString(), 
	             30, 
	             TimeUnit.MINUTES);
	    
	 //  在原請求過程中取得 HttpServletRequest，動態組 baseUrl
	    ServletRequestAttributes attrs =
	        (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
	    HttpServletRequest req = attrs.getRequest();
	    String baseUrl = req.getScheme()      // http or https
	                   + "://" + req.getServerName()
	                   + ":"   + req.getServerPort()
	                   + req.getContextPath();
	    String verificationLink = baseUrl + "/members/verifyEmail?token=" + token;

		// 發送驗證信
	    new Thread(() -> {
	        emailService.sendVerificationEmail(
	            savedMember.getMemEmail(), 
	            verificationLink
	        );
	    }).start();

		return savedMember;
	}

//      驗證 token 並啟用會員帳號

	@Transactional
	public boolean verifyAndActivateMember(String token) {
		Integer memId = emailService.verifyToken(token);
		if (memId == null)
			return false;

		Optional<MembersVO> opt = membersRepository.findById(memId);
		if (opt.isPresent()) {
			MembersVO member = opt.get();
			// 只在尚未啟用時才更新，避免重複啟用
			       if (member.getMemStatus() != 0) {
			           return false;
			       }
			member.setMemStatus((byte) 1); // 設為啟用
			membersRepository.save(member);
			return true;
		}
		return false;
	}

	// 2. 忘記密碼流程

//    處理忘記密碼流程：找會員、寄出重設連結（不告知是否存在）

	 public void processForgotPassword(String email) {
		 Optional<MembersVO> opt = membersRepository.findByMemEmail(email);
		    if (opt.isPresent()) {
		        MembersVO member = opt.get();
		        
		     // 1. 產生 token 並存 Redis（15 分鐘）
		        String token = UUID.randomUUID().toString();
		        redisTemplate.opsForValue()
		            .set("reset:" + token,
		                 member.getMemId().toString(),
		                 15, TimeUnit.MINUTES);

		        // 2. 在原請求線程中動態抓 baseUrl
		        ServletRequestAttributes attrs =
		            (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		        HttpServletRequest req = attrs.getRequest();
		        String baseUrl = req.getScheme()
		                       + "://" + req.getServerName()
		                       + ":"   + req.getServerPort()
		                       + req.getContextPath();
		        String resetLink = baseUrl + "/members/resetPassword?token=" + token;

		        // 3. 非同步執行寄信，只傳 email + resetLink
		        new Thread(() -> {
		            emailService.sendResetPasswordEmail(email, resetLink);
		        }).start();
		    }
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

	// ───────── 新增：檢查是否鎖定 ─────────
	public boolean isLocked(MembersVO member) {
		 Timestamp lastErr = member.getMemLogErrTime();    // ← 在這拿一次變數
		    if (member.getMemLogErrCount() < MAX_ERR || lastErr == null) {
		        return false;
		    }
		    return System.currentTimeMillis() - lastErr.getTime() < LOCK_DURATION_MS;
		}

	// ───────── 新增：記錄錯誤並更新時間 ─────────
	@Transactional
	public void recordLoginError(MembersVO member) {
		// 執行自訂的sql
	    membersRepository.incrementLoginError(
	        member.getMemId(),
	        new Timestamp(System.currentTimeMillis())
	    );
	}

	// ───────── 新增：成功登入後重置錯誤 ─────────
	@Transactional
	public void resetLoginError(MembersVO member) {
		membersRepository.resetLoginErrorNative(member.getMemId());
	}

	// ─── 新增：含鎖定檢查的登入驗證 ───
	public MembersVO validateLoginWithLock(String account, String password) {
		Optional<MembersVO> opt = membersRepository.findByMemAcc(account);
		if (opt.isEmpty()) {
			return null; // 帳號不存在
		}
		MembersVO member = opt.get();

		// 1. 鎖定檢查
		if (isLocked(member)) {
			return member; // 鎖定中，Controller 會顯示「鎖定訊息」
		}

		// 2. 密碼驗證
		if (member.getMemPwd().equals(password)) {
			resetLoginError(member);
			return member; // 驗證成功
		} else {
			recordLoginError(member);
			return null; // 密碼錯誤
		}
	}

	public int minutesLeftToUnlock(MembersVO member) {
		// 新增：若從未有錯誤時間，直接回 0（尚未鎖定）
		if (member.getMemLogErrTime() == null) {
		    return 0;
		}
		// 從「最後一次錯誤時間」到現在已經過了多少毫秒
		long timeSinceLastErrorMs = System.currentTimeMillis() - member.getMemLogErrTime().getTime();
		// 鎖定總毫秒數扣掉已過去的，剩下多少毫秒還在鎖定中
		long timeUntilUnlockMs = LOCK_DURATION_MS - timeSinceLastErrorMs;
		// 換算成分鐘
		int minutesLeft = (int) (timeUntilUnlockMs / (60 * 1000));
		// 不會回負數
		return Math.max(minutesLeft, 0);
	}

	// 4. 查詢與修改

//      更新會員資料
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

	
	public MembersVO getById(Integer memId) {
        return membersRepository.findById(memId).orElse(null);
}
	
	
	 
	public Page<MembersVO> searchByCriteria(
            Byte memStatus,
            String memAcc,
            Integer memId,
            String memName,
            Pageable pageable) {

        Specification<MembersVO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (memId != null) {
                predicates.add(cb.equal(root.get("memId"), memId));
            }
            if (memAcc != null && !memAcc.isBlank()) {
                predicates.add(cb.like(root.get("memAcc"), "%" + memAcc + "%"));
            }
            if (memName != null && !memName.isBlank()) {
                predicates.add(cb.like(root.get("memName"), "%" + memName + "%"));
            }
            if (memStatus != null) {
                predicates.add(cb.equal(root.get("memStatus"), memStatus));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

//        return membersRepository.findAll(spec, pageable);
        
     // 1. 一次撈完所有符合條件的清單（不分頁）
        List<MembersVO> fullList = membersRepository.findAll(spec, Sort.by("memId").ascending());

        // 2. 用 PageImpl 把它包成一個「unpaged」的 Page 回傳
        return new PageImpl<>(fullList, Pageable.unpaged(), fullList.size());
    }
	
	
	public List<MembersVO> searchByCriteria(
            Byte memStatus,
            String memAcc,
            Integer memId,
            String memName) {
        // 直接呼叫上面那支五參數、傳入 unpaged()
        Page<MembersVO> allPage = searchByCriteria(
            memStatus,
            memAcc,
            memId,
            memName,
            Pageable.unpaged()
        );
        return allPage.getContent();
    }

	
	
	
	
	
	@Transactional
    public void editMember(MembersVO membersvo) {
        // 直接 save() 就會做 update
        membersRepository.save(membersvo);
    }
	
	
}