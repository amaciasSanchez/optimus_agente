package claro.edx.optimus.bean;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataByBank {

	private String bankId;
	private String bankName;
	private Double total;
	private Integer amount;
	private List<BankBreakdown> Breakdown;
	
	public static DataByBank newInstance(String _bankId, Double _tot, Integer count) {
		DataByBank obj = new DataByBank();
		obj.setBankId(_bankId);
		obj.setTotal(_tot);
		obj.setAmount(count);
		obj.setBreakdown(new ArrayList<BankBreakdown>());
		return obj;
	}

	public static BankBreakdown newInstanceBankBreakdown(String bankId, Double total, Integer numAmount) {
		BankBreakdown obj = new BankBreakdown();
		obj.setBankId(bankId);
		obj.setTotal(total);
		obj.setAmount(numAmount);
		return obj;
	}
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public  static class BankBreakdown{
		private String bankId;
		private String bankName;
		private Double total;
		private Integer amount;
	}
	
	@Override
	public String toString() {
		return "DataByBank [bankId=" + bankId + ", bankName=" + bankName + ", total=" + total + ", amount=" + amount + "]";
	}
	
}
