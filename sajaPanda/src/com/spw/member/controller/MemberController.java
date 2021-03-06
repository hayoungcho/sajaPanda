package com.spw.member.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.spw.member.service.MemberService;
import com.spw.member.vo.MemberVO;

/**
 * 설명: 회원가입, 로그인, ID찾기 ,비밀번호 찾기 네이버 로그인을 위한 컨트롤러 
 * 사용기술 : log4j, MVC패턴, mybatis, ajax(회원가입시 ID중복체크), 메일전송(Mail library), 네이버로그인 API(json-simple)
 * 
 * HTTP 바디에 담기는 content type의 종류 3가지 
 *  1.text type : html,css,js 등등
 *  2.multi type : multipart/form-data 
 *  3.application type : json,xml 등등 
 *  
 *  매개변수 설정 :
 *  meminsert( @RequestParam(value="mvo", defaultValue="기본값") String mvo )
 *  meminsert(String mvo)
 *  위에 두개가 같지만  request 속성을 더욱 상세하게 적용할 수 있다.
 *  
 *  meminsert(@RequestBody MemberVO mvo)
 *  post방식만 작동하고 VO, JSON,XML 데이터를 받을 경우 사용
 *  
 *  meminsert(@ModelAttribute MemberVO mvo)
 *  post방식만 작동하고 VO, JSON,XML 데이터를 받을 경우 사용
 *  
 *  @ModelAttribute : http요청에 들어있는 속성값들을 memeberVO 객체에 자동으로 바인딩
 *  @ModelAttribute([NAME]) 형태로 붙일 경우 jwsp파일에서 ${[NAME].property} 로 model 객체 값을 사용가능
 *  
 *  
 * 날짜 : 20200807
 * 작성자 : 김주호
 */
@Controller
public class MemberController {
	Logger log = Logger.getLogger(MemberController.class);

	/*
	 * 의존성 주입시켜 이 클래스에서 인스턴스 하지 않고 사용가능 하게 한다.(default로 싱글톤사용)
	 */
	@Autowired
	private MemberService memberservice;

	/*
	 * 외부에서 "meminsertform"이라는 url이 들어오면 이 컨트롤러가 받아서 처리한다. value값을 "/meminsertform"
	 * 이렇게 해도 작동된다. 페이지만 이동하기 때문에 GET방식을 적용해 보았다.
	 */

