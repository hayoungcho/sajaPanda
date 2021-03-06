package com.spw.general.controller;


import java.io.File;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import com.spw.category.service.CategoryService;
import com.spw.category.vo.CategoryVO;
import com.spw.common.CommonUtil;
import com.spw.common.FileEditUtil;
import com.spw.general.service.GeneralService;
import com.spw.general.vo.GeneralCountVO;
import com.spw.general.vo.GeneralVO;
import com.spw.reg.service.RegService;
import com.spw.reg.vo.RegVO;

@Controller
public class GeneralController {

	// log.
	private Logger log = Logger.getLogger(GeneralController.class);
	
	@Autowired
	private GeneralService generalService;
	
	@Autowired
	private CategoryService categoryService;
	
	// 전체 글 목록: generalAllList ==========================
	@RequestMapping(value="/generalAllList")
	public ModelAndView generalAllList(HttpServletRequest req, @ModelAttribute GeneralVO gvo) {
		log.info("GeneralController.generalAllList 시작 >> ");

		String groupsize = "5"; 	// 그룹사이즈  
		String pagesize = "3";   	// 페이지사이즈 
		String curpage = "1"; 	    // 현재페이지  
		String totalcount = "0"; 	// 총데이터수
		
		if(req.getParameter("curpage") != null) {
			curpage = req.getParameter("curpage");
		}
		
		gvo.setGroupsize(groupsize);
		gvo.setPagesize(pagesize);
		gvo.setCurpage(curpage);
		gvo.setTotalcount(totalcount);
		
		log.info("groupsize >>> " + gvo.getGroupsize());		
		log.info("pagesize >>> " + pagesize);		
		log.info("curpage >>> " + curpage);		
		log.info("totalcount >>> " + totalcount);		
		
		List<GeneralVO> aList = generalService.generalAllList(gvo);
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("aList", aList);
		mav.setViewName("general/generalAllList");
		
		log.info("GeneralController.generalAllList 끝 >> ");
		return mav;
	}
	
	// 사자 글 목록 : generalSList ==========================
	@RequestMapping(value="/generalSList")
	public ModelAndView generalSList(HttpServletRequest req, @ModelAttribute GeneralVO gvo) {
		log.info("GeneralController.generalSList 시작 >> ");
		
		String groupsize = "5"; 	// 그룹사이즈  
		String pagesize = "16";   	// 페이지사이즈 
		String curpage = "1"; 	    // 현재페이지  
		String totalcount = "0"; 	// 총데이터수
		
		if(req.getParameter("curpage") != null) {
			curpage = req.getParameter("curpage");
		}
		
		gvo.setGroupsize(groupsize);
		gvo.setPagesize(pagesize);
		gvo.setCurpage(curpage);
		gvo.setTotalcount(totalcount);
		
		
		List<GeneralVO> sList = generalService.generalSList(gvo);
		
		log.info("DB갔다온 후");		
		log.info("groupsize >>> " + gvo.getGroupsize());		
		log.info("pagesize >>> " + gvo.getPagesize());		
		log.info("curpage >>> " + gvo.getCurpage());		
		log.info("totalcount >>> " + gvo.getTotalcount());	
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("sList", sList);
		mav.setViewName("general/generalSList");
		
		log.info("GeneralController.generalSList 끝 >> ");
		return mav;
	}
	
	// 판다 글 목록 : generalPList ==========================
	@RequestMapping(value="/generalPList")
	public ModelAndView generalPList(HttpServletRequest req, @ModelAttribute GeneralVO gvo) {
		log.info("GeneralController.generalPList 시작 >> ");
		
		String groupsize = "5"; 	// 그룹사이즈     - 어차피 못받아옴?
		String pagesize = "16";   	// 페이지사이즈  - 안받아짐
		String curpage = "1"; 	    // 현재페이지  
		String totalcount = "0"; 	// 총데이터수
		
		if(req.getParameter("curpage") != null) {
			curpage = req.getParameter("curpage");
		}
		
		gvo.setGroupsize(groupsize);
		gvo.setPagesize(pagesize);
		gvo.setCurpage(curpage);
		gvo.setTotalcount(totalcount);
		
		List<GeneralVO> pList = generalService.generalPList(gvo);
		
		log.info("DB갔다온 후");		
		log.info("groupsize >>> " + gvo.getGroupsize());		
		log.info("pagesize >>> " + gvo.getPagesize());		
		log.info("curpage >>> " + gvo.getCurpage());		
		log.info("totalcount >>> " + gvo.getTotalcount());	
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("pList", pList);
		mav.setViewName("general/generalPList");
		
		log.info("GeneralController.generalPList 끝 >> ");
		return mav;
	}
	
