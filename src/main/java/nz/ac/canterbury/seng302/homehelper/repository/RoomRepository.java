package nz.ac.canterbury.seng302.homehelper.repository;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends CrudRepository<Room, Long> {
    void deleteById(Long id);


    @Transactional
    @Modifying
    @Query(value = "DELETE FROM room_job " +
                    "WHERE room_id = :roomId", nativeQuery = true)
    void deleteByRoomId(@Param("roomId") Long roomId);
}


