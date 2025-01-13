package com.scsb.pm.service;

import com.scsb.pm.dao.LoginRepository;
import com.scsb.pm.entity.Member;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.base.Captcha;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import javax.imageio.ImageIO;

@Service
public class LoginService {

    @Autowired
    private LoginRepository loginRepository;

    // 產生驗證碼
    public void generateCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("image/jpeg");
        // 算術類型驗證碼
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 48);
        captcha.setLen(2); // set length
        // 獲取驗證碼並儲存
        String captchaText = captcha.text();
        request.getSession().setAttribute("captchaText", captchaText);
        // 輸出驗證碼圖片
        captcha.out(response.getOutputStream());
    }
    // 驗證是否匹配
    public boolean validateCaptcha(HttpServletRequest request, String inputCaptcha) {
        String sessionCaptcha = (String) request.getSession().getAttribute("captchaText");
        return sessionCaptcha != null && sessionCaptcha.equals(inputCaptcha);
    }
    // 清空驗證碼
    public void clearCaptcha(HttpServletRequest request) {
        request.getSession().removeAttribute("captchaText");
    }

    // 驗證用戶名與密碼是否一樣
    public boolean verify(String username, String password) {
        try {
            Optional<Member> optionalMember = loginRepository.findByUsername(username);
            if (optionalMember.isPresent()) {
                Member member = optionalMember.get();
                boolean passwordMatch = member.getPassword().equals(password);
                return passwordMatch; //table密碼 = user輸入密碼
            } else {
                return false;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }
    }

    //獲取用戶role
    public String getRole(String username){
        try {
            Optional<Member> optionalMember = loginRepository.findByUsername(username);
            if (optionalMember.isPresent()) {
                String memberRole = optionalMember.get().getRole();
                return memberRole;
            }
            return "User not found";
        } catch (Exception e) {
            return "User not found";
        }
    }

    // 對輸出內容進行HTML轉義 防止XSS攻擊
    public String escapeHtml(String input) {
        if (input == null){
            return null;
        }
        return input.replace("&", "&amp;") // & 換成 &amp 防止被解釋為實體的開始
                .replace("<", "&lt;") // 標籤的開始
                .replace(">", "&gt;") // 標籤的結束
                .replace("/", "&quot;") // " 非必須 但也可用來防止
                .replace("'", "&apos;"); // 屬性質的邊界
    }

}
