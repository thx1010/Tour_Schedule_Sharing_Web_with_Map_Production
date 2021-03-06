package net.developia.board.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;
import net.developia.board.dto.MapDTO;
import net.developia.board.dto.PageDTO;
import net.developia.board.dto.PlaceDTO;
import net.developia.board.dto.PointChargeDTO;
import net.developia.board.dto.PointDTO;
import net.developia.board.dto.ReviewDTO;
import net.developia.board.dto.RouteDTO;
import net.developia.board.dto.ThemeDTO;
import net.developia.board.dto.UserDTO;
import net.developia.board.service.ChatService;
import net.developia.board.service.MapService;
import net.developia.board.service.PointService;
import net.developia.board.service.ReviewService;
import net.developia.board.service.ThemeService;

@Slf4j
@Controller
@RequestMapping("/mapcontrol")
public class MapController {
	@Autowired
	private MapService mapService;
	
	@Autowired
	private ChatService chatService;
	
	@Autowired
	private ThemeService themeService;
	
	@Autowired
	private PointService pointService;
	
	@Autowired
	private ReviewService reviewService;
	
	@RequestMapping(value="", method=RequestMethod.GET)
	public String dynamicMarker() {
		return "maker";
	}
	
	@RequestMapping(value="/position", method=RequestMethod.POST)
	@ResponseBody
	public String getPosition(@RequestParam Map<String, Object> position) {
		log.info("[?????????]" + (String)position.get("lat") + " , " + (String)position.get("lng"));
		log.info("[?????????]" + (String)position.get("loadAddr"));
		log.info("[??????]" + (String)position.get("addr"));
		return "success";
	}
	
