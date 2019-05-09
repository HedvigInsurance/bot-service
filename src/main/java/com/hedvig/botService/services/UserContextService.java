package com.hedvig.botService.services;

import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.UserContextRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UserContextService {

  private UserContextRepository userContextRepository;

  public UserContextService(UserContextRepository userContextRepository) {
    this.userContextRepository = userContextRepository;
  }

  public Optional<UserContext> findByMemberId(String memberId) {
    return userContextRepository.findByMemberId(memberId);
  }

  public void save(UserContext uc){
    userContextRepository.save(uc);
  }
}