	// 새 게시글 등록 페이지 출력 : generalInsertForm ========================
	@RequestMapping(value="/generalInsertForm" )
	public ModelAndView generalInsertForm(HttpServletRequest req) {
		log.info("GeneralController.generalInsertForm 시작 >> ");
		HttpSession hs = req.getSession();
		String mnum = (String)hs.getAttribute("mnum");
		
		ModelAndView mav = new ModelAndView();
		
		if(mnum!=null&&mnum.length()>0) {
			mav.setViewName("general/generalInsertForm");			
		}else {
			mav.setViewName("login/loginform");
		}

		return mav;
	}
	
	// 새 게시글 등록 기능  : generalInsert ========================
	@RequestMapping(value="/generalInsert", method=RequestMethod.POST)
	public ModelAndView generalInsert(HttpServletRequest req, Model model) {
		log.info("GeneralController.generalInsert 시작 >> ");
//			log.info("new 글제목 >> " + gvo.getGtitle());
//			log.info("new 거래지역 >> " + gvo.getGloc());
//			log.info("new 법정동코드 >> " + gvo.getGregcode());
		
		// mac.
		//String originPath="/Users/admin/Desktop/Spw_Project/sp/WebContent/uploadImages";
		// window.
		//String originPath = "C:\\Users\\theea\\00.KOSMO62\\Spw_Project\\spw\\WebContent\\uploadImages";
		// 상대경로
		ServletContext context = req.getSession().getServletContext();
	    String originPath = context.getRealPath("uploadImages");
	    
		int size = 10*1024*1024;
		int result=0;
		String fileName="";
		String resultStr = null;
		GeneralVO gvo=new GeneralVO();
		
		try {
				MultipartRequest mr = new MultipartRequest(req
														 , originPath
														 , size
														 , "UTF-8"
														 , new DefaultFileRenamePolicy());
				
				// 업로드 파라미터 세팅                                             
				//String mnum = mr.getParameter("mnum");            
				HttpSession hs = req.getSession();
				String mnum = (String)hs.getAttribute("mnum");	  // 회원번호
				String cgnum = mr.getParameter("cgnum");          // 카테고리 
				                                                                 
				String gnum= mr.getParameter("gnum");             // 일반 상품번호     
				String gsort = mr.getParameter("gsort");  	      // 구분          
				String gtitle = mr.getParameter("gtitle");        // 제목          
				String gloc = mr.getParameter("gloc");            // 거래지역        
				String gregcode = mr.getParameter("gregcode");    // 법정동코드       
				String gprice = mr.getParameter("gprice");        // 가격          
				String gcontents = mr.getParameter("gcontents");  // 설명          
				String gstatus = mr.getParameter("gstatus");      // 상태          
		
				log.info(">>> "+mnum);
				log.info(">>> "+cgnum);
				log.info(">>> "+gsort);
				log.info(">>> "+gtitle);
				log.info(">>> "+gloc);
				log.info(">>> "+gregcode); 
				log.info(">>> "+gprice);
				log.info(">>> "+gcontents);
				log.info(">>> "+gstatus);
				
				Enumeration<String> en= mr.getFileNames();
				while(en.hasMoreElements()) {
					String file = en.nextElement();
					log.info("file >>> "+file);
					fileName = mr.getFilesystemName(file);
					log.info("fileName >>> "+fileName);
				}
				
				File file = new File(originPath+"//"+fileName);
				int index = fileName.lastIndexOf(".");
				String fileExt = fileName.substring(index+1);
				
				log.info("확장자 : fileExt >> " + fileExt);
				log.info("파일 : file >> " + file);
				log.info("썸네일 파일 생성");
				
				log.info("" + file.getAbsolutePath());
				
				String editFile=FileEditUtil.makeThumbnail(file.getAbsolutePath(), fileName, fileExt, req);
				
				
				log.info("썸네일 파일 생성");
				
				gvo.setMnum(mnum);
				gvo.setCgnum(cgnum);          
				gvo.setGnum(gnum);
				gvo.setGsort(gsort);
				gvo.setGtitle(gtitle);
				gvo.setGloc(gloc);
				gvo.setGregcode(gregcode); 
				gvo.setGprice(gprice);
				gvo.setGcontents(gcontents);
				gvo.setGstatus(gstatus);
				gvo.setGphoto(fileName);
				
			
				result = generalService.generalInsert(gvo);
		}catch(Exception e) {
			log.info(">>> exception >>> "+e);
		}
		
		ModelAndView mav = new ModelAndView();
		
		if(result==1)
			resultStr="완료";
		else 
			resultStr="실패";
		String sort = gvo.getGsort();
		mav.addObject("resultStr", resultStr);
		mav.addObject("sort",sort);
		
		mav.setViewName("general/resultForm");
		
		log.info("GeneralController.generalInsert 끝 >> ");
		return mav;
	}
	
