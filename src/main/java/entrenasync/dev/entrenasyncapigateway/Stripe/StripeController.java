package entrenasync.dev.entrenasyncapigateway.Stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payments")
public class StripeController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${cors.allowed-origin}")
    private String frontendBaseUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestBody PaymenRequest request) throws StripeException {
        // Puedes usar los datos de `request` para incluir info personalizada
        String serviceName = request.getServiceName(); // opcional
        Long amount = request.getAmount(); // en céntimos (ej: 1500 = 15€)

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendBaseUrl + "/payment-success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendBaseUrl + "/payment-cancelled")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(amount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(serviceName)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);
        return ResponseEntity.ok(Map.of("url", session.getUrl()));
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<String> confirmPayment(@RequestBody Map<String, String> body) throws StripeException {
        String sessionId = body.get("sessionId");

        if (sessionId == null || sessionId.isEmpty()) {
            return ResponseEntity.badRequest().body("Session ID is missing");
        }

        // Recupera la sesión desde Stripe
        Session session = Session.retrieve(sessionId);

        if ("paid".equals(session.getPaymentStatus())) {
            // 🔁 Aquí haces tu lógica: guardar pago, activar subscripción, etc.
            return ResponseEntity.ok("Pago confirmado correctamente (dummy)");
        } else {
            return ResponseEntity.status(400).body("El pago no se ha completado");
        }
    }

}
