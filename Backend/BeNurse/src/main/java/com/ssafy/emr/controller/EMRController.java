package com.ssafy.emr.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.ssafy.PatientWard.model.PatientWard;
import com.ssafy.PatientWard.service.PatientWardRepository;
import com.ssafy.PatientWard.service.PatientWardService;
import com.ssafy.common.utils.APIResponse;
import com.ssafy.emr.model.CC;
import com.ssafy.emr.model.Journal;
import com.ssafy.emr.model.Patient;
import com.ssafy.emr.model.PatientRequest;
import com.ssafy.emr.model.PatientResponse;
import com.ssafy.emr.model.PatientWardResponse;
import com.ssafy.emr.service.EMRService;
import com.ssafy.emr.utils.JournalSearchCondition;
import com.ssafy.hospital.model.Hospital;
import com.ssafy.hospital.service.HospitalService;
import com.ssafy.hospital.service.WardService;
import com.ssafy.nurse.model.Nurse;
import com.ssafy.oauth.serivce.OauthService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;


@CrossOrigin(origins = "*")
@Api(value = "EMR API", tags = { "EMR." })
@RestController
@RequestMapping("/api/benurse/emr")
@Slf4j
public class EMRController {

	@Autowired
	EMRService emrService;
	
	@Autowired
	HospitalService hospitalServ;
	
	@Autowired
	WardService wardServ;
	
	@Autowired
	PatientWardRepository pwRepo;
	
	@Autowired
	PatientWardService pwServ;
	
	@Autowired
	OauthService oauthService;
	
	/* 간호일지 Journal */
	
	// 모든 간호일지 조회 GET
	@GetMapping("/journal/all")
	@ApiOperation(value = "모든 간호일지 조회", notes = "모든 간호일지 정보를 조회한다.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "등록 성공", response = Journal.class),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public APIResponse<List<Journal>> getAllJournal() {
		APIResponse<List<Journal>> journals = emrService.getAllJournal();

		return journals;
		//return emrService.getAllJournal();
	}
	
	// 간호일지 정보 조회 GET
	@GetMapping("/journal")
	@ApiOperation(value = "간호일지 정보 조회", notes = "<strong>간호일지 ID</strong>를 통해 간호일지 정보를 조회한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공", response = Journal.class),
		@ApiResponse(code = 404, message = "결과 없음"),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public APIResponse<Journal> getJournalByID(@RequestParam("id") long id){
		return emrService.getJournalByID(id);
	}
	
	// 간호일지 정보 등록 POST
	@PostMapping("/journal")
	@ApiOperation(value = "간호일지 정보 등록", notes = "<strong>간호일지 객체</strong>를 통해 간호일지 정보를 등록한다.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "등록 성공", response = Journal.class),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public APIResponse<Journal> registJournalById(@RequestHeader("Authorization") String token, @RequestBody Journal journal) {
		log.info(journal.toString());
		Nurse nurse;
		// 사용자 조회
		try {
			nurse = oauthService.getUser(token);
		}catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		journal.setWriterID(nurse.getID());
		journal.setName(nurse.getName());
		return emrService.registJournalById(journal);
	}

