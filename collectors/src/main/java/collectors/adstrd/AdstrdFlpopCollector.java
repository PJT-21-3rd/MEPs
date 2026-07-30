package collectors.adstrd;

import collectors.common.Config;
import collectors.common.Db;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdstrdFlpopCollector {

    private static final int BATCH_SIZE = 10000;

    public static void main(String[] args) {
        String csvFilePath = Config.get("csvPath");

        long startTime = System.currentTimeMillis();

        String insertSql = "INSERT IGNORE INTO adstrd_flpop (" +
                "yyqu, adstrd_cd, tot_flpop, agrde_10, agrde_20, agrde_30, agrde_40, agrde_50, agrde_60) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (FileInputStream fis = new FileInputStream(csvFilePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("MS949")));
             Connection conn = Db.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            conn.setAutoCommit(false);

            String line;
            boolean isHeader = true;
            int count = 0;
            int totalRows = 0;
            int skippedRows = 0;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                if (line.isBlank()) continue;

                String[] tokens = line.split(",", -1);
                if (tokens.length < 10) continue;

                String yyqu = tokens[0].trim();
                if (yyqu.compareTo("20251") < 0 || yyqu.compareTo("20261") > 0) {
                    skippedRows++;
                    continue;
                }

                String adstrdCd = tokens[1].trim();
                int totFlpop = parseInteger(tokens[3]);
                int agrde10 = parseInteger(tokens[4]);
                int agrde20 = parseInteger(tokens[5]);
                int agrde30 = parseInteger(tokens[6]);
                int agrde40 = parseInteger(tokens[7]);
                int agrde50 = parseInteger(tokens[8]);
                int agrde60 = parseInteger(tokens[9]);

                pstmt.setString(1, yyqu);
                pstmt.setString(2, adstrdCd);
                pstmt.setInt(3, totFlpop);
                pstmt.setInt(4, agrde10);
                pstmt.setInt(5, agrde20);
                pstmt.setInt(6, agrde30);
                pstmt.setInt(7, agrde40);
                pstmt.setInt(8, agrde50);
                pstmt.setInt(9, agrde60);

                pstmt.addBatch();
                count++;
                totalRows++;

                if (count % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                }
            }

            if (count % BATCH_SIZE != 0) {
                pstmt.executeBatch();
                conn.commit();
            }

            long endTime = System.currentTimeMillis();
            System.out.println("=========================================");
            System.out.printf("저장된 데이터: %d건 (소요시간: %.2f초)%n",
                    totalRows, (endTime - startTime) / 1000.0);
            System.out.println("=========================================");

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int parseInteger(String val) {
        if (val == null || val.isBlank()) return 0;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}