	// 게시글 상세 페이지 출력 : generalDetailForm ========================
	@RequestMapping(value="/generalDetailForm")
	public ModelAndView generalDetailForm(@ModelAttribute GeneralVO gvo) {
		log.info("GeneralController.generalDetailForm 시작 >>");

		ModelAndView mav = new ModelAndView();
		GeneralVO _gvo = generalService.generalSelect(gvo);
		CategoryVO cvo = new CategoryVO();
		cvo.setCgnum(_gvo.getCgnum());
		List<CategoryVO> list = categoryService.categorySelect(cvo);
		_gvo.setCgname(list.get(0).getCgname());
		mav.setViewName("general/generalDetailForm");
		mav.addObject("detailDate",_gvo);
		
		log.info("GeneralController.generalDetailForm 끝 >>");
		return mav;
	}
	
	// 게시글 수정 페이지 출력 : generalUpdateForm ========================
	@RequestMapping(value="/generalUpdateForm")
	public ModelAndView generalUpdateForm(@ModelAttribute GeneralVO gvo) {
		log.info("generalUpdateForm 시작 >>> : ");
		log.info("generalUpdateForm.getGnum >>> : " + gvo.getGnum());
		
		ModelAndView mav = new ModelAndView();
		GeneralVO _gvo = generalService.generalSelect(gvo);
		log.info("generalUpdateForm.getGloc >>> : " + _gvo.getGloc());
		log.info("generalUpdateForm.getGcontents >>> : " + _gvo.getGcontents());
		
		mav.setViewName("general/generalUpdateForm");
		mav.addObject("updateData",_gvo);
		
		log.info("generalUpdateForm 끝 >>> : ");
		return mav;
	}
	
