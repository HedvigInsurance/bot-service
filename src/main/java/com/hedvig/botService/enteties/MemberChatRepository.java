package com.hedvig.botService.enteties;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberChatRepository extends JpaRepository<MemberChat, Integer> {

  Optional<MemberChat> findByMemberId(String id);
}
