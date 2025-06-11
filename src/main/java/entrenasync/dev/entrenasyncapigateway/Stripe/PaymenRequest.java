package entrenasync.dev.entrenasyncapigateway.Stripe;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymenRequest {
    private String serviceName;
    private Long amount;
}