	@RequestMapping(value = "meminsertform", method = RequestMethod.GET)
	public ModelAndView memInsertForm() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("meminsert/meminsertform");
		log.info(mav);
		return mav;
	}
	/*
	 * 아이디 중복 체크 ajax에서 이용하고 아래에 회원가입에서도 이용한다.
	 * 
	 * @RequestBody, @ResponseBody(이 메서드의 리턴값을 http바디에 담아 보낸다.) 위에 두 어노테이션을 이용하면
	 *  json데이터를 주고 받을 수 있다.
	 * 
	 * @RequestBody는 controller에 들어오는 json데이터를 설정한 객체나,map에 매핑해준다.(json데이터와 객체의
	 *  프로퍼티가 동일해야한다. POST방식만 작동)
	 */
	@ResponseBody
	@RequestMapping(value = "/idchk", method = RequestMethod.POST)
	public String idchk(MemberVO memberVO) {
		int result = memberservice.idchk(memberVO);
		Integer.toString(result);
		String result2;
		result2 = result + "";
		return result2;
	}
	// 회원가입form페이지에서 입력받은 값을 DB에 저장 후 완료페이지로 이동
	@RequestMapping(value = "/meminsert", method = RequestMethod.POST)
	public ModelAndView memInsert(@ModelAttribute MemberVO mvo, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		/* request로 받아서 다시 세팅하는 이유는
		 * mvo객체에 기본으로 세팅되어 있는 값이 입력폼과 차이가 있기 때문에 따로 받아서 셋팅을 하였다.
		 */
		String mphone0 = request.getParameter("mphone0");
		String mphone1 = request.getParameter("mphone1");
		String mphone2 = request.getParameter("mphone2");
		String mphone = mphone0 + "-" + mphone1 + "-" + mphone2;
		mvo.setMphone(mphone);
		String memail0 = request.getParameter("memail0");
		String memail1 = request.getParameter("memail1");
		String memail = memail0 + "@" + memail1;
		mvo.setMemail(memail);
		try {
		    //위에서 만든 id 체크 컨트롤러 한번 사용
			//서버에서 한번 더 ID중복체크를 실행한다.	
			int idchkResult;
				idchkResult = memberservice.idchk(mvo);
			if (idchkResult >= 1) {
				mav.addObject("result", "아이디가 중복되어 초기화면으로 돌아갑니다.");
				mav.setViewName("/meminsert/meminsertform");
				return mav;
			} else {
				//여기서 실질적인 멤버인서트가 일어난다.
				int result;
					result = memberservice.memInsert(mvo);

				mav.addObject("mvo0", mvo);
				mav.addObject("result", result);
				/**
				 * 메일 전송 사용
				 * 내가 만든 메서드라서 매개변수 3가지를 받게 만들었다.
				 * 1.받는 사람메일 주소 , 2. 메일제목, 3. 메일 내용
				 **/ 
				String toUser;
				String sendSubject;
				String sendText;
					   toUser = mvo.getMemail();
					   sendSubject =  "사자판다 가입 확인";
					   sendText =mvo.getMname()+" 님 안녕하세요. "+" \n "+" 가입정보는 다음과 같습니다. \n "+"아이디 : "+mvo.getMid()+" \n 비밀번호 : "+mvo.getMpw() +"\n "
						+ "이메일 : "+mvo.getMemail();
				mailsend(toUser,sendSubject,sendText);
				
				mav.setViewName("/login/loginform");
			}
		} catch (Exception e) {
		}
		log.info(mav);
		return mav;// 회원가입 실행이 완료되면 로그인 페이지로 보내야 한다. (미구현)
	}

	//로그인 form페이지로 이동하기 위한 컨트롤러 
	@RequestMapping("loginform")
	public ModelAndView memloginform() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/login/loginform");
		return mav;
	}

	// 로그인form페이지에서 입력받아 로그인실행하는 컨트롤러
	@RequestMapping(value = "/loginOK", method = RequestMethod.POST)
	public ModelAndView memloginOK(@ModelAttribute MemberVO mvo, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		try {
			MemberVO resultVO = memberservice.memLogin(mvo);
			/** 
			 * 세션을 생성하고 세션에 값을 부여 
			 * 세션 설정은  톰켓 server에   web.xml에  <session-config>에서 한다.
			 * 기본 설정값은 30분으로 되어 있다.
			 * HttpServletRequest request 를 매개변수로 받아서 세션을 받아야 한다. 
			 * session.setAttribute 로 값을 세팅하고
			 * session.getAttribure 로 값을 빼서 사용한다.
			 * */
			if (resultVO.getMid() != "" || resultVO.getMid() != null) {
				
				HttpSession session = request.getSession();
				session.setAttribute("mnum", resultVO.getMnum());
				session.setAttribute("mid", resultVO.getMid());
				session.setMaxInactiveInterval(-1);//세션 무한대
//로그인 			
				if(resultVO.getMnum().equals("M202007300001")) {
					mav.addObject("memberVO", resultVO);
					mav.setViewName("/admin/adminform");
				}else {
					mav.addObject("memberVO", resultVO);
					mav.setViewName("/login/loginOK");
				}
			} else {
				mav.setViewName("/login/loginX");
			}
		} catch (Exception e) {
			mav.setViewName("/login/loginX");
			return mav;
		}
		return mav;
	}
	
	//로그아웃 만들기
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public ModelAndView memlogOut(@ModelAttribute MemberVO mvo, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();	
		HttpSession session=request.getSession();
		/** 
		 * 세션을 생성하고 세션에 값을 부여 
		 * 세션 설정은  톰켓 server에   web.xml에  <session-config>에서 한다.
		 * 기본 설정값은 30분으로 되어 있다.
		 * HttpServletRequest request 를 매개변수로 받아서 세션을 받아야 한다. 
		 * session.invalidate() 는 세션을 모두 삭제 한다,
		 * */
		session.invalidate(); 
		mav.setViewName("/login/loginform");
		return mav;
		}
		
	// id찾기 페이지로 보내기
	@RequestMapping(value = "idfind", method = RequestMethod.GET)
	public ModelAndView memidfind(@ModelAttribute MemberVO mvo) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/idfind/idfind");
		return mav;
	}
	

	// id찾기 페이지에서 아이디찾기 후 완료페이지로 보내기
	@RequestMapping(value = "/idfindOK", method = RequestMethod.POST)
	public ModelAndView memidfindOK(@ModelAttribute MemberVO mvo, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		String memail = request.getParameter("memail");
		String mnum = request.getParameter("mnum");
		MemberVO mvo0 = new MemberVO();
		mvo0.setMemail(memail);
		mvo0.setMnum(mnum);
		try {
			MemberVO result = memberservice.memIdfind(mvo0);
			if (result.getMid() != null && result.getMid() != "") {
				mav.addObject("result", result);
				mav.setViewName("/idfind/idfindOK");
				/**
				 * 메일 전송 사용
				 * 내가 만든 메서드라서 매개변수 3가지를 받게 만들었다.
				 * 1.받는 사람메일 주소 , 2. 메일제목, 3. 메일 내용
				 **/
				String toUser;
				String sendSubject;
				String sendText;
					   toUser = result.getMemail();
					   sendSubject = "사자판다 회원 아이디 찾기";
					   sendText = "안녕하세요. " + result.getMname() + " 님  회원님의 아이디는 " + result.getMid() + " 입니다.";
				mailsend(toUser, sendSubject, sendText);
				return mav;
			} else {
				mav.setViewName("/idfind/idfindx");
			}
		} catch (Exception e) {
			e.printStackTrace();
			mav.setViewName("/idfind/idfindx");
			return mav;
		}
		return mav;
	}

	// 비밀번호 찾기 페이지로 이동
	@RequestMapping(value = "/pwfind.spw", method = RequestMethod.GET)
	public ModelAndView mempwfind(@ModelAttribute MemberVO mvo, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/pwfind/pwfind");
		return mav;
	}

	// 비밀번호 찾기 페이지에서 입력 후 완료페이지로 이동.
	@RequestMapping(value = "/pwfindOK.spw", method = RequestMethod.POST)
	public ModelAndView mempwfindOK(@ModelAttribute MemberVO mvo, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		try {
			MemberVO resultVO = memberservice.memPwfind(mvo);
			if (resultVO.getMpw() != null && resultVO.getMpw() != "") {
				/**
				 * 메일 전송 사용
				 * 내가 만든 메서드라서 매개변수 3가지를 받게 만들었다.
				 * 1.받는 사람메일 주소 , 2. 메일제목, 3. 메일 내용
				 **/
				String toUser;
				String sendSubject;
				String sendText;
						toUser=resultVO.getMemail();
						sendSubject = "사자판다  회원 비밀번호 찾기";
						sendText = "안녕하세요.\n "+resultVO.getMname()+" 님 "+"회원님의 id : "+resultVO.getMname()+"\n 회원님의 비밀번호 : "+resultVO.getMpw();
				mailsend(toUser,sendSubject,sendText);
				mav.addObject("memberVO", resultVO);
				mav.setViewName("/pwfind/pwfindOK");
				return mav;
			} else {
				mav.setViewName("/pwfind/pwfindx");
			}
		} catch (Exception e) {
			mav.setViewName("/pwfind/pwfindx");
			return mav;
		}
		return mav;
	}

	/* 자바메일 전송
	 * 참고 사이트 주소
	 * 라이브러리
	 * https://9aram.tistory.com/28
	 * 사용법
	 * https://ktko.tistory.com/entry/JAVA-SMTP%EC%99%80-Mail-%EB%B0%9C%EC%86%A1%ED%95%98%EA%B8%B0Google-Naver
	 *  */
	public void mailsend(String toUser, String sendSubject, String sendText) {
		String user = "rlawngh8875@gmail.com";
		String password = "rlawnghdustmq";
		// SMTP 서버 정보를 설정한다.
		Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
//		보내는 사람이 gmail 사용시 포트
		prop.put("mail.smtp.port", 465);
//		보내는 사람이 naver메일 사용시 포트
//		prop.put("mail.smtp.port", 587);
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.ssl.enable", "true");
		prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
		//mail 라이브러리의 객체 메서드 사용
		Session session = Session.getDefaultInstance(prop, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, password);
			}
		});
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(user));
		    //수신자 메일 주소
			//message.addRecipient(Message.RecipientType.TO, new InternetAddress("rlaj005@naver.com"));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(toUser));
			message.setSubject(sendSubject);
			message.setText(sendText);
			Transport.send(message);// 메일 전송
			log.info(message + "메일 전송완료");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}//end of mailsend()
	
