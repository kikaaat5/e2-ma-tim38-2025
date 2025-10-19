package com.example.mobileapplication.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mobileapplication.data.models.TaskEntity;

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

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'DONE'")
    int getCompletedCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'ACTIVE'")
    int getActiveCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'CANCELLED'")
    int getCancelledCount();

    @Query("SELECT COUNT(*) FROM tasks")
    int getTotalCount();

    @Query("SELECT * FROM tasks")
    List<TaskEntity> getAllTasksSync();

    @Query("SELECT totalXp FROM tasks WHERE status = 'DONE'")
    List<Integer> getCompletedXpValues();

    @Query("SELECT createdAt FROM tasks ORDER BY createdAt DESC LIMIT 7")
    List<Long> getRecentCreationDates();

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

    @Query("""
UPDATE tasks SET
  repeatEndAt = CASE
                  WHEN repeatEndAt IS NULL OR repeatEndAt > :endAt
                  THEN :endAt ELSE repeatEndAt END
WHERE id=:id AND kind='RECURRING' AND status!='CANCELED'
""")
    int cancelRecurringFromNow(long id, long endAt);

    @Query("""
UPDATE tasks SET status='DONE'
WHERE id=:id AND kind='ONE_TIME' AND status='ACTIVE'
  AND scheduledAt IS NOT NULL
  AND scheduledAt <= :now
  AND scheduledAt >= :nowMinus3d
""")
    int markDoneOneTime(long id, long now, long nowMinus3d);

    @Query("UPDATE tasks SET status='CANCELED' WHERE id=:id AND status='ACTIVE'")
    int markCanceled(long id);

    @Query("UPDATE tasks SET status='PAUSED' WHERE id=:id AND kind='RECURRING' AND status='ACTIVE'")
    int pauseRecurring(long id);

    @Query("UPDATE tasks SET status='ACTIVE' WHERE id=:id AND kind='RECURRING' AND status='PAUSED'")
    int activateRecurring(long id);

    @Query("""
UPDATE tasks SET status='NOT_DONE'
WHERE kind='ONE_TIME' AND status='ACTIVE'
  AND scheduledAt IS NOT NULL
  AND scheduledAt < :limitTs
""")
    int sweepOverdueToNotDone(long limitTs);



    @Query("SELECT COUNT(*) FROM tasks")
    int getTotalTasks();
    @Query("""
SELECT COUNT(*) FROM tasks
WHERE status='DONE'
  AND (
        (kind='ONE_TIME'  AND scheduledAt BETWEEN :start AND :end)
     OR (kind='RECURRING' AND repeatStartAt <= :end AND (repeatEndAt IS NULL OR repeatEndAt >= :start))
  )
""")
    int doneCount(long start, long end);

    @Query("""
SELECT COUNT(*) FROM tasks
WHERE status IN ('ACTIVE','DONE','NOT_DONE')
  AND (
        (kind='ONE_TIME' AND scheduledAt BETWEEN :start AND :end)
     OR (kind='RECURRING' AND repeatStartAt <= :end AND (repeatEndAt IS NULL OR repeatEndAt >= :start))
  )
""")
    int eligibleCount(long start, long end);



}
