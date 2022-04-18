package claro.edx.optimus.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingCycle {
	private String billingCycleId;
	private Double total;
	private Double amount;
	private String description;
}
