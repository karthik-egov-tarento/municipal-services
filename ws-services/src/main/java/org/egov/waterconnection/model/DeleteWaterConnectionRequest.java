package org.egov.waterconnection.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteWaterConnectionRequest {
    @NotNull
    @Valid
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @NotNull
    @Valid
    @JsonProperty("applicationNumber")
    private String applicationNumber;
}
