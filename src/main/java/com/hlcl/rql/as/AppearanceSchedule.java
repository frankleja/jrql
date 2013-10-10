package com.hlcl.rql.as;

import java.util.Date;

/**
 * Diese Klasse repräsentiert den appearance schedule, der an einem Hauptlink angelegt werden kann.
 *
 * @author barszczewski on 09.10.13
 */
public class AppearanceSchedule {

    private static final AppearanceSchedule CLEARED_SCHEDULE = new AppearanceSchedule("0", "0");

    private ReddotDate begin;
    private ReddotDate end;

    /**
     * Erzeugt einen neuen schedule für ein Start- und Enddatum (java.util.Date)
     *
     * @param start Startdatum
     * @param end Enddatum
     * @return
     */
    public static AppearanceSchedule forPeriod(Date start, Date end){
        return new AppearanceSchedule(start, end);
    }

    /**
     * Erzeugt einen neuen schedule für ein Start- und Enddatum (com.hlcl.rql.as.ReddotDate)
     *
     * @param start Startdatum
     * @param end Enddatum
     * @return
     */
    public static AppearanceSchedule forPeriod(ReddotDate start, ReddotDate end){
        return new AppearanceSchedule(start, end);
    }

    /**
     * Erzeugt einen neuen schedule für ein Start- und Endzeitpunkt
     *
     * @param start Startzeitpunkt im Microsoft format
     * @param end  Endzeitpunkt String im Microsoft format
     * @return
     */
    public static AppearanceSchedule forPeriod(String start, String end){
        return new AppearanceSchedule(start, end);
    }

    /**
     * Liefert einen leeren schedule.
     *
     * @return
     */
    public static AppearanceSchedule clearedSchedule(){
        return CLEARED_SCHEDULE;
    }

    private AppearanceSchedule(ReddotDate begin, ReddotDate end) {
        this.begin = begin;
        this.end = end;
    }

    private AppearanceSchedule(Date begin, Date end){
        this(new ReddotDate(begin), new ReddotDate(end));
    }

    private AppearanceSchedule(String begin, String end){
        this(new ReddotDate(begin), new ReddotDate(end));
    }

    /**
     * Liefert den Startzeitpunkt.
     *
     * @return Startzeitpunkt als ReddotDate
     */
    public ReddotDate getBegin() {

        return begin;
    }

    /**
     * Liefert den Endzeitpunkt.
     *
     * @return Startzeitpunkt als ReddotDate
     */
    public ReddotDate getEnd() {
        return end;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppearanceSchedule that = (AppearanceSchedule) o;

        if (!begin.equals(that.begin)) return false;
        if (!end.equals(that.end)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = begin.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }
}
