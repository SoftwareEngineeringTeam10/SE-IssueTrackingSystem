package ui.javafx.util;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

// 화면 표시용 공통 시각 포매터
public final class DateFormats {

    public static final DateTimeFormatter ISSUE_TIMESTAMP =
        DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.ENGLISH);

    private DateFormats() {}
}
