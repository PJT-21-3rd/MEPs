package collectors.hjd;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


public class HjdStoreListCollector {
    private static Properties loadProperties() {
        Properties prop = new Properties();
        try (InputStream input = HjdStoreListCollector.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) return null;
            prop.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return prop;
    }

    public static void collect(String csvFilePath) {
        Properties prop = loadProperties();
        if (prop == null) return;

        String url = prop.getProperty("db.url");
        String user = prop.getProperty("db.username");
        String pass = prop.getProperty("db.password");

        String sql = "INSERT INTO hjd_store (yyqu, hjd_cd, induty_cd, induty_nm, stor_co) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFilePath), StandardCharsets.UTF_8))) {

            Set<String> validHjdCodes = new HashSet<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT hjd_cd FROM hjd")) {
                while (rs.next()) {
                    validHjdCodes.add(rs.getString("hjd_cd"));
                }
            }

            conn.setAutoCommit(false);
            String line;
            int count = 0;
            int skipCount = 0;
            Set<String> missingCodes = new HashSet<>();

            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] tokens = line.replaceAll("\"", "").split(",");
                if (tokens.length < 5) continue;

                String yyqu = tokens[0].trim();
                String hjdCd = tokens[1].trim();

                if (hjdCd.length() > 8) {
                    hjdCd = hjdCd.substring(0, 8);
                }


                if (!validHjdCodes.contains(hjdCd)) {
                    missingCodes.add(hjdCd);
                    skipCount++;
                    continue;
                }

                String indutyCd = tokens[2].trim();
                String indutyNm = tokens[3].trim();
                int storCo = 0;
                try {
                    storCo = Integer.parseInt(tokens[4].trim());
                } catch (NumberFormatException e) {
                    storCo = 0;
                }

                pstmt.setString(1, yyqu);
                pstmt.setString(2, hjdCd);
                pstmt.setString(3, indutyCd);
                pstmt.setString(4, indutyNm);
                pstmt.setInt(5, storCo);

                pstmt.addBatch();
                count++;

                if (count % 10000 == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                }
            }

            pstmt.executeBatch();
            conn.commit();

            System.out.println("\n--------------------------------------------------");
            System.out.println("제외된 행정동 코드 목록 (" + missingCodes.size() + "종류, 총 " + skipCount + "건):");
            for (String code : missingCodes) {
                System.out.println("  - 누락 코드: " + code);
            }
            System.out.println("--------------------------------------------------");
            System.out.println("저장된 데이터: " + count + "건");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String csvPath = "C:/Users/student/Desktop/meps_data/서울시 상권분석서비스(점포-행정동)_2025년.csv";
        collect(csvPath);
    }
}
