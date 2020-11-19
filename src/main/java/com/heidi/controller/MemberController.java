package com.heidi.controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.heidi.service.MemberService;
import com.heidi.vo.MemberVO;

@Controller
@RequestMapping("/member/*")
public class MemberController {

	private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
	
	@Inject
	MemberService service;
	
	@Inject
	BCryptPasswordEncoder pwdEncoder;
	
	// 회원가입 get (회원가입 폼으로 이동할 때)
	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public String getRegister() throws Exception {
		logger.info("get register");
		
		return "member/register";
	}
	
	// 회원가입 post (회원가입 버튼을 눌렀을 때)
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String postRegister(MemberVO vo) throws Exception {
		logger.info("post register");
		
		int result = service.idChk(vo);
		try {
			if (result == 1) {
				return "/member/register";
			} else if (result == 0) {
				String inputPass = vo.getUserPass();	//  유저 비밀번호를 가져와서
				String pwd = pwdEncoder.encode(inputPass);	//  BCryptPasswordEncoder로 암호화하고
				vo.setUserPass(pwd);	//  다시  vo 에 저장
				
				service.register(vo);	//  암호화한 비밀번호가 있는 회원 정보로 회원가입
			}
			// 여기서 입력된 아이디가 존재한다면 다시 회원가입 페이지로 돌아가고,
			// 존재하지 않는다면 register로 !
		} catch (Exception e) {
			throw new RuntimeException();
		}
				
		return "redirect:/";
	}
	
	// 로그인
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String login(MemberVO vo, HttpSession session, RedirectAttributes rttr) throws Exception {
		logger.info("login - post");
		
		session.getAttribute("member");
		MemberVO login = service.login(vo);
		boolean pwdMatch = pwdEncoder.matches(vo.getUserPass(), login.getUserPass());
		
		if (login != null && pwdMatch == true) {
			session.setAttribute("member", login);
		} else {
			session.setAttribute("member", null);
			rttr.addFlashAttribute("msg", false);
		}
		
		return "redirect:/";
	}
	
	// 로그 아웃
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(HttpSession session) throws Exception {
		logger.info("logout - get");
		
		session.invalidate();
		
		return "redirect:/";
	}
	
	// 회원 정보 수정 - 뷰로 이동하기
	@RequestMapping(value = "/memberUpdateView", method = RequestMethod.GET)
	public String memberUpdateView() throws Exception {
		logger.info("member update view");
		
		return "member/memberUpdateView";
	}
	
	// 회원 정보 수정
	@RequestMapping(value = "/memberUpdate", method = RequestMethod.POST)
	public String memberUpdate(MemberVO vo, HttpSession session) throws Exception {
		logger.info("member update");
		
/*		MemberVO login = service.login(vo);
		
		boolean pwdMatch = pwdEncoder.matches(vo.getUserPass(), login.getUserPass());
		if (pwdMatch) {
			service.memberUpdate(vo);
			session.invalidate();
		} else {
			return "member/memberUpdateView";
		}
*/		
		service.memberUpdate(vo);
		
		session.invalidate();
		
		return "redirect:/";
	}
	
	// 회원 탈퇴 - 뷰로 이동하기
	@RequestMapping(value = "/memberDeleteView", method = RequestMethod.GET)
	public String memberDeleteView() throws Exception {
		logger.info("member delete view");
		
		return "member/memberDeleteView";
	}
	
	// 회원 탈퇴
	@RequestMapping(value = "/memberDelete", method = RequestMethod.POST)
	public String memberDelet(MemberVO vo, HttpSession session, RedirectAttributes rttr) throws Exception {
		logger.info("member delete");
		
/*		세션에 존재하는 member를 가져와서 member라는 변수에 넣는다.
		MemberVO member = (MemberVO) session.getAttribute("member");
		
		세션에 있는 비밀번호
		String sessionPass = member.getUserPass();
		vo로 들어오는 비밀번호
		String voPass = vo.getUserPass();
		
		if (!(sessionPass.equals(voPass))) {
			rttr.addFlashAttribute("msg", false);
			return "redirect:/member/memberDeleteView";
		}
*/		
		service.memberDelete(vo);
		session.invalidate();
		return "redirect:/";
	}
	
	// 패스워드 체크
	@ResponseBody
	@RequestMapping(value = "/passChk", method = RequestMethod.POST)
	public boolean passChk(MemberVO vo) throws Exception {
		logger.info("pass check");
		
		MemberVO login = service.login(vo);
		boolean pwdChk = pwdEncoder.matches(vo.getUserPass(), login.getUserPass());
		return pwdChk;
	}
	
	// 회원가입 아이디 중복 체크
	// 패스워드 체크
	@ResponseBody
	@RequestMapping(value = "/idChk", method = RequestMethod.POST)
	public int idChk(MemberVO vo) throws Exception {
		logger.info("id check");
		
		int result = service.idChk(vo);
		return result;
	}
		
}