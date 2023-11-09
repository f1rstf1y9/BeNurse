package com.ssafy.Handover.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ssafy.Handover.model.Handover;
import com.ssafy.Handover.model.HandoverList;
import com.ssafy.Handover.model.HandoverSet;
import com.ssafy.Handover.model.MyHandover;
import com.ssafy.Handover.service.HandoverListRepository;
import com.ssafy.Handover.service.HandoverRepository;
import com.ssafy.Handover.service.HandoverService;
import com.ssafy.Handover.service.HandoverSetRepository;
import com.ssafy.Handover.service.HandoverSetService;
import com.ssafy.Handover.service.MyHandoverRepository;
import com.ssafy.Handover.service.MyHandoverService;
import com.ssafy.common.utils.APIResponse;
import com.ssafy.common.utils.IDRequest;
import com.ssafy.nurse.model.Nurse;
import com.ssafy.oauth.serivce.OauthService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "*")
@Api(value = "인계장 묶음 API", tags = { "인계장 묶음." })
@RestController
@RequestMapping("/api/benurse/HandoverSet")
@Slf4j
public class HandoverSetController {
	
	@Autowired
	HandoverSetRepository setRepo;
	
	@Autowired
	HandoverSetService setServ;
	
	@Autowired
	HandoverListRepository listRepo;
	
	@Autowired
	HandoverRepository handoverRepo;
	
	@Autowired
	HandoverService handoverServ;
	
	@Autowired
	MyHandoverRepository myhoRepo;
	
	@Autowired
	MyHandoverService myhoServ;
	
	@Autowired
	OauthService oauthService;
	
	
	// 인계장 묶음 생성 POST
	@PostMapping("")
	@ApiOperation(value = "인계장 묶음 생성", notes = "인계장 묶음 생성")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공", response = HandoverSet.class),
		@ApiResponse(code = 404, message = "결과 없음"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public APIResponse<HandoverSet> registHandover(@RequestHeader("Authorization") String token) {
		Nurse nurse;
		// 사용자 조회
		try {
			nurse = oauthService.getUser(token);
		}catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		HandoverSet handoverSet = new HandoverSet();
		handoverSet.setGiveID(nurse.getID());
		// 데이터베이스에 저장
	    HandoverSet savedHandoverSet = setRepo.save(handoverSet);

	    return new APIResponse<>(savedHandoverSet, HttpStatus.OK);
	}
	
	// 인계장 묶음 수정 PUT
	@PutMapping("")
	@ApiOperation(value = "인계장 묶음 수정", notes = "인계장 묶음 수정")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공", response = HandoverSet.class),
		@ApiResponse(code = 404, message = "결과 없음"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public APIResponse<HandoverSet> updateHandoverSetById(@RequestBody HandoverSet updatedHandoverSet) {
		try {
			// 업데이트된 인계장을 저장
			HandoverSet savedHandoverSet = setServ.save(updatedHandoverSet);
	        return new APIResponse<>(savedHandoverSet, HttpStatus.OK);
	    }catch (Exception e) {
	    	e.printStackTrace();
	    	throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	    }
	}

	// 인수자 인계장 조회 GET [인계자 ID]
	@GetMapping("/take")
	@ApiOperation(value = "인계장 묶음 조회 [인계자 ID]", notes = "인계자 ID를 통해 인계장 묶음 조회[인계묶음 리스트 조회]") 
	@ApiResponses({
	    @ApiResponse(code = 200, message = "성공", response = HandoverSet.class),
	    @ApiResponse(code = 404, message = "인계장을 찾을 수 없음"),
	    @ApiResponse(code = 500, message = "서버 오류")
	})
	public APIResponse<List<HandoverSet>> getHandoverSetByGiveId(@RequestHeader("Authorization") String token) {
		Nurse nurse;
		// 사용자 조회
		try {
			nurse = oauthService.getUser(token);
		}catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		
		List<HandoverSet> handoverSet = setRepo.findAllByGiveID(nurse.getID());

        return new APIResponse<>(handoverSet, HttpStatus.OK);
	}
	
	
	// 인계장 묶음 삭제 DELETE
	@DeleteMapping("")
	@ApiOperation(value = "인계장 묶음 삭제", notes = "인계장 묶음을 삭제한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공", response = HandoverSet.class),
		@ApiResponse(code = 404, message = "결과 없음"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public APIResponse<Void> deleteHandoverSetById(@RequestBody IDRequest req) {
		try {
	    	setServ.delete(req.getID());
			return new APIResponse<>(HttpStatus.OK);
	    }catch (Exception e) {
	    	e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	    }
	}  

	// 인수자 인계장 조회 GET [인계자 ID]
	@GetMapping("/details")
	@ApiOperation(value = "인계장 묶음 내역 조회", notes = "인계장 묶음 ID를 통해 인계장 묶음에 포함된 인계장 조회") 
	@ApiResponses({
	    @ApiResponse(code = 200, message = "성공", response = HandoverSet.class),
	    @ApiResponse(code = 404, message = "인계장을 찾을 수 없음"),
	    @ApiResponse(code = 500, message = "서버 오류")
	})
	public APIResponse<List<Handover>> getDetail(@RequestHeader("Authorization") String token, @RequestParam("ID") long ID){

		Nurse nurse;
		// 사용자 조회
		try {
			nurse = oauthService.getUser(token);
		}catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		
		// 읽음 여부 갱신
		try {
			HandoverSet handoverSet = setServ.findById(ID);
	    	Optional<MyHandover> optionmh = myhoRepo.findBySetIDAndTakeIDAndReaded(ID, nurse.getID(), false);
	    	
	    	//
	    	if(optionmh.isPresent()) {
	    		MyHandover mh = optionmh.get();
	    		mh.setReaded(true);
	    		myhoServ.save(mh);
	    	}
	    }catch (Exception e) {
	    	throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	    }
		
		List<HandoverList> list = listRepo.findAllBySetID(ID);
		List<Handover> resp = new ArrayList<>();
		for(HandoverList l : list) {
			try {
				Handover handover = handoverServ.findById(l.getHandoverID());
				resp.add(handover);
			}catch (Exception e) {
				log.error("not found handover (id:"+ l.getHandoverID() +")");
			}
		}
		return new APIResponse<>(resp, HttpStatus.OK);
	}
}
