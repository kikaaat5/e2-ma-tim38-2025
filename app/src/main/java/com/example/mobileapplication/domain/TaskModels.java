package com.example.mobileapplication.domain;

public final class TaskModels {

    public enum TaskKind { ONE_TIME, RECURRING }
    //public enum RepeatUnit { DAY, WEEK }





    public enum TaskWeightXP {
        VEOMA_LAK(1), LAK(3), TEZAK(7), EKSTREMNO_TEZAK(20);
        public final int xp;
        TaskWeightXP(int xp){ this.xp = xp; }
        public static int indexOfXp(int xp){
            TaskWeightXP[] a = values();
            for (int i=0;i<a.length;i++) if (a[i].xp==xp) return i;
            return 0;
        }
    }
    public enum TaskImportanceXP {
        NORMALAN(1), VAZAN(3), EKSTREMNO_VAZAN(10), SPECIJALAN(100);
        public final int xp;
        TaskImportanceXP(int xp){ this.xp = xp; }
        public static int indexOfXp(int xp){
            TaskImportanceXP[] a = values();
            for (int i=0;i<a.length;i++) if (a[i].xp==xp) return i;
            return 0;
        }
    }
    public enum RepeatUnit { DAY, WEEK;
        public static int indexOf(String name){
            RepeatUnit[] a = values();
            for (int i=0;i<a.length;i++) if (a[i].name().equals(name)) return i;
            return 0;
        }
    }


    public static final class TaskDraft {
        public String title;
        public String description; // optional
        public long categoryId;
        public TaskKind kind;
        public long scheduledAtEpochMillis; // ONE_TIME

        // RECURRING:
        public Integer repeatEvery;
        public RepeatUnit repeatUnit;
        public Long repeatStartEpochMillis;
        public Long repeatEndEpochMillis;

        public TaskWeightXP weight;
        public TaskImportanceXP importance;

        public int valueXp(){
            return (weight!=null?weight.xp:0) + (importance!=null?importance.xp:0);
        }
    }

    private TaskModels() {} // no instances
}
