package claro.edx.optimus.bean;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * <b> TITLE </b>
 * <br />
 * <br />
 * <br />
 * <br />
 * <br />
 * @author ronnb
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SumaryDigitalPayment {
	private String absoluteName;
	private String fileName;
	private String type;
	private Double total;
	private Integer amount;
	private String  processAction="";
	private String  processUser="";
	private String  processDate="";
	private List<DataByBank> dataByBanks;
	private List<DataByCreditCard> dataByCreditCards;
	private List<BillingCycle> billingCycles;
	private String nameSp0;
	private String nameSp1;
	private String nameSp2;
	private String nameSp3;

	private String resumeDate;
	
	@Override
	public String toString() {
		return "SumaryDigitalPayment("+ type +"  [absoluteName=" + absoluteName  +  ", total=" + total + ", amount=" + amount + 
				", dataByBank=" + (dataByBanks==null?"0":dataByBanks.size()) + 
				", dataByCreditCard=" + (dataByCreditCards==null?"0":dataByCreditCards.size()) +  "]";
	}
	
	
}