//---------------네이버 로그인을 위한 ----------------------------------
	/*
	 * 설명 : 네이버 로그인 API
	 * 
	 * 주요사용기술: json-simple라이브러리 
	 * 작성 법
	 * https://developers.naver.com/docs/login/api/ 에서 JSP소스
	 * 	1. naverlogin.jsp 는 로그인 화면에 붙여넣기
	 *  2. callback.jsp 에서 2번째 200번 상태를 확인하는 if문에서 json-simple라이브러리를 활용해서 access_token 값을 뽑아 온다. 그 값을 request에 담아 보낸다.  
	 * https://developers.naver.com/docs/login/profile/ 에서 JAVA소스
	 *  3. api에서 원하는 값을 모두 넣고 main에 적힌 소스를 활용한다.
	 *  
	 *  
	 *  주의 해야 할건  여기서 활용된것을 바탕으로
	 * 	통신 성공을 하면 String 타입으로 값이 넘어오는데. 여기서 다시 json-simple라이브러리를 활용해서 값을 뽑아온다.
	 *  그리고 DB값을 조회를 먼저 해서 값이 있다면 세션만 부여하고 넘기고
	 *  값이 없다면 회원가입을하고 세션을 부여하고 넘긴다.
	 *  
	 * 작성일: 20200820
	 * 작성자: 김주호
	 * */
	@RequestMapping("/naverlogin")
	public ModelAndView naverlogin(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		//여기서 부터 아래 responseBody 까지는 내가 하는게 아니라 api의 문서를 그대로 활용
		String access_token = (String)request.getAttribute("access_token");
		System.out.println("access_token:::"+access_token);
		String token = access_token; // 네이버 로그인 접근 토큰;
        String header = "Bearer " + token; // Bearer 다음에 공백 추가
        String apiURL = "https://openapi.naver.com/v1/nid/me";
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", header);
        String responseBody = get(apiURL,requestHeaders);
        System.out.println(responseBody);
        
 //{"resultcode":"00","message":"success","response":{"id":"21746765","age":"30-39","email":"rlaj005@naver.com","name":"\uae40\uc8fc\ud638","birthday":"10-09"}}
        //위에 값을 콘솔로 찍어본다. name 값이 유니코드인데 브라우저에서 자동으로 변환해서 읽고 json simple 라이브러리가 변환해준다.
        JSONObject jobj = new JSONObject();
        JSONParser parser = new JSONParser();
        MemberVO mvo = new MemberVO();
        try {
			jobj = (JSONObject)parser.parse(responseBody);
			String resultcode =(String)jobj.get("resultcode");
			String message =(String)jobj.get("message");
			jobj =(JSONObject)jobj.get("response"); //여기서 response가 json객체 안에 json객체이기 때문에
			String id =(String)jobj.get("id");
			String age =(String)jobj.get("age");
			String email =(String)jobj.get("email");
			String name =(String)jobj.get("name");
			String birthday =(String)jobj.get("birthday");
			
	
			if(resultcode.equals("00")&& message.equals("success"))  {
			 //통신을 성공적으로 받았다면	
				mvo.setMid(id);
				mvo.setMname(name);
				mvo.setMemail(email);
				MemberVO nsVO;
				nsVO=memberservice.naverSelect(mvo);
				if(nsVO.getMnum()!=null) {
					String mnum=nsVO.getMnum();
					HttpSession session=request.getSession();
					session.setAttribute("mnum", mnum);
					session.setMaxInactiveInterval(-1);//세션 무한대
				}else {
					int INresult=memberservice.naverInsert(mvo);
					
					if(INresult>0) {
					System.out.println("회원가입완료");
					MemberVO resultVO;
					resultVO=memberservice.naverLogin(mvo);
					String mnum=resultVO.getMnum();
					HttpSession session=request.getSession();
					session.setAttribute("mnum", mnum);
					session.setMaxInactiveInterval(-1);//세션 무한대
					}else{
						System.out.println("insert가 실행되지 않았습니다.");
					}//end of if(insert result) 
				}//end of if(naverSelect mnum)
			}else{
				System.out.println("네이버 API에서 값을 받아오지 못했습니다.");
			}//end of if(resultcode,massage)
        } catch (ParseException e) {
			System.out.println("json 객체 변환실패");
			e.printStackTrace();
		}//end of try catch
        mav.setViewName("/login/loginOK");
        return mav;
	}//end of naverlogin controller
	
	@RequestMapping(value = "/callback")
	public static ModelAndView callbackPage() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("login/callback");
		return mav;
	}
	
	 public static String get(String apiUrl, Map<String, String> requestHeaders){
	        HttpURLConnection con = connect(apiUrl);
	        try {
	            con.setRequestMethod("GET");
	            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
	                con.setRequestProperty(header.getKey(), header.getValue());
	            }
	            int responseCode = con.getResponseCode();
	            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
	                return readBody(con.getInputStream());
	            } else { // 에러 발생
	                return readBody(con.getErrorStream());
	            }
	        } catch (IOException e) {
	            throw new RuntimeException("API 요청과 응답 실패", e);
	        } finally {
	            con.disconnect();
	        }
	    }
	 public static HttpURLConnection connect(String apiUrl){
	        try {
	            URL url = new URL(apiUrl);
	            return (HttpURLConnection)url.openConnection();
	        } catch (MalformedURLException e) {
	            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
	        } catch (IOException e) {
	            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
	        }
	    }
	 public static String readBody(InputStream body){
	        InputStreamReader streamReader = new InputStreamReader(body);
	        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
	            StringBuilder responseBody = new StringBuilder();
	            String line;
	            while ((line = lineReader.readLine()) != null) {
	                responseBody.append(line);
	            }
	            return responseBody.toString();
	        } catch (IOException e) {
	            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
	        }
	    }

}
