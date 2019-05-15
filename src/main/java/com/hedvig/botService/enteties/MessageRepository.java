package com.hedvig.botService.enteties;

import com.hedvig.botService.enteties.message.Message;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

  @Query("select m from Message m where m.timestamp >= :timestamp")
  List<Message> findFromTimestamp(@Param("timestamp") Instant timestamp);

  Optional<Message> findByGlobalId(Integer globalId);
}
