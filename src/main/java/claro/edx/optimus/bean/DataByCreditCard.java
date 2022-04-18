package claro.edx.optimus.bean;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataByCreditCard {

	private String brandId;
	private String brandName;
	private Double total;
	private Integer amount;
	private List<BrandBreakdown> breakdown;
	
	public static DataByCreditCard newInstance(String _brandId, Double _tot, Integer count) {
		DataByCreditCard obj = new DataByCreditCard();
		obj.setBrandId(_brandId);
		obj.setTotal(_tot);
		obj.setAmount(count);
		obj.setBreakdown(new ArrayList<BrandBreakdown>());
		return obj;
	}

	public static BrandBreakdown newInstanceBankBreakdown(String brandId, Double total, Integer numAmount) {
		BrandBreakdown obj = new BrandBreakdown();
		obj.setBrandId(brandId);
		obj.setTotal(total);
		obj.setAmount(numAmount);
		return obj;
	}
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public  static class BrandBreakdown{
		private String brandId;
		private String brandName;
		private Double total;
		private Integer amount;
	}
	
	@Override
	public String toString() {
		return "DataByBrand [brandId=" + brandId + ", brandName=" + brandName + ", total=" + total + ", amount=" + amount + "]";
	}
	
}
