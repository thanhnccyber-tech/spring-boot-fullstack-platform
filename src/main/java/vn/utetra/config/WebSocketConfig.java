package vn.utetra.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.socket.config.annotation.*;
import vn.utetra.entity.User;
import vn.utetra.repository.UserRepository;

import java.security.Principal;
import java.util.Optional;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** Các nguồn (origin) khác domain được phép kết nối WebSocket. Cùng domain luôn được phép. */
    @Value("${app.allowed-origins:http://localhost:*,http://127.0.0.1:*}")
    private String[] allowedOrigins;

    @Autowired private UserRepository userRepo;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Không dùng "*": chống Cross-Site WebSocket Hijacking từ trang web lạ
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS();
    }

    /**
     * Phân quyền theo kênh khi đăng ký nhận tin:
     *  - /topic/orders            : chỉ ADMIN / MANAGER / STAFF (thông báo đơn mới)
     *  - /topic/order-status/{id} : chỉ chính khách có id đó, hoặc nhân viên
     *  - kênh khác dưới /topic/   : từ chối
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (acc != null && StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
                    authorizeSubscribe(acc.getDestination(), acc.getUser());
                }
                return message;
            }
        });
    }

    private void authorizeSubscribe(String destination, Principal principal) {
        if (destination == null || !destination.startsWith("/topic/")) return;

        Optional<User> me = principal == null ? Optional.empty() : userRepo.findByEmail(principal.getName());
        if (me.isEmpty() || !Boolean.TRUE.equals(me.get().getEnabled())) {
            throw new AccessDeniedException("Cần đăng nhập để nhận thông báo realtime");
        }
        User u = me.get();
        boolean staff = u.isAdmin() || u.isManager() || u.isStaff();

        if (destination.equals("/topic/orders")) {
            if (!staff) throw new AccessDeniedException("Không có quyền");
        } else if (destination.startsWith("/topic/order-status/")) {
            String idPart = destination.substring("/topic/order-status/".length());
            boolean own = idPart.equals(String.valueOf(u.getId()));
            if (!own && !staff) throw new AccessDeniedException("Không có quyền");
        } else {
            throw new AccessDeniedException("Kênh không tồn tại");
        }
    }
}
