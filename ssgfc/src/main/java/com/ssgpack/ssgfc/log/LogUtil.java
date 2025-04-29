package com.ssgpack.ssgfc.log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {

    private static final String BASE_DIR = "logs"; // 로그 최상위 폴더
    private static final int RETENTION_DAYS = 30;  // 로그 유지 기간 (30일)

    public static void write(String categoryPath, String message) {
        try {
            // 날짜 문자열
            String date = LocalDate.now().toString(); // ex) 2025-04-29

            // 로그 디렉토리 경로: logs/user, logs/admin/user 등
            String logDirPath = BASE_DIR + "/" + categoryPath;
            File logDir = new File(logDirPath);
            if (!logDir.exists()) {
                logDir.mkdirs(); // 폴더 없으면 생성
            }

            // 로그 파일 경로: logs/user/2025-04-29.log
            File logFile = new File(logDirPath + "/" + date + ".log");
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(logFile, true), StandardCharsets.UTF_8)) {

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                writer.write("[" + timestamp + "] " + message + "\n");
            }

            // 오래된 로그 삭제
            deleteOldLogs(logDir);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteOldLogs(File logDir) {
        File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log"));
        if (logFiles == null) return;

        LocalDate threshold = LocalDate.now().minusDays(RETENTION_DAYS);

        for (File file : logFiles) {
            String filename = file.getName().replace(".log", "");
            try {
                LocalDate fileDate = LocalDate.parse(filename); // "2025-04-01"
                if (fileDate.isBefore(threshold)) {
                    file.delete();
                }
            } catch (Exception ignored) {
                // 로그 이름이 날짜 형식이 아니면 무시
            }
        }
    }
}
