package org.egov.land.repository;

import java.util.ArrayList;
import java.util.List;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.producer.Producer;
import org.egov.land.repository.querybuilder.LandQueryBuilder;
import org.egov.land.repository.rowmapper.LandRowMapper;
import org.egov.land.web.models.LandInfo;
import org.egov.land.web.models.LandRequest;
import org.egov.land.web.models.LandSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LandRepository {

	@Autowired
	private BPAConfiguration config;

	@Autowired
	private Producer producer;
	
	@Autowired
	private LandQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private LandRowMapper rowMapper;

	/**
	 * Pushes the request on save topic through kafka
	 *
	 * @param bpaRequest
	 *            The landinfo create request
	 */
	public void save(LandRequest landRequest) {
		producer.push(config.getSaveLandInfoTopic(), landRequest);
	}

	public void update(LandRequest landRequest) {
		producer.push(config.getUpdateLandInfoTopic(), landRequest);
	}
	
	/**
	 * LandInfo search in database
	 *
	 * @param criteria
	 *            The LandInfo Search criteria
	 * @return List of LandInfo from search
	 */
	public List<LandInfo> getLandInfoData(LandSearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getLandInfoSearchQuery(criteria, preparedStmtList);
		List<LandInfo> landInfoData = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
		return landInfoData;
	}
}