	// 게시글 수정 기능 : generalUpdateOK ========================
	@RequestMapping(value="/generalUpdateOK", method=RequestMethod.POST)
	public ModelAndView generalUpdateOK(HttpServletRequest req, Model model) {
		log.info("GeneralController.generalUpdateOK 시작 >> ");
//			log.info("u 글번호 >> " + gvo.getGnum());
//			log.info("u 글제목 >> " + gvo.getGtitle());
//			log.info("u 거래지역 >> " + gvo.getGloc());
//			log.info("u 법정동코드 >> " + gvo.getGregcode());
		
		GeneralVO gvo = new GeneralVO();
		// mac.
		// String originPath="/Users/admin/Desktop/Spw_Project/sp/WebContent/uploadImages";
		// window.
		// String originPath = "C://Users//theea//00.KOSMO62//Spw_Project//spw//WebContent//uploadImages";
		// 상대경로
		ServletContext context = req.getSession().getServletContext();
		String originPath = context.getRealPath("uploadImages");
		
		
		int size = 10*1024*1024;
		String fileName=null;
		
		try {
				MultipartRequest mr = new MultipartRequest(req
														, originPath
														, size
														, "UTF-8"
														, new DefaultFileRenamePolicy());
				
				// 업로드 파라미터 세팅
				String mnum = mr.getParameter("mnum");            // 회원번호  
				String cgnum = mr.getParameter("cgnum");          // 카테고리 
				String gnum= mr.getParameter("gnum");             // 일반 상품번호     
				String gsort = mr.getParameter("gsort");  	      // 구분          
				String gtitle = mr.getParameter("gtitle");        // 제목          
				String gloc = mr.getParameter("gloc");            // 거래지역        
				String gregcode = mr.getParameter("gregcode");    // 법정동코드       
				String gprice = mr.getParameter("gprice");        // 가격          
				String gcontents = mr.getParameter("gcontents");  // 설명          
				String gstatus = mr.getParameter("gstatus");      // 상태          
				String gdelyn = mr.getParameter("gdelyn");        // 삭제여부        
				String gregdate = mr.getParameter("gregdate");    // 등록일         
				String gupdate = mr.getParameter("gupdate");      // 수정일         
				
				// 로그 찍어보기
				log.info("mnum >>> "+mnum);
				log.info("cgnum >>> "+cgnum);
				log.info("gnum >>> "+gnum);
				log.info("gsort >>> "+gsort);
				log.info("gtitle >>> "+gtitle);
				log.info("gloc >>> "+gloc);
				log.info("gregcode >>> "+gregcode); 
				log.info("gprice >>> "+gprice);
				log.info("gcontents >>> "+gcontents);
				log.info("gstatus >>> "+gstatus);
				log.info("gdelyn >>> "+gdelyn);
				log.info("gregdate >>> "+gregdate); 
				log.info("gupdate >>> "+gupdate);
				
				Enumeration<String> en= mr.getFileNames();
				while(en.hasMoreElements()) {
					String file = en.nextElement();
					log.info("file >>> "+file);
					fileName = mr.getFilesystemName(file);
					log.info("fileName >>> "+fileName);
				}
				
				File file = new File(originPath+"//"+fileName);
				int index = fileName.lastIndexOf(".");
				String fileExt = fileName.substring(index+1);
				
				log.info("확장자 : fileExt >> " + fileExt);
				log.info("파일 : file >> " + file);
				log.info("썸네일 파일 생성");
				
				log.info("" + file.getAbsolutePath());
				
				String editFile=FileEditUtil.makeThumbnail(file.getAbsolutePath(), fileName, fileExt, req);
				
				log.info("썸네일 파일 생성");
				
				
				gvo.setMnum(mnum);
				gvo.setCgnum(cgnum);          
				gvo.setGnum(gnum);
				gvo.setGsort(gsort);
				gvo.setGtitle(gtitle);
				gvo.setGloc(gloc);
				gvo.setGregcode(gregcode); 
				gvo.setGprice(gprice);
				gvo.setGcontents(gcontents);
				gvo.setGstatus(gstatus);
				gvo.setGphoto(fileName);
				
		}catch(Exception e){
			log.info("exception >> " + e);
		}
		
		int result = generalService.generalUpdate(gvo);
		String resultStr = null;
		
		if(result==1)
			resultStr = "완료";
		else
			resultStr = "실패";
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("result", resultStr);
		mav.setViewName("general/resultForm");
		
		log.info("GeneralController.generalUpdateOK 끝 >> ");
		return mav;
	}
	
	// 게시글 삭제 기능 : generalDeleteOK ========================
	@RequestMapping(value="/generalDeleteOK")
	public ModelAndView generalDeleteOK(@ModelAttribute GeneralVO gvo) {
		log.info("GeneralController.generalDeleteOK 시작 >> ");
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName("general/resultForm");
		
		log.info("GeneralController.generalDeleteOK 끝 >> ");
		return mav;
		
	}
	@Autowired
	RegService regService;

	// 일반상품 View가 저장된 폴더
	private static String VIEW_DIR = "general/";
		
