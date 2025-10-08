package com.example.mobileapplication.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert long insert(TaskEntity t);

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    LiveData<List<TaskEntity>> getAll();

    @Query("UPDATE tasks SET status=:status WHERE id=:id")
    void updateStatus(long id, String status);

    @Query("SELECT * FROM tasks WHERE id=:id LIMIT 1")
    LiveData<TaskEntity> byId(long id);

    @Query("DELETE FROM tasks WHERE id=:id")
    void delete(long id);

    @Query("""
UPDATE tasks SET
  title=:title,
  description=:desc,
  weightXp=:w,
  importanceXp=:imp,
  totalXp=:total,
  scheduledAt=:when
WHERE id=:id AND kind='ONE_TIME'
  AND status='ACTIVE'
  AND (scheduledAt IS NULL OR scheduledAt >= :now)
""")
    int updateOneTime(long id, String title, String desc,
                      int w, int imp, int total, Long when, long now);

    @Query("""
UPDATE tasks SET
  title=:title,
  description=:desc,
  weightXp=:w,
  importanceXp=:imp,
  totalXp=:total,
  repeatEvery=:every,
  repeatUnit=:unit,
  repeatStartAt=:start,
  repeatEndAt=:end
WHERE id=:id AND kind='RECURRING'
  AND (status='ACTIVE' OR status='PAUSED')
  AND (repeatEndAt IS NULL OR repeatEndAt >= :fromTs)
""")
    int updateRecurring(long id, String title, String desc,
                        int w, int imp, int total,
                        Integer every, String unit,
                        Long start, Long end, Long fromTs);

    @Query("""
DELETE FROM tasks
WHERE id=:id AND kind='ONE_TIME'
  AND status='ACTIVE'
  AND (scheduledAt IS NULL OR scheduledAt >= :now)
""")
    int deleteOneTime(long id, long now);

    /** “Brisanje” ponavljajućeg = skraćivanje perioda tako da ostanu samo prošla pojavljivanja. */
    @Query("""
UPDATE tasks SET
  repeatEndAt = CASE
                  WHEN repeatEndAt IS NULL OR repeatEndAt > :endAt
                  THEN :endAt ELSE repeatEndAt END
WHERE id=:id AND kind='RECURRING' AND status!='CANCELED'
""")
    int cancelRecurringFromNow(long id, long endAt);


    @Query("""
UPDATE tasks SET 
 title=:title,
 description=:description,
 categoryId=:categoryId,
 kind=:kind,
 scheduledAt=:scheduledAt,
 repeatEvery=:repeatEvery,
 repeatUnit=:repeatUnit,
 repeatStartAt=:repeatStartAt,
 repeatEndAt=:repeatEndAt,
 weightXp=:weightXp,
 importanceXp=:importanceXp,
 totalXp=:totalXp
WHERE id=:id
""")
    void updateCore(long id,
                    String title, String description, long categoryId,
                    String kind, Long scheduledAt,
                    Integer repeatEvery, String repeatUnit, Long repeatStartAt, Long repeatEndAt,
                    int weightXp, int importanceXp, int totalXp);


    // Jednokratni -> DONE (samo ACTIVE, ne u budućnosti, najviše 3 dana unazad)
    @Query("""
UPDATE tasks SET status='DONE'
WHERE id=:id AND kind='ONE_TIME' AND status='ACTIVE'
  AND scheduledAt IS NOT NULL
  AND scheduledAt <= :now
  AND scheduledAt >= :nowMinus3d
""")
    int markDoneOneTime(long id, long now, long nowMinus3d);

    // Globalno -> CANCELED (dozvoljeno samo iz ACTIVE)
    @Query("UPDATE tasks SET status='CANCELED' WHERE id=:id AND status='ACTIVE'")
    int markCanceled(long id);

    // RECURRING -> PAUSED (samo iz ACTIVE)
    @Query("UPDATE tasks SET status='PAUSED' WHERE id=:id AND kind='RECURRING' AND status='ACTIVE'")
    int pauseRecurring(long id);

    // RECURRING -> ACTIVE (samo iz PAUSED)
    @Query("UPDATE tasks SET status='ACTIVE' WHERE id=:id AND kind='RECURRING' AND status='PAUSED'")
    int activateRecurring(long id);

    // Svaki put kad uđemo u app/listu: sve jednokratne starije od 3 dana -> NOT_DONE
    @Query("""
UPDATE tasks SET status='NOT_DONE'
WHERE kind='ONE_TIME' AND status='ACTIVE'
  AND scheduledAt IS NOT NULL
  AND scheduledAt < :limitTs
""")
    int sweepOverdueToNotDone(long limitTs);

}