	// 환자 간호일지 삭제 DELETE
	@DeleteMapping("/journal/patient")
	@ApiOperation(value = "환자 간호일지 삭제", notes = "<strong>환자 ID</strong>로 환자에 대한 간호일지들을 삭제한다.")
	@ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 404, message = "결과 없음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public APIResponse<Void> deleteJournalByPatientID(@RequestParam("id") long patient_id){
		return emrService.deleteJournalByPatientID(patient_id);
	}

	// 간호일지 정보 수정 PUT
	@PutMapping("/journal")
	@ApiOperation(value = "간호일지 정보 수정", notes = "간호일지 정보를 수정한다.") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 404, message = "결과 없음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public APIResponse<Void> updateJournal(@RequestBody Journal journal){
		return emrService.updateJournal(journal);
	}

	// 간호일지 삭제 DELETE
	@DeleteMapping("/journal")
	@ApiOperation(value = "간호일지 정보 삭제", notes = "<strong>간호일지 ID</strong>로 간호일지 정보를 삭제한다.")
	@ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 404, message = "결과 없음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public APIResponse<Void> deleteJournal(@RequestParam("id") long id){
		return emrService.deleteJournal(id);
	}
	
	// 간호일지 검색 POST
	@PostMapping("/journal/search")
	@ApiOperation(value = "간호일지 검색", notes = "조건에 따른 간호일지 정보를 검색한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공", response = List.class),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public APIResponse<List<Journal>> searchJournal(@RequestBody JournalSearchCondition search){
		return emrService.searchJournal(search);
	}
	

	/* 주호소 CC */
	
	// 주호소 등록 POST
	@PostMapping("/cc")
	@ApiOperation(value = "주호소 등록", notes = "<strong>주호소 객체</strong>를 통해 주호소를 등록한다.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "등록 성공"),
		@ApiResponse(code = 500, message = "서버 오류") 
	})
	public APIResponse<CC> registPatientById(@RequestBody CC cc) {
		return emrService.registPatientById(cc);
	}

	// 주호소 삭제 DELETE
	@DeleteMapping("/cc")
	@ApiOperation(value = "주호소 삭제", notes = "<strong>주호소 ID</strong>로 주호소를 삭제한다.")
		@ApiResponses({ @ApiResponse(code = 200, message = "성공"), 
		@ApiResponse(code = 404, message = "결과 없음"),
		@ApiResponse(code = 500, message = "서버 오류") 
	})
	public APIResponse<Void> deleteCc(@RequestParam("id") long id) {
		return emrService.deleteCc(id);
	}

	/* 환자 등록 Patient */
	
	// 환자 정보 등록 POST
	@PostMapping("/patient")
	@ApiOperation(value = "환자 정보 등록", notes = "<strong>환자 객체</strong>를 통해 환자 정보를 등록한다.")
	@ApiResponses({ 
		@ApiResponse(code = 201, message = "등록 성공", response = Patient.class),
		@ApiResponse(code = 500, message = "서버 오류") })
	public APIResponse<Void> registPatientById(@RequestBody PatientRequest patientRequest) {
		log.info(patientRequest.toString());
		try {
			Patient patient = new Patient();
			patient.setID(patientRequest.getID());
			patient.setName(patientRequest.getName());
			patient.setAge(patientRequest.getAge());
			patient.setGender(patientRequest.getGender());
			patient.setImg(patientRequest.getImg());
			patient.setDisease(patientRequest.getDisease());
			patient.setSurgery(patientRequest.getSurgery());
			patient.setHospitalization(patientRequest.getHospitalization());
			patient.setDischarge(patientRequest.getDischarge());
			patient.setHistory(patientRequest.getHistory());
			patient.setMedicine(patientRequest.getMedicine());
			patient.setDrinking(patientRequest.isDrinking());
			patient.setSmoking(patientRequest.isSmoking());
			patient.setAlergy(patientRequest.getAlergy());
			patient.setSelfmedicine(patientRequest.getSelfmedicine());
			patient.setCcMain(patientRequest.getCcMain());
			
			APIResponse<Patient> resp = emrService.registPatientById(patient);
			
			PatientWard pw = new PatientWard();
			pw.setHospitalID(patientRequest.getHospitalID());
			pw.setWardID(patientRequest.getWardID());
			pw.setID(resp.getResponseData().getID());
			pw.setHospitalized(true);
			
			pwRepo.save(pw);
			
			return new APIResponse<>(HttpStatus.OK);
		}catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
	}

	// 환자 정보 조회 GET
	@GetMapping("/patient")
	@ApiOperation(value = "환자 정보 조회", notes = "<strong>환자 ID</strong>를 통해 환자 정보를 조회한다.")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "성공", response = PatientWardResponse.class),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public APIResponse<PatientWardResponse> getPatientById(@RequestParam("id") long id) {
		PatientWardResponse pwr = new PatientWardResponse();
		try {
			// EMR에서 환자 조회
			APIResponse<PatientResponse> pr = emrService.getPatientById(id);
			if(pr.getResponseData() == null) {
				log.error("not found patient (id:" + id+")");
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);
			}
			pwr.setPatient(pr.getResponseData());
			
			// 환자 병동 정보 조회
			PatientWard pw = pwServ.findById(id);
			pwr.setHospital(hospitalServ.findById(pw.getHospitalID()));
			pwr.setWard(wardServ.findById(pw.getWardID()));
			return new APIResponse(pwr, HttpStatus.OK);
		}catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 모든 환자 조회 GET
	@GetMapping("/patient/all")
	@ApiOperation(value = "모든 환자 조회", notes = "소속 병원 내 모든 환자 정보를 조회한다.")
	@ApiResponses({ @ApiResponse(code = 200, message = "성공", response = List.class),
			@ApiResponse(code = 500, message = "서버 오류") })
	public APIResponse<List<PatientWardResponse>> getAllPatient(@RequestHeader("Authorization") String token) {
		Nurse nurse;
		// 사용자 조회
		try {
			nurse = oauthService.getUser(token);
		}catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		
		List<PatientWardResponse> resp = new ArrayList<>();
		List<PatientWard> pwlist = pwRepo.findAllByHospitalIDAndIsHospitalized(nurse.getHospitalID(), true);
		
		Hospital hospital;
		try {
			hospital = hospitalServ.findById(nurse.getHospitalID());
		}catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		
		for(PatientWard pw : pwlist) {
			try {
				PatientWardResponse pwr = new PatientWardResponse();
				APIResponse<PatientResponse> pr = emrService.getPatientById(pw.getID());
				if(pr.getResponseData() == null) {
					log.error("not found patient (id:" + pw.getID()+")");
					continue;
				}
				pwr.setPatient(pr.getResponseData());
				pwr.setHospital(hospital);
				pwr.setWard(wardServ.findById(pw.getWardID()));
				resp.add(pwr);
			}catch (Exception e) {
				log.error("not valid patient (id:"+pw.getID()+")");
			}
		}
		
		return new APIResponse(resp, HttpStatus.OK);
	}
	
	// 병동 내 환자 조회 GET
	@GetMapping("/patient/wardall")
	@ApiOperation(value = "병동 내 모든 환자 조회", notes = "소속 병동 내 모든 환자 정보를 조회한다.")
	@ApiResponses({ @ApiResponse(code = 200, message = "성공", response = List.class),
	@ApiResponse(code = 500, message = "서버 오류") })
	public APIResponse<List<PatientWardResponse>> getAllPatientByWardID(@RequestHeader("Authorization") String token) {
		Nurse nurse;
		// 사용자 조회
		try {
			nurse = oauthService.getUser(token);
		}catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		
		List<PatientResponse> resp = new ArrayList<>();
		List<PatientWard> pwlist = pwRepo.findAllByHospitalIDAndIsHospitalizedAndWardID(nurse.getHospitalID(), true, nurse.getWardID());
		
		try {
			Hospital hospital = hospitalServ.findById(nurse.getHospitalID());
		}catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		
		for(PatientWard pw : pwlist) {
			try {
				APIResponse<PatientResponse> pr = emrService.getPatientById(pw.getID());
				if(pr.getResponseData() == null) {
					log.error("not found patient (id:" + pw.getID()+")");
					continue;
				}
				resp.add(pr.getResponseData());
			}catch (Exception e) {
				log.error("not valid patient (id:"+pw.getID()+")");
			}
		}
		
		return new APIResponse(resp, HttpStatus.OK);
	}

	// 환자 검색 GET
	@GetMapping("/patient/search")
	@ApiOperation(value = "환자 검색", notes = "이름으로 환자 정보를 검색한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공", response = List.class),
		@ApiResponse(code = 500, message = "서버 오류")
	})
	public APIResponse<List<PatientWardResponse>> searchPatient(String name){
		List<PatientWardResponse> pwrlist = new ArrayList<>();
		try {
			// EMR에서 환자 검색
			APIResponse<List<PatientResponse>> prlist = emrService.searchPatient(name);
			
			for(PatientResponse pr : prlist.getResponseData()) {
				PatientWardResponse pwr = new PatientWardResponse();
				pwr.setPatient(pr);
			
				try {
					// 환자 병동 정보 조회
					PatientWard pw = pwServ.findById(pwr.getPatient().getPatient().getID());
					pwr.setHospital(hospitalServ.findById(pw.getHospitalID()));
					pwr.setWard(wardServ.findById(pw.getWardID()));
					pwrlist.add(pwr);
				}catch (Exception e) {
					log.error("not valid patient (id:"+pwr.getPatient().getPatient().getID()+")");
				}
			}
			return new APIResponse(pwrlist, HttpStatus.OK);
		}catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 환자 정보 수정 PUT
	@PutMapping("")
	@ApiOperation(value = "환자 정보 수정", notes = "환자 정보를 수정한다.")
	@ApiResponses({ @ApiResponse(code = 200, message = "성공"), @ApiResponse(code = 404, message = "결과 없음"),
			@ApiResponse(code = 500, message = "서버 오류") })
	public APIResponse<Void> updatePatient(@RequestBody Patient patient) {
		return emrService.updatePatient(patient);
	}

	// 환자 정보 삭제 DELETE
	@DeleteMapping("")
	@ApiOperation(value = "환자 정보 삭제", notes = "<strong>환자 ID</strong>로 환자 정보를 삭제한다.")
	@ApiResponses({ 
		@ApiResponse(code = 200, message = "성공"), 
		@ApiResponse(code = 404, message = "결과 없음"),
		@ApiResponse(code = 500, message = "서버 오류") })
	public APIResponse<Void> deletePatient(@RequestParam("id") long id) {
		return emrService.deletePatient(id);
	}

}