	/**
	 * 설명
	 * : 선택한 지역내의 동네(읍면동)의 영역정보를 국토교통부(vworld) open API를 통해 JSON으로 받아오고
	 * 상품 목록에서 선택한 지역의 동네별 상품개수정보도 함께 가져옴
	 * 국토교통부(vworld) open API : http://www.vworld.kr/dev/v4dv_2ddataguide2_s002.do?svcIde=ademd
	 * 매개변수 : RegVO regVO - 사용자가 선택한 시/군/구(혹은 읍/면/동)의 법정동코드 
	 * 반환값 : ResponseEntity (JSON 객체를 직접 출력, Content-Type과 인코딩 설정)
	 * 날짜 : 2020/07/31 (기능추가 : 2020/08/13)
	 * 작성자 : 최정규
	 */
	@RequestMapping("/regArea")
	public ResponseEntity<String> regArea(@ModelAttribute RegVO regVO) {
		log.info("[ regArea 시작 ] regVO => " + regVO);

		// 입력된 법정동 코드를 통해 법정동의 이름 가져오기
		String regcode = null;

		String regName = null;

		if(regVO != null) {
			regcode = regVO.getRegcode();
		}
		log.info("[i] regcode => " + regcode);

		if(regcode != null && regcode.length() == 10) {
			// 시/도 까지만 가리키는 코드라면 작동을 차단하기.
			String chkCitPro = regcode.substring(2, 5);
			log.info("[i] chkCitPro => " + chkCitPro);

			if("000".equals(chkCitPro)) {
				regcode = null;
			}else {
				// 리 까지 가리키는 코드라면 읍/면/동까지만 가리키게 하기
				regcode = regcode.substring(0, 8) + "00";

				// 법정동 DB에서 데이터 가져오기
				RegVO rvo = new RegVO();
				rvo.setRegcode(regcode);

				List<RegVO> regList = regService.regListSelect(rvo);

				if(regList != null && regList.size() > 0) {
					RegVO regOne = regList.get(0);

					if(regOne != null) {
						regName = regOne.getRegname();
					}
				}
			}
		}
		log.info("[i] regName => " + regName);

		// 국토 교통부의 오픈 api를통해 영역정보를 JSON형식으로 받아오기
		JSONObject apiData = null;

		if(regName != null && regName.length() > 0) {
			// VO에 법정동 이름을 담기
			RegVO rvo = new RegVO();
			rvo.setRegname(regName);

			apiData = regService.getRegArea(rvo);
		}

		// DB에서 동네별 상품개수 가져오기
		List<GeneralCountVO> countList = null;
		if(apiData!= null && regcode != null && regcode.length() > 0) {
			// VO에 법정동 코드 담기
			RegVO rvo = new RegVO();
			rvo.setRegcode(regcode);

			countList = generalService.generalSelectCount(rvo);
		}
		log.info("[i] countList => " + countList);

		//JSON데이터가 너무 길어서 로그에 그대로 출력시 콘솔에 표시된 다른 로그를 지워버림
		//log.info("[i] apiData => " + apiData);
		log.info("[i] apiData => " + (apiData != null && apiData.size() > 0));

		// 받아온 데이터중에서 status, bbox, features만 추출해서
		// 각각 status, boundary, areaData로 이름을 바꾼뒤, JSON형식으로 담기
		// DB에서 가져온 동네별 상품개수는 generalData에 담기

		// 결과값
		JSONObject result = new JSONObject();

		// 오류값
		String resStat = "ERROR";

		try {

			// JSON데이터 : response.result.featureCollection
			JSONObject featureCollection = null;

			if(apiData != null && apiData.size() > 0) {
				// JSON데이터 : response
				JSONObject resObj = (JSONObject)apiData.get("response");

				if(resObj != null) {
					// JSON데이터 : response.status
					String jsonStat = (String)resObj.get("status");
					log.info("[i] response.status => " + jsonStat);

					if(jsonStat != null) {
						resStat = jsonStat;

						//response.status가 "OK"라면
						if("OK".equals(resStat)) {
							// JSON데이터 : response.result
							JSONObject resultData = (JSONObject)resObj.get("result");

							if(resultData != null) {
								// JSON데이터 : response.result.featureCollection
								featureCollection = (JSONObject)resultData.get("featureCollection");
							}
						}
					}
				}
			}

			result.put("status", resStat);

			if(featureCollection != null && featureCollection.size() > 0) {
				// JSON데이터 : response.result.featureCollection.bbox
				JSONArray bbox = (JSONArray)featureCollection.get("bbox");

				if(bbox != null && bbox.size() > 0) {
					result.put("boundary", bbox);
				}

				// JSON데이터 : response.result.featureCollection.features
				JSONArray features = (JSONArray)featureCollection.get("features");

				if(features != null && features.size() > 0) {
					result.put("areaData", features);
				}

				//DB에서 가져온 동네별 상품개수를 generalData에 담기
				if("OK".equals(resStat)) {
					JSONObject generalData = new JSONObject();

					if(countList != null && countList.size() > 0) {
						for(int i = 0; i < countList.size(); i++) {
							GeneralCountVO gcvo = countList.get(i);

							if(gcvo != null
									&& gcvo.getRegcode() != null
									&& gcvo.getRegcode().length() == 10) {
								JSONObject gcObj = new JSONObject();

								gcObj.put("saja", gcvo.getSaja());
								gcObj.put("panda", gcvo.getPanda());

								generalData.put("reg_" + gcvo.getRegcode(), gcObj);
							}
						}
					}

					log.info("[i] generalData => " + generalData);

					result.put("generalData", generalData);
				}
			}
		} catch (Exception e) {
			log.info("[!] JSON처리 오류! => " + e.getMessage());
		}

		HttpHeaders resHeader = new HttpHeaders();
		resHeader.add("Content-Type", "application/json; charset=UTF-8");

		ResponseEntity<String> resEntity = CommonUtil.responseJSON(result.toJSONString());

		// JSON데이터가 너무 길어서 로그에 그대로 출력시 콘솔에 표시된 다른 로그를 지워버림
		// log.info("[ regArea 종료 ] resEntity => " + resEntity);
		log.info("[ regArea 종료 ] result => " + (result != null && result.size() > 0));
		return resEntity;
	}

