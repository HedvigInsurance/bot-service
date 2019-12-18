package com.hedvig.botService.services;

import com.hedvig.botService.enteties.UserContext;
import com.hedvig.botService.enteties.UserContextRepository;
import com.hedvig.botService.enteties.userContextHelpers.UserData;
import com.hedvig.botService.web.dto.EditMemberNameRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UserContextService {

  private UserContextRepository userContextRepository;

  @Autowired
  public UserContextService(UserContextRepository userContextRepository) {
    this.userContextRepository = userContextRepository;
  }

  public Optional<UserContext> findByMemberId(String memberId) {
    return userContextRepository.findByMemberId(memberId);
  }

  public void save(UserContext uc){
    userContextRepository.save(uc);
  }

  public void editMemberName(String memberId, EditMemberNameRequestDTO editMemberNameRequestDTO) {
    Optional<UserContext> userContextOptional = userContextRepository.findByMemberId(memberId);

    if(!userContextOptional.isPresent()) {
      return;
    }

    UserData userData = new UserData(userContextOptional.get());

    userData.setFirstName(editMemberNameRequestDTO.getFirstName());
    userData.setFamilyName(editMemberNameRequestDTO.getLastName());
  }
}
