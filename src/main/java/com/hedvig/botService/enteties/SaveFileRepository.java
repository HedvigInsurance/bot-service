package com.hedvig.botService.enteties;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SaveFileRepository extends CrudRepository<SaveFile, UUID> {
  List<SaveFile> findByMemberId(String memberId);
}