	/**
	 * 설명
	 * : 지도로 보기 기능
	 * 매개변수 : 없음
	 * 반환값 : String - 뷰
	 * 날짜 : 2020/08/13
	 * 작성자 : 최정규
	 */
	@RequestMapping("/regAreaMap")
	public String regAreaMap() {
		return VIEW_DIR + "regAreaMap";
	}

	/**
	 * 설명
	 * : 동네별 조회기능
	 * 매개변수 : GeneralVO gvo - 필수 : 법정동 코드(없으면  상품목록 표시안함), 현재 페이지(없으면 1페이지)
	 *                            선택 : 제목, 분류, 시작일, 종료일 
	 * 반환값 : ModelAndView
	 * 날짜 : 2020/08/13
	 * 작성자 : 최정규
	 */
	@RequestMapping("/local")
	public ModelAndView generalLocalList(@ModelAttribute GeneralVO gvo) {
		log.info("[ generalLocalList 시작 ] gvo => " + gvo);

		// 페이징
		String groupSize = "5";
		String pageSize = "16";

		// 법정동 코드 / 현재페이지
		String gregcode = null;
		String curpage = "1";
		if(gvo != null) {
			gregcode = gvo.getGregcode();
			
			String cpage = gvo.getCurpage();
			if(cpage != null && cpage.length() > 0) {
				curpage = cpage;
			}
		}
		log.info("[i] gregcode => " + gregcode);
		log.info("[i] curpage  => " + curpage);

		// 법정동 코드가 있을때 상품목록 가져오기 (+ 법정동 코드)
		List<GeneralVO> list = null;
		RegVO regInfo = null;

		if(gregcode != null && gregcode.length() > 0) {
			gvo.setGregcode(gregcode);
			gvo.setCurpage(curpage);
			gvo.setGroupsize(groupSize);
			gvo.setPagesize(pageSize);

			RegVO rvo = new RegVO();
			rvo.setRegcode(gregcode);

			list = generalService.generalSelectLocal(gvo);
			List<RegVO> regInfoList = regService.regListSelect(rvo);

			if(regInfoList != null && regInfoList.size() > 0) {
				regInfo = regInfoList.get(0);
			}
		}
		log.info("[i] list    => " + list);
		log.info("[i] regInfo => " + regInfo);

		ModelAndView mav = new ModelAndView();
		mav.addObject("generalList", list);
		mav.addObject("regInfo", regInfo);
		mav.addObject("gvo", gvo);
		mav.setViewName(VIEW_DIR + "generalLocalList");

		log.info("[ generalLocalList 종료 ] mav => " + mav);
		return mav;
	}
}