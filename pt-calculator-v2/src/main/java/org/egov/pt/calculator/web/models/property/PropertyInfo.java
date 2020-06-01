package org.egov.pt.calculator.web.models.property;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is lightweight property object that can be used as reference by definitions needing property linking. Actual Property Object extends this to include more elaborate attributes of the property.
 */

@Getter
@Setter
@NoArgsConstructor
//@Builder
@EqualsAndHashCode(of= {"propertyId","tenantId"})
public class PropertyInfo   {
        @JsonProperty("propertyId")
        public String propertyId;

        @JsonProperty("tenantId")
        public String tenantId;

        @JsonProperty("acknowldgementNumber")
        public String acknowldgementNumber;

    @JsonProperty("oldPropertyId")
    @Pattern(regexp = "^[^\\$\"'<>?\\\\~`!@$%^+={}*,.:;“”‘’]*$", message = "Invalid existing property Id. should be AlphaNumeric with -, /, #, : special characters allowed")
    public String oldPropertyId;

              /**
   * status of the Property
   */
  public enum PropertyStatusEnum {
    ACTIVE("ACTIVE"),
    
    INACTIVE("INACTIVE");

    private String value;

    PropertyStatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static PropertyStatusEnum fromValue(String text) {
      for (PropertyStatusEnum b : PropertyStatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
        @JsonProperty("status")
        public PropertyStatusEnum status;

        @Valid
        @JsonProperty("address")
        public Address address;




    protected PropertyInfo(String propertyId, String tenantId, String acknowldgementNumber, String oldPropertyId, PropertyStatusEnum status, Address address) {
        this.propertyId = propertyId;
        this.tenantId = tenantId;
        this.acknowldgementNumber = acknowldgementNumber;
        this.oldPropertyId = oldPropertyId;
        this.status = status;
        this.address = address;
    }
}

