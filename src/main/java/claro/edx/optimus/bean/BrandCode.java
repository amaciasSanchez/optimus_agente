package claro.edx.optimus.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandCode {

	String _id;
	String brandCode;
	String credidCardBrand;
	String financialInstitution;
	
	
	/*
	 * public BrandCode(String brandCode, String credidCardBrand, String
	 * financialInstitution) { super(); this._id = "brand-"+brandCode;
	 * this.brandCode = brandCode; this.credidCardBrand = credidCardBrand;
	 * this.financialInstitution = financialInstitution; }
	 */
	
	
}
