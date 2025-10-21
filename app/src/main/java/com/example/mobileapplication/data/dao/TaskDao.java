package com.example.mobileapplication.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mobileapplication.data.models.TaskEntity;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insert(TaskEntity t);

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<TaskEntity>> getAll(String userId);

    @Query("UPDATE tasks SET status = :status WHERE id = :id AND userId = :userId")
    void updateStatus(long id, String status, String userId);

    @Query("SELECT * FROM tasks WHERE id = :id AND userId = :userId LIMIT 1")
    LiveData<TaskEntity> byId(long id, String userId);

    @Query("DELETE FROM tasks WHERE id = :id AND userId = :userId")
    void delete(long id, String userId);

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'DONE' AND userId = :userId")
    int getCompletedCount(String userId);

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'ACTIVE' AND userId = :userId")
    int getActiveCount(String userId);

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'CANCELLED' AND userId = :userId")
    int getCancelledCount(String userId);

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId")
    int getTotalCount(String userId);

    @Query("SELECT * FROM tasks WHERE userId = :userId")
    List<TaskEntity> getAllTasksSync(String userId);

    @Query("SELECT totalXp FROM tasks WHERE status = 'DONE' AND userId = :userId")
    List<Integer> getCompletedXpValues(String userId);

    @Query("SELECT createdAt FROM tasks WHERE userId = :userId ORDER BY createdAt DESC LIMIT 7")
    List<Long> getRecentCreationDates(String userId);

    @Query("""
UPDATE tasks SET
  title = :title,
  description = :desc,
  weightXp = :w,
  importanceXp = :imp,
  totalXp = :total,
  scheduledAt = :when
WHERE id = :id
  AND kind = 'ONE_TIME'
  AND status = 'ACTIVE'
  AND userId = :userId
  AND (scheduledAt IS NULL OR scheduledAt >= :now)
""")
    int updateOneTime(long id, String title, String desc,
                      int w, int imp, int total, Long when, long now, String userId);

    @Query("""
UPDATE tasks SET
  title = :title,
  description = :desc,
  weightXp = :w,
  importanceXp = :imp,
  totalXp = :total,
  repeatEvery = :every,
  repeatUnit = :unit,
  repeatStartAt = :start,
  repeatEndAt = :end
WHERE id = :id
  AND kind = 'RECURRING'
  AND (status = 'ACTIVE' OR status = 'PAUSED')
  AND userId = :userId
  AND (repeatEndAt IS NULL OR repeatEndAt >= :fromTs)
""")
    int updateRecurring(long id, String title, String desc,
                        int w, int imp, int total,
                        Integer every, String unit,
                        Long start, Long end, Long fromTs, String userId);

    @Query("""
DELETE FROM tasks
WHERE id = :id
  AND kind = 'ONE_TIME'
  AND status = 'ACTIVE'
  AND userId = :userId
  AND (scheduledAt IS NULL OR scheduledAt >= :now)
""")
    int deleteOneTime(long id, long now, String userId);

    @Query("""
UPDATE tasks SET
  repeatEndAt = CASE
                  WHEN repeatEndAt IS NULL OR repeatEndAt > :endAt
                  THEN :endAt ELSE repeatEndAt END
WHERE id = :id
  AND kind = 'RECURRING'
  AND status != 'CANCELED'
  AND userId = :userId
""")
    int cancelRecurringFromNow(long id, long endAt, String userId);

    @Query("""
UPDATE tasks SET status = 'DONE'
WHERE id = :id
  AND kind = 'ONE_TIME'
  AND status = 'ACTIVE'
  AND userId = :userId
  AND scheduledAt IS NOT NULL
  AND scheduledAt <= :now
  AND scheduledAt >= :nowMinus3d
""")
    int markDoneOneTime(long id, long now, long nowMinus3d, String userId);

    @Query("UPDATE tasks SET status = 'CANCELED' WHERE id = :id AND status = 'ACTIVE' AND userId = :userId")
    int markCanceled(long id, String userId);

    @Query("UPDATE tasks SET status = 'PAUSED' WHERE id = :id AND kind = 'RECURRING' AND status = 'ACTIVE' AND userId = :userId")
    int pauseRecurring(long id, String userId);

    @Query("UPDATE tasks SET status = 'ACTIVE' WHERE id = :id AND kind = 'RECURRING' AND status = 'PAUSED' AND userId = :userId")
    int activateRecurring(long id, String userId);

    @Query("""
UPDATE tasks SET status = 'NOT_DONE'
WHERE kind = 'ONE_TIME'
  AND status = 'ACTIVE'
  AND userId = :userId
  AND scheduledAt IS NOT NULL
  AND scheduledAt < :limitTs
""")
    int sweepOverdueToNotDone(long limitTs, String userId);

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId")
    int getTotalTasks(String userId);

    @Query("""
SELECT COUNT(*) FROM tasks
WHERE status = 'DONE'
  AND userId = :userId
  AND (
        (kind = 'ONE_TIME' AND scheduledAt BETWEEN :start AND :end)
     OR (kind = 'RECURRING' AND repeatStartAt <= :end AND (repeatEndAt IS NULL OR repeatEndAt >= :start))
  )
""")
    int doneCount(String userId, long start, long end);

    @Query("""
SELECT COUNT(*) FROM tasks
WHERE status IN ('ACTIVE', 'DONE', 'NOT_DONE')
  AND userId = :userId
  AND (
        (kind = 'ONE_TIME' AND scheduledAt BETWEEN :start AND :end)
     OR (kind = 'RECURRING' AND repeatStartAt <= :end AND (repeatEndAt IS NULL OR repeatEndAt >= :start))
  )
""")
    int eligibleCount(String userId, long start, long end);
}
