package org.egov.bpa.web.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.validation.Valid;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.service.BPAService;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.util.ResponseInfoFactory;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPAResponse;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.RequestInfoWrapper;
import org.egov.bpa.web.models.edcr.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.jayway.jsonpath.JsonPath;

@Controller
public class BPAController {

	@Autowired
	private BPAService bpaService;

	@Autowired
	private BPAUtil bpaUtil;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;
	
	@Autowired
	BPAConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
	private BPAUtil util;
	
	@PostMapping(value = "/_create")
	public ResponseEntity<BPAResponse> create(@Valid @RequestBody BPARequest bpaRequest) {
		bpaUtil.defaultJsonPathConfig();
		BPA bpa = bpaService.create(bpaRequest);
		List<BPA> bpas = new ArrayList<BPA>();
		bpas.add(bpa);
		BPAResponse response = BPAResponse.builder().BPA(bpas)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(bpaRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping(value = "/_update")
	public ResponseEntity<BPAResponse> update(@Valid @RequestBody BPARequest bpaRequest) {
		BPA bpa = bpaService.update(bpaRequest);
		List<BPA> bpas = new ArrayList<BPA>();
		bpas.add(bpa);
		BPAResponse response = BPAResponse.builder().BPA(bpas)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(bpaRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@PostMapping(value = "/_search")
	public ResponseEntity<BPAResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute BPASearchCriteria criteria) {

		List<BPA> bpas = bpaService.search(criteria, requestInfoWrapper.getRequestInfo());

		BPAResponse response = BPAResponse.builder().BPA(bpas).responseInfo(
				responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping(value = "/_permitorderedcr")
	public ResponseEntity<Resource> getPdf(@Valid @RequestBody BPARequest bpaRequest) {

		Path path = Paths.get(BPAConstants.EDCR_PDF);
		Resource resource = null;

		bpaService.getEdcrPdf(bpaRequest);
		try {
			resource = new UrlResource(path.toUri());
		} catch (Exception ex) {
			throw new CustomException("UNABLE_TO_DOWNLOAD", "Unable to download the file");
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}
	
	
	@SuppressWarnings({ "rawtypes", "null", "unchecked" })
	@PostMapping(value = "/_edcrdata")
	public ResponseEntity<Resource> getEdcr(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute BPASearchCriteria criteria) {

		// getting data from edcr nos and saving both the edcr details in array
		// with name data

		List<String> num = criteria.getEdcrNumbers();
		RequestInfo edcrRequestInfo = new RequestInfo();
		// LinkedHashMap response = null;
		ArrayList<LinkedHashMap<String, Object>> data = new ArrayList<LinkedHashMap<String, Object>>();
		num.forEach(edcrNo -> {
			StringBuilder uri = new StringBuilder(config.getEdcrHost());
			uri.append(config.getGetPlanEndPoint());
			uri.append("?").append("tenantId=").append(criteria.getTenantId());
			uri.append("&").append("edcrNumber=").append(edcrNo);
			try {
				LinkedHashMap response = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
						new org.egov.bpa.web.models.edcr.RequestInfoWrapper(edcrRequestInfo));
				data.add(response);

			} catch (Exception e) {
				e.printStackTrace();
			}

		});

		// using variables declaration

		ArrayList<Integer> NoOfFloors = new ArrayList<Integer>();
		ArrayList<Double> BuildingHeight = new ArrayList<Double>();
		ArrayList<Double> CarpetArea = new ArrayList<Double>();
		ArrayList<Double> FloorArea = new ArrayList<Double>();
		ArrayList<Double> BuildingArea = new ArrayList<Double>();
		Integer NoOfFloorsDiff = null;
		Double BuildingHeightDiff = null;
		Double CarpetAreaDiff = null;
		Double FloorAreaDiff = null;
		Double BuildingAreaDiff = null;

		// Getting data from mdms and saving it in a property with name result

		org.egov.common.contract.request.RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();
		String tenantId = criteria.getTenantId();
		MdmsCriteriaReq mdmsCriteriaReq = util.getMDMSRequest(requestInfo, tenantId);
		Object result = serviceRequestRepository.fetchResult(util.getMdmsSearchUrl(), mdmsCriteriaReq);
		System.out.println(result);

		ArrayList OcData = new ArrayList();
		OcData.add(JsonPath.read(result, "$.MdmsRes.oc-calc.OCCalcDiff"));
		System.out.println(OcData.get(0));
		OcData = (ArrayList) OcData.get(0); // total 5 properties data objets

		// After getting the data of 2 edcr nos need to get the mdms data
		if (data.size() >= 2) {

			for (int k = 0; k < data.size(); k++) {

				ArrayList BlocksCount = new ArrayList();
				BlocksCount = JsonPath.read(data.get(k), "$.edcrDetail[0].planDetail.blocks");

				if (BlocksCount.size() > 1) {

					String blockpath = BPAConstants.BlocksCount;
					blockpath = blockpath.replace("*", String.valueOf(k));
					BlocksCount.add(JsonPath.read(data.get(k), blockpath));
				}
				// int j = BlocksCount.size();
				// System.out.println(j);

				// if BlocksCount is greater than 1 then need to run this loop
				for (int j = 0; j < BlocksCount.size(); j++) {

					ArrayList<String> ocdata = new ArrayList<String>();
					for (int i = 0; i < OcData.size(); i++) {
						ocdata.add(JsonPath.read(OcData.get(i), "$.name"));
					}
					for (int p = 0; p < ocdata.size(); p++) {
						String mdmsPath = JsonPath.read(OcData.get(p), "$.jsonPath");
						mdmsPath = mdmsPath.replace("*", String.valueOf(j));
						System.out.println(mdmsPath);
						if (ocdata.get(p).equalsIgnoreCase("NoOfFloors")) {

							NoOfFloors.add(JsonPath.read(data.get(k), mdmsPath));
						} else if (ocdata.get(p).equalsIgnoreCase("BuildingHeight")) {
							BuildingHeight.add(JsonPath.read(data.get(k), mdmsPath));
						} else if (ocdata.get(p).equalsIgnoreCase("CarpetArea")) {
							CarpetArea.add(JsonPath.read(data.get(k), mdmsPath));
						} else if (ocdata.get(p).equalsIgnoreCase("FloorArea")) {
							FloorArea.add(JsonPath.read(data.get(k), mdmsPath));
						} else if (ocdata.get(p).equalsIgnoreCase("BuildingArea")) {
							BuildingArea.add(JsonPath.read(data.get(k), mdmsPath));
						}
					}
				}

				if (BuildingArea.size() >= 2) {
					for (int i = 0; i < OcData.size(); i++) {
						String name = JsonPath.read(OcData.get(i), "$.name");
						String vType = JsonPath.read(OcData.get(i), "$.vtype");
						if (vType.equalsIgnoreCase("number")) {
							NoOfFloorsDiff = NoOfFloors.get(0) - NoOfFloors.get(1);
							BuildingHeightDiff = BuildingHeight.get(0) - BuildingHeight.get(1);
							CarpetAreaDiff = CarpetArea.get(0) - CarpetArea.get(1);
							FloorAreaDiff = FloorArea.get(0) - FloorArea.get(1);
							BuildingAreaDiff = BuildingArea.get(0) - BuildingArea.get(1);
						} else {
							NoOfFloorsDiff = ((NoOfFloors.get(0) - NoOfFloors.get(1)) / NoOfFloors.get(1)) * 100;
							BuildingHeightDiff = ((BuildingHeight.get(0) - BuildingHeight.get(1))
									/ BuildingHeight.get(1)) * 100;
							CarpetAreaDiff = ((CarpetArea.get(0) - CarpetArea.get(1)) / CarpetArea.get(1)) * 100;
							FloorAreaDiff = ((FloorArea.get(0) - FloorArea.get(1)) / FloorArea.get(1)) * 100;
							BuildingAreaDiff = ((BuildingArea.get(0) - BuildingArea.get(1)) / BuildingArea.get(1))
									* 100;
						}
						String restrictionType = JsonPath.read(OcData.get(i), "$.restrictionType");
						String tlimit = JsonPath.read(OcData.get(i), "$.tlimit");
						if (name.equalsIgnoreCase("NoOfFloors")) {
							if (NoOfFloorsDiff > Integer.parseInt(tlimit)) {
								if (restrictionType.equalsIgnoreCase("restrict")) {
									throw new CustomException("RISTRICTED",
											"Not able to create the Application due to NoOfFloorsDiff");
								} else {
									System.out.println("Difference exceeded the tolarence limit in NoOfFloors.");
									continue;
								}
							}
						}
						if (name.equalsIgnoreCase("BuildingHeight")) {
							if (BuildingHeightDiff > Integer.parseInt(tlimit)) {
								if (restrictionType.equalsIgnoreCase("restrict")) {
									throw new CustomException("RISTRICTED",
											"Not able to create the Application due to BuildingHeightDiff");
								} else {
									System.out.println("Difference exceeded the tolarence limit BuildingHeight.");
									continue;
								}
							}
						}
						if (name.equalsIgnoreCase("CarpetArea")) {
							if (CarpetAreaDiff > Integer.parseInt(tlimit)) {
								if (restrictionType.equalsIgnoreCase("restrict")) {
									throw new CustomException("RISTRICTED",
											"Not able to create the Application due to CarpetAreaDiff");
								} else {
									System.out.println("Difference exceeded the tolarence limit in CarpetArea.");
									continue;
								}
							}
						}
						if (name.equalsIgnoreCase("FloorArea")) {
							if (FloorAreaDiff > Integer.parseInt(tlimit)) {
								if (restrictionType.equalsIgnoreCase("restrict")) {
									throw new CustomException("RISTRICTED",
											"Not able to create the Application due to FloorAreaDiff");
								} else {
									System.out.println("Difference exceeded the tolarence limit in FloorArea.");
									continue;
								}
							}
						}
						if (name.equalsIgnoreCase("BuildingArea")) {
							if (BuildingAreaDiff > Integer.parseInt(tlimit)) {
								if (restrictionType.equalsIgnoreCase("restrict")) {
									throw new CustomException("RISTRICTED",
											"Not able to create the Application due to BuildingAreaDiff");
								} else {
									System.out.println("Difference exceeded the tolarence limit in BuildingArea.");
									continue;
								}
							}
						}
					}
				}

			}
			System.out.println(NoOfFloorsDiff);
			System.out.println(BuildingHeightDiff);
			System.out.println(CarpetAreaDiff);
			System.out.println(FloorAreaDiff);
			System.out.println(BuildingAreaDiff);

		}
		return null;

	}
	
}