	@RequestMapping(value="/coursemake", method=RequestMethod.POST)
	@ResponseBody
	public String getAllData(@RequestBody String mapData, HttpSession session) {
		//log.info(mapData);
		
		JSONObject jObject = new JSONObject(mapData);
		log.info("[jObject] :: " + jObject.toString());
		List<PlaceDTO> placeList = new ArrayList<PlaceDTO>();
		List<RouteDTO> routeList = new ArrayList<RouteDTO>();
		JSONArray jPlaceInfos = jObject.getJSONArray("placeInfos");
		JSONArray jRouteInfos = jObject.getJSONArray("routeInfos");
		JSONObject jMapInfo = jObject.getJSONObject("mapInfo");
		
		log.info("[jPlaceInfos] :: " + jPlaceInfos.toString());
		log.info("[jRouteInfos] :: " + jRouteInfos.toString());
		log.info("[mapInfo] : " + jMapInfo.toString());
				
		MapDTO mapDTO = new MapDTO();
		mapDTO.setMap_title(jMapInfo.getString("map_title"));
		mapDTO.setMap_subtitle(jMapInfo.getString("map_subtitle"));
		mapDTO.setMap_country(jMapInfo.getString("map_country"));
		mapDTO.setMap_state(jMapInfo.getString("map_state"));
		mapDTO.setMap_city(jMapInfo.getString("map_city"));
		mapDTO.setMap_content(jMapInfo.getString("map_content"));
		mapDTO.setMap_photo(jMapInfo.getString("map_photo"));
		mapDTO.setMap_placett(jPlaceInfos.length());
		log.info("[mapInfoverify] : " + mapDTO.toString());
		try {
			ThemeDTO themeDTO = themeService.getThemeForThemeNo(jMapInfo.getLong("theme_no"));
			mapDTO.setThemeDTO(themeDTO);
			log.info(themeDTO.toString());
		} catch (JSONException e) {
			log.info(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info(e.getMessage());
		}
		
		
	
		try {
			long mapNo = mapService.getNextMapNo();
			
			mapDTO.setMap_no(mapNo);
			/*
			 * map_status
			 * [0] : ??????????????????(????????? default)
			 * [1] : ??????????????????
			 * [2] : ??????????????????
			 * [3] : ??????????????????
			 */
			log.info("???????????????");
			mapService.addMap(mapDTO);
			//?????? mapDTO?????? ??? ????????? ??????...! ??? ????????????
			//chatService.addChatRoom(mapDTO);
			log.info("????????????");
			for(int i=0; i<jPlaceInfos.length(); i++) {
				JSONObject obj = jPlaceInfos.getJSONObject(i);
				PlaceDTO placeDTO = new PlaceDTO();
				placeDTO.setPlace_name(obj.getString("place_name"));
				placeDTO.setPlace_lat(obj.getString("place_lat"));
				placeDTO.setPlace_lng(obj.getString("place_lng"));
				placeDTO.setPlace_loadaddr(obj.getString("place_loadaddr"));
				placeDTO.setPlace_addr(obj.getString("place_addr"));
				placeDTO.setPlace_photo(obj.getString("place_photo"));
				placeDTO.setPlace_content(obj.getString("place_content"));
				placeDTO.setPlace_course_no(obj.getLong("place_course_no"));
				placeDTO.setMap_no(mapNo);
				
				placeList.add(placeDTO);
				log.info("[" + i + "??????place]" + placeDTO.toString());
				
				
			}
			
			for(int i=0; i<jRouteInfos.length(); i++) {
				JSONObject obj = jRouteInfos.getJSONObject(i);
				RouteDTO routeDTO = new RouteDTO();
				routeDTO.setRoute_name(obj.getString("route_name"));
				routeDTO.setRoute_path(obj.getString("route_path"));
				routeDTO.setRoute_type(obj.getString("route_type"));
				routeDTO.setRoute_course_no(obj.getLong("route_course_no"));
				routeDTO.setRoute_content(obj.getString("route_content"));
				routeDTO.setRoute_option(obj.getString("route_option"));
				routeDTO.setMap_no(mapNo);
				
				routeList.add(routeDTO);
				log.info("[" + i + "??????route]" + routeDTO.toString());
			}
			for(PlaceDTO place : placeList) {
				mapService.addPlace(place);
			}
			
			for(RouteDTO route : routeList) {
				mapService.addRoute(route);
			}
			
			
			Map<String, Object> register = new HashMap<String, Object>();
			UserDTO userDTO = (UserDTO)session.getAttribute("userInfo");
			register.put("userInfo", userDTO);
			register.put("mapInfo", mapDTO);
			mapService.addRegister(register);
			/*????????? ??????*/
			UserDTO userno = mapService.getUserno(mapDTO);
			PointDTO pointDTO = pointService.selectPointInfo(userno);
			pointDTO.setUserDTO(userno);
			log.info(pointDTO.toString());
			pointService.updateUserPointno(pointDTO);
			
			PointChargeDTO pointchargeInfo = new PointChargeDTO();
			pointchargeInfo.setPoint_increase(5000);
			pointchargeInfo.setPoint_charge_log("[ ????????? ?????? ] ?????? ?????????");
			pointchargeInfo.setPointDTO(pointDTO);
			log.info(pointchargeInfo.toString());
			
			pointService.insertPointCharge(pointchargeInfo);
			pointService.updatePointIncrease(pointchargeInfo);
			/*????????? ?????? ???*/
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//redirect??? url????????????
		return Long.toString(mapDTO.getMap_no());
	}

	
	/*
	@RequestMapping(value="/coursemake", method=RequestMethod.POST)
	@ResponseBody
	public String getAllData(@RequestParam(value="placeInfos[]") List<Map<String, Object>> placeInfos,
			@RequestParam(value="mapInfo") Map<String, Object> mapInfo) {
		
		
		log.info("[????????????]\n");
		log.info(mapInfo.toString());
		
		
		log.info("[???????????????]\n");
		for(Map<String, Object> place : placeInfos) {
			log.info(place.toString());
		}
		
		
		for(Map<String, Object> map : resultMap) {
			log.info("[???????????????] : " + map.get("place_lat") + "," + map.get("place_lng"));
			log.info("[?????????]" + (String)map.get("place_loadaddr"));
			log.info("[??????]" + (String)map.get("place_addr"));
			log.info("[??????]" + (String)map.get("place_name"));
			log.info("[??????]" + map.get("place_course_no"));
			log.info("[?????????]" + (String)map.get("place_photo"));
			log.info("[??????]" + (String)map.get("place_content"));
			
			//mapService.makeCourse();
			
		}
		
		return "success";
	}
	*/
	
	@GetMapping("/coursedraw/{mapno}")
	public ModelAndView courseDraw(@PathVariable("mapno") String mapno, HttpSession session) {
		ModelAndView mav = new ModelAndView();
		log.info("[drawer map??????] : " + mapno);
		
		UserDTO userDTO = (UserDTO)session.getAttribute("userInfo");
		if(userDTO == null) {
			mav.setViewName("result");
			mav.addObject("msg", "??????????????????");
			mav.addObject("url", "/board/login");
			return mav;
		} //else ??? ????????? ?????? ??????????????? ?????? ??????
		
		
		try {
			MapDTO mapDTO = mapService.getMapForMapNo(Long.parseLong(mapno));
			log.info(mapDTO.toString());
			List<PlaceDTO> placeList = mapService.getPlaceListForMapNo(Long.parseLong(mapno));
			List<RouteDTO> routeList = mapService.getRouteListForMapNo(Long.parseLong(mapno));
			
			UserDTO userno = mapService.getUserno(mapDTO);
			UserDTO userInfo = mapService.getUserList(userno);
			log.info(userInfo.toString());
			
			mav.setViewName("coursedrawer");
			mav.addObject("userInfo", userInfo);
			mav.addObject("mapInfo", mapDTO);
			mav.addObject("placeList", placeList);
			mav.addObject("routeList", routeList);
			log.info(placeList.toString());
			log.info(routeList.toString());
		} catch(Exception e) {
			log.info(e.getMessage());
			mav.setViewName("result");
			mav.addObject("msg", "???????????? ??????????????? ??????????????????");
			mav.addObject("url", "redirect:main");
		}
	
		return mav;
	}
	
	@RequestMapping(value="/coursedraw/{mapno}/{userno}/{pageno}", method=RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> saveReview(@PathVariable("mapno") long mapno, @PathVariable("userno") long userno, @PathVariable("pageno") long pageno, @RequestParam Map<String, Object> reviewData) {
		//add review
		Map<String, Object> result = new HashMap<String, Object>();
		ReviewDTO reviewDTO = new ReviewDTO();
		Boolean isLoad = false;
		reviewDTO.setMap_no(mapno);
		reviewDTO.setUser_no(userno);
		reviewDTO.setReview_rate(Long.parseLong((String)reviewData.get("rating")));
		reviewDTO.setReview_content((String)reviewData.get("content"));
		
		isLoad = Boolean.parseBoolean((String)reviewData.get("load"));
		try {
			if(reviewService.checkMyReview(reviewDTO) == 1) {
				if(isLoad) {
					Map<String, Object> myInfo = new HashMap<String, Object>();
					myInfo.put("user_no", userno);
					myInfo.put("map_no", mapno);
					reviewDTO = reviewService.getMyReview(myInfo);
				} else {
					reviewService.updateReview(reviewDTO);
				}
			} else {
				reviewService.addReview(reviewDTO);
			}
		} catch(Exception e) {
			log.info(e.getMessage());
			result.put("result", "fail");
			result.put("msg", "?????? ?????? ??????");
			return result;
		}
		
		//review return
		PageDTO pageDTO = new PageDTO();
		long count = 0;
		try {	
			count = reviewService.countReviewForMap(mapno);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", "?????? ????????? ??????");
			return result;
		}
		
		pageDTO.setTotalArticle(count);		//????????? ?????? ??? data ??????
		pageDTO.setEachArticlePerPage(5);	//??? ???????????? data ??????
		pageDTO.setPageBlock(10);			//????????? ????????? ??????
		pageDTO.setCurrentPage(pageno);		//?????? ?????????
		pageDTO.calculatePage();
		
		Map<String, Object> param = new HashMap<String, Object>();
		//param.put("user_no", userno);
		param.put("map_no", mapno);
		param.put("page", pageDTO);
		//log.info("[ReviewSys] user_no {}", userno);
		//log.info("[ReviewSys] map_no {}", mapno);
		//log.info("[ReviewSys] page_no {}", pageno);
		//log.info("[ReviewSys] review data {}", reviewData.toString());
		
		List<ReviewDTO> reviewList = null;
		try {
			reviewList = reviewService.getReviewList(param);
			log.info("[ReviewSys] reviewList : {}", reviewList.toString());
		} catch(Exception e) {
			log.info(e.getMessage());
			result.put("result", "fail");
			result.put("msg", "?????? ???????????? ??????");
			return result;
		}
		
		
		
		
		result.put("reviewList", reviewList);
		result.put("myReview", reviewDTO);
		result.put("page", pageDTO);
		result.put("result", "success");
		result.put("msg", "?????? ?????? ??????");
		return result;
	}
	
	
}
